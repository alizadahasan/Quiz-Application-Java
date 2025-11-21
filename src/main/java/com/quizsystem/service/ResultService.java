package com.quizsystem.service;

import com.quizsystem.dao.ResultDao;
import com.quizsystem.model.Question;
import com.quizsystem.model.Result;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for managing result-related operations, interacting with the ResultDao.
 */
public class ResultService {
    /** Data access object for result database operations. */
    private final ResultDao resultDao;

    /**
     * Constructs a ResultService with a new ResultDao instance.
     */
    public ResultService() {
        this.resultDao = new ResultDao();
    }

    /**
     * Saves a quiz result, including user answers, to the database.
     *
     * @param quizId      The ID of the quiz.
     * @param userId      The ID of the user.
     * @param score       The score achieved.
     * @param userAnswers The list of user answers.
     * @param questions   The list of questions associated with the result.
     * @throws SQLException If a database error occurs during saving.
     */
    public void saveResult(int quizId, int userId, int score, List<String> userAnswers, List<Question> questions) throws SQLException {
        Result result = new Result(0, quizId, userId, score, new java.sql.Timestamp(System.currentTimeMillis()).toString());
        result.setUserAnswers(userAnswers);
        resultDao.createResult(result, questions);
    }

    /**
     * Retrieves a result by its ID from the database.
     *
     * @param resultId The ID of the result to retrieve.
     * @return The result, or null if not found.
     * @throws SQLException If a database error occurs during retrieval.
     */
    public Result getResultById(int resultId) throws SQLException {
        return resultDao.getResultById(resultId);
    }
}