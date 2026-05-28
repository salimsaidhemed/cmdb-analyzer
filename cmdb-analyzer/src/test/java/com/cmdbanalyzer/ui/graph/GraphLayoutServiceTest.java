package com.cmdbanalyzer.ui.graph;

import com.cmdbanalyzer.ui.graph.GraphLayoutService.GraphLayout;
import com.cmdbanalyzer.ui.graph.GraphLayoutService.GraphNodeLayout;
import com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.GraphNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.NodeRole.DEPENDENCY;
import static com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.NodeRole.DEPENDENT;
import static com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.NodeRole.SELECTED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphLayoutServiceTest {

    private final GraphLayoutService service = new GraphLayoutService();

    @Test
    void dependenciesArePlacedOnLeftAndDependentsOnRight() {
        GraphLayout layout = service.layout(model(List.of(
                node("app", SELECTED),
                node("db", DEPENDENCY),
                node("report", DEPENDENT)
        )));

        GraphNodeLayout selected = layout.nodes().get("app");
        GraphNodeLayout dependency = layout.nodes().get("db");
        GraphNodeLayout dependent = layout.nodes().get("report");

        assertTrue(dependency.centerX() < selected.centerX());
        assertTrue(dependent.centerX() > selected.centerX());
    }

    @Test
    void commonSideLayoutDoesNotOverlapNodes() {
        List<GraphNode> nodes = new ArrayList<>();
        nodes.add(node("app", SELECTED));
        for (int index = 0; index < 12; index++) {
            nodes.add(node("db-" + index, DEPENDENCY));
            nodes.add(node("svc-" + index, DEPENDENT));
        }

        GraphLayout layout = service.layout(model(nodes));
        List<GraphNodeLayout> positions = new ArrayList<>(layout.nodes().values());

        for (int left = 0; left < positions.size(); left++) {
            for (int right = left + 1; right < positions.size(); right++) {
                assertFalse(positions.get(left).overlaps(positions.get(right)));
            }
        }
    }

    @Test
    void canvasHeightExpandsForLargeNeighborhoods() {
        List<GraphNode> nodes = new ArrayList<>();
        nodes.add(node("app", SELECTED));
        for (int index = 0; index < 20; index++) {
            nodes.add(node("db-" + index, DEPENDENCY));
        }

        GraphLayout layout = service.layout(model(nodes));

        assertTrue(layout.height() > GraphLayoutService.MIN_CANVAS_HEIGHT);
    }

    private GraphNeighborhoodViewModel model(List<GraphNode> nodes) {
        return new GraphNeighborhoodViewModel("app", "APP01", nodes, List.of(), false, null);
    }

    private GraphNode node(String id, GraphNeighborhoodViewModel.NodeRole role) {
        return new GraphNode(id, id.toUpperCase(), "Application", "Applications", 12, role);
    }
}
