package com.cmdb.analyzer.core;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe catalog of reference values used by CMDB validators.
 * <p>
 * A {@code ReferenceCatalog} maintains lookup sets of valid CMDB attributes
 * such as allowed CI classes, relationship types, locations, and environments.
 * These values are typically loaded from ServiceNow exports, cached CSV files,
 * or static configuration.
 * <p>
 * All internal sets are concurrent, allowing multiple threads (e.g., validator,
 * importer, UI renderer) to read and modify them safely.
 *
 * <pre>
 * Example:
 *     ReferenceCatalog catalog = new ReferenceCatalog();
 *     catalog.addValidClass("Server");
 *     catalog.addValidRelationshipType("Depends on");
 *     if (catalog.isValidClass("Server")) {
 *         // proceed with validation
 *     }
 * </pre>
 */
public class ReferenceCatalog {

    /** Valid CMDB CI classes. */
    private final Set<String> validClasses = ConcurrentHashMap.newKeySet();

    /** Valid CMDB relationship types. */
    private final Set<String> validRelationshipTypes = ConcurrentHashMap.newKeySet();

    /** Valid CMDB locations. */
    private final Set<String> validLocations = ConcurrentHashMap.newKeySet();

    /** Valid CMDB environments (e.g., Prod, Test, Dev). */
    private final Set<String> validEnvironments = ConcurrentHashMap.newKeySet();

    // ---------------------------------------------------------------------
    // Accessors (return unmodifiable views for external read-only usage)
    // ---------------------------------------------------------------------

    /**
     * Returns an unmodifiable snapshot of all valid CI classes.
     *
     * @return unmodifiable set of class names
     */
    public Set<String> getValidClasses() {
        return Collections.unmodifiableSet(validClasses);
    }

    /**
     * Returns an unmodifiable snapshot of all valid relationship types.
     *
     * @return unmodifiable set of relationship types
     */
    public Set<String> getValidRelationshipTypes() {
        return Collections.unmodifiableSet(validRelationshipTypes);
    }

    /**
     * Returns an unmodifiable snapshot of all valid locations.
     *
     * @return unmodifiable set of location names
     */
    public Set<String> getValidLocations() {
        return Collections.unmodifiableSet(validLocations);
    }

    /**
     * Returns an unmodifiable snapshot of all valid environments.
     *
     * @return unmodifiable set of environment names
     */
    public Set<String> getValidEnvironments() {
        return Collections.unmodifiableSet(validEnvironments);
    }

    // ---------------------------------------------------------------------
    // Mutators (thread-safe through concurrent sets)
    // ---------------------------------------------------------------------

    /** Adds a valid CI class name to the catalog. */
    public void addValidClass(String className) {
        if (className != null && !className.isBlank())
            validClasses.add(className);
    }

    /** Adds a valid relationship type to the catalog. */
    public void addValidRelationshipType(String relType) {
        if (relType != null && !relType.isBlank())
            validRelationshipTypes.add(relType);
    }

    /** Adds a valid location to the catalog. */
    public void addValidLocation(String location) {
        if (location != null && !location.isBlank())
            validLocations.add(location);
    }

    /** Adds a valid environment to the catalog. */
    public void addValidEnvironment(String env) {
        if (env != null && !env.isBlank())
            validEnvironments.add(env);
    }

    /** Clears all reference data from the catalog. */
    public synchronized void clearAll() {
        validClasses.clear();
        validRelationshipTypes.clear();
        validLocations.clear();
        validEnvironments.clear();
    }

    // ---------------------------------------------------------------------
    // Validation helpers
    // ---------------------------------------------------------------------

    /**
     * Checks if a class name is considered valid.
     *
     * @param className the class name to validate
     * @return {@code true} if valid; {@code false} otherwise
     */
    public boolean isValidClass(String className) {
        return className != null && validClasses.contains(className);
    }

    /**
     * Checks if a relationship type is valid.
     *
     * @param relType the relationship type to validate
     * @return {@code true} if valid; {@code false} otherwise
     */
    public boolean isValidRelationshipType(String relType) {
        return relType != null && validRelationshipTypes.contains(relType);
    }

    /**
     * Checks if a location is valid.
     *
     * @param location the location name to validate
     * @return {@code true} if valid; {@code false} otherwise
     */
    public boolean isValidLocation(String location) {
        return location != null && validLocations.contains(location);
    }

    /**
     * Checks if an environment code is valid.
     *
     * @param env the environment name to validate
     * @return {@code true} if valid; {@code false} otherwise
     */
    public boolean isValidEnvironment(String env) {
        return env != null && validEnvironments.contains(env);
    }

    @Override
    public synchronized String toString() {
        return String.format(
                "ReferenceCatalog{classes=%d, relTypes=%d, locations=%d, environments=%d}",
                validClasses.size(),
                validRelationshipTypes.size(),
                validLocations.size(),
                validEnvironments.size());
    }
}
