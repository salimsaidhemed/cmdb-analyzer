package com.cmdbanalyzer.ui.detail;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.controller.preview.ImportPreviewViewModel;
import com.cmdbanalyzer.graph.CmdbGraph;
import com.cmdbanalyzer.graph.GraphQueryService;
import com.cmdbanalyzer.model.CmdbSheet;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.Relationship;
import com.cmdbanalyzer.model.RelationshipStatus;
import com.cmdbanalyzer.ui.detail.DetailViewModel.DetailField;
import com.cmdbanalyzer.ui.detail.DetailViewModel.DetailNotice;
import com.cmdbanalyzer.ui.detail.DetailViewModel.DetailSection;
import com.cmdbanalyzer.ui.detail.DetailViewModel.DetailTone;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps selected CMDB objects into detail panel view models.
 */
public class DetailSelectionService {

    public DetailViewModel empty() {
        return DetailViewModel.empty();
    }

    public DetailViewModel forConfigurationItem(ImportPreviewViewModel viewModel, String ciId) {
        return findConfigurationItem(viewModel, ciId)
                .map(item -> forConfigurationItem(viewModel, item))
                .orElseGet(DetailViewModel::empty);
    }

    public DetailViewModel forConfigurationItem(ImportPreviewViewModel viewModel, ConfigurationItem item) {
        if (item == null) {
            return DetailViewModel.empty();
        }

        GraphQueryService queryService = graphQueryService(viewModel).orElse(null);
        List<ConfigurationItem> dependencies = queryService == null
                ? directDependenciesFromRelationships(viewModel, item.getId())
                : queryService.getDirectDependencies(item.getId());
        List<ConfigurationItem> dependents = queryService == null
                ? directDependentsFromRelationships(viewModel, item.getId())
                : queryService.getDirectDependents(item.getId());

        List<DetailSection> sections = new ArrayList<>();
        sections.add(section("Summary",
                field("CI name", item.getName()),
                field("CI class", item.getCiClass()),
                field("Description", item.getDescription()),
                field("Identity key", item.getIdentityKey())
        ));
        sections.add(section("Relationships",
                field("Outgoing count", String.valueOf(dependencies.size())),
                field("Incoming count", String.valueOf(dependents.size())),
                field("Direct dependencies", ciNames(dependencies)),
                field("Direct dependents", ciNames(dependents))
        ));
        sections.add(section("Source traceability",
                field("Workbook", item.getSourceWorkbook()),
                field("Sheet", item.getSourceSheet()),
                field("Row", String.valueOf(item.getSourceRow()))
        ));
        sections.add(attributesSection(item.getAttributes()));

        return new DetailViewModel(
                safe(item.getName()),
                "Configuration Item",
                safe(item.getCiClass()),
                DetailTone.NEUTRAL,
                List.of(),
                sections
        );
    }

    public DetailViewModel forRelationship(ImportPreviewViewModel viewModel, String relationshipId) {
        Relationship relationship = findRelationship(viewModel, relationshipId).orElse(null);
        if (relationship == null) {
            return DetailViewModel.empty();
        }

        ConfigurationItem source = findConfigurationItem(viewModel, relationship.getSourceCiId()).orElse(null);
        ConfigurationItem target = findConfigurationItem(viewModel, relationship.getTargetCiId()).orElse(null);
        RelationshipStatus status = relationship.getStatus();
        List<DetailNotice> notices = new ArrayList<>();
        if (status == RelationshipStatus.UNRESOLVED) {
            notices.add(new DetailNotice(DetailTone.WARNING, "Target CI could not be resolved."));
        } else if (status == RelationshipStatus.MALFORMED) {
            notices.add(new DetailNotice(DetailTone.ERROR, "Relationship row is malformed."));
        }

        return new DetailViewModel(
                safe(relationship.getRelationshipType()),
                "Relationship",
                status == null ? "UNRESOLVED" : status.name(),
                relationshipTone(status),
                notices,
                List.of(
                        section("Summary",
                                field("Relationship type", relationship.getRelationshipType()),
                                field("Raw relationship type", relationship.getRawRelationshipType()),
                                field("Status", status == null ? null : status.name())
                        ),
                        section("Endpoints",
                                field("Source CI", ciDisplay(source, relationship.getSourceCiId())),
                                field("Source CI ID", relationship.getSourceCiId()),
                                field("Target CI", target == null ? relationship.getTargetName() : target.getName()),
                                field("Target CI ID", relationship.getTargetCiId()),
                                field("Target name", relationship.getTargetName())
                        ),
                        section("Source traceability",
                                field("Workbook", relationship.getSourceWorkbook()),
                                field("Sheet", relationship.getSourceSheet()),
                                field("Row", String.valueOf(relationship.getSourceRow()))
                        )
                )
        );
    }

