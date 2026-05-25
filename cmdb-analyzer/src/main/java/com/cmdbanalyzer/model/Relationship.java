package com.cmdbanalyzer.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Framework-independent relationship edge between a source CI and a target CI reference.
 */
public class Relationship {

    private String id;
    private String sourceCiId;
    private String targetCiId;
    private String targetName;
    private String relationshipType;
    private String rawRelationshipType;
    private String sourceWorkbook;
    private String sourceSheet;
    private int sourceRow;
    private RelationshipStatus status;

    public Relationship() {
        this(null, null, null, null, null, null, null, null, 0, RelationshipStatus.UNRESOLVED);
    }

    public Relationship(
            String id,
            String sourceCiId,
            String targetCiId,
            String targetName,
            String relationshipType,
            String rawRelationshipType,
            String sourceWorkbook,
            String sourceSheet,
            int sourceRow,
            RelationshipStatus status
    ) {
        this.id = defaultId(id);
        this.sourceCiId = sourceCiId;
        this.targetCiId = targetCiId;
        this.targetName = targetName;
        this.relationshipType = relationshipType;
        this.rawRelationshipType = rawRelationshipType;
        this.sourceWorkbook = sourceWorkbook;
        this.sourceSheet = sourceSheet;
        this.sourceRow = sourceRow;
        this.status = status == null ? RelationshipStatus.UNRESOLVED : status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = defaultId(id);
    }

    public String getSourceCiId() {
        return sourceCiId;
    }

    public void setSourceCiId(String sourceCiId) {
        this.sourceCiId = sourceCiId;
    }

    public String getTargetCiId() {
        return targetCiId;
    }

    public void setTargetCiId(String targetCiId) {
        this.targetCiId = targetCiId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getRawRelationshipType() {
        return rawRelationshipType;
    }

    public void setRawRelationshipType(String rawRelationshipType) {
        this.rawRelationshipType = rawRelationshipType;
    }

    public String getSourceWorkbook() {
        return sourceWorkbook;
    }

    public void setSourceWorkbook(String sourceWorkbook) {
        this.sourceWorkbook = sourceWorkbook;
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

    public RelationshipStatus getStatus() {
        return status;
    }

    public void setStatus(RelationshipStatus status) {
        this.status = status == null ? RelationshipStatus.UNRESOLVED : status;
    }

    private static String defaultId(String id) {
        return id == null || id.trim().isEmpty() ? UUID.randomUUID().toString() : id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Relationship that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
