package com.quizsystem.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Utility class for managing SQLite database connections and initialization.
 */
public class DatabaseConnection {
    /** Path to the SQLite database file. */
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:quiz_system.db";
    private static final String DB_URL = loadDatabaseUrl();

    /**
     * Establishes a connection to the SQLite database.
     *
     * @return A Connection object to the database.
     * @throws SQLException If a database connection error occurs.
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
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
                    role TEXT CHECK(role IN ('admin', 'user')) NOT NULL,
                    email TEXT NOT NULL UNIQUE
                )
            """);

            // Create quizzes table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS quizzes (
                    quiz_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    created_by INTEGER,
                    time_limit INTEGER NOT NULL CHECK(time_limit > 0),
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
                    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
                )
            """);

            // Create results table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS results (
                    result_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    quiz_id INTEGER,
                    user_id INTEGER,
                    score INTEGER NOT NULL,
                    completion_time TEXT,
                    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE,
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )
            """);

            // Create user_answers table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_answers (
                    answer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    result_id INTEGER,
                    question_id INTEGER,
                    user_answer TEXT NOT NULL,
                    FOREIGN KEY (result_id) REFERENCES results(result_id) ON DELETE CASCADE,
                    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
                )
            """);

            ensureUserAnswerColumn(conn);
            ensureCascadeDeleteSchema(conn);
            seedDefaultAdmin(conn);
        }
    }

    /**
     * Migrates databases created with the earlier selected_answer column name.
     */
    private static void ensureUserAnswerColumn(Connection conn) throws SQLException {
        boolean hasUserAnswer = false;
        boolean hasSelectedAnswer = false;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(user_answers)")) {
            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("user_answer".equals(columnName)) {
                    hasUserAnswer = true;
                } else if ("selected_answer".equals(columnName)) {
                    hasSelectedAnswer = true;
                }
            }
        }

        if (!hasUserAnswer) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE user_answers ADD COLUMN user_answer TEXT");
            }
        }

        if (hasSelectedAnswer) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE user_answers SET user_answer = selected_answer WHERE user_answer IS NULL");
            }
        }
    }

    private static void ensureCascadeDeleteSchema(Connection conn) throws SQLException {
        if (!requiresCascadeMigration(conn)) {
            return;
        }

        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = OFF");
                stmt.execute("DROP TABLE IF EXISTS user_answers_new");
                stmt.execute("DROP TABLE IF EXISTS results_new");
                stmt.execute("DROP TABLE IF EXISTS questions_new");
                stmt.execute("DROP TABLE IF EXISTS quizzes_new");

                stmt.execute("""
                    CREATE TABLE quizzes_new (
                        quiz_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        description TEXT,
                        created_by INTEGER,
                        time_limit INTEGER NOT NULL CHECK(time_limit > 0),
                        FOREIGN KEY (created_by) REFERENCES users(user_id)
                    )
                """);
                stmt.execute("""
                    INSERT INTO quizzes_new (quiz_id, title, description, created_by, time_limit)
                    SELECT quiz_id, title, description, created_by,
                           CASE WHEN time_limit IS NULL OR time_limit <= 0 THEN 1 ELSE time_limit END
                    FROM quizzes
                """);

                stmt.execute("""
                    CREATE TABLE questions_new (
                        question_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        quiz_id INTEGER,
                        question_text TEXT NOT NULL,
                        option_a TEXT NOT NULL,
                        option_b TEXT NOT NULL,
                        option_c TEXT NOT NULL,
                        option_d TEXT NOT NULL,
                        correct_answer TEXT NOT NULL,
                        FOREIGN KEY (quiz_id) REFERENCES quizzes_new(quiz_id) ON DELETE CASCADE
                    )
                """);
                stmt.execute("""
                    INSERT INTO questions_new (
                        question_id, quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer
                    )
                    SELECT question_id, quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer
                    FROM questions
                """);

                stmt.execute("""
                    CREATE TABLE results_new (
                        result_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        quiz_id INTEGER,
                        user_id INTEGER,
                        score INTEGER NOT NULL,
                        completion_time TEXT,
                        FOREIGN KEY (quiz_id) REFERENCES quizzes_new(quiz_id) ON DELETE CASCADE,
                        FOREIGN KEY (user_id) REFERENCES users(user_id)
                    )
                """);
                stmt.execute("""
                    INSERT INTO results_new (result_id, quiz_id, user_id, score, completion_time)
                    SELECT result_id, quiz_id, user_id, score, completion_time
                    FROM results
                """);

                String userAnswerSourceColumn = hasColumn(conn, "user_answers", "user_answer")
                        ? "user_answer"
                        : "selected_answer";
                stmt.execute("""
                    CREATE TABLE user_answers_new (
                        answer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        result_id INTEGER,
                        question_id INTEGER,
                        user_answer TEXT NOT NULL,
                        FOREIGN KEY (result_id) REFERENCES results_new(result_id) ON DELETE CASCADE,
                        FOREIGN KEY (question_id) REFERENCES questions_new(question_id) ON DELETE CASCADE
                    )
                """);
                stmt.execute("""
                    INSERT INTO user_answers_new (answer_id, result_id, question_id, user_answer)
                    SELECT answer_id, result_id, question_id, COALESCE(%s, '')
                    FROM user_answers
                """.formatted(userAnswerSourceColumn));

                stmt.execute("DROP TABLE user_answers");
                stmt.execute("DROP TABLE results");
                stmt.execute("DROP TABLE questions");
                stmt.execute("DROP TABLE quizzes");

                stmt.execute("ALTER TABLE quizzes_new RENAME TO quizzes");
                stmt.execute("ALTER TABLE questions_new RENAME TO questions");
                stmt.execute("ALTER TABLE results_new RENAME TO results");
                stmt.execute("ALTER TABLE user_answers_new RENAME TO user_answers");
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    private static boolean requiresCascadeMigration(Connection conn) throws SQLException {
        return !tableSqlContains(conn, "quizzes", "CHECK(time_limit > 0)")
                || !foreignKeyContainsCascade(conn, "questions", "quizzes")
                || !foreignKeyContainsCascade(conn, "results", "quizzes")
                || !foreignKeyContainsCascade(conn, "user_answers", "results")
                || !foreignKeyContainsCascade(conn, "user_answers", "questions");
    }

    private static boolean tableSqlContains(Connection conn, String tableName, String expectedFragment) throws SQLException {
        String sql = "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String tableSql = rs.getString("sql");
                return tableSql != null && tableSql.contains(expectedFragment);
            }
        }
    }

    private static boolean foreignKeyContainsCascade(Connection conn, String tableName, String referencedTable) throws SQLException {
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

    private static boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(%s)".formatted(tableName))) {
            while (rs.next()) {
                if (columnName.equals(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates the documented default administrator account for a fresh database.
     */
    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO users (username, password, role, email)
            VALUES (?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "admin");
            stmt.setString(2, PasswordUtils.hashPassword("admin123"));
            stmt.setString(3, "admin");
            stmt.setString(4, "admin@quizsystem.local");
            stmt.executeUpdate();
        }
    }

    private static String loadDatabaseUrl() {
        String overriddenUrl = System.getProperty("db.url");
        if (overriddenUrl != null && !overriddenUrl.isBlank()) {
            return overriddenUrl;
        }

        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                return DEFAULT_DB_URL;
            }
            properties.load(inputStream);
            String configuredUrl = properties.getProperty("db.url");
            if (configuredUrl == null || configuredUrl.isBlank()) {
                return DEFAULT_DB_URL;
            }
            return configuredUrl.trim();
        } catch (IOException e) {
            return DEFAULT_DB_URL;
        }
    }
}
