package com.cmdbanalyzer.graph;

import java.util.Objects;

/**
 * Directed graph edge backed by a resolved CMDB relationship.
 */
public class CmdbGraphEdge {

    private final String relationshipId;
    private final String relationshipType;
    private final String rawRelationshipType;
    private final String sourceCiId;
    private final String targetCiId;

    public CmdbGraphEdge(
            String relationshipId,
            String relationshipType,
            String rawRelationshipType,
            String sourceCiId,
            String targetCiId
    ) {
        this.relationshipId = relationshipId;
        this.relationshipType = relationshipType;
        this.rawRelationshipType = rawRelationshipType;
        this.sourceCiId = sourceCiId;
        this.targetCiId = targetCiId;
    }

    public String getRelationshipId() {
        return relationshipId;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public String getRawRelationshipType() {
        return rawRelationshipType;
    }

    public String getSourceCiId() {
        return sourceCiId;
    }

    public String getTargetCiId() {
        return targetCiId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CmdbGraphEdge that)) {
            return false;
        }
        return Objects.equals(relationshipId, that.relationshipId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relationshipId);
    }
}
