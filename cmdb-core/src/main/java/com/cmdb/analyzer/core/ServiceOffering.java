package com.cmdb.analyzer.core;

import java.util.Objects;

/**
 * Represents a specific service offering provided under a
 * {@link BusinessService}.
 * <p>
 * A {@code ServiceOffering} usually describes a technical or functional unit
 * exposed to consumers, often backed by a {@link CI Configuration Item}.
 * <p>
 * Thread-safe: all fields are accessed via synchronized methods.
 */
public class ServiceOffering {

    /** Unique offering identifier (e.g., "SO-001"). */
    private volatile String id;

    /** Offering name (e.g., "DMS Web Portal"). */
    private volatile String name;

    /** Human-readable description. */
    private volatile String description;

    /** Optional SLA (Service Level Agreement). */
    private volatile String sla;

    /** Optional status (Active, Planned, Retired). */
    private volatile String status;

    /** Reference to the parent business service. */
    private volatile BusinessService businessService;

    /** CI implementing this offering (e.g., an application or server). */
    private volatile CI linkedCI;

    public ServiceOffering() {
    }

    public ServiceOffering(String id, String name) {
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

    public synchronized String getSla() {
        return sla;
    }

    public synchronized void setSla(String sla) {
        this.sla = sla;
    }

    public synchronized String getStatus() {
        return status;
    }

    public synchronized void setStatus(String status) {
        this.status = status;
    }

    public synchronized BusinessService getBusinessService() {
        return businessService;
    }

    public synchronized void setBusinessService(BusinessService businessService) {
        this.businessService = businessService;
    }

    public synchronized CI getLinkedCI() {
        return linkedCI;
    }

    public synchronized void setLinkedCI(CI linkedCI) {
        this.linkedCI = linkedCI;
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ServiceOffering))
            return false;
        ServiceOffering that = (ServiceOffering) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public synchronized String toString() {
        return String.format("ServiceOffering{id='%s', name='%s', status='%s'}", id, name, status);
    }
}
