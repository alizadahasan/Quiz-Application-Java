package com.quizsystem.model;

/**
 * Represents a user in the quiz system with authentication and role information.
 */
public class User {
    /** The unique ID of the user. */
    private int userId;

    /** The username of the user. */
    private String username;

    /** The password of the user. */
    private String password;

    /** The role of the user (e.g., "admin" or "user"). */
    private String role;

    /** The email address of the user. */
    private String email;

    /**
     * Constructs a new User with the specified attributes.
     *
     * @param userId   The unique ID of the user.
     * @param username The username of the user.
     * @param password The password of the user.
     * @param role     The role of the user.
     * @param email    The email address of the user.
     */
    public User(int userId, String username, String password, String role, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
    }

    /**
     * Gets the user ID.
     *
     * @return The user ID.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId The user ID to set.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Gets the username.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the role.
     *
     * @return The role.
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role.
     *
     * @param role The role to set.
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Gets the email address.
     *
     * @return The email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }
}