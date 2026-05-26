package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.analyzer.validation.ValidationContext;

import java.util.List;

/**
 * Detects configuration items without a CI class.
 */
public class MissingCiClassRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        var workbook = context.workbook();
        return workbook.getSheets().stream()
                .flatMap(sheet -> sheet.getConfigurationItems().stream())
                .filter(item -> isBlank(item.getCiClass()))
                .map(item -> ValidationIssueFactory.ciIssue(
                        ValidationSeverity.ERROR,
                        ValidationIssueType.MISSING_CI_CLASS,
                        "Configuration item is missing a CI class.",
                        item,
                        "Populate the CI class in the source workbook."
                ))
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
