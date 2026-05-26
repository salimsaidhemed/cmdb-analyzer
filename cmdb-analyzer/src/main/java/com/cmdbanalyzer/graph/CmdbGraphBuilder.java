package com.cmdbanalyzer.graph;

import com.cmdbanalyzer.model.CmdbSheet;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.ParserWarning;
import com.cmdbanalyzer.model.Relationship;
import com.cmdbanalyzer.model.RelationshipStatus;
import com.cmdbanalyzer.model.WarningSeverity;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds a directed dependency graph from parsed and resolved CMDB data.
 */
public class CmdbGraphBuilder {

    public GraphBuildResult build(CmdbWorkbook workbook) {
        Objects.requireNonNull(workbook, "workbook must not be null");

        Graph<String, CmdbGraphEdge> dependencyGraph = new DirectedPseudograph<>(null, null, false);
        Map<String, ConfigurationItem> itemsById = new LinkedHashMap<>();
        Map<String, Relationship> relationshipsById = new LinkedHashMap<>();
        List<Relationship> unresolvedRelationships = new ArrayList<>();
        List<Relationship> malformedRelationships = new ArrayList<>();
        List<ParserWarning> warnings = new ArrayList<>();
        int skippedRelationshipCount = 0;

        for (CmdbSheet sheet : workbook.getSheets()) {
            for (ConfigurationItem item : sheet.getConfigurationItems()) {
                if (!isBlank(item.getId())) {
                    itemsById.put(item.getId(), item);
                    dependencyGraph.addVertex(item.getId());
                }
            }
        }

        for (CmdbSheet sheet : workbook.getSheets()) {
            for (Relationship relationship : sheet.getRelationships()) {
                relationshipsById.put(relationship.getId(), relationship);
                if (relationship.getStatus() == RelationshipStatus.UNRESOLVED) {
                    unresolvedRelationships.add(relationship);
                    skippedRelationshipCount++;
                    warnings.add(warning(workbook, relationship, "Skipped unresolved relationship while building graph."));
                    continue;
                }
                if (relationship.getStatus() == RelationshipStatus.MALFORMED) {
                    malformedRelationships.add(relationship);
                    skippedRelationshipCount++;
                    warnings.add(warning(workbook, relationship, "Skipped malformed relationship while building graph."));
                    continue;
                }
                if (relationship.getStatus() != RelationshipStatus.RESOLVED) {
                    skippedRelationshipCount++;
                    warnings.add(warning(workbook, relationship, "Skipped relationship with unsupported status while building graph."));
                    continue;
                }
                if (!canCreateEdge(dependencyGraph, relationship)) {
                    skippedRelationshipCount++;
                    warnings.add(warning(workbook, relationship, "Skipped relationship because source or target CI is missing from graph."));
                    continue;
                }

                CmdbGraphEdge edge = new CmdbGraphEdge(
                        relationship.getId(),
                        relationship.getRelationshipType(),
                        relationship.getRawRelationshipType(),
                        relationship.getSourceCiId(),
                        relationship.getTargetCiId()
                );
                if (!dependencyGraph.addEdge(relationship.getSourceCiId(), relationship.getTargetCiId(), edge)) {
                    skippedRelationshipCount++;
                    warnings.add(warning(workbook, relationship, "Skipped duplicate graph edge for relationship."));
                }
            }
        }

        CmdbGraph cmdbGraph = new CmdbGraph(
                dependencyGraph,
                itemsById,
                relationshipsById,
                unresolvedRelationships,
                malformedRelationships
        );
        return new GraphBuildResult(
                cmdbGraph,
                dependencyGraph.vertexSet().size(),
                dependencyGraph.edgeSet().size(),
                skippedRelationshipCount,
                warnings
        );
    }

    private boolean canCreateEdge(Graph<String, CmdbGraphEdge> graph, Relationship relationship) {
        return !isBlank(relationship.getSourceCiId())
                && !isBlank(relationship.getTargetCiId())
                && graph.containsVertex(relationship.getSourceCiId())
                && graph.containsVertex(relationship.getTargetCiId());
    }

    private ParserWarning warning(CmdbWorkbook workbook, Relationship relationship, String message) {
        return new ParserWarning(
                WarningSeverity.WARNING,
                message,
                workbook.getSourceFile(),
                relationship.getSourceSheet(),
                relationship.getSourceRow(),
                "relationshipId",
                relationship.getId()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
