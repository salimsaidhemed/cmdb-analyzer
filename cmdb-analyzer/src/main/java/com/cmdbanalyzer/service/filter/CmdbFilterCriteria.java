package com.cmdbanalyzer.service.filter;

/**
 * Framework-independent criteria used to filter parsed CMDB preview data.
 */
public record CmdbFilterCriteria(
        String query,
        String ciClass,
        String sourceSheet,
        String relationshipStatus,
        String relationshipType,
        String issueSeverity,
        String issueType
) {

    public static CmdbFilterCriteria empty() {
        return new CmdbFilterCriteria(null, null, null, null, null, null, null);
    }

    public boolean hasActiveFilters() {
        return hasText(query)
                || hasText(ciClass)
                || hasText(sourceSheet)
                || hasText(relationshipStatus)
                || hasText(relationshipType)
                || hasText(issueSeverity)
                || hasText(issueType);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
