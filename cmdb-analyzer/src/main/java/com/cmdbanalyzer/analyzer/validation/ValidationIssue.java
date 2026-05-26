package com.cmdbanalyzer.analyzer.validation;

import java.util.Objects;
import java.util.UUID;

/**
 * Structured validation finding emitted from CMDB model analysis.
 */
public class ValidationIssue {

    private String id;
    private ValidationSeverity severity;
    private ValidationIssueType type;
    private String message;
    private String sourceWorkbook;
    private String sourceSheet;
    private Integer sourceRow;
    private String affectedCiId;
    private String affectedRelationshipId;
    private String recommendedAction;

    public ValidationIssue() {
        this(null, ValidationSeverity.INFO, null, null, null, null, null, null, null, null);
    }

    public ValidationIssue(
            String id,
            ValidationSeverity severity,
            ValidationIssueType type,
            String message,
            String sourceWorkbook,
            String sourceSheet,
            Integer sourceRow,
            String affectedCiId,
            String affectedRelationshipId,
            String recommendedAction
    ) {
        this.id = isBlank(id) ? UUID.randomUUID().toString() : id;
        this.severity = severity == null ? ValidationSeverity.INFO : severity;
        this.type = type;
        this.message = message;
        this.sourceWorkbook = sourceWorkbook;
        this.sourceSheet = sourceSheet;
        this.sourceRow = sourceRow;
        this.affectedCiId = affectedCiId;
        this.affectedRelationshipId = affectedRelationshipId;
        this.recommendedAction = recommendedAction;
    }

    public String getId() {
        return id;
    }

    public ValidationSeverity getSeverity() {
        return severity;
    }

    public ValidationIssueType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getSourceWorkbook() {
        return sourceWorkbook;
    }

    public String getSourceSheet() {
        return sourceSheet;
    }

    public Integer getSourceRow() {
        return sourceRow;
    }

    public String getAffectedCiId() {
        return affectedCiId;
    }

    public String getAffectedRelationshipId() {
        return affectedRelationshipId;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValidationIssue that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
