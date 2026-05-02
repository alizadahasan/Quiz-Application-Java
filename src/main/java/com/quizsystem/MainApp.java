package com.quizsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.quizsystem.ui.LoginController;
import com.quizsystem.util.DatabaseConnection;
import com.quizsystem.util.ThemeManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

/**
 * Main application class for launching the Quiz System JavaFX application.
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
        try {
            DatabaseConnection.initializeDatabase();
        } catch (SQLException e) {
            throw new IOException("Failed to initialize database", e);
        }

        URL loginResource = getClass().getResource("/com/quizsystem/ui/login.fxml");
        if (loginResource == null) {
            System.err.println("Error: login.fxml not found at /com/quizsystem/ui/login.fxml");
            throw new IOException("Cannot find login.fxml");
        }
        FXMLLoader loader = new FXMLLoader(loginResource);
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600); // Increased default size
        ThemeManager.apply(scene);

        LoginController controller = loader.getController();
        controller.setMainStage(primaryStage, scene);

        primaryStage.setTitle("Quiz System");
        primaryStage.setResizable(true); // Allow resizing/maximizing
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Main entry point for the Quiz System application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
