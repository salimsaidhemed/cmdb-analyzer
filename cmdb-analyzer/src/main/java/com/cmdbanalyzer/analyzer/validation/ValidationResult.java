package com.cmdbanalyzer.analyzer.validation;

import java.util.List;

/**
 * Aggregated validation output for a parsed CMDB workbook.
 */
public record ValidationResult(List<ValidationIssue> issues) {

    public ValidationResult {
        issues = List.copyOf(issues == null ? List.of() : issues);
    }

    public int totalIssueCount() {
        return issues.size();
    }

    public int errorCount() {
        return countBySeverity(ValidationSeverity.ERROR);
    }

    public int warningCount() {
        return countBySeverity(ValidationSeverity.WARNING);
    }

    public int infoCount() {
        return countBySeverity(ValidationSeverity.INFO);
    }

    private int countBySeverity(ValidationSeverity severity) {
        return (int) issues.stream()
                .filter(issue -> issue.getSeverity() == severity)
                .count();
    }
}
