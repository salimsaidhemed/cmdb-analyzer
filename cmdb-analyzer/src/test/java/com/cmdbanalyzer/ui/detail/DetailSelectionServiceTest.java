package com.cmdbanalyzer.ui.detail;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationResult;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.controller.preview.ImportPreviewViewModel;
import com.cmdbanalyzer.model.CmdbSheet;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.Relationship;
import com.cmdbanalyzer.model.RelationshipStatus;
import com.cmdbanalyzer.model.SheetType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetailSelectionServiceTest {

    private final DetailSelectionService service = new DetailSelectionService();

    @Test
    void mapsConfigurationItemToDetailFields() {
        DetailViewModel detail = service.forConfigurationItem(viewModel(), "app-1");

        assertEquals("APP01", detail.title());
        assertEquals("Application", detail.badge());
        assertEquals("APP01", field(detail, "Summary", "CI name"));
        assertEquals("1", field(detail, "Relationships", "Outgoing count"));
        assertEquals("0", field(detail, "Relationships", "Incoming count"));
        assertEquals("DB01", field(detail, "Relationships", "Direct dependencies"));
        assertEquals("Platform", field(detail, "Attributes", "Owner"));
    }

    @Test
    void mapsRelationshipToDetailFields() {
        DetailViewModel detail = service.forRelationship(viewModel(), "rel-2");

        assertEquals("HOSTS", detail.title());
        assertEquals("UNRESOLVED", detail.badge());
        assertEquals("MissingService", field(detail, "Endpoints", "Target CI"));
        assertEquals("Target CI could not be resolved.", detail.notices().get(0).message());
        assertEquals(DetailViewModel.DetailTone.WARNING, detail.tone());
    }

    @Test
    void mapsIssueToDetailFields() {
        DetailViewModel detail = service.forValidationIssue(viewModel(), "issue-1");

        assertEquals("UNRESOLVED_RELATIONSHIP", detail.title());
        assertEquals("ERROR", detail.badge());
        assertEquals("Missing target", field(detail, "Summary", "Message"));
        assertEquals("rel-2", field(detail, "Affected object", "Affected relationship ID"));
        assertEquals(DetailViewModel.DetailTone.ERROR, detail.tone());
    }

    @Test
    void nullSelectionReturnsEmptyStateModel() {
        DetailViewModel detail = service.forConfigurationItem(null, "missing");

        assertEquals("Nothing selected", detail.title());
        assertTrue(detail.sections().isEmpty());
    }

    private String field(DetailViewModel detail, String sectionTitle, String fieldLabel) {
        return detail.sections().stream()
                .filter(section -> sectionTitle.equals(section.title()))
                .flatMap(section -> section.fields().stream())
                .filter(field -> fieldLabel.equals(field.label()))
                .map(DetailViewModel.DetailField::value)
                .findFirst()
                .orElseThrow();
    }

    private ImportPreviewViewModel viewModel() {
        ConfigurationItem app = new ConfigurationItem(
                "app-1",
                "APP01",
                "Application",
                "Customer portal",
                Map.of("Owner", "Platform"),
                "/tmp/sample.xlsx",
                "CMDB",
                2,
                null
        );
        ConfigurationItem db = new ConfigurationItem(
                "db-1",
                "DB01",
                "Database",
                "Primary database",
                Map.of(),
                "/tmp/sample.xlsx",
                "CMDB",
                5,
                null
        );
        Relationship resolved = new Relationship(
                "rel-1",
                "app-1",
                "db-1",
                "DB01",
                "DEPENDS_ON",
                "Depends On",
                "/tmp/sample.xlsx",
                "CMDB",
                3,
                RelationshipStatus.RESOLVED
        );
        Relationship unresolved = new Relationship(
                "rel-2",
                "db-1",
                null,
                "MissingService",
                "HOSTS",
                "Hosts",
                "/tmp/sample.xlsx",
                "CMDB",
                6,
                RelationshipStatus.UNRESOLVED
        );
        CmdbSheet sheet = new CmdbSheet(
                "CMDB",
                SheetType.CI_BLOCK,
                1,
                Map.of("name", 0),
                List.of(),
                List.of(app, db),
                List.of(resolved, unresolved),
                List.of(),
                List.of()
        );
        CmdbWorkbook workbook = new CmdbWorkbook(
                "/tmp/sample.xlsx",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(sheet),
                List.of()
        );
        ValidationIssue issue = new ValidationIssue(
                "issue-1",
                ValidationSeverity.ERROR,
                ValidationIssueType.UNRESOLVED_RELATIONSHIP,
                "Missing target",
                "/tmp/sample.xlsx",
                "CMDB",
                6,
                null,
                "rel-2",
                "Create the missing CI"
        );
        return new ImportPreviewViewModel(
                workbook,
                new ValidationResult(List.of(issue)),
                null,
                null,
                "sample.xlsx",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
