package com.cmdbanalyzer.parser;

import com.cmdbanalyzer.model.ParserWarning;

import java.util.List;

/**
 * Summary of relationship resolution after matching relationship targets to known CIs.
 *
 * @param resolvedCount relationships resolved to a target CI
 * @param unresolvedCount relationships with no matching target CI
 * @param malformedCount relationship rows that cannot be interpreted safely
 * @param warnings diagnostics emitted during resolution
 */
public record RelationshipResolutionResult(
        int resolvedCount,
        int unresolvedCount,
        int malformedCount,
        List<ParserWarning> warnings
) {
    public RelationshipResolutionResult {
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
    }
}
