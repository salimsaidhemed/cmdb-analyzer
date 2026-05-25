# CMDB Parser Architecture

## Purpose

This document defines the architecture for future CMDB Excel parsing based on the completed workbook format analysis.

This branch intentionally does not implement Apache POI parsing. It introduces only domain models, parser contracts, and design guidance so the real parser can be added in a controlled follow-up.

## Workbook Hierarchy

The parser architecture models the source Excel files as a hierarchy:

```text
CmdbWorkbook
  sourceFile
  importedAt
  sheets[]
  parserWarnings[]

CmdbSheet
  name
  type
  headerRowIndex
  headerMap
  ciBlocks[]
  flatRows[]
  warnings[]

CmdbBlock
  ciRowIndex
  relationshipRowIndexes[]
  separatorRowIndex
  sourceSheetName

ConfigurationItem
  id
  name
  ciClass
  description
  attributes
  sourceWorkbook
  sourceSheet
  sourceRow
  identityKey

Relationship
  id
  sourceCiId
  targetCiId
  targetName
  relationshipType
  rawRelationshipType
  sourceWorkbook
  sourceSheet
  sourceRow
  status
```

Every parsed entity must preserve workbook, sheet, and row information. That traceability is required for diagnostics, user review, future validation, and any workflow that links UI findings back to the original Excel file.

## Parser Pipeline

Recommended future parser pipeline:

1. **Workbook adapter**
   - Opens the Excel file with Apache POI in a future branch.
   - Converts workbook/sheet/row/cell data into parser-neutral `RawSheet`, `RawRow`, and `RawCell` values.
   - Keeps Apache POI types out of domain models and parser contracts.

2. **Header detection**
   - Uses `HeaderDetector`.
   - Detects headers without assuming row 1 or column A.
   - Normalizes known headers such as `Class`, `Name`, `Description`, `Parent CI`, and `Relationship`.
   - Reports duplicate, missing, unknown, or malformed headers through `ParserWarning`.

3. **Sheet classification**
   - Uses `SheetClassifier`.
   - Classifies each sheet as `CI_BLOCK`, `FLAT_CI_LIST`, `EVENT_BINDING`, `LOOKUP`, `METADATA`, or `UNKNOWN`.
   - Uses both sheet name and detected header shape.

4. **Content extraction**
   - Uses `BlockExtractor` for `CI_BLOCK` sheets.
   - Uses a future flat-row extractor for `FLAT_CI_LIST`, `EVENT_BINDING`, and `LOOKUP` sheets.
   - Stores block boundaries and flat rows in `CmdbSheet`.

5. **CI and relationship materialization**
   - Converts CI block rows into `ConfigurationItem`.
   - Converts relationship rows into `Relationship` with `targetName`, `rawRelationshipType`, and unresolved status until lookup is complete.
   - Preserves unknown columns in `ConfigurationItem.attributes`.

6. **Relationship resolution**
   - Uses `RelationshipResolver`.
   - Resolves `targetName` to `targetCiId` when possible.
   - Marks relationships as `RESOLVED`, `UNRESOLVED`, or `MALFORMED`.
   - Emits warnings for missing targets, duplicate targets, blank relationship types, and partial rows.

7. **Dataset assembly**
   - Produces a `CmdbWorkbook` for each imported workbook.
   - Future application services may combine multiple `CmdbWorkbook` objects into a larger import session or dataset.

## Sheet Classification Strategy

Classification should prefer structure over hard-coded sheet names.

### `CI_BLOCK`

Detected when a sheet has:

- `Class`
- `Name`
- `Description`
- `Parent CI`
- `Relationship`

Rows are expected to follow the pattern:

```text
CI row
0..N relationship rows
blank separator row
```

The parser must not assume exactly one relationship row. The analysis found blocks ranging from one data row to many relationship rows.

### `FLAT_CI_LIST`

Detected when a sheet has CI identity columns but no relationship columns:

- `Class`
- `Name`
- Usually `Description`

Examples include business service lists and VLAN-style lists.

### `EVENT_BINDING`

Detected from event-binding headers such as:

- `Node (Source)`
- `Configuration item`
- `Service Offering`
- `Business Service`
- `Resource`

These sheets should not be forced into CI block parsing. They are lookup/mapping data for future event-to-CI validation.

### `LOOKUP`

Detected for supporting reference data such as locations, stockroom lookup files, or CI binding lookup rules.

Typical examples:

- Location sheets with address and coordinates.
- Lookup rule workbooks.
- Service group dashboards.

### `METADATA`

Detected for changelog or audit/history sheets.

Typical headers:

- `Version`
- `Modification Date`
- `User`
- `Changes` / `Modifications done`

### `UNKNOWN`

Used when the sheet cannot be confidently classified. Unknown sheets should be retained with warnings rather than silently discarded.

## CI Block Extraction Strategy

For block-based sheets:

