package com.quizsystem.ui;

import com.quizsystem.model.Question;
import com.quizsystem.service.QuestionService;
import com.quizsystem.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for the View Questions UI, displaying questions for a specific quiz.
 * Allows admins to view all questions associated with a quiz.
 */
public class ViewQuestionsController {
    /** List view displaying the questions for the quiz. */
    @FXML private ListView<Question> questionListView;

    /** Label for displaying success or error messages. */
    @FXML private Label messageLabel;

    /** Main application stage for scene transitions. */
    private Stage mainStage;

    /** Main scene for applying stylesheets. */
    private Scene mainScene;

    /** ID of the quiz whose questions are displayed. */
    private int quizId;

    /** ID of the admin user. */
    private int adminId;

    /** Service for question-related database operations. */
    private QuestionService questionService;

    /**
     * Initializes the controller, setting up the question service and list view.
     */
    @FXML
    public void initialize() {
        questionService = new QuestionService();
        setupQuestionListView();
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
     * Sets the ID of the quiz and loads its questions.
     *
     * @param quizId The ID of the quiz.
     */
    public void setQuizId(int quizId) {
        this.quizId = quizId;
        loadQuestions();
    }

    /**
     * Sets the ID of the admin user.
     *
     * @param adminId The ID of the admin user.
     */
    public void setAdminId(int adminId) {
        this.adminId = adminId;
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
     * Configures the questionListView to display question details.
     */
    private void setupQuestionListView() {
        questionListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Question question, boolean empty) {
                super.updateItem(question, empty);
                if (empty || question == null) {
                    setText(null);
                } else {
                    setText(question.getQuestionId() + ": " + question.getQuestionText() +
                            "\nA: " + question.getOptionA() +
                            "\nB: " + question.getOptionB() +
                            "\nC: " + question.getOptionC() +
                            "\nD: " + question.getOptionD() +
                            "\nCorrect: " + question.getCorrectAnswer());
                }
            }
        });
    }

    /**
     * Loads all questions for the current quiz into the questionListView.
     */
    private void loadQuestions() {
        questionListView.getItems().clear();
        try {
            questionListView.getItems().addAll(questionService.getQuestionsByQuizId(quizId));
            if (questionListView.getItems().isEmpty()) {
                showMessage("No questions found for this quiz.", false);
            }
        } catch (SQLException e) {
            showMessage("Error loading questions: " + e.getMessage(), false);
            System.err.println("SQLException in loadQuestions: " + e.getMessage());
        }
    }

    /**
     * Navigates back to the admin dashboard.
     *
     * @throws IOException If the admin.fxml file cannot be loaded.
     */
    @FXML
    private void handleBackToDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/admin.fxml"));
        if (loader.getLocation() == null) {
            showMessage("Cannot load admin dashboard.", false);
            System.err.println("Error: admin.fxml not found");
            return;
        }
        Parent root = loader.load();
        AdminController controller = loader.getController();
        controller.setMainStage(mainStage, mainScene);
        controller.setAdminId(adminId);
        Scene newScene = new Scene(root);
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
