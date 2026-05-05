package com.quizsystem.ui;

import com.quizsystem.model.LeaderboardEntry;
import com.quizsystem.model.Quiz;
import com.quizsystem.model.QuizHistoryEntry;
import com.quizsystem.model.Result;
import com.quizsystem.service.QuizService;
import com.quizsystem.service.ResultService;
import com.quizsystem.util.AppLogger;
import com.quizsystem.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for the User Dashboard UI, managing quiz selection, history, and leaderboard display.
 * Handles user interactions for taking quizzes, viewing results, and logging out.
 */
public class UserController {
    /** List view displaying available quizzes. */
    @FXML private ListView<Quiz> quizListView;

    /** List view displaying the user’s quiz history. */
    @FXML private ListView<HistoryItem> historyListView;

    /** List view displaying the global leaderboard. */
    @FXML private ListView<String> leaderboardListView;

    /** Label for displaying success or error messages. */
    @FXML private Label messageLabel;

    /** Main application stage for scene transitions. */
    private Stage mainStage;

    /** Main scene for applying stylesheets. */
    private Scene mainScene;

    /** ID of the logged-in user. */
    private int userId;

    /** Service for quiz-related database operations. */
    private QuizService quizService;

    /** Service for result-related database operations. */
    private ResultService resultService;

    /**
     * Inner class representing a quiz history item for display in the historyListView.
     */
    private static class HistoryItem {
        /** ID of the result. */
        int resultId;

        /** Text to display in the history list view. */
        String displayText;

        /**
         * Constructs a HistoryItem with the specified result ID and display text.
         *
         * @param resultId    The ID of the result.
         * @param displayText The text to display in the history list.
         */
        HistoryItem(int resultId, String displayText) {
            this.resultId = resultId;
            this.displayText = displayText;
        }

        /**
         * Returns the display text for the history item.
         *
         * @return The display text.
         */
        @Override
        public String toString() {
            return displayText;
        }
    }

    /**
     * Initializes the controller, setting up services and UI components.
     */
    @FXML
    public void initialize() {
        quizService = new QuizService();
        resultService = new ResultService();
        setupQuizListView();
        setupHistoryListView();
        loadQuizzes();
        loadQuizHistory();
        loadLeaderboard();
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
        applyStylesheets(mainScene);
    }

