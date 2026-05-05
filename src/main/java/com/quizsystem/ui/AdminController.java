package com.quizsystem.ui;

import com.quizsystem.model.LeaderboardEntry;
import com.quizsystem.model.Quiz;
import com.quizsystem.model.QuizAnalyticsSummary;
import com.quizsystem.model.QuestionData;
import com.quizsystem.service.QuizService;
import com.quizsystem.service.QuestionService;
import com.quizsystem.service.ResultService;
import com.quizsystem.service.UserService;
import com.quizsystem.util.AppLogger;
import com.quizsystem.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
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

    /** Service for result-related database operations. */
    private ResultService resultService;

    /** Service for user-related database operations. */
    private UserService userService;

    /** Counter for tracking the number of question fields added. */
    private int questionCount = 0;

    private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.00");

    /** Tracks question rows without relying on node lookup and generated IDs. */
    private final List<QuestionFormRow> questionRows = new ArrayList<>();

    private static final class QuestionFormRow {
        private final TextArea questionTextField;
        private final TextField optionAField;
        private final TextField optionBField;
        private final TextField optionCField;
        private final TextField optionDField;
        private final ChoiceBox<String> correctAnswerChoiceBox;
        private final HBox container;

        private QuestionFormRow(TextArea questionTextField, TextField optionAField, TextField optionBField,
                                TextField optionCField, TextField optionDField,
                                ChoiceBox<String> correctAnswerChoiceBox, HBox container) {
            this.questionTextField = questionTextField;
            this.optionAField = optionAField;
            this.optionBField = optionBField;
            this.optionCField = optionCField;
            this.optionDField = optionDField;
            this.correctAnswerChoiceBox = correctAnswerChoiceBox;
            this.container = container;
        }
    }

    /**
     * Initializes the controller after FXML loading, setting up services and UI components.
     */
    @FXML
    public void initialize() {
        quizService = new QuizService();
        questionService = new QuestionService();
        resultService = new ResultService();
        userService = new UserService();
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
        ThemeManager.apply(scene);
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
            if (timeLimitInt <= 0) {
                showMessage("Time limit must be greater than zero.", false);
                return;
            }
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
            questionRows.clear();
            addDefaultQuestionField();
        } catch (NumberFormatException e) {
            showMessage("Time limit must be a number.", false);
        } catch (SQLException e) {
            showMessage("Error creating quiz: " + e.getMessage(), false);
            AppLogger.error("Error creating quiz", e);
        }
    }

    /**
     * Collects question data from the UI input fields.
     *
     * @return A list of valid QuestionData objects.
     */
    private List<QuestionData> collectQuestions() throws SQLException {
        List<QuestionData> questions = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < questionRows.size(); rowIndex++) {
            QuestionFormRow row = questionRows.get(rowIndex);
            int questionNumber = rowIndex + 1;
            String questionText = row.questionTextField.getText().trim();
            String optionA = row.optionAField.getText().trim();
            String optionB = row.optionBField.getText().trim();
            String optionC = row.optionCField.getText().trim();
            String optionD = row.optionDField.getText().trim();
            String correctAnswer = row.correctAnswerChoiceBox.getValue();

            boolean allFieldsEmpty = questionText.isEmpty() && optionA.isEmpty() && optionB.isEmpty()
                    && optionC.isEmpty() && optionD.isEmpty() && correctAnswer == null;
            boolean allFieldsPresent = !questionText.isEmpty() && !optionA.isEmpty() && !optionB.isEmpty()
                    && !optionC.isEmpty() && !optionD.isEmpty() && correctAnswer != null;

            if (allFieldsEmpty) {
                continue;
            }

            if (!allFieldsPresent) {
                throw new SQLException("Question " + questionNumber + " is incomplete. Fill in the question, all four options, and the correct answer.");
            }

            if (!correctAnswer.matches("[A-D]")) {
                throw new SQLException("Question " + questionNumber + " has an invalid correct answer. Choose A, B, C, or D.");
            }

            questions.add(new QuestionData(questionText, optionA, optionB, optionC, optionD, correctAnswer));
        }
        return questions;
    }

    /**
     * Adds a new set of input fields for a question to the UI.
     */
    @FXML
    private void handleAddQuestionField() {
        questionCount++;
        QuestionFormRow questionRow = createQuestionField(questionCount);
        questionRows.add(questionRow);
        questionsContainer.getChildren().add(questionRow.container);
    }

    /**
     * Creates a horizontal box containing input fields for a single question.
     *
     * @param index The index of the question (used for ID generation).
     * @return An HBox containing question input fields.
     */
    private QuestionFormRow createQuestionField(int index) {
        HBox hBox = new HBox(10);
        TextArea questionTextField = new TextArea();
        questionTextField.setPromptText("Question " + index + " Text");
        questionTextField.setPrefWidth(300);
        questionTextField.setPrefHeight(50);
        TextField optionAField = new TextField();
        optionAField.setPromptText("Option A");
        optionAField.setPrefWidth(100);
        TextField optionBField = new TextField();
        optionBField.setPromptText("Option B");
        optionBField.setPrefWidth(100);
        TextField optionCField = new TextField();
        optionCField.setPromptText("Option C");
        optionCField.setPrefWidth(100);
        TextField optionDField = new TextField();
        optionDField.setPromptText("Option D");
        optionDField.setPrefWidth(100);
        Label correctAnswerLabel = new Label("Select Correct Answer:");
        correctAnswerLabel.getStyleClass().add("question-label");
        ChoiceBox<String> correctAnswerChoiceBox = new ChoiceBox<>();
        correctAnswerChoiceBox.getItems().addAll("A", "B", "C", "D");
        correctAnswerChoiceBox.setPrefWidth(50);
        hBox.getChildren().addAll(questionTextField, optionAField, optionBField, optionCField, optionDField, correctAnswerLabel, correctAnswerChoiceBox);
        return new QuestionFormRow(
                questionTextField,
                optionAField,
                optionBField,
                optionCField,
                optionDField,
                correctAnswerChoiceBox,
                hBox
        );
    }

    /**
     * Adds a default question input field to the UI.
     */
    private void addDefaultQuestionField() {
        questionCount = 1; // Initialize with one question field
        questionRows.clear();
        QuestionFormRow defaultQuestionRow = createQuestionField(1);
        questionRows.add(defaultQuestionRow);
        questionsContainer.getChildren().add(defaultQuestionRow.container);
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
            AppLogger.error("FXML resource not found: /com/quizsystem/ui/addQuestion.fxml");
            return;
        }
        try {
            Parent root = loader.load();
            AddQuestionController controller = loader.getController();
            Scene newScene = new Scene(root);
            controller.setMainStage(mainStage, newScene);
            controller.setQuizId(selectedQuiz.getQuizId());
            controller.setAdminId(adminId);
            applyStylesheets(newScene);
            mainStage.setScene(newScene);
            mainStage.setMaximized(true);
        } catch (IOException e) {
            showMessage("Error loading question form: " + e.getMessage(), false);
            AppLogger.error("Error loading question form", e);
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
            AppLogger.error("Error deleting quiz", e);
        }
    }

    /**
     * Verifies if the user is a valid admin.
     *
     * @param userId The ID of the user to verify.
     * @return True if the user is an admin, false otherwise.
     */
    private boolean isValidUser(int userId) {
        try {
            return userService.isAdminUser(userId);
        } catch (SQLException e) {
            AppLogger.error("Error validating admin user", e);
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
            AppLogger.error("FXML resource not found: /com/quizsystem/ui/viewQuestions.fxml");
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
        try {
            int rank = 1;
            for (LeaderboardEntry entry : resultService.getQuizLeaderboard(selectedQuiz.getQuizId())) {
                leaderboardListView.getItems().add(
                        rank++ + ". " + entry.username() + ": " + entry.score() +
                                " (Date: " + entry.completionTime() + ")");
            }
            if (leaderboardListView.getItems().isEmpty()) {
                showMessage("No leaderboard data available for Quiz ID: " + selectedQuiz.getQuizId(), false);
            } else {
                showMessage("Leaderboard loaded successfully.", true);
            }
        } catch (SQLException e) {
            showMessage("Error loading leaderboard: " + e.getMessage(), false);
            AppLogger.error("Error loading quiz leaderboard", e);
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

        leaderboardListView.getItems().clear();
        try {
            int questionCount = questionService.getQuestionsByQuizId(selectedQuiz.getQuizId()).size();
            QuizAnalyticsSummary analytics = resultService.getQuizAnalytics(selectedQuiz.getQuizId());

            leaderboardListView.getItems().add("Quiz: " + selectedQuiz.getTitle());
            leaderboardListView.getItems().add("Questions: " + questionCount);
            leaderboardListView.getItems().add("Attempts: " + analytics.attemptCount());
            leaderboardListView.getItems().add("Participants: " + analytics.participantCount());
            leaderboardListView.getItems().add("Highest Score: " + analytics.highestScore());
            leaderboardListView.getItems().add("Average Score: " + SCORE_FORMAT.format(analytics.averageScore()));
            if (questionCount > 0) {
                double averagePercent = (analytics.averageScore() / questionCount) * 100.0;
                leaderboardListView.getItems().add("Average Accuracy: " + SCORE_FORMAT.format(averagePercent) + "%");
            }
            leaderboardListView.getItems().add("Latest Completion: "
                    + (analytics.latestCompletionTime() == null ? "No attempts yet" : analytics.latestCompletionTime()));

            showMessage("Analytics loaded successfully.", true);
        } catch (SQLException e) {
            showMessage("Error loading analytics: " + e.getMessage(), false);
            AppLogger.error("Error loading quiz analytics", e);
        }
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
            AppLogger.error("Cannot load login form: stage not initialized.");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/quizsystem/ui/login.fxml"));
        if (loader.getLocation() == null) {
            showMessage("Cannot load login form.", false);
            AppLogger.error("FXML resource not found: /com/quizsystem/ui/login.fxml");
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
        ThemeManager.toggle(getActiveScene());
        showMessage("Dark mode " + (ThemeManager.isDarkModeEnabled() ? "enabled" : "disabled") + ".", true);
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
            AppLogger.error("Error loading quizzes", e);
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

    private Scene getActiveScene() {
        if (mainStage != null && mainStage.getScene() != null) {
            return mainStage.getScene();
        }
        return mainScene;
    }
}
