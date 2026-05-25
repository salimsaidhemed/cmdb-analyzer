package com.cmdbanalyzer.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Framework-independent representation of a Configuration Item parsed from a CMDB workbook.
 */
public class ConfigurationItem {

    private String id;
    private String name;
    private String ciClass;
    private String description;
    private Map<String, String> attributes;
    private String sourceWorkbook;
    private String sourceSheet;
    private int sourceRow;
    private String identityKey;

    public ConfigurationItem() {
        this(null, null, null, null, new HashMap<>(), null, null, 0, null);
    }

    public ConfigurationItem(
            String id,
            String name,
            String ciClass,
            String description,
            Map<String, String> attributes,
            String sourceWorkbook,
            String sourceSheet,
            int sourceRow,
            String identityKey
    ) {
        this.id = defaultId(id);
        this.name = name;
        this.ciClass = ciClass;
        this.description = description;
        setAttributes(attributes);
        this.sourceWorkbook = sourceWorkbook;
        this.sourceSheet = sourceSheet;
        this.sourceRow = sourceRow;
        this.identityKey = isBlank(identityKey) ? createIdentityKey(sourceWorkbook, ciClass, name) : identityKey;
    }

    public static String createIdentityKey(String sourceWorkbook, String ciClass, String name) {
        return normalize(sourceWorkbook) + "|" + normalize(ciClass) + "|" + normalize(name);
    }

    public void refreshIdentityKey() {
        this.identityKey = createIdentityKey(sourceWorkbook, ciClass, name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = defaultId(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        refreshIdentityKey();
    }

    public String getCiClass() {
        return ciClass;
    }

    public void setCiClass(String ciClass) {
        this.ciClass = ciClass;
        refreshIdentityKey();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = new HashMap<>(attributes == null ? Map.of() : attributes);
    }

    public String getSourceWorkbook() {
        return sourceWorkbook;
    }

    public void setSourceWorkbook(String sourceWorkbook) {
        this.sourceWorkbook = sourceWorkbook;
        refreshIdentityKey();
    }

    public String getSourceSheet() {
        return sourceSheet;
    }

    public void setSourceSheet(String sourceSheet) {
        this.sourceSheet = sourceSheet;
    }

    public int getSourceRow() {
        return sourceRow;
    }

    public void setSourceRow(int sourceRow) {
        this.sourceRow = sourceRow;
    }

    public String getIdentityKey() {
        return identityKey;
    }

    public void setIdentityKey(String identityKey) {
        this.identityKey = identityKey;
    }

    private static String defaultId(String id) {
        return isBlank(id) ? UUID.randomUUID().toString() : id;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigurationItem that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
