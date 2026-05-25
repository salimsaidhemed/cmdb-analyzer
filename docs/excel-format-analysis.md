# CMDB Excel Format Analysis

## Scope

This document reviews the CMDB Excel samples in:

`/Users/shemed/Documents/Projects/CMDB Analyzer/sample-data`

Reviewed files:

- `CD CMDB - DMS Apps and Services - 20221020 - FB.xlsx`
- `CD CMDB - DMS Infrastructure - 20250130 - FB.xlsx`
- `CD CMDB - Event CI binding - 20251016 - FB.xlsx`
- `CD CMDB - Factories - 20251015 - FB.xlsx`
- `MW CMDB - DMS - 20260406 - KB.xlsx`
- `MW CMDB - Factories - 20250902 - KB.xlsx`
- `MW SicpaTrace CI Binding Lookup Rules - 20250312-KB - Copy.xlsx`
- `TG CMDB - DMS Apps and Services - 20251003 - KB Renewal.xlsx`
- `TG CMDB - Event CI binding - 20260416 - KB.xlsx`
- `TG CMDB - Factories - 20260416 Renewal - KB.xlsx`
- `TG Stockroom - 20240319 - KB.xlsx`
- `TZ - SPM - Alm Stockroom 03122025 - KB.xlsx`
- `TZ CMDB - DMS App and Services - 20260406 - KB.xlsx`
- `TZ CMDB - DMS Infrastructure - 20253004- SH.xlsx`
- `TZ CMDB - Factories - 20260522 - FB.xlsx`
- `TZ Event Management CI Binding Lookup Rules - 20260522 - FB.xlsx`
- `UK HMRC CMDB 20260505 - SH.xlsx`
- `Uganda CMDB - DMS Applications and Services - 20201118 - AV.xlsx`
- `Uganda CMDB-DMS Infrastructure -20260303-DK.xlsx`
- `Uganda CMDB-Factories-20260310-DK.xlsx`
- `Uganda Event CI binding-20260311-DK.xlsx`

This is analysis only. No parser, Java model, validation engine, or Apache POI implementation is introduced here.

## High-Level Findings

The primary CMDB workbooks use a repeated block format:

1. Header row, usually row 1.
2. One CI definition row.
3. Zero or more relationship rows for the same CI.
4. A blank separator row.
5. Next CI block.

The earlier two-row assumption is mostly correct for many factory sheets, but not universal. Several sheets contain one CI row followed by multiple relationship rows before the blank separator. Examples:

- `Applications` in DMS Apps and Services has block sizes from 2 to 11 rows.
- `DMS01-VM` and `DMS02-VM` in DMS Infrastructure have block sizes from 4 to 12 rows.
- `Printers` in Factories has block sizes from 3 to 19 rows.
- Flat sheets such as `Business`, `Locations`, `VLAN`, and event-binding sheets do not use CI/relationship blocks.

Most worksheets have a leading blank column A. Headers typically start in column B. A parser should not assume headers begin at column A.

Across the broader sample set, the standard block parser detected roughly 12,818 unique CI names and 17,212 relationship rows. This confirms that the CMDB data is relationship-heavy and should be modeled as a graph-friendly dataset, even before graph visualization is implemented.

## Expanded Sample Set Summary

The additional MW, TG, TZ, UK HMRC, and Uganda samples reinforce the same general format while adding more variation in sheet naming, relationship labels, and project-specific exceptions.

| Workbook family | Approx. CI rows | Approx. relationship rows | Notes |
|---|---:|---:|---|
| CD DMS Apps/Infrastructure/Factories | 2,264 | 3,112 | Original baseline set. |
| MW DMS/Factories | 936 | 1,086 | Same block pattern; separate CI binding workbook. |
| TG DMS/Factories | 608 | 980 | More DMS app/service sheets and factory sheets. |
| TZ DMS/Infrastructure/Factories | 5,661 | 6,946 | Largest expanded set; includes more unresolved relationship targets. |
| UK HMRC | 205 | 285 | One combined CMDB workbook; several service names have spaces and punctuation. |
| Uganda DMS/Infrastructure/Factories | 3,534 | 4,803 | Large factory set with same CI block grammar. |
| Event / lookup rule workbooks | 0 standard CI rows | 0 standard relationship rows | Mapping/reference workbooks, not standard CI block sheets. |

