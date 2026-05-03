package com.quizsystem.dao;

import com.quizsystem.model.User;
import com.quizsystem.util.DatabaseConnection;
import com.quizsystem.util.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for user-related database operations.
 * Provides methods to authenticate and create users.
 */
public class UserDao {

    /**
     * Authenticates a user by username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return The authenticated user, or null if authentication fails.
     * @throws SQLException If a database error occurs during authentication.
     */
    public User authenticate(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (!PasswordUtils.verifyPassword(password, storedPassword)) {
                    return null;
                }

                if (!PasswordUtils.isHashed(storedPassword)) {
                    storedPassword = PasswordUtils.hashPassword(password);
                    updatePassword(conn, rs.getInt("user_id"), storedPassword);
                }

                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        storedPassword,
                        rs.getString("role"),
                        rs.getString("email")
                );
            }
        }
        return null;
    }

    /**
     * Creates a new user in the database.
     *
     * @param user The user to create.
     * @throws SQLException If a database error occurs during creation.
     */
    public void createUser(User user) throws SQLException {
        String query = "INSERT INTO users (username, password, role, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, PasswordUtils.hashPassword(user.getPassword()));
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getEmail());
            stmt.executeUpdate();
        }
    }

    public boolean isAdminUser(int userId) throws SQLException {
        String query = "SELECT 1 FROM users WHERE user_id = ? AND role = 'admin'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void updatePassword(Connection conn, int userId, String hashedPassword) throws SQLException {
        String query = "UPDATE users SET password = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
}
