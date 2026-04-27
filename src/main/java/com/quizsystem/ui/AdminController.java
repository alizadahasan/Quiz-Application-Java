package com.quizsystem.ui;

import com.quizsystem.model.Quiz;
import com.quizsystem.model.QuestionData;
import com.quizsystem.service.QuizService;
import com.quizsystem.service.QuestionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Admin Dashboard UI, handling quiz creation, deletion, and management.
 * Manages quiz and question data through JavaFX UI components and database services.
 */
public class AdminController {
    /** Text field for entering the quiz title. */
    @FXML private TextField titleField;

    /** Text area for entering the quiz description. */
    @FXML private TextArea descriptionField;

    /** Text field for entering the quiz time limit in seconds. */
    @FXML private TextField timeLimitField;

    /** ListView displaying all available quizzes. */
    @FXML private ListView<Quiz> quizListView;

    /** ListView displaying the leaderboard for a selected quiz. */
    @FXML private ListView<String> leaderboardListView;

    /** Label for displaying success or error messages. */
    @FXML private Label messageLabel;

    /** Container for dynamically adding question input fields. */
    @FXML private VBox questionsContainer;

    /** Main application stage for scene transitions. */
    private Stage mainStage;

    /** Main scene for applying stylesheets. */
    private Scene mainScene;

    /** ID of the logged-in admin user. */
    private int adminId;

    /** Service for quiz-related database operations. */
    private QuizService quizService;

    /** Service for question-related database operations. */
    private QuestionService questionService;

    /** Path to the light theme CSS file. */
    private final String LIGHT_CSS = "/com/quizsystem/ui/styles.css";

    /** Path to the dark theme CSS file. */
    private final String DARK_CSS = "/com/quizsystem/ui/dark-mode.css";

    /** Counter for tracking the number of question fields added. */
    private int questionCount = 0;

    /**
     * Initializes the controller after FXML loading, setting up services and UI components.
     */
    @FXML
    public void initialize() {
        quizService = new QuizService();
        questionService = new QuestionService();
        setupQuizListView();
        loadQuizzes();
        addDefaultQuestionField();
    }

    /**
     * Configures the quizListView to display quizzes with a delete button for each.
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
                    Button deleteButton = new Button("Delete Quiz");
                    deleteButton.setOnAction(event -> handleDeleteQuiz(quiz.getQuizId()));
                    HBox hBox = new HBox(10, label, deleteButton);
                    setGraphic(hBox);
                }
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
     * Sets the ID of the logged-in admin user and refreshes the quiz list.
     *
     * @param adminId The ID of the admin user.
     */
    public void setAdminId(int adminId) {
        this.adminId = adminId;
        loadQuizzes();
    }

    /**
     * Applies light or dark theme stylesheets to the specified scene.
     *
     * @param scene The scene to apply stylesheets to.
     */
    private void applyStylesheets(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        boolean isDarkMode = mainScene != null && mainScene.getStylesheets().contains(
                getClass().getResource(DARK_CSS).toExternalForm()
        );
        String stylesheet = isDarkMode ? DARK_CSS : LIGHT_CSS;
        String stylesheetPath = getClass().getResource(stylesheet).toExternalForm();
        if (stylesheetPath != null) {
            scene.getStylesheets().add(stylesheetPath);
        } else {
            System.err.println("Warning: " + stylesheet + " not found");
        }
    }

    /**
     * Handles the creation of a new quiz with questions from the UI input fields.
     *
     * @throws SQLException If a database error occurs during quiz creation.
     */
    @FXML
    private void handleCreateQuiz() {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String timeLimit = timeLimitField.getText().trim();

        if (title.isEmpty() || description.isEmpty() || timeLimit.isEmpty()) {
            showMessage("Please fill in all fields.", false);
            return;
        }

        if (adminId <= 0 || !isValidUser(adminId)) {
            showMessage("Invalid admin user. Please log in again.", false);
            return;
        }

        try {
            int timeLimitInt = Integer.parseInt(timeLimit);
            List<QuestionData> questions = collectQuestions();
            if (questions.isEmpty()) {
                showMessage("Please add at least one valid question.", false);
                return;
            }
            int quizId = quizService.createQuizWithQuestions(title, description, adminId, timeLimitInt, questions);
            showMessage("Quiz created successfully with " + questions.size() + " questions!", true);
            loadQuizzes();
            clearQuizFields();
            questionsContainer.getChildren().clear();
            addDefaultQuestionField();
        } catch (NumberFormatException e) {
            showMessage("Time limit must be a number.", false);
        } catch (SQLException e) {
            showMessage("Error creating quiz: " + e.getMessage(), false);
            System.err.println("SQLException in handleCreateQuiz: " + e.getMessage());
        }
    }

