package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.analyzer.validation.ValidationContext;
import com.cmdbanalyzer.model.RelationshipStatus;

import java.util.List;

/**
 * Detects relationships that could not be resolved to a target CI.
 */
public class UnresolvedRelationshipRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        var workbook = context.workbook();
        return workbook.getSheets().stream()
                .flatMap(sheet -> sheet.getRelationships().stream())
                .filter(relationship -> relationship.getStatus() == RelationshipStatus.UNRESOLVED)
                .map(relationship -> ValidationIssueFactory.relationshipIssue(
                        ValidationSeverity.WARNING,
                        ValidationIssueType.UNRESOLVED_RELATIONSHIP,
                        "Relationship target could not be resolved: " + valueOrDash(relationship.getTargetName()),
                        relationship,
                        "Check that the target CI exists and that the relationship target name matches the CI name."
                ))
                .toList();
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }
}
