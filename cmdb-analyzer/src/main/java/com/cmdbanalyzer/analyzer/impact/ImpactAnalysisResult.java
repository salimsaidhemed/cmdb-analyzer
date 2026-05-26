package com.cmdbanalyzer.analyzer.impact;

import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.Relationship;

import java.util.List;

/**
 * Result of a bounded CI impact traversal.
 */
public record ImpactAnalysisResult(
        ConfigurationItem selectedCi,
        List<ConfigurationItem> affectedCis,
        List<Relationship> affectedRelationships,
        List<ImpactPath> paths,
        int maxDepth,
        boolean truncated,
        String message
) {

    public ImpactAnalysisResult {
        affectedCis = List.copyOf(affectedCis == null ? List.of() : affectedCis);
        affectedRelationships = List.copyOf(affectedRelationships == null ? List.of() : affectedRelationships);
        paths = List.copyOf(paths == null ? List.of() : paths);
    }

    public static ImpactAnalysisResult empty(String message, int maxDepth) {
        return new ImpactAnalysisResult(null, List.of(), List.of(), List.of(), maxDepth, false, message);
    }
}
