package com.quizsystem.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for managing SQLite database connections and initialization.
 */
public class DatabaseConnection {
    /** Path to the SQLite database file. */
    private static final String DB_URL = "jdbc:sqlite:quiz_system.db";

    /**
     * Establishes a connection to the SQLite database.
     *
     * @return A Connection object to the database.
     * @throws SQLException If a database connection error occurs.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initializes the database by creating necessary tables if they do not exist.
     *
     * @throws SQLException If a database error occurs during initialization.
     */
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Create users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL,
                    email TEXT
                )
            """);

            // Create quizzes table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS quizzes (
                    quiz_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    created_by INTEGER,
                    time_limit INTEGER,
                    FOREIGN KEY (created_by) REFERENCES users(user_id)
                )
            """);

            // Create questions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS questions (
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

            // Create results table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS results (
                    result_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    quiz_id INTEGER,
                    user_id INTEGER,
                    score INTEGER,
                    completion_time TEXT,
                    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id),
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )
            """);

            // Create user_answers table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_answers (
                    answer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    result_id INTEGER,
                    question_id INTEGER,
                    selected_answer TEXT,
                    FOREIGN KEY (result_id) REFERENCES results(result_id),
                    FOREIGN KEY (question_id) REFERENCES questions(question_id)
                )
            """);
        }
    }
}