package com.cmdbanalyzer.controller.preview;

import com.cmdbanalyzer.service.filter.CmdbFilterCriteria;
import com.cmdbanalyzer.service.filter.CmdbSearchService;
import com.cmdbanalyzer.service.filter.FilterResult;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Builds the JavaFX import preview view from a precomputed preview view-model.
 */
public class ImportPreviewViewFactory {

    private static final String ALL_CLASSES = "All CI classes";
    private static final String ALL_SHEETS = "All sheets";
    private static final String ALL_RELATIONSHIP_STATUSES = "All relationship statuses";
    private static final String ALL_RELATIONSHIP_TYPES = "All relationship types";
    private static final String ALL_ISSUE_SEVERITIES = "All issue severities";
    private static final String ALL_ISSUE_TYPES = "All issue types";

    private final CmdbSearchService searchService = new CmdbSearchService();
    private final Consumer<ImportPreviewViewModel.SheetPreviewRow> sheetSelectionHandler;
    private final Consumer<ImportPreviewViewModel.ConfigurationItemPreviewRow> ciSelectionHandler;
    private final Consumer<ImportPreviewViewModel.RelationshipPreviewRow> relationshipSelectionHandler;
    private final Consumer<ImportPreviewViewModel.ValidationIssuePreviewRow> issueSelectionHandler;

    public ImportPreviewViewFactory(
            Consumer<ImportPreviewViewModel.SheetPreviewRow> sheetSelectionHandler,
            Consumer<ImportPreviewViewModel.ConfigurationItemPreviewRow> ciSelectionHandler,
            Consumer<ImportPreviewViewModel.RelationshipPreviewRow> relationshipSelectionHandler,
            Consumer<ImportPreviewViewModel.ValidationIssuePreviewRow> issueSelectionHandler
    ) {
        this.sheetSelectionHandler = sheetSelectionHandler;
        this.ciSelectionHandler = ciSelectionHandler;
        this.relationshipSelectionHandler = relationshipSelectionHandler;
        this.issueSelectionHandler = issueSelectionHandler;
    }

    public Node create(ImportPreviewViewModel viewModel) {
        VBox root = new VBox(16);
        root.getStyleClass().add("import-preview");
        PreviewTables tables = createTabs(viewModel);
        root.getChildren().addAll(createHeader(viewModel), createFilterBar(viewModel, tables), tables.tabPane());
        VBox.setVgrow(tables.tabPane(), Priority.ALWAYS);
        return root;
    }

    private Node createHeader(ImportPreviewViewModel viewModel) {
        VBox header = new VBox(12);
        header.getStyleClass().add("preview-header");

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleText = new VBox(2);
        Label title = new Label("Import preview");
        title.getStyleClass().add("preview-title");
        Label subtitle = new Label(viewModel.workbookName());
        subtitle.getStyleClass().add("preview-subtitle");
        titleText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label state = new Label("Parsed workbook");
        state.getStyleClass().add("preview-state-chip");
        titleRow.getChildren().addAll(titleText, spacer, state);

        TilePane metrics = new TilePane(10, 10);
        metrics.getStyleClass().add("preview-metrics");
        metrics.getChildren().addAll(
                metricCard("Sheets", viewModel.sheetCount(), "fas-folder-open"),
                metricCard("CIs", viewModel.ciCount(), "fas-cube"),
                metricCard("Relationships", viewModel.relationshipCount(), "fas-link"),
                metricCard("Resolved", viewModel.resolvedRelationshipCount(), "fas-check-circle"),
                metricCard("Unresolved", viewModel.unresolvedRelationshipCount(), "fas-question-circle"),
                metricCard("Issues", viewModel.issueCount(), "fas-exclamation-circle"),
                metricCard("Graph Vertices", viewModel.graphVertexCount(), "fas-project-diagram"),
                metricCard("Graph Edges", viewModel.graphEdgeCount(), "fas-route"),
                metricCard("Skipped", viewModel.skippedGraphRelationshipCount(), "fas-ban"),
                metricCard("Warnings", viewModel.warningCount(), "fas-exclamation-triangle")
        );

        header.getChildren().addAll(titleRow, metrics);
        return header;
    }

