package com.quizsystem.dao;

import com.quizsystem.util.DatabaseConnection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

final class TestDatabaseSupport {
    private TestDatabaseSupport() {
    }

    static void resetDatabase() throws SQLException {
        ensureTestDatabaseDirectory();
        DatabaseConnection.initializeDatabase();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM user_answers");
            stmt.executeUpdate("DELETE FROM results");
            stmt.executeUpdate("DELETE FROM questions");
            stmt.executeUpdate("DELETE FROM quizzes");
            stmt.executeUpdate("DELETE FROM users WHERE username <> 'admin'");
        }
    }

    static void createLegacySchema() throws SQLException {
        ensureTestDatabaseDirectory();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = OFF");
            stmt.executeUpdate("DROP TABLE IF EXISTS user_answers");
            stmt.executeUpdate("DROP TABLE IF EXISTS results");
            stmt.executeUpdate("DROP TABLE IF EXISTS questions");
            stmt.executeUpdate("DROP TABLE IF EXISTS quizzes");
            stmt.executeUpdate("DROP TABLE IF EXISTS users");

            stmt.execute("""
                CREATE TABLE users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT CHECK(role IN ('admin', 'user')) NOT NULL,
                    email TEXT NOT NULL UNIQUE
                )
            """);

            stmt.execute("""
                CREATE TABLE quizzes (
                    quiz_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    created_by INTEGER,
                    time_limit INTEGER,
                    FOREIGN KEY (created_by) REFERENCES users(user_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE questions (
                    question_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    quiz_id INTEGER,
                    question_text TEXT NOT NULL,
                    option_a TEXT NOT NULL,
                    option_b TEXT NOT NULL,
                    option_c TEXT NOT NULL,
                    option_d TEXT NOT NULL,
                    correct_answer TEXT NOT NULL,
                    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE results (
                    result_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    quiz_id INTEGER,
                    score INTEGER NOT NULL,
                    completion_time DATETIME,
                    FOREIGN KEY (user_id) REFERENCES users(user_id),
                    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE user_answers (
                    answer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    result_id INTEGER,
                    question_id INTEGER,
                    selected_answer TEXT,
                    FOREIGN KEY (result_id) REFERENCES results(result_id),
                    FOREIGN KEY (question_id) REFERENCES questions(question_id)
                )
            """);
            stmt.execute("PRAGMA foreign_keys = ON");
        }
    }

    static int countById(String table, String column, int value) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static void ensureTestDatabaseDirectory() throws SQLException {
        String dbUrl = System.getProperty("db.url", "");
        String prefix = "jdbc:sqlite:";
        if (!dbUrl.startsWith(prefix)) {
            return;
        }

        Path dbPath = Path.of(dbUrl.substring(prefix.length()));
        Path parent = dbPath.getParent();
        if (parent == null) {
            return;
        }

        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            throw new SQLException("Failed to create test database directory: " + parent, e);
        }
    }
}
