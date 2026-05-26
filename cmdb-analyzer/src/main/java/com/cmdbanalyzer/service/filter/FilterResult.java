package com.cmdbanalyzer.service.filter;

import com.cmdbanalyzer.controller.preview.ImportPreviewViewModel;

import java.util.List;

/**
 * Filtered table rows for the import preview.
 */
public record FilterResult(
        List<ImportPreviewViewModel.SheetPreviewRow> sheets,
        List<ImportPreviewViewModel.ConfigurationItemPreviewRow> configurationItems,
        List<ImportPreviewViewModel.RelationshipPreviewRow> relationships,
        List<ImportPreviewViewModel.WarningPreviewRow> warnings,
        List<ImportPreviewViewModel.ValidationIssuePreviewRow> issues
) {

    public FilterResult {
        sheets = List.copyOf(sheets == null ? List.of() : sheets);
        configurationItems = List.copyOf(configurationItems == null ? List.of() : configurationItems);
        relationships = List.copyOf(relationships == null ? List.of() : relationships);
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
        issues = List.copyOf(issues == null ? List.of() : issues);
    }
}
