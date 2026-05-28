package com.cmdbanalyzer.ui.graph;

import com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.GraphNode;
import com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.NodeRole;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates readable positions for a CI-centered relationship graph.
 */
public class GraphLayoutService {

    public static final double NODE_WIDTH = 190;
    public static final double NODE_HEIGHT = 74;
    public static final double MIN_CANVAS_WIDTH = 1120;
    public static final double MIN_CANVAS_HEIGHT = 620;
    private static final double SIDE_MARGIN = 150;
    private static final double TOP_MARGIN = 96;
    private static final double ROW_SPACING = 112;

    public GraphLayout layout(GraphNeighborhoodViewModel viewModel) {
        if (viewModel == null || viewModel.nodes().isEmpty()) {
            return new GraphLayout(MIN_CANVAS_WIDTH, MIN_CANVAS_HEIGHT, Map.of());
        }

        List<GraphNode> dependencies = viewModel.nodes().stream()
                .filter(node -> node.role() == NodeRole.DEPENDENCY)
                .toList();
        List<GraphNode> dependents = viewModel.nodes().stream()
                .filter(node -> node.role() == NodeRole.DEPENDENT)
                .toList();
        int sideCount = Math.max(dependencies.size(), dependents.size());
        double canvasHeight = Math.max(MIN_CANVAS_HEIGHT, TOP_MARGIN * 2 + Math.max(0, sideCount - 1) * ROW_SPACING + NODE_HEIGHT);
        double canvasWidth = MIN_CANVAS_WIDTH;
        double centerY = canvasHeight / 2;
        double centerX = canvasWidth / 2;

        Map<String, GraphNodeLayout> positions = new LinkedHashMap<>();
        viewModel.nodes().stream()
                .filter(node -> node.role() == NodeRole.SELECTED)
                .findFirst()
                .ifPresent(node -> positions.put(node.ciId(), centered(node, centerX, centerY)));

        placeSide(positions, dependencies, SIDE_MARGIN, canvasHeight);
        placeSide(positions, dependents, canvasWidth - SIDE_MARGIN, canvasHeight);
        return new GraphLayout(canvasWidth, canvasHeight, positions);
    }

    private void placeSide(Map<String, GraphNodeLayout> positions, List<GraphNode> nodes, double centerX, double canvasHeight) {
        if (nodes.isEmpty()) {
            return;
        }
        double totalHeight = (nodes.size() - 1) * ROW_SPACING;
        double startY = canvasHeight / 2 - totalHeight / 2;
        for (int index = 0; index < nodes.size(); index++) {
            positions.put(nodes.get(index).ciId(), centered(nodes.get(index), centerX, startY + index * ROW_SPACING));
        }
    }

    private GraphNodeLayout centered(GraphNode node, double centerX, double centerY) {
        return new GraphNodeLayout(
                node.ciId(),
                node.role(),
                centerX - NODE_WIDTH / 2,
                centerY - NODE_HEIGHT / 2,
                NODE_WIDTH,
                NODE_HEIGHT
        );
    }

    public record GraphLayout(double width, double height, Map<String, GraphNodeLayout> nodes) {
        public GraphLayout {
            nodes = Collections.unmodifiableMap(new LinkedHashMap<>(nodes == null ? Map.of() : nodes));
        }
    }

    public record GraphNodeLayout(
            String ciId,
            NodeRole role,
            double x,
            double y,
            double width,
            double height
    ) {
        public double centerX() {
            return x + width / 2;
        }

        public double centerY() {
            return y + height / 2;
        }

        public boolean overlaps(GraphNodeLayout other) {
            return x < other.x + other.width
                    && x + width > other.x
                    && y < other.y + other.height
                    && y + height > other.y;
        }
    }
}
