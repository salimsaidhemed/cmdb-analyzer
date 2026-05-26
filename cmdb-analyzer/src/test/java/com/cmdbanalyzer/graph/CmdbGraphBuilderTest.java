package com.cmdbanalyzer.graph;

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

class CmdbGraphBuilderTest {

    private final CmdbGraphBuilder builder = new CmdbGraphBuilder();

    @Test
    void configurationItemsBecomeVertices() {
        GraphBuildResult result = builder.build(workbook(
                List.of(item("app-1", "APP01"), item("db-1", "DB01")),
                List.of()
        ));

        assertEquals(2, result.vertexCount());
        assertTrue(result.graph().vertices().contains("app-1"));
        assertTrue(result.graph().vertices().contains("db-1"));
    }

    @Test
    void resolvedRelationshipsBecomeEdges() {
        GraphBuildResult result = builder.build(workbook(
                List.of(item("app-1", "APP01"), item("db-1", "DB01")),
                List.of(relationship("rel-1", "app-1", "db-1", RelationshipStatus.RESOLVED))
        ));

        assertEquals(1, result.edgeCount());
        CmdbGraphEdge edge = result.graph().edges().iterator().next();
        assertEquals("rel-1", edge.getRelationshipId());
        assertEquals("app-1", edge.getSourceCiId());
        assertEquals("db-1", edge.getTargetCiId());
    }

    @Test
    void unresolvedRelationshipsAreSkipped() {
        GraphBuildResult result = builder.build(workbook(
                List.of(item("app-1", "APP01"), item("db-1", "DB01")),
                List.of(relationship("rel-1", "app-1", null, RelationshipStatus.UNRESOLVED))
        ));

        assertEquals(0, result.edgeCount());
        assertEquals(1, result.skippedRelationshipCount());
        assertEquals(1, result.graph().unresolvedRelationships().size());
        assertEquals(1, result.warnings().size());
    }

    @Test
    void malformedRelationshipsAreSkipped() {
        GraphBuildResult result = builder.build(workbook(
                List.of(item("app-1", "APP01"), item("db-1", "DB01")),
                List.of(relationship("rel-1", "app-1", null, RelationshipStatus.MALFORMED))
        ));

        assertEquals(0, result.edgeCount());
        assertEquals(1, result.skippedRelationshipCount());
        assertEquals(1, result.graph().malformedRelationships().size());
    }

    @Test
    void directDependencyQueryReturnsOutgoingTargets() {
        GraphBuildResult result = builder.build(workbook(
                List.of(item("app-1", "APP01"), item("db-1", "DB01")),
                List.of(relationship("rel-1", "app-1", "db-1", RelationshipStatus.RESOLVED))
        ));
        GraphQueryService queryService = new GraphQueryService(result.graph());

        assertEquals(List.of("db-1"), queryService.getDirectDependencies("app-1").stream()
                .map(ConfigurationItem::getId)
                .toList());
    }

    @Test
    void directDependentQueryReturnsIncomingSources() {
        GraphBuildResult result = builder.build(workbook(
                List.of(item("app-1", "APP01"), item("db-1", "DB01")),
                List.of(relationship("rel-1", "app-1", "db-1", RelationshipStatus.RESOLVED))
        ));
        GraphQueryService queryService = new GraphQueryService(result.graph());

        assertEquals(List.of("app-1"), queryService.getDirectDependents("db-1").stream()
                .map(ConfigurationItem::getId)
                .toList());
    }

    @Test
    void emptyWorkbookDoesNotCrash() {
        GraphBuildResult result = builder.build(new CmdbWorkbook("empty.xlsx", Instant.now(), List.of(), List.of()));

        assertEquals(0, result.vertexCount());
        assertEquals(0, result.edgeCount());
        assertEquals(0, result.skippedRelationshipCount());
    }

    private CmdbWorkbook workbook(List<ConfigurationItem> items, List<Relationship> relationships) {
        CmdbSheet sheet = new CmdbSheet();
        sheet.setName("Applications");
        sheet.setType(SheetType.CI_BLOCK);
        items.forEach(sheet::addConfigurationItem);
        relationships.forEach(sheet::addRelationship);
        return new CmdbWorkbook("sample.xlsx", Instant.now(), List.of(sheet), List.of());
    }

    private ConfigurationItem item(String id, String name) {
        return new ConfigurationItem(
                id,
                name,
                "Application",
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
                "DB01",
                "depends on",
                "Depends On",
                "sample.xlsx",
                "Applications",
                3,
                status
        );
    }
}
