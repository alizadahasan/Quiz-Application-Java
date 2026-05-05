package com.quizsystem.dao;

import com.quizsystem.model.LeaderboardEntry;
import com.quizsystem.model.Result;
import com.quizsystem.model.Question;
import com.quizsystem.model.QuizAnalyticsSummary;
import com.quizsystem.model.QuizHistoryEntry;
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
        validateResult(result, questions);

        String sql = "INSERT INTO results (user_id, quiz_id, score, completion_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, result.getUserId());
                    stmt.setInt(2, result.getQuizId());
                    stmt.setInt(3, result.getScore());
                    stmt.setString(4, result.getCompletionTime());
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            result.setResultId(rs.getInt(1));
                        } else {
                            throw new SQLException("Failed to retrieve generated result ID");
                        }
                    }
                }

                String answerSql = "INSERT INTO user_answers (result_id, question_id, user_answer) VALUES (?, ?, ?)";
                try (PreparedStatement answerStmt = conn.prepareStatement(answerSql)) {
                    for (int i = 0; i < result.getUserAnswers().size(); i++) {
                        answerStmt.setInt(1, result.getResultId());
                        answerStmt.setInt(2, questions.get(i).getQuestionId());
                        answerStmt.setString(3, result.getUserAnswers().get(i));
                        answerStmt.addBatch();
                    }
                    answerStmt.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void validateResult(Result result, List<Question> questions) throws SQLException {
        if (result == null) {
            throw new SQLException("Result cannot be null");
        }
        if (result.getUserAnswers() == null) {
            throw new SQLException("User answers cannot be null");
        }
        if (questions == null) {
            throw new SQLException("Questions cannot be null");
        }
        if (result.getUserAnswers().size() != questions.size()) {
            throw new SQLException("User answers count must match question count");
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

    public List<QuizHistoryEntry> getQuizHistoryByUserId(int userId) throws SQLException {
        String sql = """
                SELECT r.result_id, q.title, r.score, r.completion_time
                FROM results r
                JOIN quizzes q ON r.quiz_id = q.quiz_id
                WHERE r.user_id = ?
                ORDER BY r.completion_time DESC, r.result_id DESC
                """;
        List<QuizHistoryEntry> historyEntries = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    historyEntries.add(new QuizHistoryEntry(
                            rs.getInt("result_id"),
                            rs.getString("title"),
                            rs.getInt("score"),
                            rs.getString("completion_time")
                    ));
                }
            }
        }
        return historyEntries;
    }

    public List<LeaderboardEntry> getGlobalLeaderboard() throws SQLException {
        String sql = """
                SELECT u.username, SUM(r.score) AS total_score
                FROM results r
                JOIN users u ON r.user_id = u.user_id
                GROUP BY r.user_id, u.username
                ORDER BY total_score DESC
                LIMIT 10
                """;
        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                leaderboardEntries.add(new LeaderboardEntry(
                        rs.getString("username"),
                        rs.getInt("total_score"),
                        null
                ));
            }
        }
        return leaderboardEntries;
    }

    public List<LeaderboardEntry> getQuizLeaderboard(int quizId) throws SQLException {
        String sql = """
                SELECT u.username, r.score, r.completion_time
                FROM results r
                JOIN users u ON r.user_id = u.user_id
                WHERE r.quiz_id = ?
                ORDER BY r.score DESC
                LIMIT 10
                """;
        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    leaderboardEntries.add(new LeaderboardEntry(
                            rs.getString("username"),
                            rs.getInt("score"),
                            rs.getString("completion_time")
                    ));
                }
            }
        }
        return leaderboardEntries;
    }

    public QuizAnalyticsSummary getQuizAnalytics(int quizId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS attempt_count,
                       COUNT(DISTINCT user_id) AS participant_count,
                       COALESCE(MAX(score), 0) AS highest_score,
                       COALESCE(AVG(score), 0) AS average_score,
                       MAX(completion_time) AS latest_completion_time
                FROM results
                WHERE quiz_id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return new QuizAnalyticsSummary(0, 0, 0, 0.0, null);
                }
                return new QuizAnalyticsSummary(
                        rs.getInt("attempt_count"),
                        rs.getInt("participant_count"),
                        rs.getInt("highest_score"),
                        rs.getDouble("average_score"),
                        rs.getString("latest_completion_time")
                );
            }
        }
    }
}
