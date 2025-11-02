# CI/CD Pipeline — CMDB Analyzer

The CMDB Analyzer project uses a multi-stage GitHub Actions pipeline for automated build, test, packaging, and (optional) artifact publishing.

## Pipeline Overview

| Stage          | Description                                                                                                                                       | Trigger                                     |
| -------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------- |
| 🧱 **Build**   | Compiles all Maven modules (`cmdb-core`, `cmdb-importers`, `cmdb-rules`, `cmdb-reports`, `cmdb-ui`) and installs them into the local Maven cache. | On every `push` or `pull_request` to `main` |
| ✅ **Test**    | Runs all JUnit 5 tests across modules. Uploads Surefire test reports as artifacts.                                                                | After successful build                      |
| 📦 **Package** | Packages JARs for each module and stores them as workflow artifacts (`target/*.jar`).                                                             | After successful tests                      |
| 🚀 **Publish** | (Optional) Deploys snapshot artifacts to GitHub Packages or Nexus. Runs only on `main`.                                                           | After packaging on `main` branch            |

## Workflow File

**Location**: [.github/workflows/build.yml](../.github/workflows/build.yml)

### Main workflow command:

```yaml
- name: Build & Test (Maven)
  run: mvn -B clean verify
```

### Full pipeline structure:

```yaml
jobs:
  build:
    # Compiles and installs all modules (skipping tests)
  test:
    # Runs all tests and uploads reports
  package:
    # Packages distributable JARs
  publish:
    # Deploys artifacts (conditional)
```

Each stage runs on Ubuntu runners with Temurin JDK 21 and uses Maven cache to speed up subsequent builds.

## Artifacts

| Artifact                     | Description                         | Path                          |
| ---------------------------- | ----------------------------------- | ----------------------------- |
| `cmdb-analyzer-build`        | Compiled intermediate module builds | `**/target/*.jar`             |
| `test-reports`               | JUnit / Surefire XML reports        | `**/target/surefire-reports/` |
| `cmdb-analyzer-jars`         | Final packaged JARs for all modules | `**/target/*.jar`             |
| `code-coverage` _(optional)_ | Jacoco HTML reports                 | `**/target/site/jacoco/`      |

## Developer Notes

- **Run Locally:**

```bash
mvn clean verify
```

(Runs full build + test locally, just like CI.)

- **Test a Specific Module:**

```bash
mvn -pl cmdb-importers test
```

- **Add a new module:**
  Add the module to the root pom.xml <modules> section — it will automatically be included in the pipeline.

- **Publishing:**

Artifact publishing is gated to the main branch.
The workflow uses:

```yaml
if: github.ref == 'refs/heads/main'
```

to prevent accidental deployments from feature branches.

## CI/CD Philosophy

Fast feedback, reproducible builds, and traceable artifacts.

The CI pipeline ensures that:

- All modules compile and pass tests before merging to main.

- Artifacts are always reproducible from clean sources.

- Failures are visible immediately with clear logs and reports.

## Future Enhancements

| Feature                       | Purpose                                               |
| ----------------------------- | ----------------------------------------------------- |
| **Jacoco Coverage Reports**   | Generate and upload HTML/coverage stats               |
| **CodeQL or SonarQube Scan**  | Security and code quality scanning                    |
| **Release Stage (vX.Y.Z)**    | Bundle `cmdb-ui` fat JAR + embedded JRE for end users |
| **Slack/Teams Notifications** | CI result notifications for internal teams            |
| **Parallel Matrix Testing**   | Run tests across multiple Java versions if needed     |
