package com.cmdb.analyzer.core;

import java.util.Objects;

/**
 * Represents a Configuration Item (CI) in the CMDB.
 */

public class CI {
    private String id;
    private String ciClass;
    private String name;
    private String description;
    private String location;
    private String project;

    public CI(String id, String ciClass, String name, String description, String location, String project) {
        this.id = id;
        this.ciClass = ciClass;
        this.name = name;
    }

    public CI() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCiClass() {
        return ciClass;
    }

    public void setCiClass(String ciClass) {
        this.ciClass = ciClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
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