Expanded-set observations:

- Relationship labels increased from 9 to 14 observed values.
- `Depends on` remains dominant by a large margin.
- `Connected`, `Houses`, `Operates by`, and `Operated by` appear in the broader samples.
- Some rows contain `Parent CI` values with blank `Relationship` cells.
- Some project-specific targets are unresolved within the available sample set, especially in UK HMRC, TG, TZ, and Uganda files.
- Duplicate CI names are common enough that identity should not rely on `Name` alone without source context, class, or a duplicate-resolution policy.

## Workbook Summary

### CD CMDB - DMS Apps and Services - 20221020 - FB.xlsx

| Sheet | Header row | Pattern | Notes |
|---|---:|---|---|
| `Changelog` | 2 | Flat metadata | Not CMDB data. |
| `Business` | 1 | Flat CI list | No relationship columns. |
| `Applications` | 1 | CI blocks | 43 CIs, 101 relationships, mostly `Depends on`. |
| `Software` | 1 | CI blocks | 5 CIs, 20 relationships, `Runs on` and `Depends on`. |
| `Application Servers` | 1 | CI blocks | 9 CIs, 24 relationships, `Runs on` and `Depends on`. |
| `DB Instance` | 1 | CI blocks | 13 CIs, 48 relationships. |
| `Ordering` | 1 | CI blocks | 2 CIs, 2 relationships. |
| `Reporting` | 1 | CI blocks | 4 CIs, 4 relationships. |
| `Stamps Delivery` | 1 | CI blocks | 2 CIs, 2 relationships. |
| `DAS` | 1 | CI blocks | 2 CIs, 2 relationships. |
| `SDAS` | 1 | CI blocks | 2 CIs, 2 relationships. |
| `Auditing` | 1 | CI blocks | Header row has stray leading value `KIN-DGDA Data Center`. |
| `Data Sync` | 1 | CI blocks | 2 CIs, 2 relationships. |
| `Other DMS Services` | 1 | CI blocks | Header row has stray leading value `KIN-DGDA Data Center`. |
| `IT Services` | 1 | CI blocks | Worksheet has many trailing empty rows. |

### CD CMDB - DMS Infrastructure - 20250130 - FB.xlsx

| Sheet | Header row | Pattern | Notes |
|---|---:|---|---|
| `Changelog` | 2 | Flat metadata | Not CMDB data. |
| `Locations` | 1 | Flat location list | No `Class` or relationship columns. |
| `Datacenter` | 1 | CI blocks | Relationships include `Located in`, `Used by`. |
| `DMS01-Servers` | 1 | CI blocks | Includes maintenance, model, IP, serial fields. |
| `DMS02-Servers` | 1 | CI blocks | Includes event hostname. |
| `Storage` | 1 | CI blocks | Uses `Located in`, `Connects to`, `Uses`, `Depends on`. |
| `UPS` | 1 | CI blocks | `Depends on`, `Located in`. |
| `DMS01-VM` | 1 | CI blocks | Includes `Virtualized by`, `Virtualizes`, `Runs on`, `Connects to`. |
| `DMS02-VM` | 1 | CI blocks | Same VM relationship pattern as DMS01. |
| `SC-VM` | 1 | CI blocks | Small VM sheet. |
| `VLAN` | 1 | Flat CI list | No `Parent CI` or `Relationship`. |
| `IP_Switch` | 1 | CI blocks | Heavy `Connects to` relationships. |
| `IP_Firewall` | 1 | CI blocks | Heavy `Connects to`; includes `Operates`, `Used by`. |
| `Net_Gear` | 1 | CI blocks | Network gear relationships. |

### CD CMDB - Event CI binding - 20251016 - FB.xlsx

