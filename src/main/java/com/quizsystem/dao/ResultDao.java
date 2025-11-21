package com.quizsystem.dao;

import com.quizsystem.model.Result;
import com.quizsystem.model.Question;
import com.quizsystem.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for result-related database operations.
 * Provides methods to create and retrieve quiz results, including user answers.
 */
public class ResultDao {

    /**
     * Creates a new result in the database, including user answers, and sets its generated ID.
     *
     * @param result   The result to create.
     * @param questions The list of questions associated with the result.
     * @throws SQLException If a database error occurs during creation.
     */
    public void createResult(Result result, List<Question> questions) throws SQLException {
        String sql = "INSERT INTO results (user_id, quiz_id, score, completion_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, result.getUserId());
            stmt.setInt(2, result.getQuizId());
            stmt.setInt(3, result.getScore());
            stmt.setString(4, result.getCompletionTime());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                result.setResultId(rs.getInt(1));
            }

            // Save user answers
            String answerSql = "INSERT INTO user_answers (result_id, question_id, user_answer) VALUES (?, ?, ?)";
            try (PreparedStatement answerStmt = conn.prepareStatement(answerSql)) {
                for (int i = 0; i < result.getUserAnswers().size(); i++) {
                    answerStmt.setInt(1, result.getResultId());
                    answerStmt.setInt(2, questions.get(i).getQuestionId());
                    answerStmt.setString(3, result.getUserAnswers().get(i));
                    answerStmt.executeUpdate();
                }
            }
        }
    }

    /**
     * Retrieves a result by its ID, including user answers.
     *
     * @param resultId The ID of the result to retrieve.
     * @return The result, or null if not found.
     * @throws SQLException If a database error occurs during retrieval.
     */
    public Result getResultById(int resultId) throws SQLException {
        String sql = "SELECT * FROM results WHERE result_id = ?";
        Result result = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resultId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result = new Result(
                        rs.getInt("result_id"),
                        rs.getInt("quiz_id"),
                        rs.getInt("user_id"),
                        rs.getInt("score"),
                        rs.getString("completion_time")
                );
                // Load user answers
                String answerSql = "SELECT user_answer FROM user_answers WHERE result_id = ? ORDER BY answer_id";
                try (PreparedStatement answerStmt = conn.prepareStatement(answerSql)) {
                    answerStmt.setInt(1, resultId);
                    ResultSet answerRs = answerStmt.executeQuery();
                    List<String> userAnswers = new ArrayList<>();
                    while (answerRs.next()) {
                        userAnswers.add(answerRs.getString("user_answer"));
                    }
                    result.setUserAnswers(userAnswers);
                }
            }
        }
        return result;
    }
}