package com.cmdb.analyzer.core;

import java.util.Objects;

/**
 * Represents a directed relationship between two Configuration Items (CIs) in
 * the CMDB.
 * <p>
 * Each {@code Relationship} defines a linkage between a source CI and a target
 * CI, with
 * an associated relationship type (e.g., {@code "Depends on"},
 * {@code "Runs on"}, {@code "Uses"}).
 * Relationships are thread-safe; all accessors and mutators are synchronized to
 * protect
 * against concurrent modification when CIs are loaded or updated in parallel.
 * <p>
 * Example:
 * 
 * <pre>
 * CI server = new CI("CI-001", "Server", "AppServer");
 * CI db = new CI("CI-002", "Database", "DB01");
 * Relationship rel = new Relationship(server, db, "Depends on");
 * </pre>
 */
public class Relationship {

    /** The CI initiating this relationship (source). */
    private CI source;

    /** The CI being referenced by this relationship (target). */
    private CI target;

    /** The type of relationship (e.g., "Depends on", "Runs on", "Uses"). */
    private String type;

    /**
     * Optional Excel sheet or source identifier from which this relationship was
     * parsed.
     */
    private String sourceSheet;

    /**
     * Creates an empty relationship (used during parsing).
     * All fields must be initialized later through setters.
     */
    public Relationship() {
    }

    /**
     * Creates a new relationship between two CIs.
     *
     * @param source the source CI (non-null preferred)
     * @param target the target CI (non-null preferred)
     * @param type   the relationship type (e.g., "Depends on", "Uses")
     */
    public Relationship(CI source, CI target, String type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    /**
     * Returns the source CI of this relationship.
     *
     * @return the source CI, or {@code null} if not set
     */
    public synchronized CI getSource() {
        return source;
    }

    /**
     * Sets the source CI for this relationship.
     *
     * @param source the source CI
     */
    public synchronized void setSource(CI source) {
        this.source = source;
    }

    /**
     * Returns the target CI of this relationship.
     *
     * @return the target CI, or {@code null} if not set
     */
    public synchronized CI getTarget() {
        return target;
    }

    /**
     * Sets the target CI for this relationship.
     *
     * @param target the target CI
     */
    public synchronized void setTarget(CI target) {
        this.target = target;
    }

    /**
     * Returns the relationship type.
     *
     * @return the type (e.g., "Runs on", "Depends on")
     */
    public synchronized String getType() {
        return type;
    }

    /**
     * Sets the relationship type.
     *
     * @param type the new relationship type
     */
    public synchronized void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the sheet or source name from which this relationship was parsed.
     *
     * @return the source sheet name, or {@code null} if not recorded
     */
    public synchronized String getSourceSheet() {
        return sourceSheet;
    }

    /**
     * Sets the sheet or source name associated with this relationship.
     *
     * @param sourceSheet the Excel sheet or file name
     */
    public synchronized void setSourceSheet(String sourceSheet) {
        this.sourceSheet = sourceSheet;
    }

    /**
     * Compares this relationship to another object for equality.
     * Two relationships are considered equal if they link the same source, target,
     * and type.
     *
     * @param o another object
     * @return {@code true} if both relationships are equal
     */
    @Override
    public synchronized boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Relationship))
            return false;
        Relationship that = (Relationship) o;
        return Objects.equals(source, that.source)
                && Objects.equals(target, that.target)
                && Objects.equals(type, that.type);
    }

    /**
     * Computes a hash code based on source, target, and type.
     *
     * @return hash code for this relationship
     */
    @Override
    public synchronized int hashCode() {
        return Objects.hash(source, target, type);
    }

    /**
     * Returns a human-readable representation of the relationship,
     * e.g., {@code "AppServer -Depends on-> DB01"}.
     *
     * @return string representation of the relationship
     */
    @Override
    public synchronized String toString() {
        String s = (source != null ? source.getName() : "<?>");
        String t = (target != null ? target.getName() : "<?>");
        return s + " -" + type + "-> " + t;
    }
}
