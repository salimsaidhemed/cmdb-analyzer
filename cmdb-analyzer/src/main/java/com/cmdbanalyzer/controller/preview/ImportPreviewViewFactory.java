package com.cmdbanalyzer.controller.preview;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Builds the JavaFX import preview view from a precomputed preview view-model.
 */
public class ImportPreviewViewFactory {

    private final Consumer<ImportPreviewViewModel.ConfigurationItemPreviewRow> ciSelectionHandler;
    private final Consumer<ImportPreviewViewModel.RelationshipPreviewRow> relationshipSelectionHandler;
    private final Consumer<ImportPreviewViewModel.ValidationIssuePreviewRow> issueSelectionHandler;

    public ImportPreviewViewFactory(
            Consumer<ImportPreviewViewModel.ConfigurationItemPreviewRow> ciSelectionHandler,
            Consumer<ImportPreviewViewModel.RelationshipPreviewRow> relationshipSelectionHandler,
            Consumer<ImportPreviewViewModel.ValidationIssuePreviewRow> issueSelectionHandler
    ) {
        this.ciSelectionHandler = ciSelectionHandler;
        this.relationshipSelectionHandler = relationshipSelectionHandler;
        this.issueSelectionHandler = issueSelectionHandler;
    }

    public Node create(ImportPreviewViewModel viewModel) {
        VBox root = new VBox(16);
        root.getStyleClass().add("import-preview");
        root.getChildren().addAll(createHeader(viewModel), createTabs(viewModel));
        VBox.setVgrow(root.getChildren().get(1), Priority.ALWAYS);
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

        HBox metrics = new HBox(10);
        metrics.getStyleClass().add("preview-metrics");
        metrics.getChildren().addAll(
                metricCard("Sheets", viewModel.sheetCount()),
                metricCard("CIs", viewModel.ciCount()),
                metricCard("Relationships", viewModel.relationshipCount()),
                metricCard("Resolved", viewModel.resolvedRelationshipCount()),
                metricCard("Unresolved", viewModel.unresolvedRelationshipCount()),
                metricCard("Issues", viewModel.issueCount()),
                metricCard("Warnings", viewModel.warningCount())
        );

        header.getChildren().addAll(titleRow, metrics);
        return header;
    }

    private Node metricCard(String label, int value) {
        VBox card = new VBox(3);
        card.getStyleClass().add("metric-card");
        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.getStyleClass().add("metric-value");
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("metric-label");
        card.getChildren().addAll(valueLabel, labelNode);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private Node createTabs(ImportPreviewViewModel viewModel) {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("preview-tabs");
        tabPane.getTabs().addAll(
                tab("Sheets", createSheetsTable(viewModel)),
                tab("Configuration Items", createConfigurationItemsTable(viewModel)),
                tab("Relationships", createRelationshipsTable(viewModel)),
                tab("Issues", createIssuesTable(viewModel)),
                tab("Warnings", createWarningsTable(viewModel))
        );
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
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
}
