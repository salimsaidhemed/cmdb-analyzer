package com.cmdbanalyzer.ui.graph;

import com.cmdbanalyzer.graph.CmdbGraph;
import com.cmdbanalyzer.graph.CmdbGraphEdge;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.GraphEdge;
import com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.GraphNode;
import com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.NodeRole;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Builds small, CI-centered graph models from the resolved CMDB graph.
 */
public class GraphNeighborhoodService {

    public static final int MAX_NODES = 50;
    public static final int MAX_EDGES = 100;

    public GraphNeighborhoodViewModel build(CmdbGraph graph, String selectedCiId) {
        if (graph == null || selectedCiId == null || selectedCiId.isBlank()) {
            return GraphNeighborhoodViewModel.empty();
        }

        Optional<ConfigurationItem> selected = graph.findConfigurationItem(selectedCiId);
        if (selected.isEmpty() || !graph.graph().containsVertex(selectedCiId)) {
            return GraphNeighborhoodViewModel.empty();
        }

        Map<String, GraphNode> nodes = new LinkedHashMap<>();
        List<GraphEdge> edges = new ArrayList<>();
        nodes.put(selectedCiId, node(selected.get(), NodeRole.SELECTED));
        boolean limited = false;

        for (CmdbGraphEdge edge : graph.graph().outgoingEdgesOf(selectedCiId)) {
            if (edges.size() >= MAX_EDGES) {
                limited = true;
                break;
            }
            String targetId = edge.getTargetCiId();
            if (!nodes.containsKey(targetId) && nodes.size() >= MAX_NODES) {
                limited = true;
                continue;
            }
            graph.findConfigurationItem(targetId)
                    .map(item -> node(item, NodeRole.DEPENDENCY))
                    .ifPresent(graphNode -> nodes.putIfAbsent(targetId, graphNode));
            if (nodes.containsKey(targetId)) {
                edges.add(edge(edge));
            }
        }

        for (CmdbGraphEdge edge : graph.graph().incomingEdgesOf(selectedCiId)) {
            if (edges.size() >= MAX_EDGES) {
                limited = true;
                break;
            }
            String sourceId = edge.getSourceCiId();
            if (!nodes.containsKey(sourceId) && nodes.size() >= MAX_NODES) {
                limited = true;
                continue;
            }
            graph.findConfigurationItem(sourceId)
                    .map(item -> node(item, NodeRole.DEPENDENT))
                    .ifPresent(graphNode -> nodes.putIfAbsent(sourceId, graphNode));
            if (nodes.containsKey(sourceId)) {
                edges.add(edge(edge));
            }
        }

        return new GraphNeighborhoodViewModel(
                selectedCiId,
                selected.get().getName(),
                List.copyOf(nodes.values()),
                List.copyOf(edges),
                limited,
                limited ? "Graph limited to direct relationships for readability." : null
        );
    }

    private GraphNode node(ConfigurationItem item, NodeRole role) {
        return new GraphNode(
                item.getId(),
                item.getName(),
                item.getCiClass(),
                item.getSourceSheet(),
                item.getSourceRow(),
                role
        );
    }

    private GraphEdge edge(CmdbGraphEdge edge) {
        return new GraphEdge(
                edge.getRelationshipId(),
                edge.getSourceCiId(),
                edge.getTargetCiId(),
                edge.getRelationshipType()
        );
    }
}
