package com.quizsystem.dao;

import com.quizsystem.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

public class UserDaoTest {
    private final UserDao userDao = new UserDao();

    @Test
    public void testAuthenticateInvalidUser() throws SQLException {
        User user = userDao.authenticate("nonexistent", "wrongpass");
        assertNull(user, "Should return null for invalid credentials");
    }
}
