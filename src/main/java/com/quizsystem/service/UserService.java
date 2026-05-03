package com.quizsystem.service;

import com.quizsystem.dao.UserDao;
import com.quizsystem.model.User;

import java.sql.SQLException;

/**
 * Service layer for managing user-related operations, interacting with the UserDao.
 */
public class UserService {
    /** Data access object for user database operations. */
    private final UserDao userDao;

    /**
     * Constructs a UserService with a new UserDao instance.
     */
    public UserService() {
        this.userDao = new UserDao();
    }

    /**
     * Authenticates a user by username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return The authenticated user, or null if authentication fails.
     * @throws SQLException If a database error occurs during authentication.
     */
    public User login(String username, String password) throws SQLException {
        return userDao.authenticate(username, password);
    }

    /**
     * Registers a new user in the database.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @param role     The role of the user (e.g., "admin" or "user").
     * @param email    The email address of the user.
     * @throws SQLException If a database error occurs during registration.
     */
    public void register(String username, String password, String role, String email) throws SQLException {
        User user = new User(0, username, password, role, email);
        userDao.createUser(user);
    }

    public boolean isAdminUser(int userId) throws SQLException {
        return userDao.isAdminUser(userId);
    }
}