    public DetailViewModel forSheet(ImportPreviewViewModel viewModel, String sheetName) {
        CmdbSheet sheet = findSheet(viewModel, sheetName).orElse(null);
        if (sheet == null) {
            return DetailViewModel.empty();
        }

        return new DetailViewModel(
                safe(sheet.getName()),
                "Worksheet",
                sheet.getType() == null ? "UNKNOWN" : sheet.getType().name(),
                DetailTone.NEUTRAL,
                List.of(),
                List.of(
                        section("Summary",
                                field("Sheet name", sheet.getName()),
                                field("Sheet type", sheet.getType() == null ? null : sheet.getType().name()),
                                field("Header row index", sheet.getHeaderRowIndex() == null ? null : String.valueOf(sheet.getHeaderRowIndex()))
                        ),
                        section("Contents",
                                field("CI block count", String.valueOf(sheet.getCiBlocks().size())),
                                field("Parsed CI count", String.valueOf(sheet.getConfigurationItems().size())),
                                field("Relationship count", String.valueOf(sheet.getRelationships().size())),
                                field("Flat row count", String.valueOf(sheet.getFlatRows().size())),
                                field("Warning count", String.valueOf(sheet.getWarnings().size()))
                        )
                )
        );
    }

    public DetailViewModel forValidationIssue(ImportPreviewViewModel viewModel, String issueId) {
        ValidationIssue issue = findValidationIssue(viewModel, issueId).orElse(null);
        if (issue == null) {
            return DetailViewModel.empty();
        }

        DetailTone tone = switch (issue.getSeverity()) {
            case ERROR -> DetailTone.ERROR;
            case WARNING -> DetailTone.WARNING;
            case INFO -> DetailTone.NEUTRAL;
        };

        return new DetailViewModel(
                safe(issue.getType() == null ? null : issue.getType().name()),
                "Validation Issue",
                issue.getSeverity() == null ? "INFO" : issue.getSeverity().name(),
                tone,
                hasText(issue.getRecommendedAction())
                        ? List.of(new DetailNotice(DetailTone.NEUTRAL, issue.getRecommendedAction()))
                        : List.of(),
                List.of(
                        section("Summary",
                                field("Severity", issue.getSeverity() == null ? null : issue.getSeverity().name()),
                                field("Issue type", issue.getType() == null ? null : issue.getType().name()),
                                field("Message", issue.getMessage()),
                                field("Recommended action", issue.getRecommendedAction())
                        ),
                        section("Source traceability",
                                field("Workbook", issue.getSourceWorkbook()),
                                field("Sheet", issue.getSourceSheet()),
                                field("Row", issue.getSourceRow() == null ? null : String.valueOf(issue.getSourceRow()))
                        ),
                        section("Affected object",
                                field("Affected CI ID", issue.getAffectedCiId()),
                                field("Affected relationship ID", issue.getAffectedRelationshipId())
                        )
                )
        );
    }

    private Optional<ConfigurationItem> findConfigurationItem(ImportPreviewViewModel viewModel, String ciId) {
        if (viewModel == null || viewModel.workbook() == null || !hasText(ciId)) {
            return Optional.empty();
        }
        return viewModel.workbook().getSheets().stream()
                .flatMap(sheet -> sheet.getConfigurationItems().stream())
                .filter(item -> ciId.equals(item.getId()))
                .findFirst();
    }

