package com.cmdbanalyzer.service;

import com.cmdbanalyzer.util.FxThreadUtils;
import javafx.scene.control.Label;

/**
 * Centralizes status bar notifications so service callbacks do not update UI controls unsafely.
 */
public class UiNotificationService {

    private final Label statusLabel;
    private final Label loadedFileLabel;
    private final Label issueCountLabel;

    public UiNotificationService(Label statusLabel, Label loadedFileLabel, Label issueCountLabel) {
        this.statusLabel = statusLabel;
        this.loadedFileLabel = loadedFileLabel;
        this.issueCountLabel = issueCountLabel;
    }

    public void showStatus(String message) {
        FxThreadUtils.runOnFxThread(() -> statusLabel.setText(message));
    }

    public void showLoadedFile(String message) {
        FxThreadUtils.runOnFxThread(() -> loadedFileLabel.setText(message));
    }

    public void showIssueCount(int issueCount) {
        FxThreadUtils.runOnFxThread(() -> issueCountLabel.setText("Issues: " + issueCount));
    }
}
