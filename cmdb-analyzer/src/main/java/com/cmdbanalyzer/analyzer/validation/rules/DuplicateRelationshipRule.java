package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationContext;
import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.graph.CmdbGraphEdge;
import com.cmdbanalyzer.model.Relationship;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Detects multiple relationships with the same source, target, and relationship type.
 */
public class DuplicateRelationshipRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        return context.graphOptional()
                .map(graph -> {
                    Map<String, List<CmdbGraphEdge>> edgesByIdentity = graph.edges().stream()
                            .collect(Collectors.groupingBy(this::relationshipIdentity));
                    return edgesByIdentity.values().stream()
                            .filter(edges -> edges.size() > 1)
                            .flatMap(edges -> edges.stream()
                                    .map(edge -> graph.findRelationship(edge.getRelationshipId())
                                            .map(relationship -> duplicateIssue(relationship))
                                            .orElseGet(() -> new ValidationIssue(
                                                    null,
                                                    ValidationSeverity.WARNING,
                                                    ValidationIssueType.DUPLICATE_RELATIONSHIP,
                                                    "Duplicate relationship detected between the same source and target.",
                                                    context.workbook().getSourceFile(),
                                                    null,
                                                    null,
                                                    edge.getSourceCiId(),
                                                    edge.getRelationshipId(),
                                                    "Remove duplicate relationship rows or clarify relationship types."
                                            ))))
                            .toList();
                })
                .orElseGet(List::of);
    }

    private ValidationIssue duplicateIssue(Relationship relationship) {
        return ValidationIssueFactory.relationshipIssue(
                ValidationSeverity.WARNING,
                ValidationIssueType.DUPLICATE_RELATIONSHIP,
                "Duplicate relationship detected between the same source, target, and relationship type.",
                relationship,
                "Remove duplicate relationship rows or clarify relationship types."
        );
    }

    private String relationshipIdentity(CmdbGraphEdge edge) {
        return normalize(edge.getSourceCiId())
                + "|"
                + normalize(edge.getTargetCiId())
                + "|"
                + normalize(edge.getRelationshipType());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
