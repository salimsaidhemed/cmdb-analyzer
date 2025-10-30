# 🧮 CMDB Analyzer

> A cross-platform desktop tool to **validate, analyze, and generate CMDB Excel files** for ServiceNow and internal configuration management workflows.

---

## 🚀 Overview

CMDB Analyzer helps non-technical users quickly:

- Import Excel-based CMDB files (Factories, Applications, Infrastructure)
- Detect and fix **missing relationships**, **invalid classes**, or **dangling CIs**
- Generate **validated CMDB templates** and **Event Management CI Bindings**
- Export validation reports (HTML, PDF, CSV)

The app is written in **Java 21 + JavaFX**, using **Apache POI** for Excel parsing and **JGraphT** for relationship visualization.

---

## 🧩 Tech Stack

| Component    | Technology                   |
| ------------ | ---------------------------- |
| Language     | Java 21 (OpenJDK / Corretto) |
| UI           | JavaFX 21 + ControlsFX       |
| Build System | Maven                        |
| Excel Parser | Apache POI                   |
| Graph Engine | JGraphT                      |
| Packaging    | jpackage (bundled JRE)       |

---

## 🧱 Modules

| Module           | Description                                    |
| ---------------- | ---------------------------------------------- |
| `cmdb-core`      | Core data model (CIs, Relationships, Findings) |
| `cmdb-importers` | Excel file detectors and parsers               |
| `cmdb-rules`     | Validation engine and rules evaluator          |
| `cmdb-ui`        | JavaFX front-end application                   |
| `cmdb-reports`   | HTML/PDF/CSV report generation                 |

---

## 🧰 Build & Run (WSL / Linux)

```bash
# Clone the repository
git clone https://github.com/<your-username>/cmdb-analyzer.git
cd cmdb-analyzer

# Build all modules
mvn clean package

# Run the JavaFX app
mvn -pl cmdb-ui javafx:run
```
