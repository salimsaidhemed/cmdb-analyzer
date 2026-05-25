package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.ParserWarning;
import com.cmdbanalyzer.model.Relationship;

final class ValidationIssueFactory {

    private ValidationIssueFactory() {
    }

    static ValidationIssue ciIssue(
            ValidationSeverity severity,
            ValidationIssueType type,
            String message,
            ConfigurationItem item,
            String recommendedAction
    ) {
        return new ValidationIssue(
                null,
                severity,
                type,
                message,
                item.getSourceWorkbook(),
                item.getSourceSheet(),
                item.getSourceRow(),
                item.getId(),
                null,
                recommendedAction
        );
    }

    static ValidationIssue relationshipIssue(
            ValidationSeverity severity,
            ValidationIssueType type,
            String message,
            Relationship relationship,
            String recommendedAction
    ) {
        return new ValidationIssue(
                null,
                severity,
                type,
                message,
                relationship.getSourceWorkbook(),
                relationship.getSourceSheet(),
                relationship.getSourceRow(),
                relationship.getSourceCiId(),
                relationship.getId(),
                recommendedAction
        );
    }

    static ValidationIssue parserWarningIssue(ParserWarning warning) {
        return new ValidationIssue(
                null,
                warning.getSeverity() == null ? ValidationSeverity.INFO : ValidationSeverity.valueOf(warning.getSeverity().name()),
                ValidationIssueType.PARSER_WARNING,
                warning.getMessage(),
                warning.getWorkbook(),
                warning.getSheet(),
                warning.getRow(),
                null,
                null,
                "Review the parser warning and correct the source workbook if needed."
        );
    }
}
