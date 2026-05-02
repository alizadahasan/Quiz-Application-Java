package com.quizsystem.ui;

import com.quizsystem.service.UserService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for the registration UI, handling user registration and navigation.
 * Manages user input for username, password, email, and role, and persists new users to the database.
 */
public class RegisterController {
    /** Text field for entering the username. */
    @FXML private TextField usernameField;

    /** Password field for entering the password (hidden). */
    @FXML private PasswordField passwordField;

    /** Text field for displaying the password (visible when toggled). */
    @FXML private TextField visiblePasswordField;

    /** Checkbox to toggle password visibility. */
    @FXML private CheckBox showPasswordCheckBox;

    /** Text field for entering the email address. */
    @FXML private TextField emailField;

    /** Choice box for selecting the user role (user or admin). */
    @FXML private ChoiceBox<String> roleChoiceBox;

    /** Label for displaying success or error messages. */
    @FXML private Label messageLabel;

    /** Main application stage for scene transitions. */
    private Stage mainStage;

    /** Service for user persistence. */
    private final UserService userService = new UserService();

    /** Main scene for applying stylesheets. */
    private Scene mainScene;

    /** Flag indicating if dark mode is enabled. */
    private boolean isDarkMode = false;

    /** Path to the light theme CSS file. */
    private final String LIGHT_CSS = "/com/quizsystem/ui/styles.css";

    /** Path to the dark theme CSS file. */
    private final String DARK_CSS = "/com/quizsystem/ui/dark-mode.css";

    /**
     * Initializes the controller, setting up the role choice box and password field visibility.
     */
    @FXML
    public void initialize() {
        roleChoiceBox.setItems(FXCollections.observableArrayList("user", "admin"));
        roleChoiceBox.setValue("user");
        setupPasswordFieldsVisibility(false);
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (passwordField.isFocused() || !showPasswordCheckBox.isSelected()) {
                visiblePasswordField.setText(newValue);
            }
        });
        visiblePasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (visiblePasswordField.isFocused() || showPasswordCheckBox.isSelected()) {
                passwordField.setText(newValue);
            }
        });
    }

    /**
     * Sets the main stage and scene for the controller.
     *
     * @param stage The main application stage.
     * @param scene The main scene for stylesheet application.
     */
    public void setMainStage(Stage stage, Scene scene) {
        this.mainStage = stage;
        this.mainScene = scene;
        applyStylesheets(this.mainScene);
    }

    /**
     * Applies light or dark theme stylesheets to the specified scene.
     *
     * @param scene The scene to apply stylesheets to.
     */
    private void applyStylesheets(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        String stylesheet = isDarkMode ? DARK_CSS : LIGHT_CSS;
        String stylesheetPath = getClass().getResource(stylesheet).toExternalForm();
        if (stylesheetPath != null) {
            scene.getStylesheets().add(stylesheetPath);
        } else {
            System.err.println("Warning: " + stylesheet + " not found.");
        }
    }

    /**
     * Configures the visibility of password fields based on the show password checkbox.
     *
     * @param showPlainText True to show the password in plain text, false to hide it.
     */
    private void setupPasswordFieldsVisibility(boolean showPlainText) {
        if (showPlainText) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
        }
    }

    /**
     * Toggles the visibility of the password field based on the checkbox state.
     *
     * @param event The action event from the checkbox.
     */
    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        setupPasswordFieldsVisibility(showPasswordCheckBox.isSelected());
        if (showPasswordCheckBox.isSelected()) {
            visiblePasswordField.requestFocus();
        } else {
            passwordField.requestFocus();
        }
    }

    /**
     * Handles user registration by validating input and saving to the database.
     *
     * @throws SQLException If a database error occurs during registration.
     */
    @FXML
    private void handleRegistrationSubmit() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleChoiceBox.getValue();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || role == null) {
            showMessage("Please fill in all fields.", false);
            return;
        }

        try {
            userService.register(username, password, role, email);
            showMessage("Registration successful! Returning to login...", true);
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(this::handleBackToLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (SQLException e) {
            showMessage("Database error: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    /**
     * Navigates back to the login screen.
     *
     * @throws IOException If the login.fxml file cannot be loaded.
     */
    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/login.fxml"));
            Parent root = loadFxml(loader);
            if (root == null) return;

            LoginController controller = loader.getController();
            controller.setMainStage(mainStage, mainScene);
            prepareAndShowScene(root);
        } catch (IOException e) {
            showMessage("Error loading login screen: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    /**
     * Toggles between light and dark themes for the UI.
     */
    @FXML
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        applyStylesheets(mainScene);
        showMessage("Dark mode " + (isDarkMode ? "enabled" : "disabled") + ".", true);
    }

    /**
     * Loads an FXML file and returns its root node.
     *
     * @param loader The FXMLLoader for the FXML file.
     * @return The root node of the loaded FXML, or null if loading fails.
     * @throws IOException If the FXML file cannot be loaded.
     */
    private Parent loadFxml(FXMLLoader loader) throws IOException {
        if (loader.getLocation() == null) {
            String errorMessage = "Cannot load FXML file: " + loader.getLocation();
            showMessage(errorMessage, false);
            System.err.println(errorMessage);
            return null;
        }
        return loader.load();
    }

    /**
     * Prepares and displays a new scene with applied stylesheets.
     *
     * @param root The root node of the new scene.
     */
    private void prepareAndShowScene(Parent root) {
        Scene newScene = new Scene(root);
        applyStylesheets(newScene);
        mainStage.setScene(newScene);
        mainStage.setMaximized(true);
    }

    /**
     * Displays a message in the UI with appropriate styling.
     *
     * @param message The message to display.
     * @param success True for success (green), false for error (red).
     */
    private void showMessage(String message, boolean success) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle(success ? "-fx-text-fill: green; -fx-font-weight: bold;" : "-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            System.out.println("MessageLabel is null. Message: " + message);
        }
    }
}
