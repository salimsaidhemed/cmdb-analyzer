package com.cmdbanalyzer.controller.preview;

import com.cmdbanalyzer.model.CmdbSheet;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.ParserWarning;
import com.cmdbanalyzer.model.Relationship;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Converts parsed domain objects into immutable rows for the import preview tables.
 */
public class CmdbTableMapper {

    public ImportPreviewViewModel toViewModel(CmdbWorkbook workbook) {
        Objects.requireNonNull(workbook, "workbook must not be null");

        Map<String, String> ciNamesById = new HashMap<>();
        List<ImportPreviewViewModel.SheetPreviewRow> sheetRows = new ArrayList<>();
        List<ImportPreviewViewModel.ConfigurationItemPreviewRow> ciRows = new ArrayList<>();
        List<ImportPreviewViewModel.RelationshipPreviewRow> relationshipRows = new ArrayList<>();

        for (CmdbSheet sheet : workbook.getSheets()) {
            sheetRows.add(toSheetRow(sheet));
            for (ConfigurationItem item : sheet.getConfigurationItems()) {
                ciNamesById.put(item.getId(), display(item.getName()));
                ciRows.add(toConfigurationItemRow(item));
            }
        }

        for (CmdbSheet sheet : workbook.getSheets()) {
            for (Relationship relationship : sheet.getRelationships()) {
                relationshipRows.add(toRelationshipRow(relationship, ciNamesById));
            }
        }

        List<ImportPreviewViewModel.WarningPreviewRow> warningRows = workbook.getParserWarnings().stream()
                .map(this::toWarningRow)
                .toList();

        return new ImportPreviewViewModel(
                workbook,
                workbookName(workbook.getSourceFile()),
                List.copyOf(sheetRows),
                List.copyOf(ciRows),
                List.copyOf(relationshipRows),
                warningRows
        );
    }

    private ImportPreviewViewModel.SheetPreviewRow toSheetRow(CmdbSheet sheet) {
        return new ImportPreviewViewModel.SheetPreviewRow(
                display(sheet.getName()),
                sheet.getType() == null ? "" : sheet.getType().name(),
                sheet.getHeaderRowIndex() == null ? "" : String.valueOf(sheet.getHeaderRowIndex()),
                sheet.getCiBlocks().size(),
                sheet.getWarnings().size()
        );
    }

    private ImportPreviewViewModel.ConfigurationItemPreviewRow toConfigurationItemRow(ConfigurationItem item) {
        return new ImportPreviewViewModel.ConfigurationItemPreviewRow(
                display(item.getId()),
                display(item.getName()),
                display(item.getCiClass()),
                display(item.getDescription()),
                display(item.getSourceSheet()),
                item.getSourceRow(),
                display(item.getIdentityKey()),
                safeAttributes(item.getAttributes())
        );
    }

    private ImportPreviewViewModel.RelationshipPreviewRow toRelationshipRow(
            Relationship relationship,
            Map<String, String> ciNamesById
    ) {
        String sourceCiId = display(relationship.getSourceCiId());
        String sourceCiDisplay = ciNamesById.getOrDefault(relationship.getSourceCiId(), sourceCiId);
        return new ImportPreviewViewModel.RelationshipPreviewRow(
                display(relationship.getId()),
                sourceCiId,
                sourceCiDisplay,
                display(relationship.getTargetName()),
                display(relationship.getRelationshipType()),
                display(relationship.getRawRelationshipType()),
                relationship.getStatus() == null ? "" : relationship.getStatus().name(),
                display(relationship.getSourceSheet()),
                relationship.getSourceRow()
        );
    }

    private ImportPreviewViewModel.WarningPreviewRow toWarningRow(ParserWarning warning) {
        return new ImportPreviewViewModel.WarningPreviewRow(
                warning.getSeverity() == null ? "" : warning.getSeverity().name(),
                display(warning.getMessage()),
                display(warning.getSheet()),
                warning.getRow() == null ? "" : String.valueOf(warning.getRow()),
                display(warning.getColumn()),
                display(warning.getRawValue())
        );
    }

    private String workbookName(String sourceFile) {
        if (sourceFile == null || sourceFile.trim().isEmpty()) {
            return "Untitled workbook";
        }
        try {
            return Path.of(sourceFile).getFileName().toString();
        } catch (RuntimeException exception) {
            return sourceFile;
        }
    }

    private String display(String value) {
        return value == null ? "" : value;
    }

    private Map<String, String> safeAttributes(Map<String, String> attributes) {
        Map<String, String> safeAttributes = new HashMap<>();
        if (attributes != null) {
            attributes.forEach((key, value) -> {
                if (key != null) {
                    safeAttributes.put(key, display(value));
                }
            });
        }
        return Map.copyOf(safeAttributes);
    }
}
