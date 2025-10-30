package com.cmdb.analyzer.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Simple JavaFX test to verify WSL display + JavaFX runtime.
 */
public class TestFX extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("JavaFX is working in WSL!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 400, 200);
        stage.setTitle("CMDB Analyzer - JavaFX Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
