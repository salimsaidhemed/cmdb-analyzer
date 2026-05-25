package com.cmdbanalyzer.service;

import com.cmdbanalyzer.model.ParserWarning;

import java.util.List;

/**
 * Counts and diagnostics produced by relationship target resolution.
 */
public record RelationshipResolutionSummary(
        int totalRelationships,
        int resolvedCount,
        int unresolvedCount,
        int malformedCount,
        int ambiguousCount,
        List<ParserWarning> warnings
) {

    public RelationshipResolutionSummary {
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
    }
}