    /**
     * Collects question data from the UI input fields.
     *
     * @return A list of valid QuestionData objects.
     */
    private List<QuestionData> collectQuestions() {
        List<QuestionData> questions = new ArrayList<>();
        int index = 1;
        while (true) {
            TextArea questionTextField = (TextArea) questionsContainer.lookup("#questionTextField" + index);
            TextField optionAField = (TextField) questionsContainer.lookup("#optionAField" + index);
            TextField optionBField = (TextField) questionsContainer.lookup("#optionBField" + index);
            TextField optionCField = (TextField) questionsContainer.lookup("#optionCField" + index);
            TextField optionDField = (TextField) questionsContainer.lookup("#optionDField" + index);
            ChoiceBox<String> correctAnswerChoiceBox = (ChoiceBox<String>) questionsContainer.lookup("#correctAnswerChoiceBox" + index);

            if (questionTextField == null || optionAField == null || optionBField == null ||
                    optionCField == null || optionDField == null || correctAnswerChoiceBox == null) {
                break; // No more question fields
            }

            String questionText = questionTextField.getText().trim();
            String optionA = optionAField.getText().trim();
            String optionB = optionBField.getText().trim();
            String optionC = optionCField.getText().trim();
            String optionD = optionDField.getText().trim();
            String correctAnswer = correctAnswerChoiceBox.getValue();

            if (!questionText.isEmpty() && !optionA.isEmpty() && !optionB.isEmpty() && !optionC.isEmpty() &&
                    !optionD.isEmpty() && correctAnswer != null && correctAnswer.matches("[A-D]")) {
                questions.add(new QuestionData(questionText, optionA, optionB, optionC, optionD, correctAnswer));
            }
            index++;
        }
        return questions;
    }

    /**
     * Adds a new set of input fields for a question to the UI.
     */
    @FXML
    private void handleAddQuestionField() {
        questionCount++;
        HBox questionBox = createQuestionField(questionCount);
        questionsContainer.getChildren().add(questionBox);
    }

    /**
     * Creates a horizontal box containing input fields for a single question.
     *
     * @param index The index of the question (used for ID generation).
     * @return An HBox containing question input fields.
     */
    private HBox createQuestionField(int index) {
        HBox hBox = new HBox(10);
        TextArea questionTextField = new TextArea();
        questionTextField.setId("questionTextField" + index);
        questionTextField.setPromptText("Question " + index + " Text");
        questionTextField.setPrefWidth(300);
        questionTextField.setPrefHeight(50);
        TextField optionAField = new TextField();
        optionAField.setId("optionAField" + index);
        optionAField.setPromptText("Option A");
        optionAField.setPrefWidth(100);
        TextField optionBField = new TextField();
        optionBField.setId("optionBField" + index);
        optionBField.setPromptText("Option B");
        optionBField.setPrefWidth(100);
        TextField optionCField = new TextField();
        optionCField.setId("optionCField" + index);
        optionCField.setPromptText("Option C");
        optionCField.setPrefWidth(100);
        TextField optionDField = new TextField();
        optionDField.setId("optionDField" + index);
        optionDField.setPromptText("Option D");
        optionDField.setPrefWidth(100);
        Label correctAnswerLabel = new Label("Select Correct Answer:");
        correctAnswerLabel.getStyleClass().add("question-label");
        ChoiceBox<String> correctAnswerChoiceBox = new ChoiceBox<>();
        correctAnswerChoiceBox.setId("correctAnswerChoiceBox" + index);
        correctAnswerChoiceBox.getItems().addAll("A", "B", "C", "D");
        correctAnswerChoiceBox.setPrefWidth(50);
        hBox.getChildren().addAll(questionTextField, optionAField, optionBField, optionCField, optionDField, correctAnswerLabel, correctAnswerChoiceBox);
        return hBox;
    }

