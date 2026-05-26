package com.cmdbanalyzer.graph;

import com.cmdbanalyzer.model.ParserWarning;

import java.util.List;

/**
 * Result of building the CMDB dependency graph.
 */
public record GraphBuildResult(
        CmdbGraph graph,
        int vertexCount,
        int edgeCount,
        int skippedRelationshipCount,
        List<ParserWarning> warnings
) {

    public GraphBuildResult {
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
    }
}
