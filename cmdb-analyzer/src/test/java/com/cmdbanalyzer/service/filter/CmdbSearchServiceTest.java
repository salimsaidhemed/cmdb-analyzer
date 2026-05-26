package com.cmdbanalyzer.service.filter;

import com.cmdbanalyzer.analyzer.validation.ValidationResult;
import com.cmdbanalyzer.controller.preview.ImportPreviewViewModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CmdbSearchServiceTest {

    private final CmdbSearchService searchService = new CmdbSearchService();

    @Test
    void queryMatchesConfigurationItemName() {
        FilterResult result = searchService.filter(viewModel(), criteria("app01", null, null, null, null, null, null));

        assertEquals(1, result.configurationItems().size());
        assertEquals("APP01", result.configurationItems().get(0).name());
    }

    @Test
    void queryMatchesConfigurationItemClass() {
        FilterResult result = searchService.filter(viewModel(), criteria("database", null, null, null, null, null, null));

        assertEquals(1, result.configurationItems().size());
        assertEquals("DB01", result.configurationItems().get(0).name());
    }

    @Test
    void queryMatchesRelationshipTargetName() {
        FilterResult result = searchService.filter(viewModel(), criteria("missingservice", null, null, null, null, null, null));

        assertEquals(1, result.relationships().size());
        assertEquals("MissingService", result.relationships().get(0).targetName());
    }

    @Test
    void relationshipStatusFilterWorks() {
        FilterResult result = searchService.filter(viewModel(), criteria(null, null, null, "UNRESOLVED", null, null, null));

        assertEquals(1, result.relationships().size());
        assertEquals("UNRESOLVED", result.relationships().get(0).status());
    }

    @Test
    void issueSeverityFilterWorks() {
        FilterResult result = searchService.filter(viewModel(), criteria(null, null, null, null, null, "ERROR", null));

        assertEquals(1, result.issues().size());
        assertEquals("ERROR", result.issues().get(0).severity());
    }

    @Test
    void combinedFiltersWork() {
        FilterResult result = searchService.filter(viewModel(), criteria("missing", null, "Infra", "UNRESOLVED", "HOSTS", "ERROR", null));

        assertEquals(0, result.configurationItems().size());
        assertEquals(1, result.relationships().size());
        assertEquals(1, result.issues().size());
    }

    @Test
    void emptyQueryReturnsAllData() {
        ImportPreviewViewModel viewModel = viewModel();

        FilterResult result = searchService.filter(viewModel, CmdbFilterCriteria.empty());

        assertEquals(viewModel.configurationItems().size(), result.configurationItems().size());
        assertEquals(viewModel.relationships().size(), result.relationships().size());
        assertEquals(viewModel.issues().size(), result.issues().size());
        assertEquals(viewModel.sheets().size(), result.sheets().size());
    }

    @Test
    void nullSafeFilteringReturnsAllData() {
        ImportPreviewViewModel viewModel = viewModelWithNullLikeValues();

        FilterResult result = searchService.filter(viewModel, null);

        assertEquals(1, result.configurationItems().size());
        assertEquals(1, result.relationships().size());
        assertEquals(1, result.issues().size());
    }

    private CmdbFilterCriteria criteria(
            String query,
            String ciClass,
            String sourceSheet,
            String relationshipStatus,
            String relationshipType,
            String issueSeverity,
            String issueType
    ) {
        return new CmdbFilterCriteria(
                query,
                ciClass,
                sourceSheet,
                relationshipStatus,
                relationshipType,
                issueSeverity,
                issueType
        );
    }

    private ImportPreviewViewModel viewModel() {
        return new ImportPreviewViewModel(
                null,
                new ValidationResult(List.of()),
                null,
                null,
                "sample.xlsx",
                List.of(
                        new ImportPreviewViewModel.SheetPreviewRow("Apps", "CI_BLOCK", "1", 1, 0),
                        new ImportPreviewViewModel.SheetPreviewRow("Infra", "CI_BLOCK", "1", 1, 0)
                ),
                List.of(
                        new ImportPreviewViewModel.ConfigurationItemPreviewRow(
                                "ci-1",
                                "APP01",
                                "Application",
                                "Customer portal",
                                "Apps",
                                2,
                                "sample|application|app01",
                                Map.of("Owner", "Platform")
                        ),
                        new ImportPreviewViewModel.ConfigurationItemPreviewRow(
                                "ci-2",
                                "DB01",
                                "Database",
                                "Primary database",
                                "Infra",
                                5,
                                "sample|database|db01",
                                Map.of()
                        )
                ),
                List.of(
                        new ImportPreviewViewModel.RelationshipPreviewRow(
                                "rel-1",
                                "ci-1",
                                "APP01",
                                "DB01",
                                "DEPENDS_ON",
                                "Depends On",
                                "RESOLVED",
                                "Apps",
                                3
                        ),
                        new ImportPreviewViewModel.RelationshipPreviewRow(
                                "rel-2",
                                "ci-2",
                                "DB01",
                                "MissingService",
                                "HOSTS",
                                "Hosts",
                                "UNRESOLVED",
                                "Infra",
                                6
                        )
                ),
                List.of(),
                List.of(
                        new ImportPreviewViewModel.ValidationIssuePreviewRow(
                                "issue-1",
                                "ERROR",
                                "UNRESOLVED_RELATIONSHIP",
                                "Missing target",
                                "sample.xlsx",
                                "Infra",
                                "6",
                                null,
                                "rel-2",
                                "Create the missing CI"
                        ),
                        new ImportPreviewViewModel.ValidationIssuePreviewRow(
                                "issue-2",
                                "WARNING",
                                "DUPLICATE_CI_IDENTITY",
                                "Duplicate CI",
                                "sample.xlsx",
                                "Apps",
                                "2",
                                "ci-1",
                                null,
                                "Review duplicates"
                        )
                )
        );
    }

    private ImportPreviewViewModel viewModelWithNullLikeValues() {
        return new ImportPreviewViewModel(
                null,
                new ValidationResult(List.of()),
                null,
                null,
                "sample.xlsx",
                List.of(new ImportPreviewViewModel.SheetPreviewRow("", "", "", 0, 0)),
                List.of(new ImportPreviewViewModel.ConfigurationItemPreviewRow(
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        null,
                        null
                )),
                List.of(new ImportPreviewViewModel.RelationshipPreviewRow(null, null, null, null, null, null, null, null, 0)),
                List.of(new ImportPreviewViewModel.WarningPreviewRow(null, null, null, null, null, null)),
                List.of(new ImportPreviewViewModel.ValidationIssuePreviewRow(null, null, null, null, null, null, null, null, null, null))
        );
    }
}
