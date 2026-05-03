package com.quizsystem.dao;

import com.quizsystem.model.Quiz;
import com.quizsystem.model.QuestionData;
import com.quizsystem.util.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuizDaoTest {

    private final QuizDao quizDao = new QuizDao();

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSupport.resetDatabase();
    }

    @Test
    void deleteQuizRemovesQuizAndDependentRecords() throws SQLException {
        int adminId = insertUser("quiz_admin", "quiz_admin@example.com", "admin");
        Quiz quiz = new Quiz(0, "History", "European capitals", adminId, 60);
        quizDao.createQuiz(quiz);
        int questionId = insertQuestion(quiz.getQuizId());
        int resultId = insertResult(adminId, quiz.getQuizId());
        insertUserAnswer(resultId, questionId);

        quizDao.deleteQuiz(quiz.getQuizId());

        try (Connection conn = DatabaseConnection.getConnection()) {
            assertEquals(0, countById(conn, "quizzes", "quiz_id", quiz.getQuizId()));
            assertEquals(0, countById(conn, "questions", "quiz_id", quiz.getQuizId()));
            assertEquals(0, countById(conn, "results", "quiz_id", quiz.getQuizId()));
            assertEquals(0, countById(conn, "user_answers", "result_id", resultId));
        }
    }

    @Test
    void createQuizWithQuestionsRejectsNonPositiveTimeLimit() {
        Quiz quiz = new Quiz(0, "Invalid Quiz", "Should fail", 1, 0);
        List<QuestionData> questions = List.of(
                new QuestionData("Question?", "A", "B", "C", "D", "A")
        );

        assertThrows(SQLException.class, () -> quizDao.createQuizWithQuestions(quiz, questions));
    }

    private int insertUser(String username, String email, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, "placeholder");
            stmt.setString(3, role);
            stmt.setString(4, email);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int insertQuestion(int quizId) throws SQLException {
        String sql = """
                INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, quizId);
            stmt.setString(2, "Capital of France?");
            stmt.setString(3, "Paris");
            stmt.setString(4, "Berlin");
            stmt.setString(5, "Rome");
            stmt.setString(6, "Madrid");
            stmt.setString(7, "A");
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int insertResult(int userId, int quizId) throws SQLException {
        String sql = "INSERT INTO results (user_id, quiz_id, score, completion_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, quizId);
            stmt.setInt(3, 1);
            stmt.setString(4, "2026-05-04 10:00:00");
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void insertUserAnswer(int resultId, int questionId) throws SQLException {
        String sql = "INSERT INTO user_answers (result_id, question_id, user_answer) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resultId);
            stmt.setInt(2, questionId);
            stmt.setString(3, "A");
            stmt.executeUpdate();
        }
    }

    private int countById(Connection conn, String table, String column, int value) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
