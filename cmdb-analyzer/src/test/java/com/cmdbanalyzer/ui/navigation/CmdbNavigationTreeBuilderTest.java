package com.cmdbanalyzer.ui.navigation;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationResult;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
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

import static com.cmdbanalyzer.ui.navigation.CmdbNavigationNode.NavigationNodeType.CI_CLASS;
import static com.cmdbanalyzer.ui.navigation.CmdbNavigationNode.NavigationNodeType.GROUP;
import static com.cmdbanalyzer.ui.navigation.CmdbNavigationNode.NavigationNodeType.ISSUE_SEVERITY;
import static com.cmdbanalyzer.ui.navigation.CmdbNavigationNode.NavigationNodeType.RELATIONSHIP_STATUS;
import static com.cmdbanalyzer.ui.navigation.CmdbNavigationNode.NavigationNodeType.SHEET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CmdbNavigationTreeBuilderTest {

    private final CmdbNavigationTreeBuilder builder = new CmdbNavigationTreeBuilder();

    @Test
    void groupsConfigurationItemsBySheet() {
        CmdbWorkbook workbook = workbook(
                sheet("Applications", List.of(ci("app-1", "APP01", "Application", "Applications")), List.of()),
                sheet("Databases", List.of(ci("db-1", "DB01", "Database", "Databases")), List.of())
        );

        CmdbNavigationNode root = builder.build(workbook, new ValidationResult(List.of()));
        CmdbNavigationNode sheets = child(root, GROUP, "Sheets");

        assertEquals(2, sheets.count());
        assertEquals(1, child(sheets, SHEET, "Applications").count());
        assertEquals("APP01", child(child(sheets, SHEET, "Applications"), CmdbNavigationNode.NavigationNodeType.CI, "APP01").label());
    }

    @Test
    void groupsConfigurationItemsByClass() {
        CmdbWorkbook workbook = workbook(sheet(
                "CMDB",
                List.of(
                        ci("app-1", "APP01", "Application", "CMDB"),
                        ci("app-2", "APP02", "Application", "CMDB"),
                        ci("db-1", "DB01", "Database", "CMDB")
                ),
                List.of()
        ));

        CmdbNavigationNode classes = child(builder.build(workbook, null), GROUP, "CI Classes");

        assertEquals(2, classes.count());
        assertEquals(2, child(classes, CI_CLASS, "Application").count());
        assertEquals(1, child(classes, CI_CLASS, "Database").count());
    }

    @Test
    void groupsRelationshipsByStatus() {
        CmdbWorkbook workbook = workbook(sheet(
                "CMDB",
                List.of(ci("app-1", "APP01", "Application", "CMDB")),
                List.of(
                        relationship("rel-1", RelationshipStatus.RESOLVED),
                        relationship("rel-2", RelationshipStatus.UNRESOLVED),
                        relationship("rel-3", RelationshipStatus.MALFORMED),
                        relationship("rel-4", RelationshipStatus.UNRESOLVED)
                )
        ));

        CmdbNavigationNode statuses = child(builder.build(workbook, null), GROUP, "Relationship Status");

        assertEquals(4, statuses.count());
        assertEquals(1, child(statuses, RELATIONSHIP_STATUS, "RESOLVED").count());
        assertEquals(2, child(statuses, RELATIONSHIP_STATUS, "UNRESOLVED").count());
        assertEquals(1, child(statuses, RELATIONSHIP_STATUS, "MALFORMED").count());
    }

    @Test
    void groupsIssuesBySeverity() {
        ValidationResult validationResult = new ValidationResult(List.of(
                issue("issue-1", ValidationSeverity.ERROR),
                issue("issue-2", ValidationSeverity.WARNING),
                issue("issue-3", ValidationSeverity.WARNING),
                issue("issue-4", ValidationSeverity.INFO)
        ));

        CmdbNavigationNode issues = child(builder.build(workbook(), validationResult), GROUP, "Issues");

        assertEquals(4, issues.count());
        assertEquals(1, child(issues, ISSUE_SEVERITY, "ERROR").count());
        assertEquals(2, child(issues, ISSUE_SEVERITY, "WARNING").count());
        assertEquals(1, child(issues, ISSUE_SEVERITY, "INFO").count());
    }

    @Test
    void handlesEmptyWorkbookSafely() {
        CmdbNavigationNode root = builder.build(workbook(), null);

        assertEquals("sample.xlsx", root.label());
        assertEquals("sample.xlsx (0)", root.displayLabel());
        assertEquals(4, root.children().size());
        assertEquals(0, child(root, GROUP, "Sheets").count());
        assertEquals(0, child(root, GROUP, "CI Classes").count());
        assertEquals(0, child(root, GROUP, "Relationship Status").count());
        assertEquals(0, child(root, GROUP, "Issues").count());
    }

    private CmdbNavigationNode child(
            CmdbNavigationNode node,
            CmdbNavigationNode.NavigationNodeType type,
            String label
    ) {
        CmdbNavigationNode child = node.children().stream()
                .filter(candidate -> candidate.type() == type)
                .filter(candidate -> label.equals(candidate.label()))
                .findFirst()
                .orElse(null);
        assertNotNull(child, () -> "Expected child " + label + " under " + node.label());
        return child;
    }

    private CmdbWorkbook workbook(CmdbSheet... sheets) {
        return new CmdbWorkbook("/tmp/sample.xlsx", Instant.parse("2026-01-01T00:00:00Z"), List.of(sheets), List.of());
    }

    private CmdbSheet sheet(String name, List<ConfigurationItem> items, List<Relationship> relationships) {
        return new CmdbSheet(
                name,
                SheetType.CI_BLOCK,
                1,
                Map.of("name", 0),
                List.of(),
                items,
                relationships,
                List.of(),
                List.of()
        );
    }

    private ConfigurationItem ci(String id, String name, String ciClass, String sourceSheet) {
        return new ConfigurationItem(
                id,
                name,
                ciClass,
                "Description",
                Map.of(),
                "/tmp/sample.xlsx",
                sourceSheet,
                2,
                null
        );
    }

    private Relationship relationship(String id, RelationshipStatus status) {
        return new Relationship(
                id,
                "app-1",
                status == RelationshipStatus.RESOLVED ? "db-1" : null,
                "DB01",
                "DEPENDS_ON",
                "Depends On",
                "/tmp/sample.xlsx",
                "CMDB",
                3,
                status
        );
    }

    private ValidationIssue issue(String id, ValidationSeverity severity) {
        return new ValidationIssue(
                id,
                severity,
                ValidationIssueType.UNRESOLVED_RELATIONSHIP,
                "Issue",
                "/tmp/sample.xlsx",
                "CMDB",
                3,
                null,
                "rel-1",
                "Review relationship"
        );
    }
}
