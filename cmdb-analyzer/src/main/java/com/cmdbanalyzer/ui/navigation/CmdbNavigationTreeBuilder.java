package com.cmdbanalyzer.ui.navigation;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationResult;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.model.CmdbSheet;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.Relationship;
import com.cmdbanalyzer.model.RelationshipStatus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.cmdbanalyzer.ui.navigation.CmdbNavigationNode.NavigationNodeType.CI;
import static com.cmdbanalyzer.ui.navigation.CmdbNavigationNode.NavigationNodeType.CI_CLASS;
import static com.cmdbanalyzer.ui.navigation.CmdbNavigationNode.NavigationNodeType.GROUP;
import static com.cmdbanalyzer.ui.navigation.CmdbNavigationNode.NavigationNodeType.ISSUE_SEVERITY;
import static com.cmdbanalyzer.ui.navigation.CmdbNavigationNode.NavigationNodeType.RELATIONSHIP_STATUS;
import static com.cmdbanalyzer.ui.navigation.CmdbNavigationNode.NavigationNodeType.SHEET;
import static com.cmdbanalyzer.ui.navigation.CmdbNavigationNode.NavigationNodeType.WORKBOOK;

/**
 * Builds a navigation model for the imported CMDB workbook.
 */
public class CmdbNavigationTreeBuilder {

    public CmdbNavigationNode build(CmdbWorkbook workbook, ValidationResult validationResult) {
        Objects.requireNonNull(workbook, "workbook must not be null");
        ValidationResult safeValidationResult = validationResult == null
                ? new ValidationResult(List.of())
                : validationResult;

        List<CmdbNavigationNode> rootChildren = List.of(
                buildSheetsNode(workbook),
                buildCiClassesNode(workbook),
                buildRelationshipStatusNode(workbook),
                buildIssuesNode(safeValidationResult)
        );

        return new CmdbNavigationNode(
                WORKBOOK,
                workbookName(workbook.getSourceFile()),
                workbook.getSheets().size(),
                null,
                null,
                null,
                null,
                null,
                null,
                rootChildren
        );
    }

    private CmdbNavigationNode buildSheetsNode(CmdbWorkbook workbook) {
        List<CmdbNavigationNode> sheetNodes = workbook.getSheets().stream()
                .map(this::sheetNode)
                .toList();
        return groupNode("Sheets", sheetNodes.size(), sheetNodes);
    }

    private CmdbNavigationNode sheetNode(CmdbSheet sheet) {
        List<CmdbNavigationNode> ciNodes = sheet.getConfigurationItems().stream()
                .sorted(Comparator.comparing(this::ciName, String.CASE_INSENSITIVE_ORDER))
                .map(item -> ciNode(item, item.getName()))
                .toList();
        return new CmdbNavigationNode(
                SHEET,
                safe(sheet.getName(), "Unnamed sheet"),
                ciNodes.size(),
                sheet.getName(),
                sheet.getType(),
                null,
                null,
                null,
                null,
                ciNodes
        );
    }

    private CmdbNavigationNode buildCiClassesNode(CmdbWorkbook workbook) {
        Map<String, List<ConfigurationItem>> itemsByClass = new LinkedHashMap<>();
        workbook.getSheets().stream()
                .flatMap(sheet -> sheet.getConfigurationItems().stream())
                .sorted(Comparator.comparing(this::ciName, String.CASE_INSENSITIVE_ORDER))
                .forEach(item -> itemsByClass.computeIfAbsent(ciClass(item), key -> new ArrayList<>()).add(item));

        List<CmdbNavigationNode> classNodes = itemsByClass.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> new CmdbNavigationNode(
                        CI_CLASS,
                        entry.getKey(),
                        entry.getValue().size(),
                        null,
                        null,
                        null,
                        entry.getKey(),
                        null,
                        null,
                        entry.getValue().stream()
                                .map(item -> ciNode(item, item.getName()))
                                .toList()
                ))
                .toList();
        return groupNode("CI Classes", classNodes.size(), classNodes);
    }

    private CmdbNavigationNode buildRelationshipStatusNode(CmdbWorkbook workbook) {
        Map<RelationshipStatus, Long> counts = new EnumMap<>(RelationshipStatus.class);
        workbook.getSheets().stream()
                .flatMap(sheet -> sheet.getRelationships().stream())
                .map(Relationship::getStatus)
                .map(status -> status == null ? RelationshipStatus.UNRESOLVED : status)
                .forEach(status -> counts.merge(status, 1L, Long::sum));

        List<CmdbNavigationNode> statusNodes = List.of(
                relationshipStatusNode(RelationshipStatus.RESOLVED, counts),
                relationshipStatusNode(RelationshipStatus.UNRESOLVED, counts),
                relationshipStatusNode(RelationshipStatus.MALFORMED, counts)
        );
        return groupNode("Relationship Status", totalCount(statusNodes), statusNodes);
    }

    private CmdbNavigationNode relationshipStatusNode(
            RelationshipStatus status,
            Map<RelationshipStatus, Long> counts
    ) {
        return new CmdbNavigationNode(
                RELATIONSHIP_STATUS,
                status.name(),
                counts.getOrDefault(status, 0L).intValue(),
                null,
                null,
                null,
                null,
                status,
                null,
                List.of()
        );
    }

    private CmdbNavigationNode buildIssuesNode(ValidationResult validationResult) {
        Map<ValidationSeverity, Long> counts = new EnumMap<>(ValidationSeverity.class);
        validationResult.issues().stream()
                .map(ValidationIssue::getSeverity)
                .map(severity -> severity == null ? ValidationSeverity.INFO : severity)
                .forEach(severity -> counts.merge(severity, 1L, Long::sum));

        List<CmdbNavigationNode> severityNodes = List.of(
                issueSeverityNode(ValidationSeverity.ERROR, counts),
                issueSeverityNode(ValidationSeverity.WARNING, counts),
                issueSeverityNode(ValidationSeverity.INFO, counts)
        );
        return groupNode("Issues", totalCount(severityNodes), severityNodes);
    }

    private CmdbNavigationNode issueSeverityNode(
            ValidationSeverity severity,
            Map<ValidationSeverity, Long> counts
    ) {
        return new CmdbNavigationNode(
                ISSUE_SEVERITY,
                severity.name(),
                counts.getOrDefault(severity, 0L).intValue(),
                null,
                null,
                null,
                null,
                null,
                severity,
                List.of()
        );
    }

    private CmdbNavigationNode ciNode(ConfigurationItem item, String label) {
        return new CmdbNavigationNode(
                CI,
                safe(label, "Unnamed CI"),
                -1,
                item.getSourceSheet(),
                null,
                item,
                item.getCiClass(),
                null,
                null,
                List.of()
        );
    }

    private CmdbNavigationNode groupNode(String label, int count, List<CmdbNavigationNode> children) {
        return new CmdbNavigationNode(
                GROUP,
                label,
                count,
                null,
                null,
                null,
                null,
                null,
                null,
                children
        );
    }

    private int totalCount(List<CmdbNavigationNode> nodes) {
        return nodes.stream().mapToInt(CmdbNavigationNode::count).sum();
    }

    private String workbookName(String sourceFile) {
        if (sourceFile == null || sourceFile.isBlank()) {
            return "Workbook";
        }
        try {
            return Path.of(sourceFile).getFileName().toString();
        } catch (RuntimeException exception) {
            return sourceFile;
        }
    }

    private String ciName(ConfigurationItem item) {
        return safe(item.getName(), "Unnamed CI");
    }

    private String ciClass(ConfigurationItem item) {
        return safe(item.getCiClass(), "Unclassified");
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
