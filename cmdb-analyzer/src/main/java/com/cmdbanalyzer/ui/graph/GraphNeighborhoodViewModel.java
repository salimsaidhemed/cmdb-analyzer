package com.cmdbanalyzer.ui.graph;

import java.util.List;

/**
 * UI-neutral CI-centered graph model for direct relationship visualization.
 */
public record GraphNeighborhoodViewModel(
        String selectedCiId,
        String selectedCiName,
        List<GraphNode> nodes,
        List<GraphEdge> edges,
        boolean limited,
        String message
) {

    public GraphNeighborhoodViewModel {
        nodes = List.copyOf(nodes == null ? List.of() : nodes);
        edges = List.copyOf(edges == null ? List.of() : edges);
    }

    public static GraphNeighborhoodViewModel empty() {
        return new GraphNeighborhoodViewModel(
                null,
                null,
                List.of(),
                List.of(),
                false,
                "Select a CI to view its relationships."
        );
    }

    public record GraphNode(
            String ciId,
            String name,
            String ciClass,
            String sourceSheet,
            int sourceRow,
            NodeRole role
    ) {
    }

    public record GraphEdge(
            String relationshipId,
            String sourceCiId,
            String targetCiId,
            String relationshipType
    ) {
    }

    public enum NodeRole {
        SELECTED,
        DEPENDENCY,
        DEPENDENT
    }
}
