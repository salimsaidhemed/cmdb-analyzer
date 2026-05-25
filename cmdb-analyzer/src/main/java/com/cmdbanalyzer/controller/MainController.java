package com.cmdbanalyzer.controller;

import com.cmdbanalyzer.service.AppTaskExecutor;
import com.cmdbanalyzer.service.UiNotificationService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Handles UI events for the main application window.
 */
public class MainController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    private final AppTaskExecutor taskExecutor = new AppTaskExecutor();
    private UiNotificationService notificationService;

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
    private ProgressIndicator taskProgressIndicator;

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
        // Future integration point: delegate Excel import to parser/service layer.
        notificationService.showStatus("Open file action is not implemented yet");
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

    private void setTaskRunning(boolean running) {
        openButton.setDisable(running);
        analyzeButton.setDisable(running);
        exportButton.setDisable(running);
        refreshButton.setDisable(running);
        taskProgressIndicator.setVisible(running);
        taskProgressIndicator.setManaged(running);
    }
}
