package com.cmdbanalyzer.analyzer.validation;

import com.cmdbanalyzer.graph.CmdbGraphBuilder;
import com.cmdbanalyzer.graph.GraphBuildResult;
import com.cmdbanalyzer.model.CmdbSheet;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.Relationship;
import com.cmdbanalyzer.model.RelationshipStatus;
import com.cmdbanalyzer.model.SheetType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphAwareValidationTest {

    private final CmdbGraphBuilder graphBuilder = new CmdbGraphBuilder();
    private final CmdbValidationEngine validationEngine = new CmdbValidationEngine();

    @Test
    void detectsOrphanCi() {
        CmdbWorkbook workbook = workbook(List.of(item("ci-1", "APP01")), List.of());

        ValidationResult result = validateWithGraph(workbook);

        assertHasIssue(result, ValidationIssueType.ORPHAN_CI);
    }

    @Test
    void detectsCircularDependency() {
        CmdbWorkbook workbook = workbook(
                List.of(item("app-1", "APP01"), item("db-1", "DB01")),
                List.of(
                        relationship("rel-1", "app-1", "db-1", RelationshipStatus.RESOLVED),
                        relationship("rel-2", "db-1", "app-1", RelationshipStatus.RESOLVED)
                )
        );

        ValidationResult result = validateWithGraph(workbook);

        assertHasIssue(result, ValidationIssueType.CIRCULAR_DEPENDENCY);
    }

    @Test
    void detectsDuplicateRelationships() {
        CmdbWorkbook workbook = workbook(
                List.of(item("app-1", "APP01"), item("db-1", "DB01")),
                List.of(
                        relationship("rel-1", "app-1", "db-1", RelationshipStatus.RESOLVED),
                        relationship("rel-2", "app-1", "db-1", RelationshipStatus.RESOLVED)
                )
        );

        ValidationResult result = validateWithGraph(workbook);

        assertHasIssue(result, ValidationIssueType.DUPLICATE_RELATIONSHIP);
    }

    @Test
    void detectsIsolatedSubgraph() {
        CmdbWorkbook workbook = workbook(
                List.of(
                        item("app-1", "APP01"),
                        item("db-1", "DB01"),
                        item("app-2", "APP02"),
                        item("db-2", "DB02")
                ),
                List.of(
                        relationship("rel-1", "app-1", "db-1", RelationshipStatus.RESOLVED),
                        relationship("rel-2", "app-2", "db-2", RelationshipStatus.RESOLVED)
                )
        );

        ValidationResult result = validateWithGraph(workbook);

        assertHasIssue(result, ValidationIssueType.ISOLATED_SUBGRAPH);
    }

    @Test
    void detectsResolvedRelationshipMissingFromGraph() {
        CmdbWorkbook workbook = workbook(
                List.of(item("db-1", "DB01")),
                List.of(relationship("rel-1", "missing-source", "db-1", RelationshipStatus.RESOLVED))
        );

        ValidationResult result = validateWithGraph(workbook);

        assertHasIssue(result, ValidationIssueType.MISSING_GRAPH_EDGE);
    }

    @Test
    void validationContextWorksWithExistingRules() {
        CmdbWorkbook workbook = workbook(List.of(item("ci-1", "", "Application")), List.of());

        ValidationResult result = validateWithGraph(workbook);

        assertHasIssue(result, ValidationIssueType.MISSING_CI_NAME);
    }

    @Test
    void engineIncludesBasicAndGraphRules() {
        CmdbWorkbook workbook = workbook(List.of(item("ci-1", "", "Application")), List.of());

        ValidationResult result = validateWithGraph(workbook);

        assertHasIssue(result, ValidationIssueType.MISSING_CI_NAME);
        assertHasIssue(result, ValidationIssueType.ORPHAN_CI);
    }

    @Test
    void emptyGraphDoesNotCrash() {
        CmdbWorkbook workbook = new CmdbWorkbook("empty.xlsx", Instant.now(), List.of(), List.of());

        ValidationResult result = validateWithGraph(workbook);

        assertEquals(0, result.totalIssueCount());
    }

    private ValidationResult validateWithGraph(CmdbWorkbook workbook) {
        GraphBuildResult graphBuildResult = graphBuilder.build(workbook);
        return validationEngine.validate(new ValidationContext(workbook, graphBuildResult.graph(), graphBuildResult));
    }

    private void assertHasIssue(ValidationResult result, ValidationIssueType issueType) {
        assertTrue(
                result.issues().stream().anyMatch(issue -> issue.getType() == issueType),
                "Expected validation issue: " + issueType
        );
    }

    private CmdbWorkbook workbook(List<ConfigurationItem> items, List<Relationship> relationships) {
        CmdbSheet sheet = new CmdbSheet();
        sheet.setName("Applications");
        sheet.setType(SheetType.CI_BLOCK);
        items.forEach(sheet::addConfigurationItem);
        relationships.forEach(sheet::addRelationship);
        return new CmdbWorkbook("sample.xlsx", Instant.now(), new ArrayList<>(List.of(sheet)), List.of());
    }

    private ConfigurationItem item(String id, String name) {
        return item(id, name, "Application");
    }

    private ConfigurationItem item(String id, String name, String ciClass) {
        return new ConfigurationItem(
                id,
                name,
                ciClass,
                null,
                Map.of(),
                "sample.xlsx",
                "Applications",
                2,
                null
        );
    }

    private Relationship relationship(String id, String sourceCiId, String targetCiId, RelationshipStatus status) {
        return new Relationship(
                id,
                sourceCiId,
                targetCiId,
                targetCiId,
                "depends on",
                "Depends On",
                "sample.xlsx",
                "Applications",
                3,
                status
        );
    }
}
