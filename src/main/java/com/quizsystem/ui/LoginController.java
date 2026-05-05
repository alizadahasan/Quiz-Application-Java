package com.quizsystem.ui;

import com.quizsystem.model.User;
import com.quizsystem.service.UserService;
import com.quizsystem.util.AppLogger;
import com.quizsystem.util.ThemeManager;
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
 * Controller for the login UI, handling user authentication and navigation to dashboards.
 * Manages user input for username and password, toggles password visibility, and applies theme styles.
 */
public class LoginController {
    /** Text field for entering the username. */
    @FXML private TextField usernameField;

    /** Password field for entering the password (hidden). */
    @FXML private PasswordField passwordField;

    /** Text field for displaying the password (visible when toggled). */
    @FXML private TextField visiblePasswordField;

    /** Checkbox to toggle password visibility. */
    @FXML private CheckBox showPasswordCheckBox;

    /** Label for displaying login success or error messages. */
    @FXML private Label messageLabel;

    /** Main application stage for scene transitions. */
    private Stage mainStage;

    /** Service for user authentication and registration. */
    private final UserService userService = new UserService();

    /** Main scene for applying stylesheets. */
    private Scene mainScene;

    /**
     * Initializes the controller, setting up password field visibility and listeners.
     */
    @FXML
    public void initialize() {
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
        ThemeManager.apply(scene);
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
     * Authenticates the user and navigates to the appropriate dashboard.
     *
     * @throws SQLException If a database error occurs during authentication.
     * @throws IOException If the dashboard FXML file cannot be loaded.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter username and password.", false);
            return;
        }

        try {
            User user = userService.login(username, password);

            if (user != null) {
                showMessage("Login successful!", true);

                if ("admin".equals(user.getRole())) {
                    loadAdminDashboard(user.getUserId());
                } else if ("user".equals(user.getRole())) {
                    loadUserDashboard(user.getUserId());
                } else {
                    showMessage("Invalid user role.", false);
                }
            } else {
                showMessage("Invalid username or password.", false);
            }
        } catch (SQLException e) {
            showMessage("Database error during login: " + e.getMessage(), false);
            AppLogger.error("Database error during login", e);
        } catch (IOException e) {
            showMessage("Error loading dashboard: " + e.getMessage(), false);
            AppLogger.error("Error loading dashboard", e);
        }
    }

    /**
     * Navigates to the registration screen.
     *
     * @throws IOException If the register.fxml file cannot be loaded.
     */
    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/register.fxml"));
            Parent root = loadFxml(loader);
            if (root == null) return;

            RegisterController controller = loader.getController();
            controller.setMainStage(mainStage, mainScene);
            prepareAndShowScene(root);
        } catch (IOException e) {
            showMessage("Error loading register screen: " + e.getMessage(), false);
            AppLogger.error("Error loading register screen", e);
        }
    }

    /**
     * Toggles between light and dark themes for the UI.
     */
    @FXML
    private void toggleDarkMode() {
        ThemeManager.toggle(getActiveScene());
        showMessage("Dark mode " + (ThemeManager.isDarkModeEnabled() ? "enabled" : "disabled") + ".", true);
    }

    /**
     * Loads the Admin Dashboard for the specified admin user.
     *
     * @param adminId The ID of the admin user.
     * @throws IOException If the admin.fxml file cannot be loaded.
     */
    private void loadAdminDashboard(int adminId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/admin.fxml"));
        Parent root = loadFxml(loader);
        if (root == null) return;

        AdminController controller = loader.getController();
        controller.setMainStage(mainStage, mainScene);
        controller.setAdminId(adminId);
        prepareAndShowScene(root);
    }

    /**
     * Loads the User Dashboard for the specified user.
     *
     * @param userId The ID of the user.
     * @throws IOException If the user.fxml file cannot be loaded.
     */
    private void loadUserDashboard(int userId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/user.fxml"));
        Parent root = loadFxml(loader);
        if (root == null) return;

        UserController controller = loader.getController();
        controller.setMainStage(mainStage, mainScene);
        controller.setUserId(userId);
        prepareAndShowScene(root);
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
            AppLogger.error(errorMessage);
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
        this.mainScene = newScene;
        applyStylesheets(newScene);
        mainStage.setScene(newScene);
        mainStage.setMaximized(true);
    }

    private Scene getActiveScene() {
        if (mainStage != null && mainStage.getScene() != null) {
            return mainStage.getScene();
        }
        return mainScene;
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
            AppLogger.warn("MessageLabel is null. Message: " + message);
        }
    }
}
