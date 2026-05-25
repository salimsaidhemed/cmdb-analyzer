package com.cmdbanalyzer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Handles UI events for the main application window.
 */
public class MainController {

    @FXML
    private Label statusLabel;

    @FXML
    private Label loadedFileLabel;

    @FXML
    private Label issueCountLabel;

    @FXML
    private void initialize() {
        setStatus("Ready");
        loadedFileLabel.setText("No file loaded");
        issueCountLabel.setText("Issues: 0");
    }

    @FXML
    private void handleOpen() {
        // Future integration point: delegate Excel import to parser/service layer.
        setStatus("Open file action is not implemented yet");
    }

    @FXML
    private void handleAnalyze() {
        // Future integration point: delegate CMDB validation and analysis to analyzer services.
        setStatus("Analysis action is not implemented yet");
    }

    @FXML
    private void handleExport() {
        // Future integration point: delegate reports and data exports to export services.
        setStatus("Export action is not implemented yet");
    }

    @FXML
    private void handleRefresh() {
        setStatus("Workspace refreshed");
    }

    @FXML
    private void handleExit() {
        statusLabel.getScene().getWindow().hide();
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }
}
