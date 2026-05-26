package com.cmdbanalyzer.graph;

import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.Relationship;
import org.jgrapht.Graph;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Convenience wrapper around the JGraphT CMDB dependency graph.
 */
public class CmdbGraph {

    private final Graph<String, CmdbGraphEdge> graph;
    private final Map<String, ConfigurationItem> configurationItemsById;
    private final Map<String, Relationship> relationshipsById;
    private final List<Relationship> unresolvedRelationships;
    private final List<Relationship> malformedRelationships;

    public CmdbGraph(
            Graph<String, CmdbGraphEdge> graph,
            Map<String, ConfigurationItem> configurationItemsById,
            Map<String, Relationship> relationshipsById,
            List<Relationship> unresolvedRelationships,
            List<Relationship> malformedRelationships
    ) {
        this.graph = graph;
        this.configurationItemsById = Map.copyOf(configurationItemsById);
        this.relationshipsById = Map.copyOf(relationshipsById);
        this.unresolvedRelationships = List.copyOf(unresolvedRelationships);
        this.malformedRelationships = List.copyOf(malformedRelationships);
    }

    public Graph<String, CmdbGraphEdge> graph() {
        return graph;
    }

    public Set<String> vertices() {
        return graph.vertexSet();
    }

    public Set<CmdbGraphEdge> edges() {
        return graph.edgeSet();
    }

    public Optional<ConfigurationItem> findConfigurationItem(String ciId) {
        return Optional.ofNullable(configurationItemsById.get(ciId));
    }

    public Optional<Relationship> findRelationship(String relationshipId) {
        return Optional.ofNullable(relationshipsById.get(relationshipId));
    }

    public Map<String, ConfigurationItem> configurationItemsById() {
        return configurationItemsById;
    }

    public Map<String, Relationship> relationshipsById() {
        return relationshipsById;
    }

    public List<Relationship> unresolvedRelationships() {
        return unresolvedRelationships;
    }

    public List<Relationship> malformedRelationships() {
        return malformedRelationships;
    }
}
