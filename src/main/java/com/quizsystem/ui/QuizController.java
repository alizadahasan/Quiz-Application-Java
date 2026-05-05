package com.quizsystem.ui;

import com.quizsystem.model.Question;
import com.quizsystem.service.QuestionService;
import com.quizsystem.service.ResultService;
import com.quizsystem.util.AppLogger;
import com.quizsystem.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Quiz UI, handling the display and submission of quiz questions.
 * Manages question navigation, user answers, and result submission with feedback.
 */
public class QuizController {
    /** Label displaying the quiz title or ID. */
    @FXML private Label quizTitleLabel;

    /** Label displaying the current question text. */
    @FXML private Label questionLabel;

    /** Label displaying feedback after quiz submission. */
    @FXML private Label feedbackLabel;

    /** Radio button for option A. */
    @FXML private RadioButton optionA;

    /** Radio button for option B. */
    @FXML private RadioButton optionB;

    /** Radio button for option C. */
    @FXML private RadioButton optionC;

    /** Radio button for option D. */
    @FXML private RadioButton optionD;

    /** Button to navigate to the previous question. */
    @FXML private Button prevButton;

    /** Button to navigate to the next question. */
    @FXML private Button nextButton;

    /** Button to submit the quiz. */
    @FXML private Button submitButton;

    /** Button to toggle between light and dark themes. */
    @FXML private Button toggleDarkModeButton;

    /** Button to return to the user dashboard. */
    @FXML private Button backButton;

    /** Label for displaying success or error messages. */
    @FXML private Label messageLabel;

    /** Toggle group for answer radio buttons. */
    @FXML private ToggleGroup answerGroup;

    /** Main application stage for scene transitions. */
    private Stage mainStage;

    /** Main scene for applying stylesheets. */
    private Scene mainScene;

    /** ID of the current quiz. */
    private int quizId;

    /** ID of the user taking the quiz. */
    private int userId;

    /** Service for question-related database operations. */
    private QuestionService questionService;

    /** Service for result-related database operations. */
    private ResultService resultService;

    /** List of questions for the current quiz. */
    private List<Question> questions;

    /** List of user-selected answers. */
    private List<String> userAnswers;

    /** List of feedback for each question (Correct/Incorrect). */
    private List<String> feedback;

    /** Index of the currently displayed question. */
    private int currentQuestionIndex = 0;

    /** Flag indicating if the quiz has been submitted. */
    private boolean isSubmitted = false;

    /**
     * Initializes the controller, setting up services and listeners for answer selection.
     */
    @FXML
    public void initialize() {
        questionService = new QuestionService();
        resultService = new ResultService();
        questions = new ArrayList<>();
        userAnswers = new ArrayList<>();
        feedback = new ArrayList<>();
        answerGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (!isSubmitted && newToggle != null) { // Only update answers before submission
                RadioButton selected = (RadioButton) newToggle;
                userAnswers.set(currentQuestionIndex, selected.getText().substring(0, 1));
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
        applyStylesheets(mainScene);
    }

    /**
     * Sets the ID of the current quiz and loads its questions.
     *
     * @param quizId The ID of the quiz.
     */
    public void setQuizId(int quizId) {
        this.quizId = quizId;
        loadQuestions();
    }

    /**
     * Sets the ID of the user taking the quiz.
     *
     * @param userId The ID of the user.
     */
    public void setUserId(int userId) {
        this.userId = userId;
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
     * Loads questions for the current quiz from the database.
     */
    private void loadQuestions() {
        try {
            questions = questionService.getQuestionsByQuizId(quizId);
            userAnswers.clear();
            feedback.clear();
            for (int i = 0; i < questions.size(); i++) {
                userAnswers.add("");
                feedback.add("");
            }
            if (questions.isEmpty()) {
                showMessage("No questions found for this quiz.", false);
                prevButton.setDisable(true);
                nextButton.setDisable(true);
                submitButton.setDisable(true);
            } else {
                quizTitleLabel.setText("Quiz ID: " + quizId);
                displayQuestion(0);
            }
        } catch (SQLException e) {
            showMessage("Error loading questions: " + e.getMessage(), false);
            AppLogger.error("Error loading questions", e);
        }
    }

    /**
     * Displays the question at the specified index.
     *
     * @param index The index of the question to display.
     */
    private void displayQuestion(int index) {
        currentQuestionIndex = index;
        Question q = questions.get(index);
        questionLabel.setText((index + 1) + ". " + q.getQuestionText());
        optionA.setText("A: " + q.getOptionA());
        optionB.setText("B: " + q.getOptionB());
        optionC.setText("C: " + q.getOptionC());
        optionD.setText("D: " + q.getOptionD());
        prevButton.setDisable(index == 0);
        nextButton.setDisable(index == questions.size() - 1);
        submitButton.setDisable(questions.isEmpty() || isSubmitted);

        // Set RadioButton states
        String answer = userAnswers.get(index);
        answerGroup.getToggles().forEach(toggle -> {
            RadioButton rb = (RadioButton) toggle;
            rb.setSelected(answer.equals(rb.getText().substring(0, 1)));
            rb.setDisable(isSubmitted); // Disable after submission
        });

        // Show feedback if submitted
        feedbackLabel.setText(isSubmitted ? feedback.get(index) : "");
    }

    /**
     * Navigates to the previous question in the quiz.
     */
    @FXML
    private void handlePreviousQuestion() {
        if (currentQuestionIndex > 0) {
            displayQuestion(currentQuestionIndex - 1);
        }
    }

    /**
     * Navigates to the next question in the quiz.
     */
    @FXML
    private void handleNextQuestion() {
        if (currentQuestionIndex < questions.size() - 1) {
            displayQuestion(currentQuestionIndex + 1);
        }
    }

    /**
     * Submits the quiz, calculates the score, and saves the result.
     *
     * @throws SQLException If a database error occurs during result submission.
     */
    @FXML
    private void handleSubmitQuiz() {
        try {
            // Validate that all questions have been answered
            for (String answer : userAnswers) {
                if (answer.isEmpty()) {
                    showMessage("Please answer all questions before submitting.", false);
                    return;
                }
            }
            // Calculate score and feedback
            int score = 0;
            for (int i = 0; i < questions.size(); i++) {
                String userAnswer = userAnswers.get(i);
                String correctAnswer = String.valueOf(questions.get(i).getCorrectAnswer());
                if (userAnswer.equals(correctAnswer)) {
                    score++;
                    feedback.set(i, "Correct");
                } else {
                    feedback.set(i, "Incorrect (Correct: " + correctAnswer + ")");
                }
            }
            // Save result
            resultService.saveResult(quizId, userId, score, userAnswers, questions);
            // Update UI
            isSubmitted = true;
            showMessage("Quiz submitted! Score: " + score + "/" + questions.size(), true);
            submitButton.setDisable(true);
            displayQuestion(currentQuestionIndex); // Refresh to show feedback
        } catch (SQLException e) {
            showMessage("Error submitting quiz: " + e.getMessage(), false);
            AppLogger.error("Error submitting quiz", e);
            if (e.getMessage().contains("no such table: user_answers")) {
                showMessage("Database error: user_answers table is missing. Please contact the administrator.", false);
            }
        }
    }

    /**
     * Navigates back to the user dashboard.
     *
     * @throws IOException If the user.fxml file cannot be loaded.
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
        Scene newScene = new Scene(root);
        controller.setMainStage(mainStage, newScene);
        controller.setUserId(userId);
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
