# CMDB Analyzer

Desktop CMDB analysis and visualization tool built with JavaFX.

CMDB Analyzer parses Excel-based CMDB exports, validates configuration items and relationships, detects structural issues, and visualizes dependencies between systems.

---

## Features

### Current Goals (MVP)

- Import Excel CMDB files
- Parse Configuration Items (CIs)
- Parse CI relationships
- Validate CMDB structure
- Detect orphaned or invalid CIs
- Relationship graph analysis
- CI search and filtering
- JSON export/import
- Desktop visualization interface

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17+ |
| UI | JavaFX |
| Build Tool | Maven |
| Excel Parsing | Apache POI |
| Graph Analysis | JGraphT |
| JSON | Jackson |
| Logging | SLF4J + Logback |

---

## Project Structure

```text
src/main/java/com/netos/cmdb/
├── app/
├── controller/
├── model/
├── parser/
├── service/
├── analyzer/
├── graph/
├── export/
└── util/

src/main/resources/
├── fxml/
├── css/
└── icons/
```

---

## Planned Architecture

### Core Modules

- Excel Import Engine
- CMDB Validation Engine
- Relationship Graph Builder
- Analysis Engine
- Visualization UI
- Reporting & Export

---

## Validation Goals

The analyzer will detect:

- Orphaned CIs
- Missing relationships
- Invalid references
- Circular dependencies
- Duplicate entities
- Missing required fields
- Unknown CI classes

---

## Development Workflow

Git Flow style branching:

```text
main
develop
feature/*
```

Feature branches map directly to implementation milestones.

Example:

```text
feature/03-excel-parser
feature/05-validation-engine
feature/09-graph-builder
```

---

## Running the Application

```bash
mvn clean javafx:run
```

---

## Build

```bash
mvn clean package
```

---

## Roadmap

### Milestone 1
- Project shell
- JavaFX layout
- Domain model

### Milestone 2
- Excel parsing
- Import preview

### Milestone 3
- Validation engine
- Relationship analysis

### Milestone 4
- Graph visualization
- Search/filtering

### Milestone 5
- Export/reporting
- Packaging

---

## License

Internal / Private Project
