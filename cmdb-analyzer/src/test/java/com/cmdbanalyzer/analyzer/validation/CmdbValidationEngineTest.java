package com.cmdbanalyzer.analyzer.validation;

import com.cmdbanalyzer.model.CmdbSheet;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.ParserWarning;
import com.cmdbanalyzer.model.Relationship;
import com.cmdbanalyzer.model.RelationshipStatus;
import com.cmdbanalyzer.model.SheetType;
import com.cmdbanalyzer.model.WarningSeverity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CmdbValidationEngineTest {

    private final CmdbValidationEngine engine = new CmdbValidationEngine();

    @Test
    void reportsMissingCiName() {
        CmdbWorkbook workbook = workbook(List.of(item("ci-1", " ", "Application", null)), List.of(), List.of());

        ValidationResult result = engine.validate(workbook);

        assertHasIssue(result, ValidationIssueType.MISSING_CI_NAME);
    }

    @Test
    void reportsMissingCiClass() {
        CmdbWorkbook workbook = workbook(List.of(item("ci-1", "App01", null, null)), List.of(), List.of());

        ValidationResult result = engine.validate(workbook);

        assertHasIssue(result, ValidationIssueType.MISSING_CI_CLASS);
    }

    @Test
    void reportsDuplicateIdentityKeys() {
        ConfigurationItem first = item("ci-1", "App01", "Application", "same-key");
        ConfigurationItem second = item("ci-2", "App02", "Application", "same-key");
        CmdbWorkbook workbook = workbook(List.of(first, second), List.of(), List.of());

        ValidationResult result = engine.validate(workbook);

        long duplicateCount = result.issues().stream()
                .filter(issue -> issue.getType() == ValidationIssueType.DUPLICATE_CI_IDENTITY)
                .count();
        assertEquals(2, duplicateCount);
    }

    @Test
    void reportsUnresolvedRelationship() {
        CmdbWorkbook workbook = workbook(List.of(), List.of(relationship("rel-1", "ci-1", null, RelationshipStatus.UNRESOLVED)), List.of());

        ValidationResult result = engine.validate(workbook);

        assertHasIssue(result, ValidationIssueType.UNRESOLVED_RELATIONSHIP);
    }

    @Test
    void reportsMalformedRelationship() {
        CmdbWorkbook workbook = workbook(List.of(), List.of(relationship("rel-1", "ci-1", null, RelationshipStatus.MALFORMED)), List.of());

        ValidationResult result = engine.validate(workbook);

        assertHasIssue(result, ValidationIssueType.MALFORMED_RELATIONSHIP);
    }

    @Test
    void reportsSelfReferenceRelationship() {
        CmdbWorkbook workbook = workbook(List.of(), List.of(relationship("rel-1", "ci-1", "ci-1", RelationshipStatus.RESOLVED)), List.of());

        ValidationResult result = engine.validate(workbook);

        assertHasIssue(result, ValidationIssueType.SELF_REFERENCE);
    }

    @Test
    void convertsParserWarningsToValidationIssues() {
        ParserWarning warning = new ParserWarning(
                WarningSeverity.WARNING,
                "Parser warning",
                "sample.xlsx",
                "Applications",
                3,
                "Name",
                ""
        );
        CmdbWorkbook workbook = workbook(List.of(), List.of(), List.of(warning));

        ValidationResult result = engine.validate(workbook);

        assertHasIssue(result, ValidationIssueType.PARSER_WARNING);
    }

    @Test
    void countsValidationResultSeverities() {
        ValidationResult result = new ValidationResult(List.of(
                new ValidationIssue(null, ValidationSeverity.ERROR, ValidationIssueType.MISSING_CI_NAME, "", "", "", 1, "ci-1", null, ""),
                new ValidationIssue(null, ValidationSeverity.WARNING, ValidationIssueType.UNRESOLVED_RELATIONSHIP, "", "", "", 2, null, "rel-1", ""),
                new ValidationIssue(null, ValidationSeverity.INFO, ValidationIssueType.UNKNOWN_SHEET_TYPE, "", "", "", null, null, null, "")
        ));

        assertEquals(3, result.totalIssueCount());
        assertEquals(1, result.errorCount());
        assertEquals(1, result.warningCount());
        assertEquals(1, result.infoCount());
    }

    private void assertHasIssue(ValidationResult result, ValidationIssueType issueType) {
        assertTrue(
                result.issues().stream().anyMatch(issue -> issue.getType() == issueType),
                "Expected validation issue: " + issueType
        );
    }

    private CmdbWorkbook workbook(
            List<ConfigurationItem> items,
            List<Relationship> relationships,
            List<ParserWarning> parserWarnings
    ) {
        CmdbSheet sheet = new CmdbSheet();
        sheet.setName("Applications");
        sheet.setType(SheetType.CI_BLOCK);
        items.forEach(sheet::addConfigurationItem);
        relationships.forEach(sheet::addRelationship);
        return new CmdbWorkbook("sample.xlsx", Instant.now(), new ArrayList<>(List.of(sheet)), parserWarnings);
    }

    private ConfigurationItem item(String id, String name, String ciClass, String identityKey) {
        return new ConfigurationItem(
                id,
                name,
                ciClass,
                null,
                Map.of(),
                "sample.xlsx",
                "Applications",
                2,
                identityKey
        );
    }

    private Relationship relationship(String id, String sourceCiId, String targetCiId, RelationshipStatus status) {
        return new Relationship(
                id,
                sourceCiId,
                targetCiId,
                "Target",
                "depends on",
                "Depends On",
                "sample.xlsx",
                "Applications",
                3,
                status
        );
    }
}
