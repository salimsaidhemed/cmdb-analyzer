package com.cmdbanalyzer.controller;

import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ParserWarning;
import com.cmdbanalyzer.parser.ParseResult;
import com.cmdbanalyzer.service.AppTaskExecutor;
import com.cmdbanalyzer.service.CmdbImportService;
import com.cmdbanalyzer.service.UiNotificationService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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
import java.util.List;

/**
 * Handles UI events for the main application window.
 */
public class MainController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    private final AppTaskExecutor taskExecutor = new AppTaskExecutor();
    private final CmdbImportService importService = new CmdbImportService();
    private UiNotificationService notificationService;
    private CmdbWorkbook currentWorkbook;

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
    private void initialize() {
        notificationService = new UiNotificationService(statusLabel, loadedFileLabel, issueCountLabel);
        notificationService.showStatus("Ready");
        notificationService.showLoadedFile("No file loaded");
        notificationService.showIssueCount(0);
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

        Task<ParseResult<CmdbWorkbook>> task = taskExecutor.submit(
                "Loading workbook",
                () -> importService.importWorkbook(workbookPath)
        );

        task.setOnSucceeded(event -> {
            ParseResult<CmdbWorkbook> result = task.getValue();
            if (result.success()) {
                currentWorkbook = result.result();
                int warningCount = result.warnings().size();
                notificationService.showLoadedFile(workbookPath.getFileName().toString());
                notificationService.showIssueCount(warningCount);
                notificationService.showStatus("Workbook loaded");
                renderWorkbookSummary(workbookPath.getFileName().toString(), currentWorkbook, result.warnings());
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

    private void renderWorkbookSummary(String fileName, CmdbWorkbook workbook, List<ParserWarning> warnings) {
        int sheetCount = workbook.getSheets().size();
        int ciCount = workbook.getSheets().stream()
                .mapToInt(sheet -> sheet.getConfigurationItems().size())
                .sum();
        int relationshipCount = workbook.getSheets().stream()
                .mapToInt(sheet -> sheet.getRelationships().size())
                .sum();

        datasetChipLabel.setText("Dataset: loaded");

        VBox summary = new VBox(12);
        summary.setAlignment(Pos.CENTER_LEFT);
        summary.setMaxWidth(640);
        summary.getStyleClass().add("summary-card");

        Label title = new Label("Workbook loaded");
        title.getStyleClass().add("empty-title");

        summary.getChildren().addAll(
                title,
                summaryLine("File", fileName),
                summaryLine("Sheets", String.valueOf(sheetCount)),
                summaryLine("Parsed CIs", String.valueOf(ciCount)),
                summaryLine("Relationships", String.valueOf(relationshipCount)),
                summaryLine("Parser warnings", String.valueOf(warnings.size()))
        );

        warnings.stream()
                .limit(5)
                .map(this::warningLine)
                .forEach(summary.getChildren()::add);

        workspaceContent.getChildren().setAll(summary);
    }

    private Label summaryLine(String label, String value) {
        Label line = new Label(label + ": " + value);
        line.getStyleClass().add("summary-line");
        return line;
    }

    private Label warningLine(ParserWarning warning) {
        String location = warning.getSheet() == null ? "" : warning.getSheet();
        String row = warning.getRow() == null ? "" : " row " + warning.getRow();
        Label line = new Label("Warning: " + location + row + " - " + warning.getMessage());
        line.setWrapText(true);
        line.getStyleClass().add("warning-line");
        return line;
    }

    private void showImportError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Workbook Import Failed");
        alert.setHeaderText("The workbook could not be parsed.");
        alert.setContentText(message == null ? "Unknown import error." : message);
        alert.showAndWait();
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
