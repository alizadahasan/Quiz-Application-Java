package com.quizsystem.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main entry point for the Quiz System application.
 * Initializes the JavaFX application and loads the login screen.
 */
public class MainApp extends Application {

    /**
     * Starts the JavaFX application by loading the login screen.
     *
     * @param primaryStage The primary stage for the application.
     * @throws IOException If the login.fxml file cannot be loaded.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 600, 400);
        String defaultStylesheet = getClass().getResource("styles.css").toExternalForm();
        scene.getStylesheets().add(defaultStylesheet);

        LoginController controller = loader.getController();
        controller.setMainStage(primaryStage, scene);

        primaryStage.setTitle("Quiz System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Main method to launch the JavaFX application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}