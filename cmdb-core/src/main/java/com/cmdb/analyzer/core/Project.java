package com.cmdb.analyzer.core;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a logical grouping of Configuration Items (CIs), such as
 * a Project, Factory, or Site within the CMDB.
 * <p>
 * A {@code Project} typically groups CIs that share a common purpose,
 * geographical location, or operational context (e.g., a production site
 * or a customer project environment).
 * <p>
 * This class is thread-safe: all mutable fields are accessed via synchronized
 * methods or thread-safe collections ({@link CopyOnWriteArrayList}).
 * <p>
 * Example:
 * 
 * <pre>
 * Project factory = new Project("FactoryNet");
 * factory.setLocation("Spain - Plant 1");
 * factory.addCI(new CI("CI-001", "PLC", "LineController"));
 * </pre>
 */
public class Project {

    /** The name of the project or factory (unique within context). */
    private volatile String name;

    /** Optional short code identifier (e.g., "FN01"). */
    private volatile String code;

    /** Physical or logical location (e.g., "Spain / Alcalá"). */
    private volatile String location;

    /** Thread-safe list of all Configuration Items (CIs) under this project. */
    private final List<CI> cis = new CopyOnWriteArrayList<>();

    /**
     * Default constructor for frameworks and serializers.
     */
    public Project() {
    }

    /**
     * Constructs a new Project with the specified name.
     *
     * @param name the project name
     */
    public Project(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this project or factory.
     *
     * @return project name
     */
    public synchronized String getName() {
        return name;
    }

    /**
     * Sets the name of this project or factory.
     *
     * @param name project name
     */
    public synchronized void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the project code or shorthand identifier.
     *
     * @return project code
     */
    public synchronized String getCode() {
        return code;
    }

    /**
     * Sets the project code or shorthand identifier.
     *
     * @param code shorthand code (e.g., "FN01")
     */
    public synchronized void setCode(String code) {
        this.code = code;
    }

    /**
     * Returns the project location.
     *
     * @return project location (region/site)
     */
    public synchronized String getLocation() {
        return location;
    }

    /**
     * Sets the project location (region or site name).
     *
     * @param location project location
     */
    public synchronized void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns an immutable snapshot of the CIs assigned to this project.
     *
     * @return unmodifiable list of configuration items
     */
    public List<CI> getCis() {
        return Collections.unmodifiableList(cis);
    }

    /**
     * Adds a Configuration Item (CI) to this project.
     * <p>
     * Thread-safe: uses {@link CopyOnWriteArrayList#add(Object)} internally.
     *
     * @param ci configuration item to add (ignored if null)
     */
    public void addCI(CI ci) {
        if (ci != null)
            cis.add(ci);
    }

    /**
     * Removes a Configuration Item (CI) from this project, if present.
     *
     * @param ci the configuration item to remove
     */
    public void removeCI(CI ci) {
        if (ci != null)
            cis.remove(ci);
    }

    /**
     * Clears all Configuration Items from this project.
     */
    public synchronized void clearCIs() {
        cis.clear();
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Project))
            return false;
        Project that = (Project) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public synchronized String toString() {
        return String.format("Project{name='%s', code='%s', location='%s', ciCount=%d}",
                name, code, location, cis.size());
    }
}
