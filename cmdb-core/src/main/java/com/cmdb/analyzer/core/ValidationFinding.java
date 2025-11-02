package com.cmdb.analyzer.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a validation result generated during CMDB analysis.
 * <p>
 * Each {@code ValidationFinding} describes a detected issue, warning,
 * or informational note, optionally linked to a {@link CI} or
 * {@link Relationship}.
 * <p>
 * Thread-safe: internal maps are concurrent, and accessors are synchronized.
 */
public class ValidationFinding {

    /** Severity levels for findings. */
    public enum Severity {
        ERROR, WARNING, INFO
    }

    /** Standardized finding codes. */
    public enum Code {
        MISSING_PARENT,
        ORPHAN_CI,
        DANGLING_RELATIONSHIP,
        INVALID_RELATIONSHIP_TYPE,
        INVALID_CLASS,
        MISSING_SERVICE_OFFERING,
        DUPLICATE_CI,
        CIRCULAR_DEPENDENCY,
        LOCATION_INVALID,
        GENERIC_ERROR
    }

    /** Unique identifier of the finding. */
    private final String id = UUID.randomUUID().toString();

    /** Severity of the finding (Error, Warning, Info). */
    private volatile Severity severity = Severity.ERROR;

    /** Type code categorizing the issue. */
    private volatile Code code = Code.GENERIC_ERROR;

    /** Human-readable message explaining the issue. */
    private volatile String message;

    /** Affected CI (optional). */
    private volatile CI ci;

    /** Affected relationship (optional). */
    private volatile Relationship relation;

    /** Provenance: file and sheet information. */
    private volatile String sourceFile;
    private volatile String sheetName;
    private volatile Integer rowIndex;

    /** Extra contextual information. */
    private final Map<String, String> context = new ConcurrentHashMap<>();

    /** Suggested remediation text, if applicable. */
    private volatile String suggestion;

    // --- Accessors and Mutators ---

    public String getId() {
        return id;
    }

    public synchronized Severity getSeverity() {
        return severity;
    }

    public synchronized void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public synchronized Code getCode() {
        return code;
    }

    public synchronized void setCode(Code code) {
        this.code = code;
    }

    public synchronized String getMessage() {
        return message;
    }

    public synchronized void setMessage(String message) {
        this.message = message;
    }

    public synchronized CI getCi() {
        return ci;
    }

    public synchronized void setCi(CI ci) {
        this.ci = ci;
    }

    public synchronized Relationship getRelation() {
        return relation;
    }

    public synchronized void setRelation(Relationship relation) {
        this.relation = relation;
    }

    public synchronized String getSourceFile() {
        return sourceFile;
    }

    public synchronized void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public synchronized String getSheetName() {
        return sheetName;
    }

    public synchronized void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public synchronized Integer getRowIndex() {
        return rowIndex;
    }

    public synchronized void setRowIndex(Integer rowIndex) {
        this.rowIndex = rowIndex;
    }

    public Map<String, String> getContext() {
        return new HashMap<>(context);
    }

    public void putContext(String key, String value) {
        if (key != null && value != null)
            context.put(key, value);
    }

    public synchronized String getSuggestion() {
        return suggestion;
    }

    public synchronized void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ValidationFinding))
            return false;
        ValidationFinding that = (ValidationFinding) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public synchronized String toString() {
        return String.format("[%s] %s - %s", severity, code, message);
    }
}
