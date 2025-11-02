package com.cmdb.analyzer.core;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe container representing the in-memory normalized CMDB dataset
 * after import.
 * <p>
 * A {@code CMDBDataset} aggregates all Configuration Items (CIs),
 * Relationships,
 * Business Services, Service Offerings, Projects, and Validation Findings
 * discovered during CMDB file parsing or rule validation.
 * <p>
 * All internal collections are backed by {@link CopyOnWriteArrayList} to allow
 * safe
 * concurrent reads and writes across multiple threads (e.g., import,
 * validation, and UI).
 * <p>
 * Example:
 * 
 * <pre>
 * CMDBDataset ds = new CMDBDataset();
 * ds.addCI(new CI("CI-001", "Server", "AppSrv01"));
 * ds.addRelationship(new Relationship(ci1, ci2, "Depends on"));
 * </pre>
 */
public class CMDBDataset {

    /** All configuration items discovered from import. */
    private final List<CI> configurationItems = new CopyOnWriteArrayList<>();

    /** All relationships defined between CIs. */
    private final List<Relationship> relationships = new CopyOnWriteArrayList<>();

    /** All discovered or defined business services. */
    private final List<BusinessService> businessServices = new CopyOnWriteArrayList<>();

    /** All service offerings grouped by business service or CI. */
    private final List<ServiceOffering> serviceOfferings = new CopyOnWriteArrayList<>();

    /** All project or factory-level groupings. */
    private final List<Project> projects = new CopyOnWriteArrayList<>();

    /** All validation findings (errors, warnings, infos) produced by analysis. */
    private final List<ValidationFinding> findings = new CopyOnWriteArrayList<>();

    /**
     * Returns an unmodifiable view of all Configuration Items (CIs).
     *
     * @return immutable list of configuration items
     */
    public List<CI> getConfigurationItems() {
        return Collections.unmodifiableList(configurationItems);
    }

    /**
     * Returns an unmodifiable view of all relationships.
     *
     * @return immutable list of relationships
     */
    public List<Relationship> getRelationships() {
        return Collections.unmodifiableList(relationships);
    }

    /**
     * Returns an unmodifiable view of all business services.
     *
     * @return immutable list of business services
     */
    public List<BusinessService> getBusinessServices() {
        return Collections.unmodifiableList(businessServices);
    }

    /**
     * Returns an unmodifiable view of all service offerings.
     *
     * @return immutable list of service offerings
     */
    public List<ServiceOffering> getServiceOfferings() {
        return Collections.unmodifiableList(serviceOfferings);
    }

    /**
     * Returns an unmodifiable view of all projects or factories.
     *
     * @return immutable list of projects
     */
    public List<Project> getProjects() {
        return Collections.unmodifiableList(projects);
    }

    /**
     * Returns an unmodifiable view of all validation findings.
     *
     * @return immutable list of findings
     */
    public List<ValidationFinding> getFindings() {
        return Collections.unmodifiableList(findings);
    }

    // --------------------------------------------------------------------
    // Thread-safe mutators (null-safe, atomic at method level)
    // --------------------------------------------------------------------

    /**
     * Adds a Configuration Item (CI) to this dataset.
     *
     * @param ci the configuration item to add
     */
    public synchronized void addCI(CI ci) {
        if (ci != null)
            configurationItems.add(ci);
    }

    /**
     * Adds a relationship to this dataset.
     *
     * @param relationship the relationship to add
     */
    public synchronized void addRelationship(Relationship relationship) {
        if (relationship != null)
            relationships.add(relationship);
    }

    /**
     * Adds a business service to this dataset.
     *
     * @param service the business service to add
     */
    public synchronized void addBusinessService(BusinessService service) {
        if (service != null)
            businessServices.add(service);
    }

    /**
     * Adds a service offering to this dataset.
     *
     * @param offering the service offering to add
     */
    public synchronized void addServiceOffering(ServiceOffering offering) {
        if (offering != null)
            serviceOfferings.add(offering);
    }

    /**
     * Adds a project or factory grouping to this dataset.
     *
     * @param project the project to add
     */
    public synchronized void addProject(Project project) {
        if (project != null)
            projects.add(project);
    }

    /**
     * Adds a validation finding to this dataset.
     *
     * @param finding the finding to add
     */
    public synchronized void addFinding(ValidationFinding finding) {
        if (finding != null)
            findings.add(finding);
    }

    /**
     * Clears all data from this dataset.
     * <p>
     * Use with caution: this method removes all items atomically across lists.
     */
    public synchronized void clearAll() {
        configurationItems.clear();
        relationships.clear();
        businessServices.clear();
        serviceOfferings.clear();
        projects.clear();
        findings.clear();
    }

    @Override
    public synchronized String toString() {
        return String.format(
                "CMDBDataset{CIs=%d, Rels=%d, Services=%d, Offerings=%d, Projects=%d, Findings=%d}",
                configurationItems.size(),
                relationships.size(),
                businessServices.size(),
                serviceOfferings.size(),
                projects.size(),
                findings.size());
    }
}
