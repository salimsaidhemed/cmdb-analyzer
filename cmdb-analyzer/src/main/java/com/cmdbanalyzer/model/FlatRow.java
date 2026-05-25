package com.cmdbanalyzer.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Generic row model for flat or lookup-style worksheets that do not use CI blocks.
 */
public class FlatRow {

    private String sourceSheet;
    private int sourceRow;
    private Map<String, String> values;

    public FlatRow() {
        this(null, 0, new HashMap<>());
    }

    public FlatRow(String sourceSheet, int sourceRow, Map<String, String> values) {
        this.sourceSheet = sourceSheet;
        this.sourceRow = sourceRow;
        setValues(values);
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

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = new HashMap<>(values == null ? Map.of() : values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlatRow flatRow)) {
            return false;
        }
        return sourceRow == flatRow.sourceRow
                && Objects.equals(sourceSheet, flatRow.sourceSheet)
                && Objects.equals(values, flatRow.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceSheet, sourceRow, values);
    }
}
