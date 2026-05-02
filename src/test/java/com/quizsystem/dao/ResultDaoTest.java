package com.quizsystem.dao;

import com.quizsystem.model.Question;
import com.quizsystem.model.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ResultDaoTest {

    private final ResultDao resultDao = new ResultDao();

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSupport.resetDatabase();
    }

    @Test
    void createResultRejectsMismatchedAnswerCount() {
        Result result = new Result(0, 1, 1, 0, "2026-04-28 00:00:00");
        result.setUserAnswers(List.of("A"));

        Question questionOne = new Question(1, 1, "First?", "A", "B", "C", "D", "A");
        Question questionTwo = new Question(2, 1, "Second?", "A", "B", "C", "D", "B");

        assertThrows(
                SQLException.class,
                () -> resultDao.createResult(result, List.of(questionOne, questionTwo))
        );
    }
}
