package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationContext;
import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import org.jgrapht.alg.cycle.CycleDetector;

import java.util.List;

/**
 * Detects CIs participating in directed dependency cycles.
 */
public class CircularDependencyRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        return context.graphOptional()
                .map(graph -> {
                    CycleDetector<String, ?> cycleDetector = new CycleDetector<>(graph.graph());
                    return cycleDetector.findCycles().stream()
                            .map(ciId -> graph.findConfigurationItem(ciId)
                                    .map(item -> ValidationIssueFactory.ciIssue(
                                            ValidationSeverity.ERROR,
                                            ValidationIssueType.CIRCULAR_DEPENDENCY,
                                            "Configuration item participates in a dependency cycle.",
                                            item,
                                            "Review the dependency chain and remove unintended circular references."
                                    ))
                                    .orElseGet(() -> new ValidationIssue(
                                            null,
                                            ValidationSeverity.ERROR,
                                            ValidationIssueType.CIRCULAR_DEPENDENCY,
                                            "Configuration item participates in a dependency cycle: " + ciId,
                                            context.workbook().getSourceFile(),
                                            null,
                                            null,
                                            ciId,
                                            null,
                                            "Review the dependency chain and remove unintended circular references."
                                    )))
                            .toList();
                })
                .orElseGet(List::of);
    }
}
