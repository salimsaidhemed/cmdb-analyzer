package com.cmdbanalyzer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Row-boundary model for a variable-sized CI block in a block-based worksheet.
 */
public class CmdbBlock {

    private int ciRowIndex;
    private List<Integer> relationshipRowIndexes;
    private Integer separatorRowIndex;
    private String sourceSheetName;

    public CmdbBlock() {
        this(0, new ArrayList<>(), null, null);
    }

    public CmdbBlock(
            int ciRowIndex,
            List<Integer> relationshipRowIndexes,
            Integer separatorRowIndex,
            String sourceSheetName
    ) {
        this.ciRowIndex = ciRowIndex;
        setRelationshipRowIndexes(relationshipRowIndexes);
        this.separatorRowIndex = separatorRowIndex;
        this.sourceSheetName = sourceSheetName;
    }

    public int getCiRowIndex() {
        return ciRowIndex;
    }

    public void setCiRowIndex(int ciRowIndex) {
        this.ciRowIndex = ciRowIndex;
    }

    public List<Integer> getRelationshipRowIndexes() {
        return relationshipRowIndexes;
    }

    public void setRelationshipRowIndexes(List<Integer> relationshipRowIndexes) {
        this.relationshipRowIndexes = new ArrayList<>(relationshipRowIndexes == null ? List.of() : relationshipRowIndexes);
    }

    public Integer getSeparatorRowIndex() {
        return separatorRowIndex;
    }

    public void setSeparatorRowIndex(Integer separatorRowIndex) {
        this.separatorRowIndex = separatorRowIndex;
    }

    public String getSourceSheetName() {
        return sourceSheetName;
    }

    public void setSourceSheetName(String sourceSheetName) {
        this.sourceSheetName = sourceSheetName;
    }

    public void addRelationshipRowIndex(int rowIndex) {
        relationshipRowIndexes.add(rowIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CmdbBlock cmdbBlock)) {
            return false;
        }
        return ciRowIndex == cmdbBlock.ciRowIndex
                && Objects.equals(relationshipRowIndexes, cmdbBlock.relationshipRowIndexes)
                && Objects.equals(separatorRowIndex, cmdbBlock.separatorRowIndex)
                && Objects.equals(sourceSheetName, cmdbBlock.sourceSheetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ciRowIndex, relationshipRowIndexes, separatorRowIndex, sourceSheetName);
    }
}
