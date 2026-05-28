package com.cmdbanalyzer.ui.graph;

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

import static com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.NodeRole.DEPENDENCY;
import static com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.NodeRole.DEPENDENT;
import static com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.NodeRole.SELECTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphNeighborhoodServiceTest {

    private final CmdbGraphBuilder graphBuilder = new CmdbGraphBuilder();
    private final GraphNeighborhoodService service = new GraphNeighborhoodService();

    @Test
    void selectedCiCreatesNeighborhoodModel() {
        GraphBuildResult result = graphBuilder.build(workbook(
                List.of(item("app-1", "APP01"), item("db-1", "DB01"), item("svc-1", "SVC01")),
                List.of(
                        relationship("rel-1", "app-1", "db-1", RelationshipStatus.RESOLVED),
                        relationship("rel-2", "svc-1", "app-1", RelationshipStatus.RESOLVED)
                )
        ));

        GraphNeighborhoodViewModel model = service.build(result.graph(), "app-1");

        assertEquals(3, model.nodes().size());
        assertEquals(2, model.edges().size());
        assertEquals(SELECTED, role(model, "app-1"));
        assertEquals(DEPENDENCY, role(model, "db-1"));
        assertEquals(DEPENDENT, role(model, "svc-1"));
    }

    @Test
    void graphNodeKeepsFullLabelAndSourceTraceabilityForTooltips() {
        String longName = "Application Service With A Very Long Enterprise CI Name";
        GraphBuildResult result = graphBuilder.build(workbook(
                List.of(item("app-1", longName)),
                List.of()
        ));

        GraphNeighborhoodViewModel model = service.build(result.graph(), "app-1");
        GraphNeighborhoodViewModel.GraphNode node = model.nodes().get(0);

        assertEquals(longName, node.name());
        assertEquals("Application", node.ciClass());
        assertEquals("Applications", node.sourceSheet());
        assertEquals(2, node.sourceRow());
    }

    @Test
    void unresolvedRelationshipsAreNotIncluded() {
        GraphBuildResult result = graphBuilder.build(workbook(
                List.of(item("app-1", "APP01"), item("db-1", "DB01")),
                List.of(relationship("rel-1", "app-1", null, RelationshipStatus.UNRESOLVED))
        ));

        GraphNeighborhoodViewModel model = service.build(result.graph(), "app-1");

        assertEquals(1, model.nodes().size());
        assertEquals(0, model.edges().size());
    }

    @Test
    void maxNodeLimitIsRespected() {
        List<ConfigurationItem> items = new ArrayList<>();
        List<Relationship> relationships = new ArrayList<>();
        items.add(item("app-1", "APP01"));
        for (int index = 0; index < 70; index++) {
            String id = "db-" + index;
            items.add(item(id, "DB" + index));
            relationships.add(relationship("rel-" + index, "app-1", id, RelationshipStatus.RESOLVED));
        }
        GraphBuildResult result = graphBuilder.build(workbook(items, relationships));

        GraphNeighborhoodViewModel model = service.build(result.graph(), "app-1");

        assertEquals(GraphNeighborhoodService.MAX_NODES, model.nodes().size());
        assertTrue(model.edges().size() <= GraphNeighborhoodService.MAX_EDGES);
        assertTrue(model.limited());
    }

    @Test
    void emptySelectionReturnsEmptyGraphModel() {
        GraphNeighborhoodViewModel model = service.build(null, null);

        assertTrue(model.nodes().isEmpty());
        assertTrue(model.edges().isEmpty());
        assertEquals("Select a CI to view its relationships.", model.message());
    }

    private GraphNeighborhoodViewModel.NodeRole role(GraphNeighborhoodViewModel model, String ciId) {
        return model.nodes().stream()
                .filter(node -> ciId.equals(node.ciId()))
                .map(GraphNeighborhoodViewModel.GraphNode::role)
                .findFirst()
                .orElseThrow();
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
