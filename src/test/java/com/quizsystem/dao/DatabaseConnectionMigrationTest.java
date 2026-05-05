package com.quizsystem.dao;

import com.quizsystem.util.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConnectionMigrationTest {

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSupport.createLegacySchema();
    }

    @Test
    void initializeDatabaseMigratesLegacySchemaAndPreservesAnswers() throws SQLException {
        int userId = insertLegacyUser();
        int quizId = insertLegacyQuiz(userId, 0);
        int questionId = insertLegacyQuestion(quizId);
        int resultId = insertLegacyResult(userId, quizId);
        insertLegacyAnswer(resultId, questionId, "B");

        DatabaseConnection.initializeDatabase();

        try (Connection conn = DatabaseConnection.getConnection()) {
            assertEquals(1, queryInt(conn, "SELECT time_limit FROM quizzes WHERE quiz_id = ?", quizId));
            assertEquals(1, queryInt(conn, "SELECT COUNT(*) FROM users WHERE username = ?", "admin"));
            assertEquals(1, queryInt(conn, "SELECT COUNT(*) FROM users WHERE user_id = ?", userId));
            assertEquals(1, queryInt(conn, "SELECT COUNT(*) FROM user_answers WHERE user_answer = ?", "B"));
            assertTrue(tableSqlContains(conn, "quizzes", "CHECK(time_limit > 0)"));
            assertTrue(hasCascadeDelete(conn, "questions", "quizzes"));
            assertTrue(hasCascadeDelete(conn, "results", "quizzes"));
            assertTrue(hasCascadeDelete(conn, "user_answers", "results"));
            assertTrue(hasCascadeDelete(conn, "user_answers", "questions"));
        }
    }

    @Test
    void migratedSchemaSupportsCascadeDelete() throws SQLException {
        int userId = insertLegacyUser();
        int quizId = insertLegacyQuiz(userId, 15);
        int questionId = insertLegacyQuestion(quizId);
        int resultId = insertLegacyResult(userId, quizId);
        insertLegacyAnswer(resultId, questionId, "A");

        DatabaseConnection.initializeDatabase();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM quizzes WHERE quiz_id = ?")) {
            stmt.setInt(1, quizId);
            stmt.executeUpdate();
        }

        assertEquals(0, TestDatabaseSupport.countById("quizzes", "quiz_id", quizId));
        assertEquals(0, TestDatabaseSupport.countById("questions", "quiz_id", quizId));
        assertEquals(0, TestDatabaseSupport.countById("results", "quiz_id", quizId));
        assertEquals(0, TestDatabaseSupport.countById("user_answers", "result_id", resultId));
    }

    private int insertLegacyUser() throws SQLException {
        String sql = "INSERT INTO users (username, password, role, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "legacy-user");
            stmt.setString(2, "plaintext");
            stmt.setString(3, "user");
            stmt.setString(4, "legacy-user@example.com");
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int insertLegacyQuiz(int userId, int timeLimit) throws SQLException {
        String sql = "INSERT INTO quizzes (title, description, created_by, time_limit) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "Legacy Quiz");
            stmt.setString(2, "Migrated from old schema");
            stmt.setInt(3, userId);
            stmt.setInt(4, timeLimit);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int insertLegacyQuestion(int quizId) throws SQLException {
        String sql = """
                INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, quizId);
            stmt.setString(2, "Legacy question?");
            stmt.setString(3, "A");
            stmt.setString(4, "B");
            stmt.setString(5, "C");
            stmt.setString(6, "D");
            stmt.setString(7, "A");
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int insertLegacyResult(int userId, int quizId) throws SQLException {
        String sql = "INSERT INTO results (user_id, quiz_id, score, completion_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, quizId);
            stmt.setInt(3, 1);
            stmt.setString(4, "2026-05-05 10:00:00");
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void insertLegacyAnswer(int resultId, int questionId, String selectedAnswer) throws SQLException {
        String sql = "INSERT INTO user_answers (result_id, question_id, selected_answer) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resultId);
            stmt.setInt(2, questionId);
            stmt.setString(3, selectedAnswer);
            stmt.executeUpdate();
        }
    }

    private int queryInt(Connection conn, String sql, int value) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int queryInt(Connection conn, String sql, String value) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private boolean tableSqlContains(Connection conn, String tableName, String fragment) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                String sql = rs.getString("sql");
                return sql != null && sql.contains(fragment);
            }
        }
    }

    private boolean hasCascadeDelete(Connection conn, String tableName, String referencedTable) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA foreign_key_list(%s)".formatted(tableName))) {
            while (rs.next()) {
                if (referencedTable.equals(rs.getString("table"))
                        && "CASCADE".equalsIgnoreCase(rs.getString("on_delete"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