    /**
     * Adds a default question input field to the UI.
     */
    private void addDefaultQuestionField() {
        questionCount = 1; // Initialize with one question field
        HBox defaultQuestionBox = createQuestionField(1);
        questionsContainer.getChildren().add(defaultQuestionBox);
    }

    /**
     * Navigates to the Add Question screen for the selected quiz.
     *
     * @throws IOException If the addQuestion.fxml file cannot be loaded.
     */
    @FXML
    private void handleAddQuestion() throws IOException {
        Quiz selectedQuiz = quizListView.getSelectionModel().getSelectedItem();
        if (selectedQuiz == null) {
            showMessage("Please select a quiz from the list.", false);
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/addQuestion.fxml"));
        if (loader.getLocation() == null) {
            showMessage("Cannot load question form. Check if addQuestion.fxml exists in src/main/resources/com/quizsystem/ui/", false);
            System.err.println("Error: addQuestion.fxml not found at /com/quizsystem/ui/addQuestion.fxml");
            return;
        }
        try {
            Parent root = loader.load();
            AddQuestionController controller = loader.getController();
            controller.setMainStage(mainStage, mainScene);
            controller.setQuizId(selectedQuiz.getQuizId());
            Scene newScene = new Scene(root);
            applyStylesheets(newScene);
            mainStage.setScene(newScene);
            mainStage.setMaximized(true);
        } catch (IOException e) {
            showMessage("Error loading question form: " + e.getMessage(), false);
            System.err.println("IOException in handleAddQuestion: " + e.getMessage());
        }
    }

    /**
     * Handles quiz deletion for the selected quiz in the UI.
     */
    @FXML
    private void handleDeleteQuiz() {
        Quiz selectedQuiz = quizListView.getSelectionModel().getSelectedItem();
        if (selectedQuiz == null) {
            showMessage("Please select a quiz to delete.", false);
            return;
        }
        handleDeleteQuiz(selectedQuiz.getQuizId());
    }

    /**
     * Deletes a quiz and its associated data after user confirmation.
     *
     * @param quizId The ID of the quiz to delete.
     * @throws SQLException If a database error occurs during deletion.
     */
    private void handleDeleteQuiz(int quizId) {
        Quiz selectedQuiz = quizListView.getItems().stream()
                .filter(quiz -> quiz.getQuizId() == quizId)
                .findFirst()
                .orElse(null);
        if (selectedQuiz == null) {
            showMessage("Please select a quiz to delete.", false);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Quiz: " + selectedQuiz.getTitle());
        confirmation.setContentText("Are you sure you want to delete this quiz and all its questions, results, and user answers? This action cannot be undone.");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            quizService.deleteQuiz(quizId);
            showMessage("Quiz and associated data deleted successfully!", true);
            loadQuizzes();
        } catch (SQLException e) {
            showMessage("Error deleting quiz: " + e.getMessage(), false);
            System.err.println("SQLException in handleDeleteQuiz: " + e.getMessage());
        }
    }

    /**
     * Verifies if the user is a valid admin.
     *
     * @param userId The ID of the user to verify.
     * @return True if the user is an admin, false otherwise.
     */
    private boolean isValidUser(int userId) {
        try (Connection conn = com.quizsystem.util.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM users WHERE user_id = ? AND role = 'admin'")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("SQLException in isValidUser: " + e.getMessage());
            return false;
        }
    }

    /**
     * Navigates to the View Questions screen for the selected quiz.
     *
     * @throws IOException If the viewQuestions.fxml file cannot be loaded.
     */
    @FXML
    private void handleViewQuestions() throws IOException {
        Quiz selectedQuiz = quizListView.getSelectionModel().getSelectedItem();
        if (selectedQuiz == null) {
            showMessage("Please select a quiz to view its questions.", false);
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/viewQuestions.fxml"));
        if (loader.getLocation() == null) {
            showMessage("Cannot load questions view.", false);
            System.err.println("Error: viewQuestions.fxml not found");
            return;
        }
        Parent root = loader.load();
        ViewQuestionsController controller = loader.getController();
        controller.setMainStage(mainStage, mainScene);
        controller.setQuizId(selectedQuiz.getQuizId());
        controller.setAdminId(adminId);
        Scene newScene = new Scene(root);
        applyStylesheets(newScene);
        mainStage.setScene(newScene);
        mainStage.setMaximized(true);
    }

    /**
     * Displays the leaderboard for the selected quiz.
     *
     * @throws SQLException If a database error occurs while fetching leaderboard data.
     */
    @FXML
    private void handleViewLeaderboard() {
        Quiz selectedQuiz = quizListView.getSelectionModel().getSelectedItem();
        if (selectedQuiz == null) {
            showMessage("Please select a quiz to view its leaderboard.", false);
            return;
        }

        leaderboardListView.getItems().clear();
        try (var conn = com.quizsystem.util.DatabaseConnection.getConnection();
             var stmt = conn.prepareStatement(
                     "SELECT u.username, r.score, r.completion_time FROM results r JOIN users u ON r.user_id = u.user_id WHERE r.quiz_id = ? ORDER BY r.score DESC LIMIT 10")) {
            stmt.setInt(1, selectedQuiz.getQuizId());
            var rs = stmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                leaderboardListView.getItems().add(
                        rank++ + ". " + rs.getString("username") + ": " + rs.getInt("score") +
                                " (Date: " + rs.getString("completion_time") + ")");
            }
            if (leaderboardListView.getItems().isEmpty()) {
                showMessage("No leaderboard data available for Quiz ID: " + selectedQuiz.getQuizId(), false);
            } else {
                showMessage("Leaderboard loaded successfully.", true);
            }
        } catch (SQLException e) {
            showMessage("Error loading leaderboard: " + e.getMessage(), false);
            System.err.println("SQLException in handleViewLeaderboard: " + e.getMessage());
        }
    }

    /**
     * Placeholder for viewing analytics of the selected quiz (not implemented).
     */
    @FXML
    private void handleViewAnalytics() {
        Quiz selectedQuiz = quizListView.getSelectionModel().getSelectedItem();
        if (selectedQuiz == null) {
            showMessage("Please select a quiz to view analytics.", false);
            return;
        }
        showMessage("Analytics not implemented yet for Quiz ID: " + selectedQuiz.getQuizId(), false);
    }

    /**
     * Logs out the admin and returns to the login screen.
     *
     * @throws IOException If the login.fxml file cannot be loaded.
     */
    @FXML
    private void handleLogout() throws IOException {
        if (mainStage == null) {
            showMessage("Cannot load login form: stage not initialized.", false);
            System.err.println("Error: mainStage is null in handleLogout");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/login.fxml"));
        if (loader.getLocation() == null) {
            showMessage("Cannot load login form.", false);
            System.err.println("Error: login.fxml not found at /com/quizsystem/ui/login.fxml");
            return;
        }
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setMainStage(mainStage, mainScene);
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
        boolean isDarkMode = mainScene.getStylesheets().contains(
                getClass().getResource(DARK_CSS).toExternalForm()
        );
        mainScene.getStylesheets().clear();
        String stylesheet = isDarkMode ? LIGHT_CSS : DARK_CSS;
        String stylesheetPath = getClass().getResource(stylesheet).toExternalForm();
        if (stylesheetPath != null) {
            mainScene.getStylesheets().add(stylesheetPath);
        } else {
            System.err.println("Warning: " + stylesheet + " not found");
        }
        showMessage("Dark mode " + (isDarkMode ? "disabled" : "enabled") + ".", true);
    }

    /**
     * Loads all quizzes from the database into the quizListView.
     */
    private void loadQuizzes() {
        quizListView.getItems().clear();
        try {
            quizListView.getItems().addAll(quizService.getAllQuizzes());
        } catch (SQLException e) {
            showMessage("Error loading quizzes: " + e.getMessage(), false);
            System.err.println("SQLException in loadQuizzes: " + e.getMessage());
        }
    }

    /**
     * Clears the quiz input fields in the UI.
     */
    private void clearQuizFields() {
        titleField.clear();
        descriptionField.clear();
        timeLimitField.clear();
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
}