1. Start after the detected header row.
2. Ignore fully blank rows until a data row is found.
3. Treat a row with blank `Parent CI` and blank `Relationship` as the CI row.
4. Treat following rows with populated `Parent CI` or `Relationship` as relationship rows for that CI.
5. Treat a blank row as the block separator.
6. Allow the final block to end at end-of-sheet without a separator row.

Malformed block situations should produce warnings:

- Relationship row before any CI row.
- CI row missing `Class` or `Name`.
- Relationship row missing target name.
- Relationship row has target name but blank relationship type.
- Multiple apparent CI rows in one block.
- Unknown or duplicate headers.

The `CmdbBlock` model stores only row boundaries. Actual CI and relationship materialization should happen later so diagnostics can distinguish row structure from domain conversion.

## Unresolved Relationship Handling

Relationships are created before target lookup is complete.

Initial relationship fields:

- `sourceCiId`: known from the current CI block.
- `targetName`: raw value from `Parent CI`.
- `rawRelationshipType`: original value from `Relationship`.
- `relationshipType`: normalized value, when possible.
- `targetCiId`: null until resolved.
- `status`: `UNRESOLVED` or `MALFORMED` until resolution.

Resolution outcomes:

- `RESOLVED`: exactly one acceptable target CI was found.
- `UNRESOLVED`: no matching target CI was found in the import scope.
- `MALFORMED`: row is too incomplete or contradictory to resolve safely.

Unresolved relationships should remain in the model. They are important for user diagnostics, missing-CI validation, and later graph completeness reporting.

## Duplicate Identity Strategy

The workbook analysis found duplicate CI names across sheets and sometimes within project workbooks. Therefore, `Name` alone is not a safe global identifier.

Recommended identity strategy:

1. Create a parser-generated `id` for every CI.
2. Create an `identityKey` using normalized source context and CI identity fields.
3. Recommended initial key shape:

```text
normalized(project) + "|" + normalized(ciClass) + "|" + normalized(name)
```

4. If `Project` is absent, use source workbook or another import-scope namespace.
5. Preserve source workbook, sheet, and row regardless of identity strategy.
6. When a target name matches multiple CIs:
   - Prefer exact match within same workbook/project.
   - Prefer class-compatible targets if relationship hierarchy rules exist.
   - Otherwise mark as unresolved or ambiguous with a `WARNING`.

The first implementation should avoid clever automatic merging. Duplicate handling should be visible and auditable.

## Parser Warning Strategy

`ParserWarning` is the common diagnostic model for non-fatal and fatal parser issues.

Severity guidance:

- `INFO`: parser made a benign assumption, such as ignoring a metadata sheet.
- `WARNING`: parser tolerated questionable data, such as duplicate CI names, unknown headers, missing optional fields, unresolved targets, or blank separators missing at end-of-sheet.
- `ERROR`: parser could not safely parse an expected structure, such as a missing header row, missing required CI identity fields, or a malformed relationship that cannot be represented.

Warnings should include as much source context as possible:

- workbook
- sheet
- row
- column
- raw value

Do not throw away warnings after parsing. They should remain attached to `CmdbWorkbook` or `CmdbSheet` and later be displayed in the UI.

## Raw Value Preservation

The parser must preserve raw source values where they affect auditability:

- `Relationship.rawRelationshipType` stores the original Excel relationship label.
- `Relationship.relationshipType` stores the normalized label.
- `ConfigurationItem.attributes` stores unknown or class-specific columns.
- Future parser internals should preserve raw row values when emitting warnings.

This allows normalization without losing the ability to explain what came from Excel.

## Graph Readiness

The model is designed so `ConfigurationItem` can become a graph vertex and `Relationship` can become a graph edge later.

Important graph considerations:

- Relationship direction should preserve source row direction first.
- Display direction can be normalized later for readability.
- `RelationshipStatus` allows graph construction to include or exclude unresolved/malformed edges.
- `relationshipType` and `rawRelationshipType` allow grouped analysis without losing source labels.
- `sourceWorkbook`, `sourceSheet`, and `sourceRow` allow graph findings to be traced back to Excel.

Future JGraphT integration should live in the `graph` package, not in parser or model classes.

## Contract Responsibilities

### `WorkbookParser`

Coordinates the parser pipeline and returns a `CmdbWorkbook`. Future implementations may compose `HeaderDetector`, `SheetClassifier`, `BlockExtractor`, and `RelationshipResolver`.

### `HeaderDetector`

Detects and normalizes headers from a `RawSheet`. Must tolerate leading blank columns and non-row-1 headers.

### `SheetClassifier`

Classifies sheets into known `SheetType` values using headers and sheet shape.

### `BlockExtractor`

Extracts row boundaries for variable-sized CI blocks. It does not create Java domain objects from cell values.

### `RelationshipResolver`

Resolves target names to CI identifiers and assigns relationship statuses.

## Non-Goals For This Branch

This branch does not implement:

- Apache POI workbook loading.
- Real row/cell parsing.
- Validation engine.
- Graph construction.
- JavaFX UI changes.
- JSON export.
- Database or persistence.
