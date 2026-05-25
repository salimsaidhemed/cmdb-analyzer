package com.cmdbanalyzer.app;

import com.cmdbanalyzer.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * JavaFX entry point for the CMDB Analyzer desktop application.
 */
public class CmdbAnalyzerApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL mainWindow = CmdbAnalyzerApp.class.getResource("/fxml/MainWindow.fxml");
        FXMLLoader loader = new FXMLLoader(mainWindow);
        Parent root = loader.load();
        MainController controller = loader.getController();

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(
                CmdbAnalyzerApp.class.getResource("/css/application.css").toExternalForm()
        );

        stage.setTitle("CMDB Analyzer");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> controller.shutdown());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
