package com.cmdbanalyzer.controller.preview;

import com.cmdbanalyzer.analyzer.validation.ValidationResult;
import com.cmdbanalyzer.model.CmdbWorkbook;

import java.util.List;
import java.util.Map;

/**
 * UI-ready projection of a parsed CMDB workbook for the import preview screen.
 */
public record ImportPreviewViewModel(
        CmdbWorkbook workbook,
        ValidationResult validationResult,
        String workbookName,
        List<SheetPreviewRow> sheets,
        List<ConfigurationItemPreviewRow> configurationItems,
        List<RelationshipPreviewRow> relationships,
        List<WarningPreviewRow> warnings,
        List<ValidationIssuePreviewRow> issues
) {

    public int sheetCount() {
        return sheets.size();
    }

    public int ciCount() {
        return configurationItems.size();
    }

    public int relationshipCount() {
        return relationships.size();
    }

    public int warningCount() {
        return warnings.size();
    }

    public int issueCount() {
        return issues.size();
    }

    public int resolvedRelationshipCount() {
        return countRelationshipsByStatus("RESOLVED");
    }

    public int unresolvedRelationshipCount() {
        return countRelationshipsByStatus("UNRESOLVED");
    }

    public int malformedRelationshipCount() {
        return countRelationshipsByStatus("MALFORMED");
    }

    private int countRelationshipsByStatus(String status) {
        return (int) relationships.stream()
                .filter(relationship -> status.equals(relationship.status()))
                .count();
    }

    public record SheetPreviewRow(
            String name,
            String type,
            String headerRowIndex,
            int ciBlockCount,
            int warningCount
    ) {
    }

    public record ConfigurationItemPreviewRow(
            String id,
            String name,
            String ciClass,
            String description,
            String sourceSheet,
            int sourceRow,
            String identityKey,
            Map<String, String> attributes
    ) {
    }

    public record RelationshipPreviewRow(
            String id,
            String sourceCiId,
            String sourceCiDisplay,
            String targetName,
            String relationshipType,
            String rawRelationshipType,
            String status,
            String sourceSheet,
            int sourceRow
    ) {
    }

    public record WarningPreviewRow(
            String severity,
            String message,
            String sheet,
            String row,
            String column,
            String rawValue
    ) {
    }

    public record ValidationIssuePreviewRow(
            String id,
            String severity,
            String type,
            String message,
            String sourceWorkbook,
            String sourceSheet,
            String sourceRow,
            String affectedCiId,
            String affectedRelationshipId,
            String recommendedAction
    ) {
    }
}
