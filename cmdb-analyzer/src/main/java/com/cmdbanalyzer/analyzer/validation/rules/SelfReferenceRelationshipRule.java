package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.analyzer.validation.ValidationContext;

import java.util.List;

/**
 * Detects relationships where source and target point to the same CI.
 */
public class SelfReferenceRelationshipRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        var workbook = context.workbook();
        return workbook.getSheets().stream()
                .flatMap(sheet -> sheet.getRelationships().stream())
                .filter(relationship -> !isBlank(relationship.getSourceCiId()))
                .filter(relationship -> relationship.getSourceCiId().equals(relationship.getTargetCiId()))
                .map(relationship -> ValidationIssueFactory.relationshipIssue(
                        ValidationSeverity.ERROR,
                        ValidationIssueType.SELF_REFERENCE,
                        "Relationship points from a CI back to itself.",
                        relationship,
                        "Change either the source or target relationship reference."
                ))
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
