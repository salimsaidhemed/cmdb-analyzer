package com.cmdbanalyzer.analyzer.impact;

import com.cmdbanalyzer.graph.CmdbGraph;
import com.cmdbanalyzer.graph.CmdbGraphEdge;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.Relationship;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * Performs bounded impact traversal over the resolved CMDB graph.
 */
public class ImpactAnalysisService {

    public static final int DEFAULT_MAX_DEPTH = 3;
    public static final int DEFAULT_MAX_NODES = 200;
    public static final int DEFAULT_MAX_PATHS = 500;

    private final CmdbGraph graph;
    private final int maxNodes;
    private final int maxPaths;

    public ImpactAnalysisService(CmdbGraph graph) {
        this(graph, DEFAULT_MAX_NODES, DEFAULT_MAX_PATHS);
    }

    public ImpactAnalysisService(CmdbGraph graph, int maxNodes, int maxPaths) {
        this.graph = graph;
        this.maxNodes = Math.max(1, maxNodes);
        this.maxPaths = Math.max(1, maxPaths);
    }

    public ImpactAnalysisResult analyzeImpact(String ciId, ImpactDirection direction, int maxDepth) {
        int safeDepth = maxDepth <= 0 ? DEFAULT_MAX_DEPTH : maxDepth;
        ImpactDirection safeDirection = direction == null ? ImpactDirection.BOTH : direction;
        if (graph == null || ciId == null || ciId.isBlank()) {
            return ImpactAnalysisResult.empty("Select a CI to run impact analysis.", safeDepth);
        }

        Optional<ConfigurationItem> selected = graph.findConfigurationItem(ciId);
        if (selected.isEmpty() || !graph.graph().containsVertex(ciId)) {
            return ImpactAnalysisResult.empty("Selected CI is not available in the resolved graph.", safeDepth);
        }

        Map<String, ConfigurationItem> affectedCis = new LinkedHashMap<>();
        Map<String, Relationship> affectedRelationships = new LinkedHashMap<>();
        List<ImpactPath> paths = new ArrayList<>();
        boolean truncated = false;

        if (safeDirection == ImpactDirection.DEPENDENCIES || safeDirection == ImpactDirection.BOTH) {
            truncated = traverse(
                    selected.get(),
                    ImpactDirection.DEPENDENCIES,
                    safeDepth,
                    affectedCis,
                    affectedRelationships,
                    paths
            ) || truncated;
        }
        if (safeDirection == ImpactDirection.DEPENDENTS || safeDirection == ImpactDirection.BOTH) {
            truncated = traverse(
                    selected.get(),
                    ImpactDirection.DEPENDENTS,
                    safeDepth,
                    affectedCis,
                    affectedRelationships,
                    paths
            ) || truncated;
        }

        String message = truncated
                ? "Impact analysis was truncated by safety limits."
                : "Impact analysis complete.";
        return new ImpactAnalysisResult(
                selected.get(),
                List.copyOf(affectedCis.values()),
                List.copyOf(affectedRelationships.values()),
                List.copyOf(paths),
                safeDepth,
                truncated,
                message
        );
    }

    private boolean traverse(
            ConfigurationItem selected,
            ImpactDirection direction,
            int maxDepth,
            Map<String, ConfigurationItem> affectedCis,
            Map<String, Relationship> affectedRelationships,
            List<ImpactPath> paths
    ) {
        Queue<TraversalState> queue = new ArrayDeque<>();
        queue.add(new TraversalState(
                selected.getId(),
                0,
                List.of(selected.getId()),
                List.of()
        ));
        boolean truncated = false;

        while (!queue.isEmpty()) {
            TraversalState state = queue.remove();
            if (state.depth() >= maxDepth) {
                continue;
            }

            for (CmdbGraphEdge edge : nextEdges(state.ciId(), direction)) {
                String nextCiId = direction == ImpactDirection.DEPENDENCIES
                        ? edge.getTargetCiId()
                        : edge.getSourceCiId();
                if (state.pathCiIds().contains(nextCiId)) {
                    continue;
                }
                if (affectedCis.size() >= maxNodes && !affectedCis.containsKey(nextCiId)) {
                    truncated = true;
                    continue;
                }
                if (paths.size() >= maxPaths) {
                    return true;
                }

                Optional<ConfigurationItem> nextCi = graph.findConfigurationItem(nextCiId);
                Optional<Relationship> relationship = graph.findRelationship(edge.getRelationshipId());
                if (nextCi.isEmpty() || relationship.isEmpty()) {
                    continue;
                }

                List<String> nextPathCiIds = append(state.pathCiIds(), nextCiId);
                List<String> nextRelationshipIds = append(state.relationshipIds(), edge.getRelationshipId());
                affectedCis.putIfAbsent(nextCiId, nextCi.get());
                affectedRelationships.putIfAbsent(edge.getRelationshipId(), relationship.get());
                paths.add(toPath(direction, nextPathCiIds, nextRelationshipIds));
                queue.add(new TraversalState(
                        nextCiId,
                        state.depth() + 1,
                        nextPathCiIds,
                        nextRelationshipIds
                ));
            }
        }

        return truncated;
    }

    private Set<CmdbGraphEdge> nextEdges(String ciId, ImpactDirection direction) {
        if (!graph.graph().containsVertex(ciId)) {
            return Set.of();
        }
        if (direction == ImpactDirection.DEPENDENCIES) {
            return new LinkedHashSet<>(graph.graph().outgoingEdgesOf(ciId));
        }
        return new LinkedHashSet<>(graph.graph().incomingEdgesOf(ciId));
    }

    private ImpactPath toPath(ImpactDirection direction, List<String> ciIds, List<String> relationshipIds) {
        List<ConfigurationItem> items = ciIds.stream()
                .map(graph::findConfigurationItem)
                .flatMap(Optional::stream)
                .toList();
        List<Relationship> relationships = relationshipIds.stream()
                .map(graph::findRelationship)
                .flatMap(Optional::stream)
                .toList();
        return new ImpactPath(direction, Math.max(0, items.size() - 1), items, relationships);
    }

    private List<String> append(List<String> values, String value) {
        List<String> next = new ArrayList<>(values);
        next.add(value);
        return List.copyOf(next);
    }

    private record TraversalState(
            String ciId,
            int depth,
            List<String> pathCiIds,
            List<String> relationshipIds
    ) {
        private TraversalState {
            pathCiIds = List.copyOf(pathCiIds);
            relationshipIds = List.copyOf(relationshipIds);
        }
    }
}
