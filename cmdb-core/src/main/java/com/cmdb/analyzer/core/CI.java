package com.cmdb.analyzer.core;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a single Configuration Item (CI) within the CMDB.
 *
 * <p>
 * This class is designed to be thread-safe. Scalar fields rely on a
 * combination of {@code volatile} reads and writes paired with synchronized
 * mutators to guarantee visibility of changes to concurrent readers. The
 * attribute map is backed by a {@link ConcurrentHashMap}, allowing concurrent
 * callers to look up attributes without taking locks.
 *
 * <p>
 * Each instance models a unique CI (e.g., server, application, PLC) sourced
 * from the CMDB spreadsheets. Mutable data can be updated after construction,
 * while the identifier remains stable for use in equality checks and hashing.
 */

public class CI {
    private final String id;
    private volatile String ciClass;
    private volatile String name;
    private volatile String description;
    private volatile String location;
    private volatile String project;
    private volatile String environment;

    private final Map<String, String> attributes = new ConcurrentHashMap<>();

    public CI(String id, String ciClass, String name) {
        this.id = id;
        this.ciClass = ciClass;
        this.name = name;
    }

    // --- Accessors ---
    public String getId() {
        return id;
    }

    public String getCiClass() {
        return ciClass;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getProject() {
        return project;
    }

    public String getEnvironment() {
        return environment;
    }

    // --- Mutators (synchronized for thread-safety) ---
    public synchronized void setCiClass(String ciClass) {
        this.ciClass = ciClass;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized void setDescription(String description) {
        this.description = description;
    }

    public synchronized void setLocation(String location) {
        this.location = location;
    }

    public synchronized void setProject(String project) {
        this.project = project;
    }

    public synchronized void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * Returns a snapshot of the current attribute key/value pairs.
     *
     * <p>
     * A defensive copy is returned so callers can iterate without observing
     * concurrent mutations, while the internal map remains mutable and thread
     * safe.
     */
    public Map<String, String> getAttributes() {
        return new ConcurrentHashMap<>(attributes);
    }

    /**
     * Records or updates an attribute value.
     *
     * <p>
     * If either argument is {@code null} no change is applied. The underlying
     * map is concurrent, so callers can safely invoke this method alongside
     * other readers without additional synchronization.
     */
    public synchronized void putAttribute(String key, String value) {
        if (key != null && value != null)
            attributes.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CI))
            return false;
        CI ci = (CI) o;
        return Objects.equals(id, ci.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " [" + ciClass + "]";
    }
}
