package com.quizsystem.ui;

import com.quizsystem.model.Question;
import com.quizsystem.model.Result;
import com.quizsystem.service.QuestionService;
import com.quizsystem.service.ResultService;
import com.quizsystem.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for the Results UI, displaying quiz results and user answers.
 * Loads result details and associated questions, showing scores and correctness.
 */
public class ResultsController {
    /** Label displaying the result summary (score and completion time). */
    @FXML private Label summaryLabel;

    /** List view displaying detailed results for each question. */
    @FXML private ListView<String> resultsListView;

    /** Label for displaying success or error messages. */
    @FXML private Label messageLabel;

    /** Main application stage for scene transitions. */
    private Stage mainStage;

    /** Main scene for applying stylesheets. */
    private Scene mainScene;

    /** ID of the result to display. */
    private int resultId;

    /** Service for result-related database operations. */
    private ResultService resultService;

    /** Service for question-related database operations. */
    private QuestionService questionService;

    /**
     * Initializes the controller, setting up the result and question services.
     */
    @FXML
    public void initialize() {
        resultService = new ResultService();
        questionService = new QuestionService();
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
     * Sets the ID of the result to display and loads the results.
     *
     * @param resultId The ID of the result.
     */
    public void setResultId(int resultId) {
        this.resultId = resultId;
        loadResults();
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
     * Loads and displays the result details and associated questions.
     *
     * @throws SQLException If a database error occurs while loading results.
     */
    private void loadResults() {
        try {
            Result result = resultService.getResultById(resultId);
            if (result == null) {
                showMessage("Result not found.", false);
                return;
            }
            List<Question> questions = questionService.getQuestionsByQuizId(result.getQuizId());
            summaryLabel.setText("Score: " + result.getScore() + "/" + questions.size() + " | Completed: " + result.getCompletionTime());
            resultsListView.getItems().clear();
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                String userAnswer = i < result.getUserAnswers().size() ? result.getUserAnswers().get(i) : "Not answered";
                String status = userAnswer.equals(String.valueOf(q.getCorrectAnswer())) ? "Correct" : "Incorrect";
                resultsListView.getItems().add(
                        (i + 1) + ". " + q.getQuestionText() +
                                "\nA: " + q.getOptionA() +
                                "\nB: " + q.getOptionB() +
                                "\nC: " + q.getOptionC() +
                                "\nD: " + q.getOptionD() +
                                "\nYour Answer: " + userAnswer +
                                "\nCorrect Answer: " + q.getCorrectAnswer() +
                                "\nStatus: " + status
                );
            }
        } catch (SQLException e) {
            showMessage("Error loading results: " + e.getMessage(), false);
            System.err.println("SQLException in loadResults: " + e.getMessage());
        }
    }

    /**
     * Navigates back to the user dashboard.
     *
     * @throws IOException If the user.fxml file cannot be loaded.
     * @throws SQLException If a database error occurs while retrieving the user ID.
     */
    @FXML
    private void handleBackToDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/user.fxml"));
        if (loader.getLocation() == null) {
            showMessage("Cannot load user dashboard.", false);
            return;
        }
        Parent root = loader.load();
        UserController controller = loader.getController();
        try {
            Result result = resultService.getResultById(resultId);
            if (result == null) {
                showMessage("Cannot retrieve user ID for result.", false);
                return;
            }
            Scene newScene = new Scene(root);
            controller.setMainStage(mainStage, newScene);
            controller.setUserId(result.getUserId());
            applyStylesheets(newScene);
            mainStage.setScene(newScene);
            mainStage.setMaximized(true);
        } catch (SQLException e) {
            showMessage("Error retrieving user ID: " + e.getMessage(), false);
            System.err.println("SQLException in handleBackToDashboard: " + e.getMessage());
            return;
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
