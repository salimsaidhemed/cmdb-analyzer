package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.RelationshipStatus;

import java.util.List;

/**
 * Detects malformed relationship rows.
 */
public class MalformedRelationshipRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(CmdbWorkbook workbook) {
        return workbook.getSheets().stream()
                .flatMap(sheet -> sheet.getRelationships().stream())
                .filter(relationship -> relationship.getStatus() == RelationshipStatus.MALFORMED)
                .map(relationship -> ValidationIssueFactory.relationshipIssue(
                        ValidationSeverity.ERROR,
                        ValidationIssueType.MALFORMED_RELATIONSHIP,
                        "Relationship row is malformed.",
                        relationship,
                        "Provide the missing relationship target and type values in the source workbook."
                ))
                .toList();
    }
}
