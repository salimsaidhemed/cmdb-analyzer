package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationContext;
import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.graph.CmdbGraphEdge;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Detects disconnected multi-CI groups in the dependency graph.
 */
public class IsolatedSubgraphRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        return context.graphOptional()
                .map(graph -> {
                    Graph<String, CmdbGraphEdge> undirectedGraph = new AsUndirectedGraph<>(graph.graph());
                    List<Set<String>> connectedSets = new ConnectivityInspector<>(undirectedGraph).connectedSets();
                    if (connectedSets.size() <= 1) {
                        return List.<ValidationIssue>of();
                    }

                    Set<String> largestComponent = connectedSets.stream()
                            .max(Comparator.comparingInt(Set::size))
                            .orElse(Set.of());
                    return connectedSets.stream()
                            .filter(component -> component.size() > 1)
                            .filter(component -> component != largestComponent)
                            .map(component -> new ValidationIssue(
                                    null,
                                    ValidationSeverity.INFO,
                                    ValidationIssueType.ISOLATED_SUBGRAPH,
                                    "Disconnected CI group detected with " + component.size() + " CIs.",
                                    context.workbook().getSourceFile(),
                                    null,
                                    null,
                                    component.stream().findFirst().orElse(null),
                                    null,
                                    "Review whether this disconnected group belongs in the same CMDB export."
                            ))
                            .toList();
                })
                .orElseGet(List::of);
    }
}
