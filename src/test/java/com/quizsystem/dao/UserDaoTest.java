package com.quizsystem.dao;

import com.quizsystem.model.User;
import com.quizsystem.util.DatabaseConnection;
import com.quizsystem.util.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoTest {
    private final UserDao userDao = new UserDao();

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSupport.resetDatabase();
    }

    @Test
    public void testAuthenticateInvalidUser() throws SQLException {
        User user = userDao.authenticate("nonexistent", "wrongpass");
        assertNull(user, "Should return null for invalid credentials");
    }

    @Test
    void createUserStoresHashedPasswordAndAuthenticates() throws SQLException {
        userDao.createUser(new User(0, "alice", "secret123", "user", "alice@example.com"));

        User user = userDao.authenticate("alice", "secret123");

        assertNotNull(user, "Should authenticate valid credentials");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE username = ?")) {
            stmt.setString(1, "alice");
            var rs = stmt.executeQuery();
            assertTrue(rs.next(), "Stored user should exist");
            String storedPassword = rs.getString("password");
            assertTrue(PasswordUtils.isHashed(storedPassword), "Password should be stored as a hash");
            assertNotEquals("secret123", storedPassword, "Password should not be stored in plaintext");
        }
    }

    @Test
    void authenticateUpgradesLegacyPlaintextPassword() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password, role, email) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, "legacy");
            stmt.setString(2, "legacyPass");
            stmt.setString(3, "user");
            stmt.setString(4, "legacy@example.com");
            stmt.executeUpdate();
        }

        User user = userDao.authenticate("legacy", "legacyPass");

        assertNotNull(user, "Legacy plaintext user should still be able to log in once");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE username = ?")) {
            stmt.setString(1, "legacy");
            var rs = stmt.executeQuery();
            assertTrue(rs.next(), "Legacy user should still exist");
            assertTrue(PasswordUtils.isHashed(rs.getString("password")), "Legacy plaintext password should be upgraded");
        }
    }
}
