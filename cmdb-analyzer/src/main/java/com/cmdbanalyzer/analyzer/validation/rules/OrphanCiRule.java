package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationContext;
import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.graph.CmdbGraph;

import java.util.List;

/**
 * Detects CIs with no incoming or outgoing graph relationships.
 */
public class OrphanCiRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        return context.graphOptional()
                .map(graph -> validateGraph(context, graph))
                .orElseGet(List::of);
    }

    private List<ValidationIssue> validateGraph(ValidationContext context, CmdbGraph graph) {
        return graph.vertices().stream()
                .filter(ciId -> graph.graph().inDegreeOf(ciId) == 0 && graph.graph().outDegreeOf(ciId) == 0)
                .map(ciId -> graph.findConfigurationItem(ciId)
                        .map(item -> ValidationIssueFactory.ciIssue(
                                ValidationSeverity.WARNING,
                                ValidationIssueType.ORPHAN_CI,
                                "Configuration item has no incoming or outgoing relationships.",
                                item,
                                "Verify whether this CI should have dependency or dependent relationships."
                        ))
                        .orElseGet(() -> new ValidationIssue(
                                null,
                                ValidationSeverity.WARNING,
                                ValidationIssueType.ORPHAN_CI,
                                "Configuration item has no incoming or outgoing relationships: " + ciId,
                                context.workbook().getSourceFile(),
                                null,
                                null,
                                ciId,
                                null,
                                "Verify whether this CI should have dependency or dependent relationships."
                        )))
                .toList();
    }
}
