package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationContext;
import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.graph.CmdbGraphEdge;
import com.cmdbanalyzer.model.RelationshipStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Detects resolved relationships that are absent from the built graph.
 */
public class MissingGraphEdgeRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        return context.graphOptional()
                .map(graph -> {
                    Set<String> graphRelationshipIds = graph.edges().stream()
                            .map(CmdbGraphEdge::getRelationshipId)
                            .collect(Collectors.toSet());
                    return context.workbook().getSheets().stream()
                            .flatMap(sheet -> sheet.getRelationships().stream())
                            .filter(relationship -> relationship.getStatus() == RelationshipStatus.RESOLVED)
                            .filter(relationship -> !graphRelationshipIds.contains(relationship.getId()))
                            .map(relationship -> ValidationIssueFactory.relationshipIssue(
                                    ValidationSeverity.WARNING,
                                    ValidationIssueType.MISSING_GRAPH_EDGE,
                                    "Resolved relationship was not added to the dependency graph.",
                                    relationship,
                                    "Confirm that both source and target CIs exist in the parsed workbook."
                            ))
                            .toList();
                })
                .orElseGet(List::of);
    }
}
