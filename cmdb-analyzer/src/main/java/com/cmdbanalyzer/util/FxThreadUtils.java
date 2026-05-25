package com.cmdbanalyzer.util;

import javafx.application.Platform;

/**
 * Small JavaFX thread helpers for services that need to notify the UI safely.
 */
public final class FxThreadUtils {

    private FxThreadUtils() {
    }

    public static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