| Sheet | Header row | Pattern | Notes |
|---|---:|---|---|
| `Changelog` | 3 | Flat metadata | Not CMDB data. |
| `Hostname-CI mapping` | 1 | Flat mapping | Maps event/source nodes to CIs and service context. |
| `Service Group Dashboard` | 2 | Flat dashboard-like data | Repeated `CI-classes` columns; not standard CMDB blocks. |

This workbook should probably be parsed by a different importer later. It is useful for event-to-CI lookup and service grouping, but it does not follow the CI block pattern.

### CD CMDB - Factories - 20251015 - FB.xlsx

| Sheet | Header row | Pattern | Notes |
|---|---:|---|---|
| `Changelog` | 2 | Flat metadata | Not CMDB data. |
| `Location` | 1 | Flat location list | Factory locations. |
| `Business` | 1 | Flat CI list | Business service CIs. |
| `SPM` | 1 | CI blocks | 35 CIs, 35 `Depends on` relationships. |
| `Stockrooms` | 1 | CI blocks | 36 CIs, 36 `Depends on` relationships. |
| `Spare Equipment` | 1 | CI blocks | 31 CIs, 31 relationships. |
| `SCL-B` | 1 | CI blocks | 72 CIs, 72 relationships. |
| `SAS-T` | 1 | CI blocks | Includes `GPS coordinates`. |
| `SAS-A` | 1 | CI blocks | Includes `GPS coordinates`. |
| `Masters` | 1 | CI blocks | 30 CIs, 30 relationships. |
| `Coding Lines` | 1 | CI blocks | Includes solution/product/packaging/BRS/line-speed fields. |
| `Activation Lines` | 1 | CI blocks | Similar line-specific fields. |
| `Applications` | 1 | CI blocks | 140 CIs, 140 relationships. |
| `App Server` | 1 | CI blocks | 30 CIs, 60 `Runs on` relationships. |
| `DB Instance` | 1 | CI blocks | 30 CIs, 60 `Depends on` relationships. |
| `Servers` | 1 | CI blocks | OS, RAM, IP, hostname fields. |
| `Router` | 1 | CI blocks | 30 CIs, 30 relationships. |
| `Switch` | 1 | CI blocks | 190 CIs, 190 relationships. |
| `PanelPC` | 1 | CI blocks | Two IP address fields. |
| `HMI Display` | 1 | CI blocks | Display hardware fields. |
| `PLC` | 1 | CI blocks | PLC-specific IP and firmware fields. |
| `UPS` | 1 | CI blocks | 110 CIs, 110 relationships. |
| `Beams` | 1 | CI blocks | 80 CIs, 80 relationships. |
| `Lightsource` | 1 | CI blocks | 80 CIs, 80 relationships. |
| `Printers` | 1 | CI blocks | Variable block sizes; many relationships per printer. |
| `Spare Printers` | 1 | CI blocks | 28 CIs, 28 relationships. |
| `Recognition` | 1 | CI blocks | Recognition systems. |
| `Cameras` | 1 | CI blocks | 236 CIs, 236 relationships; no `Project` column. |
| `Cabinet` | 1 | CI blocks | 81 CIs, 405 `Located in` relationships. |
| `Encoder` | 1 | CI blocks | 80 CIs, 80 relationships. |
| `Label applicator` | 1 | CI blocks | 8 CIs, 8 relationships. |
| `Sensors` | 1 | CI blocks | 80 CIs, 80 relationships. |
| `Ink` | 1 | CI blocks | 72 CIs, 72 `Uses` relationships. |
| `Spare Ink` | 1 | CI blocks | 28 CIs, 28 `Uses` relationships. |
| `Stamps` | 1 | CI blocks | 2 CIs, 8 `Uses` relationships. |

## Common Header and Column Patterns

### Core CI Columns

Common CI columns:

- `Class`
- `Name`
- `Description`
- `Location`
- `Manufacturer`
- `Model`
- `Version`
- `Project`

The first three are the strongest CI identity columns. In standard CMDB sheets, `Class`, `Name`, and `Description` are repeated on both CI definition rows and relationship rows.

### Relationship Columns

Standard relationship columns:

