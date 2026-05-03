package com.quizsystem.dao;

import com.quizsystem.model.Quiz;
import com.quizsystem.model.QuestionData;
import com.quizsystem.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for quiz-related database operations.
 * Provides methods to create, retrieve, and delete quizzes, and manage associated questions.
 */
public class QuizDao {

    /**
     * Creates a new quiz in the database and sets its generated ID.
     *
     * @param quiz The quiz to create.
     * @throws SQLException If a database error occurs during creation.
     */
    public void createQuiz(Quiz quiz) throws SQLException {
        validateQuiz(quiz);
        String sql = "INSERT INTO quizzes (title, description, created_by, time_limit) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, quiz.getTitle());
            stmt.setString(2, quiz.getDescription());
            stmt.setInt(3, quiz.getCreatedBy());
            stmt.setInt(4, quiz.getTimeLimit());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                quiz.setQuizId(rs.getInt(1));
            }
        }
    }

    /**
     * Retrieves all quizzes from the database.
     *
     * @return A list of all quizzes.
     * @throws SQLException If a database error occurs during retrieval.
     */
    public List<Quiz> getAllQuizzes() throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT * FROM quizzes ORDER BY quiz_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                quizzes.add(new Quiz(
                        rs.getInt("quiz_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("created_by"),
                        rs.getInt("time_limit")
                ));
            }
        }
        return quizzes;
    }

    /**
     * Deletes a quiz and its associated questions, results, and user answers.
     *
     * @param quizId The ID of the quiz to delete.
     * @throws SQLException If a database error occurs or the quiz is not found.
     */
    public void deleteQuiz(int quizId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String deleteQuiz = "DELETE FROM quizzes WHERE quiz_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuiz)) {
                    stmt.setInt(1, quizId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("No quiz found with quiz_id: " + quizId);
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Creates a new quiz with associated questions in a single database transaction.
     *
     * @param quiz      The quiz to create.
     * @param questions The list of questions to associate with the quiz.
     * @return The ID of the created quiz.
     * @throws SQLException If a database error occurs during creation or if question data is invalid.
     */
    public int createQuizWithQuestions(Quiz quiz, List<QuestionData> questions) throws SQLException {
        if (questions == null || questions.isEmpty()) {
            throw new SQLException("Question list cannot be null or empty");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                validateQuiz(quiz);

                // Insert quiz
                String insertQuiz = "INSERT INTO quizzes (title, description, created_by, time_limit) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuiz, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, quiz.getTitle());
                    stmt.setString(2, quiz.getDescription());
                    stmt.setInt(3, quiz.getCreatedBy());
                    stmt.setInt(4, quiz.getTimeLimit());
                    stmt.executeUpdate();
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        quiz.setQuizId(rs.getInt(1));
                    } else {
                        throw new SQLException("Failed to retrieve generated quiz ID");
                    }
                }

                // Validate and insert questions
                String insertQuestion = "INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
                int questionIndex = 0;
                for (QuestionData q : questions) {
                    // Validate question data
                    if (q.getQuestionText() == null || q.getQuestionText().trim().isEmpty() ||
                            q.getOptionA() == null || q.getOptionA().trim().isEmpty() ||
                            q.getOptionB() == null || q.getOptionB().trim().isEmpty() ||
                            q.getOptionC() == null || q.getOptionC().trim().isEmpty() ||
                            q.getOptionD() == null || q.getOptionD().trim().isEmpty() ||
                            q.getCorrectAnswer() == null || q.getCorrectAnswer().trim().isEmpty()) {
                        throw new SQLException("Invalid question data at index " + questionIndex + ": all fields must be non-null and non-empty");
                    }
                    if (!q.getCorrectAnswer().matches("[A-D]")) {
                        throw new SQLException("Invalid correct answer at index " + questionIndex + ": must be A, B, C, or D");
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(insertQuestion)) {
                        stmt.setInt(1, quiz.getQuizId());
                        stmt.setString(2, q.getQuestionText());
                        stmt.setString(3, q.getOptionA());
                        stmt.setString(4, q.getOptionB());
                        stmt.setString(5, q.getOptionC());
                        stmt.setString(6, q.getOptionD());
                        stmt.setString(7, q.getCorrectAnswer());
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        throw new SQLException("Failed to insert question at index " + questionIndex + ": " + e.getMessage(), e);
                    }
                    questionIndex++;
                }

                conn.commit();
                return quiz.getQuizId();
            } catch (SQLException e) {
                conn.rollback();
                throw new SQLException("Failed to create quiz with questions: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void validateQuiz(Quiz quiz) throws SQLException {
        if (quiz == null) {
            throw new SQLException("Quiz cannot be null");
        }
        if (quiz.getTitle() == null || quiz.getTitle().trim().isEmpty()) {
            throw new SQLException("Quiz title cannot be null or empty");
        }
        if (quiz.getTimeLimit() <= 0) {
            throw new SQLException("Quiz time limit must be greater than zero");
        }
    }
}
