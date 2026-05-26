package com.cmdbanalyzer.graph;

import com.cmdbanalyzer.model.ConfigurationItem;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Basic query helper for direct CMDB graph traversal.
 */
public class GraphQueryService {

    private final CmdbGraph cmdbGraph;

    public GraphQueryService(CmdbGraph cmdbGraph) {
        this.cmdbGraph = cmdbGraph;
    }

    public boolean hasCi(String ciId) {
        return ciId != null && cmdbGraph.graph().containsVertex(ciId);
    }

    public List<ConfigurationItem> getDirectDependencies(String ciId) {
        if (!hasCi(ciId)) {
            return List.of();
        }
        Set<ConfigurationItem> dependencies = new LinkedHashSet<>();
        cmdbGraph.graph().outgoingEdgesOf(ciId).stream()
                .map(CmdbGraphEdge::getTargetCiId)
                .map(cmdbGraph::findConfigurationItem)
                .flatMap(java.util.Optional::stream)
                .forEach(dependencies::add);
        return List.copyOf(dependencies);
    }

    public List<ConfigurationItem> getDirectDependents(String ciId) {
        if (!hasCi(ciId)) {
            return List.of();
        }
        Set<ConfigurationItem> dependents = new LinkedHashSet<>();
        cmdbGraph.graph().incomingEdgesOf(ciId).stream()
                .map(CmdbGraphEdge::getSourceCiId)
                .map(cmdbGraph::findConfigurationItem)
                .flatMap(java.util.Optional::stream)
                .forEach(dependents::add);
        return List.copyOf(dependents);
    }

    public List<ConfigurationItem> getNeighbors(String ciId) {
        Set<ConfigurationItem> neighbors = new LinkedHashSet<>();
        neighbors.addAll(getDirectDependencies(ciId));
        neighbors.addAll(getDirectDependents(ciId));
        return List.copyOf(neighbors);
    }
}