- `Parent CI`
- `Relationship`

Observed relationship values:

- `Depends on`
- `Located in`
- `Runs on`
- `Connects to`
- `Uses`
- `Connected`
- `Houses`
- `Virtualized by`
- `Virtualizes`
- `Used by`
- `Operates`
- `Operates by`
- `Operated by`

Some rows in the expanded sample set contain a populated `Parent CI` with a blank `Relationship`. These should be imported as malformed or incomplete relationships and surfaced as warnings.

The column name `Parent CI` is probably too narrow for the domain model. It is used as the relationship target regardless of relationship type.

## Draft Relationship Hierarchy

The hierarchy below is inferred from repeated source-class, relationship-type, target-class patterns across the expanded sample set. It should be treated as a working model for parser and validation design, not as final business semantics.

Important direction rule: in the Excel rows, the current row CI is the source and `Parent CI` is the target. For example, a `service_offering` row with `Relationship = Depends on` and `Parent CI = cmdb_ci_service` means the service offering depends on the business service. Some UI and graph views may want to display this in the reverse direction for readability.

### Logical Service Layer

Observed top-level service chain:

```text
cmdb_ci_service
  <- Depends on - service_offering
    <- Depends on - cmdb_ci_appl
    <- Depends on - cmdb_ci_vm
    <- Depends on - cmdb_ci_server
    <- Depends on - cmdb_ci_ip_switch / cmdb_ci_ip_router / cmdb_ci_ups
    <- Depends on - factory and production CIs
```

Common patterns:

- `service_offering Depends on cmdb_ci_service`
- `cmdb_ci_appl Depends on service_offering`
- `cmdb_ci_ups Depends on service_offering`
- `cmdb_ci_ip_switch Depends on service_offering`
- `u_cmdb_ci_coding_lines Depends on service_offering`
- `u_cmdb_ci_stockroom Depends on service_offering`

Interpretation: `cmdb_ci_service` is the business/service anchor, and `service_offering` acts as the main operational grouping under it. Applications, infrastructure, and factory assets often point back to service offerings.

### Application Runtime Layer

Observed runtime chain:

```text
service_offering
  <- Depends on - cmdb_ci_appl
    <- Runs on - cmdb_ci_app_server
    <- Runs on - cmdb_ci_computer / cmdb_ci_vm
    <- Depends on - cmdb_ci_db_instance
    <- Runs on - cmdb_ci_server
```

Common patterns:

- `cmdb_ci_appl Depends on service_offering`
- `cmdb_ci_appl Depends on cmdb_ci_appl`
- `cmdb_ci_app_server Runs on cmdb_ci_appl`
- `cmdb_ci_computer Runs on cmdb_ci_appl`
- `cmdb_ci_db_instance Depends on cmdb_ci_appl`
- `cmdb_ci_server Runs on cmdb_ci_db_instance`
- `cmdb_ci_server Runs on cmdb_ci_app_server`

Interpretation: the data mixes logical dependencies and runtime placement. `Runs on` should not be blindly collapsed into containment; it should remain a relationship type until graph semantics are defined.

### Infrastructure Location and Hosting Layer

Observed location/hosting chain:

```text
cmdb_ci_datacenter
  <- Located in / Depends on / Houses - cmdb_ci_computer_room
    <- Located in / Depends on / Houses - cmdb_ci_rack
      <- Located in / Houses - cmdb_ci_server
      <- Located in / Houses - cmdb_ci_ip_switch
      <- Located in / Houses - cmdb_ci_ip_firewall
      <- Located in / Houses - cmdb_ci_netgear
      <- Located in - cmdb_ci_ups
```

Common patterns:

- `cmdb_ci_computer_room Located in cmdb_ci_datacenter`
- `cmdb_ci_rack Located in cmdb_ci_computer_room`
- `cmdb_ci_server Located in cmdb_ci_rack`
- `cmdb_ci_ip_switch Located in cmdb_ci_rack`
- `cmdb_ci_ip_firewall Located in cmdb_ci_rack`
- `cmdb_ci_netgear Located in cmdb_ci_rack`
- `cmdb_ci_rack Houses cmdb_ci_computer_room`
- `cmdb_ci_server Houses cmdb_ci_rack`