    private Node metricCard(String label, int value, String iconLiteral) {
        VBox card = new VBox(8);
        card.getStyleClass().add("metric-card");

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);
        FontIcon icon = new FontIcon(iconLiteral);
        icon.getStyleClass().add("metric-icon");
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("metric-label");
        topRow.getChildren().addAll(icon, labelNode);

        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.getStyleClass().add("metric-value");
        card.getChildren().addAll(topRow, valueLabel);
        return card;
    }

    private Node createFilterBar(ImportPreviewViewModel viewModel, PreviewTables tables) {
        VBox container = new VBox(10);
        container.getStyleClass().add("preview-filter-bar");

        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search CIs, relationships, issues...");
        searchField.getStyleClass().add("preview-search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Label resultCount = new Label(resultCountText(viewModel, fullResult(viewModel)));
        resultCount.getStyleClass().add("filter-result-count");

        Button clearButton = new Button("Clear filters");
        clearButton.getStyleClass().add("filter-clear-button");

        searchRow.getChildren().addAll(searchField, resultCount, clearButton);

        TilePane filterControls = new TilePane(8, 8);
        filterControls.getStyleClass().add("preview-filter-controls");

        ComboBox<String> ciClassFilter = combo(ALL_CLASSES, distinctCiClasses(viewModel));
        ComboBox<String> sourceSheetFilter = combo(ALL_SHEETS, distinctSourceSheets(viewModel));
        ComboBox<String> relationshipStatusFilter = combo(ALL_RELATIONSHIP_STATUSES, distinctRelationshipStatuses(viewModel));
        ComboBox<String> relationshipTypeFilter = combo(ALL_RELATIONSHIP_TYPES, distinctRelationshipTypes(viewModel));
        ComboBox<String> issueSeverityFilter = combo(ALL_ISSUE_SEVERITIES, distinctIssueSeverities(viewModel));
        ComboBox<String> issueTypeFilter = combo(ALL_ISSUE_TYPES, distinctIssueTypes(viewModel));

        filterControls.getChildren().addAll(
                filterControl("CI class", ciClassFilter),
                filterControl("Source sheet", sourceSheetFilter),
                filterControl("Relationship status", relationshipStatusFilter),
                filterControl("Relationship type", relationshipTypeFilter),
                filterControl("Issue severity", issueSeverityFilter),
                filterControl("Issue type", issueTypeFilter)
        );

        Runnable applyFilters = () -> {
            CmdbFilterCriteria criteria = new CmdbFilterCriteria(
                    searchField.getText(),
                    selectedFilter(ciClassFilter, ALL_CLASSES),
                    selectedFilter(sourceSheetFilter, ALL_SHEETS),
                    selectedFilter(relationshipStatusFilter, ALL_RELATIONSHIP_STATUSES),
                    selectedFilter(relationshipTypeFilter, ALL_RELATIONSHIP_TYPES),
                    selectedFilter(issueSeverityFilter, ALL_ISSUE_SEVERITIES),
                    selectedFilter(issueTypeFilter, ALL_ISSUE_TYPES)
            );
            FilterResult result = searchService.filter(viewModel, criteria);
            applyFilterResult(tables, result, criteria);
            resultCount.setText(resultCountText(viewModel, result));
        };

        PauseTransition searchDebounce = new PauseTransition(Duration.millis(250));
        searchDebounce.setOnFinished(event -> applyFilters.run());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchDebounce.playFromStart());

        List.of(
                ciClassFilter,
                sourceSheetFilter,
                relationshipStatusFilter,
                relationshipTypeFilter,
                issueSeverityFilter,
                issueTypeFilter
        ).forEach(combo -> combo.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters.run()));

        clearButton.setOnAction(event -> {
            searchField.clear();
            ciClassFilter.setValue(ALL_CLASSES);
            sourceSheetFilter.setValue(ALL_SHEETS);
            relationshipStatusFilter.setValue(ALL_RELATIONSHIP_STATUSES);
            relationshipTypeFilter.setValue(ALL_RELATIONSHIP_TYPES);
            issueSeverityFilter.setValue(ALL_ISSUE_SEVERITIES);
            issueTypeFilter.setValue(ALL_ISSUE_TYPES);
            applyFilters.run();
        });

        container.getChildren().addAll(searchRow, filterControls);
        return container;
    }

    private PreviewTables createTabs(ImportPreviewViewModel viewModel) {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("preview-tabs");
        TableView<ImportPreviewViewModel.SheetPreviewRow> sheetsTable = createSheetsTable(viewModel);
        TableView<ImportPreviewViewModel.ConfigurationItemPreviewRow> ciTable = createConfigurationItemsTable(viewModel);
        TableView<ImportPreviewViewModel.RelationshipPreviewRow> relationshipTable = createRelationshipsTable(viewModel);
        TableView<ImportPreviewViewModel.ValidationIssuePreviewRow> issueTable = createIssuesTable(viewModel);
        TableView<ImportPreviewViewModel.WarningPreviewRow> warningTable = createWarningsTable(viewModel);
        tabPane.getTabs().addAll(
                tab("Sheets", sheetsTable),
                tab("Configuration Items", ciTable),
                tab("Relationships", relationshipTable),
                tab("Issues", issueTable),
                tab("Warnings", warningTable)
        );
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return new PreviewTables(tabPane, sheetsTable, ciTable, relationshipTable, issueTable, warningTable);
    }

    private Tab tab(String title, Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        return tab;
    }

    private TableView<ImportPreviewViewModel.SheetPreviewRow> createSheetsTable(ImportPreviewViewModel viewModel) {
        TableView<ImportPreviewViewModel.SheetPreviewRow> table = table("No sheets were detected.");
        setColumns(table, List.of(
                stringColumn("Sheet", 220, ImportPreviewViewModel.SheetPreviewRow::name),
                stringColumn("Type", 150, ImportPreviewViewModel.SheetPreviewRow::type),
                stringColumn("Header Row", 110, ImportPreviewViewModel.SheetPreviewRow::headerRowIndex),
                intColumn("CI Blocks", 110, ImportPreviewViewModel.SheetPreviewRow::ciBlockCount),
                intColumn("Warnings", 110, ImportPreviewViewModel.SheetPreviewRow::warningCount)
        ));
        table.getItems().setAll(viewModel.sheets());
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                sheetSelectionHandler.accept(newValue);
            }
        });
        return table;
    }

    private TableView<ImportPreviewViewModel.ConfigurationItemPreviewRow> createConfigurationItemsTable(
            ImportPreviewViewModel viewModel
    ) {
        TableView<ImportPreviewViewModel.ConfigurationItemPreviewRow> table = table("No configuration items were parsed.");
        setColumns(table, List.of(
                stringColumn("Name", 180, ImportPreviewViewModel.ConfigurationItemPreviewRow::name),
                stringColumn("Class", 140, ImportPreviewViewModel.ConfigurationItemPreviewRow::ciClass),
                stringColumn("Description", 260, ImportPreviewViewModel.ConfigurationItemPreviewRow::description),
                stringColumn("Source Sheet", 150, ImportPreviewViewModel.ConfigurationItemPreviewRow::sourceSheet),
                intColumn("Source Row", 100, ImportPreviewViewModel.ConfigurationItemPreviewRow::sourceRow),
                stringColumn("Identity Key", 260, ImportPreviewViewModel.ConfigurationItemPreviewRow::identityKey)
        ));
        table.getItems().setAll(viewModel.configurationItems());
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ciSelectionHandler.accept(newValue);
            }
        });
        return table;
    }

    private TableView<ImportPreviewViewModel.RelationshipPreviewRow> createRelationshipsTable(
            ImportPreviewViewModel viewModel
    ) {
        TableView<ImportPreviewViewModel.RelationshipPreviewRow> table = table("No relationships were parsed.");
        setColumns(table, List.of(
                stringColumn("Source CI", 180, ImportPreviewViewModel.RelationshipPreviewRow::sourceCiDisplay),
                stringColumn("Target Name", 180, ImportPreviewViewModel.RelationshipPreviewRow::targetName),
                stringColumn("Type", 150, ImportPreviewViewModel.RelationshipPreviewRow::relationshipType),
                stringColumn("Raw Type", 150, ImportPreviewViewModel.RelationshipPreviewRow::rawRelationshipType),
                stringColumn("Status", 120, ImportPreviewViewModel.RelationshipPreviewRow::status),
                stringColumn("Source Sheet", 150, ImportPreviewViewModel.RelationshipPreviewRow::sourceSheet),
                intColumn("Source Row", 100, ImportPreviewViewModel.RelationshipPreviewRow::sourceRow)
        ));
        table.getItems().setAll(viewModel.relationships());
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                relationshipSelectionHandler.accept(newValue);
            }
        });
        return table;
    }

    private TableView<ImportPreviewViewModel.WarningPreviewRow> createWarningsTable(ImportPreviewViewModel viewModel) {
        TableView<ImportPreviewViewModel.WarningPreviewRow> table = table("No parser warnings were reported.");
        setColumns(table, List.of(
                stringColumn("Severity", 110, ImportPreviewViewModel.WarningPreviewRow::severity),
                stringColumn("Message", 320, ImportPreviewViewModel.WarningPreviewRow::message),
                stringColumn("Sheet", 150, ImportPreviewViewModel.WarningPreviewRow::sheet),
                stringColumn("Row", 80, ImportPreviewViewModel.WarningPreviewRow::row),
                stringColumn("Column", 110, ImportPreviewViewModel.WarningPreviewRow::column),
                stringColumn("Raw Value", 180, ImportPreviewViewModel.WarningPreviewRow::rawValue)
        ));
        table.getItems().setAll(viewModel.warnings());
        return table;
    }

    private TableView<ImportPreviewViewModel.ValidationIssuePreviewRow> createIssuesTable(ImportPreviewViewModel viewModel) {
        TableView<ImportPreviewViewModel.ValidationIssuePreviewRow> table = table("No validation issues were reported.");
        setColumns(table, List.of(
                stringColumn("Severity", 110, ImportPreviewViewModel.ValidationIssuePreviewRow::severity),
                stringColumn("Type", 190, ImportPreviewViewModel.ValidationIssuePreviewRow::type),
                stringColumn("Message", 320, ImportPreviewViewModel.ValidationIssuePreviewRow::message),
                stringColumn("Sheet", 150, ImportPreviewViewModel.ValidationIssuePreviewRow::sourceSheet),
                stringColumn("Row", 80, ImportPreviewViewModel.ValidationIssuePreviewRow::sourceRow),
                stringColumn("Recommended Action", 300, ImportPreviewViewModel.ValidationIssuePreviewRow::recommendedAction)
        ));
        table.getItems().setAll(viewModel.issues());
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                issueSelectionHandler.accept(newValue);
            }
        });
        return table;
    }

    private <T> TableView<T> table(String emptyMessage) {
        TableView<T> table = new TableView<>();
        table.getStyleClass().add("preview-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label(emptyMessage));
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    private <T> void setColumns(TableView<T> table, List<TableColumn<T, ?>> columns) {
        table.getColumns().setAll(columns);
    }

    private ComboBox<String> combo(String allLabel, List<String> values) {
        ComboBox<String> comboBox = new ComboBox<>();
        List<String> options = new ArrayList<>();
        options.add(allLabel);
        options.addAll(values);
        comboBox.getItems().setAll(options);
        comboBox.setValue(allLabel);
        comboBox.getStyleClass().add("preview-filter-combo");
        return comboBox;
    }

    private Node filterControl(String label, ComboBox<String> comboBox) {
        VBox control = new VBox(4);
        control.getStyleClass().add("preview-filter-control");
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("field-label");
        control.getChildren().addAll(labelNode, comboBox);
        return control;
    }

    private String selectedFilter(ComboBox<String> comboBox, String allLabel) {
        String value = comboBox.getValue();
        return value == null || allLabel.equals(value) ? null : value;
    }

    private void applyFilterResult(PreviewTables tables, FilterResult result, CmdbFilterCriteria criteria) {
        tables.sheetsTable().getItems().setAll(result.sheets());
        tables.configurationItemsTable().getItems().setAll(result.configurationItems());
        tables.relationshipsTable().getItems().setAll(result.relationships());
        tables.issuesTable().getItems().setAll(result.issues());
        tables.warningsTable().getItems().setAll(result.warnings());

        boolean filtered = criteria.hasActiveFilters();
        tables.sheetsTable().setPlaceholder(new Label(filtered ? "No sheets match the current filters." : "No sheets were detected."));
        tables.configurationItemsTable().setPlaceholder(new Label(filtered ? "No configuration items match the current filters." : "No configuration items were parsed."));
        tables.relationshipsTable().setPlaceholder(new Label(filtered ? "No relationships match the current filters." : "No relationships were parsed."));
        tables.issuesTable().setPlaceholder(new Label(filtered ? "No validation issues match the current filters." : "No validation issues were reported."));
        tables.warningsTable().setPlaceholder(new Label(filtered ? "No parser warnings match the current filters." : "No parser warnings were reported."));
    }

    private FilterResult fullResult(ImportPreviewViewModel viewModel) {
        return new FilterResult(
                viewModel.sheets(),
                viewModel.configurationItems(),
                viewModel.relationships(),
                viewModel.warnings(),
                viewModel.issues()
        );
    }

    private String resultCountText(ImportPreviewViewModel viewModel, FilterResult result) {
        return "Showing "
                + result.configurationItems().size() + " of " + viewModel.ciCount() + " CIs, "
                + result.relationships().size() + " of " + viewModel.relationshipCount() + " relationships, "
                + result.issues().size() + " of " + viewModel.issueCount() + " issues";
    }

    private List<String> distinctCiClasses(ImportPreviewViewModel viewModel) {
        Set<String> values = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        viewModel.configurationItems().stream()
                .map(ImportPreviewViewModel.ConfigurationItemPreviewRow::ciClass)
                .filter(this::hasText)
                .forEach(values::add);
        return List.copyOf(values);
    }

    private List<String> distinctSourceSheets(ImportPreviewViewModel viewModel) {
        Set<String> values = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        viewModel.sheets().stream()
                .map(ImportPreviewViewModel.SheetPreviewRow::name)
                .filter(this::hasText)
                .forEach(values::add);
        viewModel.configurationItems().stream()
                .map(ImportPreviewViewModel.ConfigurationItemPreviewRow::sourceSheet)
                .filter(this::hasText)
                .forEach(values::add);
        viewModel.relationships().stream()
                .map(ImportPreviewViewModel.RelationshipPreviewRow::sourceSheet)
                .filter(this::hasText)
                .forEach(values::add);
        viewModel.issues().stream()
                .map(ImportPreviewViewModel.ValidationIssuePreviewRow::sourceSheet)
                .filter(this::hasText)
                .forEach(values::add);
        return List.copyOf(values);
    }

    private List<String> distinctRelationshipStatuses(ImportPreviewViewModel viewModel) {
        Set<String> values = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        viewModel.relationships().stream()
                .map(ImportPreviewViewModel.RelationshipPreviewRow::status)
                .filter(this::hasText)
                .forEach(values::add);
        return List.copyOf(values);
    }

    private List<String> distinctRelationshipTypes(ImportPreviewViewModel viewModel) {
        Set<String> values = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        viewModel.relationships().stream()
                .map(ImportPreviewViewModel.RelationshipPreviewRow::relationshipType)
                .filter(this::hasText)
                .forEach(values::add);
        return List.copyOf(values);
    }

    private List<String> distinctIssueSeverities(ImportPreviewViewModel viewModel) {
        Set<String> values = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        viewModel.issues().stream()
                .map(ImportPreviewViewModel.ValidationIssuePreviewRow::severity)
                .filter(this::hasText)
                .forEach(values::add);
        return List.copyOf(values);
    }

    private List<String> distinctIssueTypes(ImportPreviewViewModel viewModel) {
        Set<String> values = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        viewModel.issues().stream()
                .map(ImportPreviewViewModel.ValidationIssuePreviewRow::type)
                .filter(this::hasText)
                .forEach(values::add);
        return List.copyOf(values);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private <T> TableColumn<T, String> stringColumn(String title, double width, ValueProvider<T, String> valueProvider) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueProvider.value(data.getValue())));
        return column;
    }

    private <T> TableColumn<T, Integer> intColumn(String title, double width, ValueProvider<T, Integer> valueProvider) {
        TableColumn<T, Integer> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(valueProvider.value(data.getValue())));
        return column;
    }

    @FunctionalInterface
    private interface ValueProvider<T, R> {
        R value(T row);
    }

    private record PreviewTables(
            TabPane tabPane,
            TableView<ImportPreviewViewModel.SheetPreviewRow> sheetsTable,
            TableView<ImportPreviewViewModel.ConfigurationItemPreviewRow> configurationItemsTable,
            TableView<ImportPreviewViewModel.RelationshipPreviewRow> relationshipsTable,
            TableView<ImportPreviewViewModel.ValidationIssuePreviewRow> issuesTable,
            TableView<ImportPreviewViewModel.WarningPreviewRow> warningsTable
    ) {
    }
}
