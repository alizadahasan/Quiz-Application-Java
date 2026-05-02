package com.quizsystem.ui;

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
 * Controller for the Add Question UI, handling the creation of new questions for a quiz.
 * Manages user input for question text, options, and correct answer, and interacts with
 * the QuestionService to persist questions to the database.
 */
public class AddQuestionController {
    /** Text area for entering the question text. */
    @FXML private TextArea questionTextField;

    /** Text field for entering option A. */
    @FXML private TextField optionAField;

    /** Text field for entering option B. */
    @FXML private TextField optionBField;

    /** Text field for entering option C. */
    @FXML private TextField optionCField;

    /** Text field for entering option D. */
    @FXML private TextField optionDField;

    /** Choice box for selecting the correct answer (A, B, C, or D). */
    @FXML private ChoiceBox<String> correctAnswerChoiceBox;

    /** Label for displaying success or error messages. */
    @FXML private Label messageLabel;

    /** Text field for displaying the quiz ID. */
    @FXML private TextField quizIdField;

    /** Main application stage for scene transitions. */
    private Stage mainStage;

    /** Main scene for applying stylesheets. */
    private Scene mainScene;

    /** ID of the quiz to which the question is added. */
    private int quizId;

    /** Service for question-related database operations. */
    private QuestionService questionService;

    /**
     * Initializes the controller after FXML loading, setting up the QuestionService
     * and populating the correct answer choice box with options A, B, C, and D.
     */
    @FXML
    public void initialize() {
        questionService = new QuestionService();
        correctAnswerChoiceBox.getItems().addAll("A", "B", "C", "D");
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
     * Sets the ID of the quiz to which the question will be added and updates the UI.
     *
     * @param quizId The ID of the quiz.
     */
    public void setQuizId(int quizId) {
        this.quizId = quizId;
        quizIdField.setText(String.valueOf(quizId));
    }

    /**
     * Handles the submission of a new question, validating input and saving it to the database.
     */
    @FXML
    private void handleAddQuestion() {
        if (quizId <= 0) {
            showMessage("Invalid Quiz ID", false);
            return;
        }

        String questionText = questionTextField.getText().trim();
        String optionA = optionAField.getText().trim();
        String optionB = optionBField.getText().trim();
        String optionC = optionCField.getText().trim();
        String optionD = optionDField.getText().trim();
        String correctAnswer = correctAnswerChoiceBox.getValue();

        if (questionText.isEmpty() || optionA.isEmpty() || optionB.isEmpty() ||
                optionC.isEmpty() || optionD.isEmpty() || correctAnswer == null) {
            showMessage("Please fill in all fields.", false);
            return;
        }

        if (!correctAnswer.matches("[A-D]")) {
            showMessage("Correct answer must be A, B, C, or D.", false);
            return;
        }

        try {
            questionService.createQuestion(quizId, questionText, optionA, optionB, optionC, optionD, correctAnswer);
            showMessage("Question added successfully!", true);
            clearFields();
        } catch (SQLException e) {
            showMessage("Error adding question: " + e.getMessage(), false);
        }
    }

    /**
     * Toggles between light and dark themes.
     */
    @FXML
    private void toggleDarkMode() {
        ThemeManager.toggle(getActiveScene());
        showMessage("Dark mode " + (ThemeManager.isDarkModeEnabled() ? "enabled" : "disabled") + ".", true);
    }

    /**
     * Navigates back to the Admin Dashboard UI.
     *
     * @throws IOException If the admin.fxml file cannot be loaded.
     */
    @FXML
    private void handleBack() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/admin.fxml"));
        Parent root = loader.load();
        AdminController controller = loader.getController();
        controller.setMainStage(mainStage, mainScene);
        Scene newScene = new Scene(root);
        applyStylesheets(newScene);
        mainStage.setScene(newScene);
        mainStage.setMaximized(true);
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
     * Clears all input fields in the UI.
     */
    private void clearFields() {
        questionTextField.clear();
        optionAField.clear();
        optionBField.clear();
        optionCField.clear();
        optionDField.clear();
        correctAnswerChoiceBox.setValue(null);
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