Interpretation: `Located in` and `Houses` represent inverse containment concepts in natural language, but the spreadsheet direction is not always intuitive. Preserve the original source and target, then normalize display direction later.

### Virtualization Layer

Observed virtualization chain:

```text
cmdb_ci_cluster
  <- Virtualized by - cmdb_ci_vm
  <- Virtualizes - cmdb_ci_vm
```

Common patterns:

- `cmdb_ci_vm Virtualized by cmdb_ci_cluster`
- `cmdb_ci_vm Virtualizes cmdb_ci_cluster`
- Some VM rows have blank relationship labels while still pointing to clusters, networks, applications, or service offerings.

Interpretation: virtualization relationships need validation because both `Virtualized by` and `Virtualizes` appear, and blank relationship values appear in the same domain area.

### Network Connectivity Layer

Observed network chain:

```text
cmdb_ci_ip_network
  <- Connects to / Connected - cmdb_ci_ip_switch
  <- Connects to / Connected - cmdb_ci_ip_firewall
  <- Connects to / Connected - cmdb_ci_netgear
  <- Connects to - cmdb_ci_vm / cmdb_ci_server / cmdb_ci_computer
  <- Connects to - factory devices
```

Common patterns:

- `cmdb_ci_ip_switch Connects to cmdb_ci_ip_network`
- `cmdb_ci_ip_firewall Connects to cmdb_ci_ip_network`
- `cmdb_ci_ip_firewall Connected cmdb_ci_ip_network`
- `cmdb_ci_netgear Connected cmdb_ci_ip_network`
- `cmdb_ci_computer Connects to cmdb_ci_ip_network`
- `u_cmdb_ci_camera Connects to cmdb_ci_ip_network`
- `u_cmdb_ci_plc Connects to cmdb_ci_ip_network`

Interpretation: `Connected` and `Connects to` should probably normalize to a canonical connectivity relationship for analysis, while preserving the original label for audit.

### Factory Production Layer

Observed factory chain:

```text
service_offering
  <- Depends on - u_cmdb_ci_coding_lines / u_cmdb_ci_activation_lines
    <- Depends on - cmdb_ci_appl
    <- Depends on - u_cmdb_ci_plc
    <- Depends on - u_cmdb_ci_beam
    <- Depends on - u_cmdb_ci_light_source
    <- Depends on - u_cmdb_ci_recognition_system
    <- Depends on - u_cmdb_ci_camera
    <- Depends on - u_cmdb_ci_encoder
    <- Depends on - u_cmdb_ci_sensor
    <- Depends on - cmdb_ci_printer
    <- Uses - u_cmdb_ci_ink / u_cmdb_ci_stamp
```

Common patterns:

- `u_cmdb_ci_coding_lines Depends on service_offering`
- `u_cmdb_ci_activation_lines Depends on service_offering`
- `cmdb_ci_appl Depends on u_cmdb_ci_coding_lines`
- `cmdb_ci_appl Depends on u_cmdb_ci_activation_lines`
- `u_cmdb_ci_sensor Depends on u_cmdb_ci_coding_lines`
- `u_cmdb_ci_camera Depends on u_cmdb_ci_recognition_system`
- `u_cmdb_ci_camera Depends on u_cmdb_ci_coding_lines`
- `u_cmdb_ci_light_source Depends on u_cmdb_ci_beam`
- `cmdb_ci_printer Depends on u_cmdb_ci_beam`
- `u_cmdb_ci_ink Uses cmdb_ci_printer`
- `u_cmdb_ci_stamp Uses u_cmdb_ci_label_applicator`

Interpretation: factory sheets are consistent enough to support a production-line hierarchy, but device semantics should remain type-driven and relationship-driven rather than hard-coded by sheet name.

### Relationship Normalization Candidates

Potential canonical groups for analysis:

| Canonical group | Source values | Notes |
|---|---|---|
| Dependency | `Depends on` | Most common relationship type. |
| Location / containment | `Located in`, `Houses` | Direction may need display normalization. |
| Runtime / hosting | `Runs on` | Used for app/server/VM relationships. |
| Connectivity | `Connects to`, `Connected` | Normalize for graph analysis, preserve raw label. |
| Usage | `Uses` | Common for ink, stamps, storage-like references. |
| Virtualization | `Virtualized by`, `Virtualizes` | Needs direction validation. |
| Ownership / operation | `Used by`, `Operates`, `Operates by`, `Operated by` | Rare and should be reviewed manually. |

### Infrastructure-Specific Columns

Observed infrastructure columns:

- `Maintenance Group`
- `Start IP Address`
- `First IP Adress` (typo in source)
- `Second IP Address`
- `IP Address`
- `IP Address 1`
- `IP Address 2`
- `Event-Hostname`
- `Serial number`
- `Firmware version`
- `Core count`
- `RAM`
- `OS`
- `OS Version`
- `OS Image Number`

### Factory/Production-Specific Columns

Observed factory or line columns:

- `GPS coordinates`
- `Solution Type`
- `Product Type`
- `Packaging Type`
- `BRS Type`
- `Line Speed (products per min)`
- `Operating hours`
- `Last service hours`
- `Last service date`
- `Vendor`

Several service and operational columns are intentionally empty in the samples, especially `Maintenance Group`, `Operating hours`, `Last service hours`, and `Last service date`.

## Blank-Row Separators and Repeated Patterns

Most block-based sheets use a blank row after each CI block. A block consists of:

- One CI definition row where `Parent CI` and `Relationship` are blank.
- One or more relationship rows where `Parent CI` and `Relationship` are filled.

Examples:

- Two-row block: CI row + one relationship row.
- Three-row block: CI row + two relationship rows.
- Larger block: CI row + many relationship rows.

Flat sheets do not use this block grammar:

- `Business`
- `Locations` / `Location`
- `VLAN`
- `Hostname-CI mapping`
- `Service Group Dashboard`
- `Changelog`

The parser should treat blank rows as block separators, but it should not require a blank row after the final block.

## Mandatory vs Optional Fields

### Mandatory for Standard CI Rows

Recommended mandatory fields for a CI definition:

- `Class`
- `Name`
- `Description`

Recommended near-mandatory fields for managed CMDB sheets:

- `Project`
- `Location`

However, `Project` and `Location` are absent from some sheets or intentionally blank for some records. They should be warnings, not hard parse failures.

### Mandatory for Relationship Rows

Recommended mandatory fields for a relationship row:

- Source CI identity, inherited from or repeated in `Class` + `Name`
- `Parent CI`
- `Relationship`

In the samples, relationship rows repeat `Class`, `Name`, and `Description`, while CI attribute columns such as `Location`, `Manufacturer`, `Model`, `Version`, and `Project` are usually blank.

### Optional / Class-Specific Fields

Optional fields should be stored as attributes rather than as fixed top-level properties until the domain stabilizes. Examples:

- Hardware: serial number, firmware version, model, manufacturer.
- Network: IP address, event hostname.
- Servers: OS, RAM, core count.
- Production lines: solution type, product type, packaging type, BRS type, line speed.
- Printers: operating hours, last service fields, vendor.

## Proposed Domain Fields

### `ConfigurationItem`

Suggested fields:

- `id`: internal generated identifier.
- `sourceWorkbook`: original file name.
- `sourceSheet`: sheet name.
- `sourceRow`: row number of the CI definition row.
- `ciClass`: value from `Class`.
- `name`: value from `Name`.
- `description`: value from `Description`.
- `project`: value from `Project`, when present.
- `location`: value from `Location`, when present.
- `manufacturer`: value from `Manufacturer`, when present.
- `model`: value from `Model`, when present.
- `version`: value from `Version`, when present.
- `attributes`: map for all additional sheet-specific fields.
- `rawValues`: optional map of original header to original cell value for traceability.

Do not make every observed Excel column a first-class Java field yet. The samples show enough variation that an `attributes` map is safer for the MVP parser.

