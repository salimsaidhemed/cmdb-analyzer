package com.cmdbanalyzer.service;

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
import static org.junit.jupiter.api.Assertions.assertNull;

class RelationshipResolutionServiceTest {

    private final RelationshipResolutionService service = new RelationshipResolutionService();

    @Test
    void resolvesExactNameMatch() {
        ConfigurationItem target = item("ci-db", "DB01", "Database");
        Relationship relationship = relationship("DB01", RelationshipStatus.UNRESOLVED);
        CmdbWorkbook workbook = workbook(List.of(item("ci-app", "App01", "Application"), target), List.of(relationship));

        RelationshipResolutionSummary summary = service.resolve(workbook);

        assertEquals(RelationshipStatus.RESOLVED, relationship.getStatus());
        assertEquals("ci-db", relationship.getTargetCiId());
        assertEquals(1, summary.resolvedCount());
        assertEquals(0, summary.warnings().size());
    }

    @Test
    void resolvesUsingCaseAndWhitespaceNormalization() {
        ConfigurationItem target = item("ci-db", "db01", "Database");
        Relationship relationship = relationship("  DB01  ", RelationshipStatus.UNRESOLVED);
        CmdbWorkbook workbook = workbook(List.of(target), List.of(relationship));

        RelationshipResolutionSummary summary = service.resolve(workbook);

        assertEquals(RelationshipStatus.RESOLVED, relationship.getStatus());
        assertEquals("ci-db", relationship.getTargetCiId());
        assertEquals(1, summary.resolvedCount());
    }

    @Test
    void leavesMissingTargetUnresolvedAndAddsWarning() {
        Relationship relationship = relationship("Missing DB", RelationshipStatus.UNRESOLVED);
        CmdbWorkbook workbook = workbook(List.of(item("ci-app", "App01", "Application")), List.of(relationship));

        RelationshipResolutionSummary summary = service.resolve(workbook);

        assertEquals(RelationshipStatus.UNRESOLVED, relationship.getStatus());
        assertNull(relationship.getTargetCiId());
        assertEquals(1, summary.unresolvedCount());
        assertEquals(1, summary.warnings().size());
        assertEquals("Relationship target could not be resolved", summary.warnings().get(0).getMessage());
        assertEquals(1, workbook.getParserWarnings().size());
    }

    @Test
    void marksBlankTargetAsMalformed() {
        Relationship relationship = relationship("   ", RelationshipStatus.UNRESOLVED);
        CmdbWorkbook workbook = workbook(List.of(item("ci-app", "App01", "Application")), List.of(relationship));

        RelationshipResolutionSummary summary = service.resolve(workbook);

        assertEquals(RelationshipStatus.MALFORMED, relationship.getStatus());
        assertNull(relationship.getTargetCiId());
        assertEquals(1, summary.malformedCount());
        assertEquals(1, summary.warnings().size());
        assertEquals("Relationship target is blank", summary.warnings().get(0).getMessage());
    }

    @Test
    void leavesDuplicateTargetNamesUnresolvedAndAddsAmbiguityWarning() {
        Relationship relationship = relationship("Shared DB", RelationshipStatus.UNRESOLVED);
        CmdbWorkbook workbook = workbook(
                List.of(
                        item("ci-db-1", "Shared DB", "Database"),
                        item("ci-db-2", "Shared DB", "Database")
                ),
                List.of(relationship)
        );

        RelationshipResolutionSummary summary = service.resolve(workbook);

        assertEquals(RelationshipStatus.UNRESOLVED, relationship.getStatus());
        assertNull(relationship.getTargetCiId());
        assertEquals(1, summary.unresolvedCount());
        assertEquals(1, summary.ambiguousCount());
        assertEquals(1, summary.warnings().size());
        assertEquals("Relationship target is ambiguous: 2 matching CIs found", summary.warnings().get(0).getMessage());
    }

    @Test
    void skipsMalformedRelationshipsWithTargetValue() {
        Relationship relationship = relationship("DB01", RelationshipStatus.MALFORMED);
        CmdbWorkbook workbook = workbook(List.of(item("ci-db", "DB01", "Database")), List.of(relationship));

        RelationshipResolutionSummary summary = service.resolve(workbook);

        assertEquals(RelationshipStatus.MALFORMED, relationship.getStatus());
        assertNull(relationship.getTargetCiId());
        assertEquals(1, summary.malformedCount());
        assertEquals(0, summary.resolvedCount());
        assertEquals(0, summary.warnings().size());
    }

    private CmdbWorkbook workbook(List<ConfigurationItem> items, List<Relationship> relationships) {
        CmdbSheet sheet = new CmdbSheet();
        sheet.setName("Applications");
        sheet.setType(SheetType.CI_BLOCK);
        items.forEach(sheet::addConfigurationItem);
        relationships.forEach(sheet::addRelationship);
        return new CmdbWorkbook("sample.xlsx", Instant.now(), List.of(sheet), List.of());
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

    private Relationship relationship(String targetName, RelationshipStatus status) {
        return new Relationship(
                "rel-" + Math.abs(targetName == null ? 0 : targetName.hashCode()),
                "source-ci",
                null,
                targetName,
                "depends on",
                "Depends On",
                "sample.xlsx",
                "Applications",
                3,
                status
        );
    }
}