    /**
     * Sets the ID of the logged-in user and refreshes the quiz history and leaderboard.
     *
     * @param userId The ID of the user.
     */
    public void setUserId(int userId) {
        this.userId = userId;
        loadQuizHistory();
        loadLeaderboard();
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
     * Configures the quizListView to display quizzes with a "Take Quiz" button.
     */
    private void setupQuizListView() {
        quizListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Quiz quiz, boolean empty) {
                super.updateItem(quiz, empty);
                if (empty || quiz == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(quiz.getQuizId() + ": " + quiz.getTitle() + " (" + quiz.getDescription() + ", " + quiz.getTimeLimit() + "s)");
                    Button takeQuizButton = new Button("Take Quiz");
                    takeQuizButton.setOnAction(event -> {
                        try {
                            handleTakeQuiz(quiz.getQuizId());
                        } catch (IOException e) {
                            showMessage("Error loading quiz: " + e.getMessage(), false);
                            AppLogger.error("Error loading quiz", e);
                        }
                    });
                    HBox hBox = new HBox(10, label, takeQuizButton);
                    setGraphic(hBox);
                }
            }
        });
    }

    /**
     * Configures the historyListView to display quiz history with a "View Results" button.
     */
    private void setupHistoryListView() {
        historyListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(HistoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(item.displayText);
                    Button viewResultsButton = new Button("View Results");
                    viewResultsButton.setOnAction(event -> {
                        try {
                            handleViewResults(item.resultId);
                        } catch (IOException e) {
                            showMessage("Error loading results: " + e.getMessage(), false);
                            AppLogger.error("Error loading results", e);
                        }
                    });
                    HBox hBox = new HBox(10, label, viewResultsButton);
                    setGraphic(hBox);

                    // Update text color based on selection state and dark mode
                    selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                        boolean isDarkMode = ThemeManager.isDarkModeEnabled();
                        if (isSelected) {
                            label.setStyle(isDarkMode ? "-fx-text-fill: white;" : "-fx-text-fill: black;");
                        } else {
                            label.setStyle(isDarkMode ? "-fx-text-fill: white;" : "-fx-text-fill: black;");
                        }
                    });
                }
            }
        });
    }

    /**
     * Navigates to the quiz UI for the specified quiz.
     *
     * @param quizId The ID of the quiz to take.
     * @throws IOException If the quiz.fxml file cannot be loaded.
     */
    private void handleTakeQuiz(int quizId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/quiz.fxml"));
        if (loader.getLocation() == null) {
            AppLogger.error("FXML resource not found: /com/quizsystem/ui/quiz.fxml");
            showMessage("Cannot load quiz form.", false);
            return;
        }
        Parent root = loader.load();
        QuizController controller = loader.getController();
        controller.setQuizId(quizId);
        controller.setUserId(userId);
        Scene newScene = new Scene(root);
        controller.setMainStage(mainStage, newScene);
        applyStylesheets(newScene);
        mainStage.setScene(newScene);
        mainStage.setMaximized(true);
    }

    /**
     * Navigates to the results UI for the specified result.
     *
     * @param resultId The ID of the result to view.
     * @throws IOException If the results.fxml file cannot be loaded.
     */
    private void handleViewResults(int resultId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/results.fxml"));
        if (loader.getLocation() == null) {
            showMessage("Cannot load results view.", false);
            return;
        }
        Parent root = loader.load();
        ResultsController controller = loader.getController();
        controller.setResultId(resultId);
        Scene newScene = new Scene(root);
        controller.setMainStage(mainStage, newScene);
        applyStylesheets(newScene);
        mainStage.setScene(newScene);
        mainStage.setMaximized(true);
    }

    /**
     * Logs out the user and returns to the login screen.
     *
     * @throws IOException If the login.fxml file cannot be loaded.
     */
    @FXML
    private void handleLogout() throws IOException {
        if (mainStage == null) {
            showMessage("Cannot load login form: stage not initialized.", false);
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/login.fxml"));
        if (loader.getLocation() == null) {
            showMessage("Cannot load login form.", false);
            return;
        }
        Parent root = loader.load();
        LoginController controller = loader.getController();
        Scene newScene = new Scene(root);
        controller.setMainStage(mainStage, newScene);
        applyStylesheets(newScene);
        mainStage.setScene(newScene);
        mainStage.setMaximized(true);
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
     * Loads all available quizzes into the quizListView.
     */
    private void loadQuizzes() {
        quizListView.getItems().clear();
        try {
            quizListView.getItems().addAll(quizService.getAllQuizzes());
        } catch (SQLException e) {
            showMessage("Error loading quizzes: " + e.getMessage(), false);
            AppLogger.error("Error loading quizzes", e);
        }
    }

    /**
     * Loads the user’s quiz history into the historyListView.
     */
    private void loadQuizHistory() {
        historyListView.getItems().clear();
        try {
            for (QuizHistoryEntry entry : resultService.getQuizHistoryForUser(userId)) {
                String displayText = "Quiz: " + entry.quizTitle() + " | Score: " + entry.score() +
                        " | Date: " + entry.completionTime();
                historyListView.getItems().add(new HistoryItem(entry.resultId(), displayText));
            }
        } catch (SQLException e) {
            showMessage("Error loading quiz history: " + e.getMessage(), false);
            AppLogger.error("Error loading quiz history", e);
        }
    }

    /**
     * Loads the global leaderboard into the leaderboardListView.
     */
    private void loadLeaderboard() {
        leaderboardListView.getItems().clear();
        try {
            int rank = 1;
            for (LeaderboardEntry entry : resultService.getGlobalLeaderboard()) {
                leaderboardListView.getItems().add(
                        rank++ + ". " + entry.username() + ": " + entry.score());
            }
        } catch (SQLException e) {
            showMessage("Error loading leaderboard: " + e.getMessage(), false);
            AppLogger.error("Error loading leaderboard", e);
        }
    }

    /**
     * Displays a message in the UI with appropriate styling.
     *
     * @param message The message to display.
     * @param success True for success (green), false for error (red).
     */
    private void showMessage(String message, boolean success) {
        messageLabel.setText(message);
        messageLabel.setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    private Scene getActiveScene() {
        if (mainStage != null && mainStage.getScene() != null) {
            return mainStage.getScene();
        }
        return mainScene;
    }
}