### `Relationship`

Suggested fields:

- `id`: internal generated identifier.
- `sourceWorkbook`: original file name.
- `sourceSheet`: sheet name.
- `sourceRow`: relationship row number.
- `sourceCiName`: current CI `Name`.
- `sourceCiClass`: current CI `Class`, when present.
- `targetCiName`: value from `Parent CI`.
- `relationshipType`: value from `Relationship`.
- `description`: optional repeated description from the row.
- `rawValues`: optional map of original header to original cell value.

Important naming note: although the Excel column says `Parent CI`, the actual semantics vary by relationship type. In the model, prefer `targetCiName` or `relatedCiName`.

### `CmdbDataset`

Suggested fields:

- `sourceFiles`: list of imported workbook names.
- `items`: all parsed `ConfigurationItem` records.
- `relationships`: all parsed `Relationship` records.
- `locations`: optional location reference records from location sheets.
- `eventBindings`: optional event binding records from the event workbook.
- `changelogEntries`: optional changelog metadata.
- `parseWarnings`: non-fatal parsing warnings.
- `parseErrors`: fatal or skipped-row errors.
- `loadedAt`: import timestamp.

## Parsing Assumptions

Recommended assumptions for the future parser:

1. Detect the header row by locating known headers such as `Class`, `Name`, `Description`, `Parent CI`, and `Relationship`, not by hard-coding row 1.
2. Ignore leading blank columns.
3. Normalize header names by trimming whitespace and collapsing variants.
4. Treat `Class`, `Name`, and `Description` as core CI identity columns.
5. Treat `Parent CI` + `Relationship` as relationship target/type columns.
6. For block sheets, a CI definition row has blank `Parent CI` and blank `Relationship`.
7. A relationship row has `Parent CI` and `Relationship` populated and belongs to the most recent CI block, even though `Class` and `Name` are repeated.
8. Blank rows terminate the current block.
9. Flat sheets should be routed to specialized import handlers or skipped with a clear warning.
10. Preserve source workbook, sheet, and row number for every parsed entity and relationship.

## Edge Cases to Handle

- Header row may not be row 1 (`Changelog`, `Service Group Dashboard`).
- Header row may contain a stray leading value, seen in `Auditing` and `Other DMS Services`.
- Column A is often blank; do not assume cell `A1` is the first header.
- Relationship blocks can have many relationship rows, not just one.
- Some sheets have no relationship columns and should parse as flat CI lists or reference data.
- Some sheets have large trailing empty row ranges.
- Some headers contain typos or variants, e.g. `First IP Adress`.
- Some columns have repeated names, e.g. multiple `CI-classes` columns in `Service Group Dashboard`.
- Some fields are intentionally empty across all records.
- CI names are not globally unique in all samples. The combined sample set contains duplicate CI names, especially between `SPM` and `Stockrooms`.
- All observed relationship targets were resolvable within the combined sample set, but future imports should still report missing target CIs.
- Relationship direction is not guaranteed by the column name. `Parent CI` is a generic target field.

## Recommended Parser Design Direction

Use two parser modes:

1. **Standard CMDB block parser**
   - For sheets with `Class`, `Name`, `Description`, `Parent CI`, and `Relationship`.
   - Produces `ConfigurationItem` and `Relationship` records.

2. **Flat reference parser**
   - For sheets such as `Business`, `Locations`, `VLAN`, and event-binding sheets.
   - Produces reference data or specialized records.

Do not hard-code sheet names as the primary parser strategy. Prefer structure detection, then apply sheet-specific overrides only for known anomalies.

## Open Questions

- Should duplicate CI names be allowed if class or sheet differs?
- Is `Parent CI` always the target of the relationship, or should some relationship types be inverted for graph analysis?
- Should `Business`, `VLAN`, and `Locations` become `ConfigurationItem` records or separate reference entities?
- Should `Hostname-CI mapping` be part of the main dataset or a separate event-binding dataset?
- Which fields are mandatory for enterprise validation versus merely recommended quality checks?
