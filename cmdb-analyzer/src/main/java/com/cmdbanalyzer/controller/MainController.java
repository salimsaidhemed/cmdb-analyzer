package com.cmdbanalyzer.controller;

import com.cmdbanalyzer.analyzer.validation.CmdbValidationEngine;
import com.cmdbanalyzer.analyzer.validation.ValidationContext;
import com.cmdbanalyzer.analyzer.validation.ValidationResult;
import com.cmdbanalyzer.controller.preview.CmdbTableMapper;
import com.cmdbanalyzer.controller.preview.ImportPreviewViewFactory;
import com.cmdbanalyzer.controller.preview.ImportPreviewViewModel;
import com.cmdbanalyzer.graph.CmdbGraphBuilder;
import com.cmdbanalyzer.graph.GraphBuildResult;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.parser.ParseResult;
import com.cmdbanalyzer.service.AppTaskExecutor;
import com.cmdbanalyzer.service.CmdbImportService;
import com.cmdbanalyzer.service.RelationshipResolutionService;
import com.cmdbanalyzer.service.UiNotificationService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Handles UI events for the main application window.
 */
public class MainController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    private final AppTaskExecutor taskExecutor = new AppTaskExecutor();
    private final CmdbImportService importService = new CmdbImportService();
    private final RelationshipResolutionService relationshipResolutionService = new RelationshipResolutionService();
    private final CmdbValidationEngine validationEngine = new CmdbValidationEngine();
    private final CmdbGraphBuilder graphBuilder = new CmdbGraphBuilder();
    private final CmdbTableMapper tableMapper = new CmdbTableMapper();
    private UiNotificationService notificationService;
    private ImportPreviewViewFactory previewViewFactory;
    private CmdbWorkbook currentWorkbook;
    private ValidationResult latestValidationResult;
    private GraphBuildResult latestGraphBuildResult;

    @FXML
    private Button openButton;

    @FXML
    private Button analyzeButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label loadedFileLabel;

    @FXML
    private Label issueCountLabel;

    @FXML
    private Label datasetChipLabel;

    @FXML
    private ProgressIndicator taskProgressIndicator;

    @FXML
    private StackPane workspaceContent;

    @FXML
    private VBox detailsContent;

    @FXML
    private void initialize() {
        notificationService = new UiNotificationService(statusLabel, loadedFileLabel, issueCountLabel);
        previewViewFactory = new ImportPreviewViewFactory(
                this::showConfigurationItemDetails,
                this::showRelationshipDetails,
                this::showValidationIssueDetails
        );
        notificationService.showStatus("Ready");
        notificationService.showLoadedFile("No file loaded");
        notificationService.showIssueCount(0);
        showDefaultDetails();
        setTaskRunning(false);
    }

    @FXML
    private void handleOpen() {
        File selectedFile = showWorkbookChooser();
        if (selectedFile == null) {
            notificationService.showStatus("Open cancelled");
            return;
        }
        importWorkbook(selectedFile.toPath());
    }

    @FXML
    private void handleAnalyze() {
        // Future integration point: delegate CMDB validation and analysis to analyzer services.
        runPlaceholderTask("Analyze", "Preparing analysis workspace...");
    }

    @FXML
    private void handleExport() {
        // Future integration point: delegate reports and data exports to export services.
        notificationService.showStatus("Export action is not implemented yet");
    }

    @FXML
    private void handleRefresh() {
        runPlaceholderTask("Refresh", "Refreshing workspace...");
    }

    @FXML
    private void handleExit() {
        shutdown();
        statusLabel.getScene().getWindow().hide();
    }

    public void shutdown() {
        taskExecutor.shutdown();
    }

    private void runPlaceholderTask(String taskName, String initialStatus) {
        notificationService.showStatus(initialStatus);
        setTaskRunning(true);

        Task<String> task = taskExecutor.submitPlaceholderTask(taskName, Duration.ofMillis(900));

        statusLabel.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(event -> {
            statusLabel.textProperty().unbind();
            notificationService.showStatus(task.getValue());
            setTaskRunning(false);
        });

        task.setOnFailed(event -> {
            statusLabel.textProperty().unbind();
            LOGGER.error("{} placeholder task failed", taskName, task.getException());
            notificationService.showStatus(taskName + " failed");
            setTaskRunning(false);
        });
    }

    private File showWorkbookChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CMDB Workbook");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Workbooks (*.xlsx)", "*.xlsx")
        );
        Window owner = openButton.getScene() == null ? null : openButton.getScene().getWindow();
        return fileChooser.showOpenDialog(owner);
    }

    private void importWorkbook(Path workbookPath) {
        notificationService.showStatus("Loading workbook...");
        setTaskRunning(true);

        Task<ParseResult<ImportPreviewViewModel>> task = taskExecutor.submit(
                "Loading workbook",
                () -> {
                    ParseResult<CmdbWorkbook> parseResult = importService.importWorkbook(workbookPath);
                    if (!parseResult.success()) {
                        return ParseResult.failure(parseResult.errorMessage(), parseResult.warnings());
                    }
                    relationshipResolutionService.resolve(parseResult.result());
                    GraphBuildResult graphBuildResult = graphBuilder.build(parseResult.result());
                    ValidationResult validationResult = validationEngine.validate(new ValidationContext(
                            parseResult.result(),
                            graphBuildResult.graph(),
                            graphBuildResult
                    ));
                    return ParseResult.success(
                            tableMapper.toViewModel(parseResult.result(), validationResult, graphBuildResult),
                            parseResult.result().getParserWarnings()
                    );
                }
        );

        task.setOnSucceeded(event -> {
            ParseResult<ImportPreviewViewModel> result = task.getValue();
            if (result.success()) {
                ImportPreviewViewModel viewModel = result.result();
                currentWorkbook = viewModel.workbook();
                latestValidationResult = viewModel.validationResult();
                latestGraphBuildResult = viewModel.graphBuildResult();
                notificationService.showLoadedFile(workbookPath.getFileName().toString());
                notificationService.showIssueCount(viewModel.issueCount());
                notificationService.showStatus("Workbook loaded, validated, and graph built");
                renderImportPreview(viewModel);
            } else {
                notificationService.showStatus("Workbook could not be loaded");
                showImportError(result.errorMessage());
            }
            setTaskRunning(false);
        });

        task.setOnFailed(event -> {
            LOGGER.error("Workbook import task failed", task.getException());
            notificationService.showStatus("Workbook could not be loaded");
            showImportError("Unexpected import error: " + task.getException().getMessage());
            setTaskRunning(false);
        });
    }

    private void renderImportPreview(ImportPreviewViewModel viewModel) {
        datasetChipLabel.setText("Dataset: loaded");
        Node preview = previewViewFactory.create(viewModel);
        workspaceContent.getChildren().setAll(preview);
        showDefaultDetails();
    }

    private void showImportError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Workbook Import Failed");
        alert.setHeaderText("The workbook could not be parsed.");
        alert.setContentText(message == null ? "Unknown import error." : message);
        alert.showAndWait();
    }

    private void showDefaultDetails() {
        detailsContent.getChildren().setAll(
                detailCard("Selected CI", "None selected"),
                detailCard("Relationships", currentWorkbook == null ? "No relationship data loaded" : "Select a relationship row"),
                detailCard("Metadata", currentWorkbook == null ? "Dataset metadata placeholder" : "Workbook is ready for preview"),
                detailCard("Validation", latestValidationResult == null
                        ? "No validation run yet"
                        : latestValidationResult.totalIssueCount() + " issue(s) reported"),
                detailCard("Graph", latestGraphBuildResult == null
                        ? "No graph built yet"
                        : latestGraphBuildResult.vertexCount() + " vertices, "
                        + latestGraphBuildResult.edgeCount() + " edges")
        );
    }

    private void showConfigurationItemDetails(ImportPreviewViewModel.ConfigurationItemPreviewRow row) {
        VBox attributesCard = detailCard("Attributes", row.attributes().isEmpty() ? "No additional attributes" : "");
        if (!row.attributes().isEmpty()) {
            attributesCard.getChildren().remove(1);
            row.attributes().entrySet().stream()
                    .limit(8)
                    .map(entry -> valueLabel(entry.getKey() + ": " + safe(entry.getValue())))
                    .forEach(attributesCard.getChildren()::add);
        }

        detailsContent.getChildren().setAll(
                detailCard("Selected CI", safe(row.name()), "Class: " + safe(row.ciClass())),
                detailCard("Source", "Sheet: " + safe(row.sourceSheet()), "Row: " + row.sourceRow()),
                detailCard("Identity", safe(row.identityKey())),
                attributesCard
        );
    }

    private void showRelationshipDetails(ImportPreviewViewModel.RelationshipPreviewRow row) {
        detailsContent.getChildren().setAll(
                detailCard("Relationship", "Source: " + safe(row.sourceCiDisplay()), "Target: " + safe(row.targetName())),
                detailCard("Type", "Normalized: " + safe(row.relationshipType()), "Raw: " + safe(row.rawRelationshipType())),
                detailCard("Status", safe(row.status())),
                detailCard("Source", "Sheet: " + safe(row.sourceSheet()), "Row: " + row.sourceRow())
        );
    }

    private void showValidationIssueDetails(ImportPreviewViewModel.ValidationIssuePreviewRow row) {
        detailsContent.getChildren().setAll(
                detailCard("Validation Issue", safe(row.severity()), safe(row.type())),
                detailCard("Message", safe(row.message())),
                detailCard("Source", "Sheet: " + safe(row.sourceSheet()), "Row: " + safe(row.sourceRow())),
                detailCard("Affected Object",
                        "CI: " + safe(row.affectedCiId()),
                        "Relationship: " + safe(row.affectedRelationshipId())),
                detailCard("Recommended Action", safe(row.recommendedAction()))
        );
    }

    private VBox detailCard(String title, String... lines) {
        VBox card = new VBox(5);
        card.getStyleClass().add("detail-card");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("field-label");
        card.getChildren().add(titleLabel);
        for (String line : lines) {
            card.getChildren().add(valueLabel(line));
        }
        return card;
    }

    private Label valueLabel(String text) {
        Label label = new Label(safe(text));
        label.setWrapText(true);
        label.getStyleClass().add("field-value");
        return label;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void setTaskRunning(boolean running) {
        openButton.setDisable(running);
        analyzeButton.setDisable(running);
        exportButton.setDisable(running);
        refreshButton.setDisable(running);
        taskProgressIndicator.setVisible(running);
        taskProgressIndicator.setManaged(running);
    }
}
