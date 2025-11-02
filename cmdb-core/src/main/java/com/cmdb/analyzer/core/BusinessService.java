package com.cmdb.analyzer.core;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a high-level business-facing service grouping one or more
 * {@link ServiceOffering ServiceOfferings} and supporting {@link CI
 * Configuration Items}.
 * <p>
 * A {@code BusinessService} describes what value a service delivers to the
 * business
 * and references the technical components (CIs) and offerings that implement
 * it.
 * <p>
 * Thread-safe: all mutable state is accessed through synchronized methods
 * or thread-safe collections ({@link CopyOnWriteArrayList}).
 *
 * <pre>
 * Example:
 *     BusinessService dms = new BusinessService("BS-01", "Document Management");
 *     ServiceOffering portal = new ServiceOffering("SO-01", "DMS Portal");
 *     dms.addServiceOffering(portal);
 *     dms.addDependency(new CI("CI-001", "Server", "WebSrv01"));
 * </pre>
 */
public class BusinessService {

    /** Unique identifier for this business service (e.g., "BS-001"). */
    private volatile String id;

    /** Business-friendly name of the service. */
    private volatile String name;

    /** Optional description of the service's purpose or scope. */
    private volatile String description;

    /** Optional owner or responsible team name. */
    private volatile String owner;

    /** Thread-safe list of related Service Offerings. */
    private final List<ServiceOffering> serviceOfferings = new CopyOnWriteArrayList<>();

    /** Thread-safe list of dependent Configuration Items (supporting CIs). */
    private final List<CI> dependsOn = new CopyOnWriteArrayList<>();

    /**
     * Default constructor for serialization frameworks.
     */
    public BusinessService() {
    }

    /**
     * Creates a new Business Service with a given ID and name.
     *
     * @param id   unique service identifier
     * @param name business service name
     */
    public BusinessService(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // --- Accessors and Mutators ---

    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(String id) {
        this.id = id;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized String getDescription() {
        return description;
    }

    public synchronized void setDescription(String description) {
        this.description = description;
    }

    public synchronized String getOwner() {
        return owner;
    }

    public synchronized void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Returns an immutable list of all service offerings belonging to this business
     * service.
     *
     * @return unmodifiable list of offerings
     */
    public List<ServiceOffering> getServiceOfferings() {
        return Collections.unmodifiableList(serviceOfferings);
    }

    /**
     * Returns an immutable list of all CIs that this business service depends on.
     *
     * @return unmodifiable list of dependent configuration items
     */
    public List<CI> getDependsOn() {
        return Collections.unmodifiableList(dependsOn);
    }

    // --- Thread-safe collection mutators ---

    public void addServiceOffering(ServiceOffering so) {
        if (so != null)
            serviceOfferings.add(so);
    }

    public void addDependency(CI ci) {
        if (ci != null)
            dependsOn.add(ci);
    }

    public void clearDependencies() {
        dependsOn.clear();
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BusinessService))
            return false;
        BusinessService that = (BusinessService) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public synchronized String toString() {
        return String.format("BusinessService{id='%s', name='%s', offerings=%d, deps=%d}",
                id, name, serviceOfferings.size(), dependsOn.size());
    }
}