    private Optional<Relationship> findRelationship(ImportPreviewViewModel viewModel, String relationshipId) {
        if (viewModel == null || viewModel.workbook() == null || !hasText(relationshipId)) {
            return Optional.empty();
        }
        return viewModel.workbook().getSheets().stream()
                .flatMap(sheet -> sheet.getRelationships().stream())
                .filter(relationship -> relationshipId.equals(relationship.getId()))
                .findFirst();
    }

    private Optional<CmdbSheet> findSheet(ImportPreviewViewModel viewModel, String sheetName) {
        if (viewModel == null || viewModel.workbook() == null || !hasText(sheetName)) {
            return Optional.empty();
        }
        return viewModel.workbook().getSheets().stream()
                .filter(sheet -> sheetName.equals(sheet.getName()))
                .findFirst();
    }

    private Optional<ValidationIssue> findValidationIssue(ImportPreviewViewModel viewModel, String issueId) {
        if (viewModel == null || viewModel.validationResult() == null || !hasText(issueId)) {
            return Optional.empty();
        }
        return viewModel.validationResult().issues().stream()
                .filter(issue -> issueId.equals(issue.getId()))
                .findFirst();
    }

    private Optional<GraphQueryService> graphQueryService(ImportPreviewViewModel viewModel) {
        if (viewModel == null || viewModel.graphBuildResult() == null) {
            return Optional.empty();
        }
        CmdbGraph graph = viewModel.graphBuildResult().graph();
        return graph == null ? Optional.empty() : Optional.of(new GraphQueryService(graph));
    }

    private List<ConfigurationItem> directDependenciesFromRelationships(ImportPreviewViewModel viewModel, String ciId) {
        if (viewModel == null || viewModel.workbook() == null || !hasText(ciId)) {
            return List.of();
        }
        return viewModel.workbook().getSheets().stream()
                .flatMap(sheet -> sheet.getRelationships().stream())
                .filter(relationship -> ciId.equals(relationship.getSourceCiId()))
                .filter(relationship -> relationship.getStatus() == RelationshipStatus.RESOLVED)
                .map(Relationship::getTargetCiId)
                .distinct()
                .map(targetId -> findConfigurationItem(viewModel, targetId))
                .flatMap(Optional::stream)
                .toList();
    }

    private List<ConfigurationItem> directDependentsFromRelationships(ImportPreviewViewModel viewModel, String ciId) {
        if (viewModel == null || viewModel.workbook() == null || !hasText(ciId)) {
            return List.of();
        }
        return viewModel.workbook().getSheets().stream()
                .flatMap(sheet -> sheet.getRelationships().stream())
                .filter(relationship -> ciId.equals(relationship.getTargetCiId()))
                .filter(relationship -> relationship.getStatus() == RelationshipStatus.RESOLVED)
                .map(Relationship::getSourceCiId)
                .distinct()
                .map(sourceId -> findConfigurationItem(viewModel, sourceId))
                .flatMap(Optional::stream)
                .toList();
    }

    private DetailSection attributesSection(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return section("Attributes", field("Attributes", "No additional attributes"));
        }
        List<DetailField> fields = attributes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> field(entry.getKey(), entry.getValue()))
                .toList();
        return new DetailSection("Attributes", fields);
    }

    private DetailSection section(String title, DetailField... fields) {
        return new DetailSection(title, List.of(fields));
    }

    private DetailField field(String label, String value) {
        return new DetailField(label, safe(value));
    }

    private String ciDisplay(ConfigurationItem item, String fallbackId) {
        if (item == null) {
            return safe(fallbackId);
        }
        return safe(item.getName()) + " (" + safe(item.getCiClass()) + ")";
    }

    private String ciNames(List<ConfigurationItem> items) {
        if (items == null || items.isEmpty()) {
            return "None";
        }
        return items.stream()
                .sorted(Comparator.comparing(item -> safe(item.getName()), String.CASE_INSENSITIVE_ORDER))
                .map(ConfigurationItem::getName)
                .map(this::safe)
                .collect(Collectors.joining(", "));
    }

    private DetailTone relationshipTone(RelationshipStatus status) {
        if (status == RelationshipStatus.RESOLVED) {
            return DetailTone.SUCCESS;
        }
        if (status == RelationshipStatus.MALFORMED) {
            return DetailTone.ERROR;
        }
        return DetailTone.WARNING;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
