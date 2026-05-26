package com.cmdbanalyzer.service.filter;

import com.cmdbanalyzer.controller.preview.ImportPreviewViewModel;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Applies case-insensitive search and scoped filters to imported CMDB preview data.
 */
public class CmdbSearchService {

    public FilterResult filter(ImportPreviewViewModel viewModel, CmdbFilterCriteria criteria) {
        Objects.requireNonNull(viewModel, "viewModel must not be null");
        CmdbFilterCriteria safeCriteria = criteria == null ? CmdbFilterCriteria.empty() : criteria;
        String query = normalize(safeCriteria.query());

        return new FilterResult(
                viewModel.sheets().stream()
                        .filter(row -> matchesSheet(row, query, safeCriteria))
                        .toList(),
                viewModel.configurationItems().stream()
                        .filter(row -> matchesConfigurationItem(row, query, safeCriteria))
                        .toList(),
                viewModel.relationships().stream()
                        .filter(row -> matchesRelationship(row, query, safeCriteria))
                        .toList(),
                viewModel.warnings().stream()
                        .filter(row -> matchesWarning(row, query, safeCriteria))
                        .toList(),
                viewModel.issues().stream()
                        .filter(row -> matchesIssue(row, query, safeCriteria))
                        .toList()
        );
    }

    private boolean matchesSheet(
            ImportPreviewViewModel.SheetPreviewRow row,
            String query,
            CmdbFilterCriteria criteria
    ) {
        return matchesSourceSheet(row.name(), criteria.sourceSheet())
                && queryMatches(query, row.name(), row.type(), row.headerRowIndex());
    }

    private boolean matchesConfigurationItem(
            ImportPreviewViewModel.ConfigurationItemPreviewRow row,
            String query,
            CmdbFilterCriteria criteria
    ) {
        return matchesExact(row.ciClass(), criteria.ciClass())
                && matchesSourceSheet(row.sourceSheet(), criteria.sourceSheet())
                && queryMatches(
                        query,
                        row.name(),
                        row.ciClass(),
                        row.description(),
                        row.sourceSheet(),
                        row.identityKey(),
                        attributeKeys(row.attributes()),
                        attributeValues(row.attributes())
                );
    }

    private boolean matchesRelationship(
            ImportPreviewViewModel.RelationshipPreviewRow row,
            String query,
            CmdbFilterCriteria criteria
    ) {
        return matchesExact(row.status(), criteria.relationshipStatus())
                && matchesExact(row.relationshipType(), criteria.relationshipType())
                && matchesSourceSheet(row.sourceSheet(), criteria.sourceSheet())
                && queryMatches(
                        query,
                        row.sourceCiDisplay(),
                        row.sourceCiId(),
                        row.targetName(),
                        row.relationshipType(),
                        row.rawRelationshipType(),
                        row.status(),
                        row.sourceSheet()
                );
    }

    private boolean matchesWarning(
            ImportPreviewViewModel.WarningPreviewRow row,
            String query,
            CmdbFilterCriteria criteria
    ) {
        return matchesSourceSheet(row.sheet(), criteria.sourceSheet())
                && queryMatches(query, row.severity(), row.message(), row.sheet(), row.column(), row.rawValue());
    }

    private boolean matchesIssue(
            ImportPreviewViewModel.ValidationIssuePreviewRow row,
            String query,
            CmdbFilterCriteria criteria
    ) {
        return matchesExact(row.severity(), criteria.issueSeverity())
                && matchesExact(row.type(), criteria.issueType())
                && matchesSourceSheet(row.sourceSheet(), criteria.sourceSheet())
                && queryMatches(
                        query,
                        row.severity(),
                        row.type(),
                        row.message(),
                        row.sourceSheet(),
                        row.affectedCiId(),
                        row.affectedRelationshipId(),
                        row.recommendedAction()
                );
    }

    private boolean queryMatches(String query, String... values) {
        if (query.isEmpty()) {
            return true;
        }
        for (String value : values) {
            if (normalize(value).contains(query)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesExact(String value, String filterValue) {
        String normalizedFilter = normalize(filterValue);
        return normalizedFilter.isEmpty() || normalize(value).equals(normalizedFilter);
    }

    private boolean matchesSourceSheet(String value, String filterValue) {
        return matchesExact(value, filterValue);
    }

    private String attributeKeys(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        }
        return attributes.keySet().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private String attributeValues(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        }
        return attributes.values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
