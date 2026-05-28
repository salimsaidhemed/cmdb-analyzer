package com.cmdbanalyzer.ui.impact;

import com.cmdbanalyzer.analyzer.impact.ImpactAnalysisResult;
import com.cmdbanalyzer.analyzer.impact.ImpactDirection;
import com.cmdbanalyzer.analyzer.impact.ImpactPath;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.function.Consumer;

/**
 * JavaFX impact analysis panel for the selected CI.
 */
public class ImpactTabView {

    private final ImpactAnalysisRequestHandler requestHandler;
    private final Consumer<String> endpointSelectionHandler;
    private final BorderPane root = new BorderPane();
    private final Label selectedLabel = new Label("No CI selected");
    private final Label summaryLabel = new Label("Affected CIs: 0 | Relationships: 0 | Paths: 0");
    private final Label noticeLabel = new Label("Select a CI, choose options, then run impact analysis.");
    private final ComboBox<ImpactDirection> directionCombo = new ComboBox<>();
    private final Spinner<Integer> depthSpinner = new Spinner<>();
    private final Button runButton = new Button("Run Impact Analysis");
    private final TableView<ImpactPath> table = new TableView<>();
    private String selectedCiId;

    public ImpactTabView(
            ImpactAnalysisRequestHandler requestHandler,
            Consumer<String> endpointSelectionHandler
    ) {
        this.requestHandler = requestHandler;
        this.endpointSelectionHandler = endpointSelectionHandler;
        configure();
    }

    public Node node() {
        return root;
    }

    public void showSelection(String ciId, String ciName) {
        selectedCiId = ciId;
        selectedLabel.setText(ciName == null || ciName.isBlank() ? "No CI selected" : "Selected CI: " + ciName);
        runButton.setDisable(selectedCiId == null || selectedCiId.isBlank());
        if (runButton.isDisabled()) {
            table.getItems().clear();
            summaryLabel.setText("Affected CIs: 0 | Relationships: 0 | Paths: 0");
            noticeLabel.setText("Select a CI, choose options, then run impact analysis.");
        }
    }

    private void configure() {
        root.getStyleClass().add("impact-view");
        root.setTop(header());
        root.setCenter(table);
        configureTable();
        showSelection(null, null);
    }

    private Node header() {
        VBox header = new VBox(10);
        header.getStyleClass().add("impact-header");

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        FontIcon icon = new FontIcon("fas-random");
        icon.getStyleClass().add("impact-header-icon");
        Label title = new Label("Impact Analysis");
        title.getStyleClass().add("graph-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        summaryLabel.getStyleClass().add("graph-count-label");
        titleRow.getChildren().addAll(icon, title, spacer, summaryLabel);

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        selectedLabel.getStyleClass().add("graph-selected-label");
        directionCombo.getItems().setAll(ImpactDirection.values());
        directionCombo.setValue(ImpactDirection.DEPENDENTS);
        directionCombo.getStyleClass().add("impact-control");
        depthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));
        depthSpinner.setEditable(true);
        depthSpinner.getStyleClass().add("impact-depth-spinner");
        runButton.getStyleClass().add("filter-clear-button");
        runButton.setOnAction(event -> runImpactAnalysis());
        controls.getChildren().addAll(
                selectedLabel,
                label("Direction"),
                directionCombo,
                label("Max depth"),
                depthSpinner,
                runButton
        );

        noticeLabel.getStyleClass().add("impact-notice-label");
        header.getChildren().addAll(titleRow, controls, noticeLabel);
        return header;
    }

    private Label label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("detail-field-label");
        return label;
    }

    private void configureTable() {
        table.getStyleClass().add("preview-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Run impact analysis to see affected paths."));
        setColumns(table, List.of(
                intColumn("Depth", 80, ImpactPath::depth),
                stringColumn("Direction", 120, path -> path.direction().name()),
                stringColumn("Endpoint CI", 180, path -> path.endpoint() == null ? "" : path.endpoint().getName()),
                stringColumn("Path", 360, this::pathText),
                stringColumn("Relationship Types", 220, this::relationshipTypes)
        ));
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.endpoint() != null) {
                endpointSelectionHandler.accept(newValue.endpoint().getId());
            }
        });
    }

    private void runImpactAnalysis() {
        if (selectedCiId == null || selectedCiId.isBlank()) {
            noticeLabel.setText("Select a CI before running impact analysis.");
            return;
        }
        runButton.setDisable(true);
        noticeLabel.getStyleClass().removeAll("impact-notice-warning");
        noticeLabel.setText("Running impact analysis...");
        requestHandler.analyze(
                selectedCiId,
                directionCombo.getValue(),
                depthSpinner.getValue(),
                this::showResult,
                error -> {
                    noticeLabel.getStyleClass().removeAll("impact-notice-warning");
                    noticeLabel.setText("Impact analysis failed: " + error.getMessage());
                },
                () -> runButton.setDisable(false)
        );
    }

    private void showResult(ImpactAnalysisResult result) {
        table.getItems().setAll(result.paths());
        summaryLabel.setText("Affected CIs: " + result.affectedCis().size()
                + " | Relationships: " + result.affectedRelationships().size()
                + " | Paths: " + result.paths().size());
        noticeLabel.setText(result.message());
        noticeLabel.getStyleClass().removeAll("impact-notice-warning");
        if (result.truncated()) {
            noticeLabel.getStyleClass().add("impact-notice-warning");
        }
    }

    private String pathText(ImpactPath path) {
        return path.configurationItems().stream()
                .map(item -> item.getName() == null || item.getName().isBlank() ? item.getId() : item.getName())
                .reduce((left, right) -> left + " -> " + right)
                .orElse("");
    }

    private String relationshipTypes(ImpactPath path) {
        return path.relationships().stream()
                .map(relationship -> relationship.getRelationshipType() == null
                        ? relationship.getRawRelationshipType()
                        : relationship.getRelationshipType())
                .filter(value -> value != null && !value.isBlank())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
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
