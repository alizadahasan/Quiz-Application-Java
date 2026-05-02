package com.quizsystem.dao;

import com.quizsystem.util.DatabaseConnection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
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
