package com.quizsystem.dao;

import com.quizsystem.model.LeaderboardEntry;
import com.quizsystem.model.Question;
import com.quizsystem.model.QuizHistoryEntry;
import com.quizsystem.model.Result;
import com.quizsystem.util.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void createResultPersistsAnswersAndCanBeLoadedBack() throws SQLException {
        int userId = insertUser("result_user", "result_user@example.com");
        int quizId = insertQuiz(userId, "Math Quiz");
        Question question = insertQuestion(quizId, "2 + 2?", "4", "3", "5", "6", "A");

        Result result = new Result(0, quizId, userId, 1, "2026-05-04 11:00:00");
        result.setUserAnswers(List.of("A"));

        resultDao.createResult(result, List.of(question));
        Result storedResult = resultDao.getResultById(result.getResultId());

        assertNotNull(storedResult, "Stored result should be retrievable");
        assertEquals(quizId, storedResult.getQuizId());
        assertEquals(userId, storedResult.getUserId());
        assertEquals(1, storedResult.getScore());
        assertEquals(List.of("A"), storedResult.getUserAnswers());
    }

    @Test
    void historyAndLeaderboardQueriesReturnExpectedRows() throws SQLException {
        int userOneId = insertUser("alice", "alice-leaderboard@example.com");
        int userTwoId = insertUser("bob", "bob-leaderboard@example.com");
        int quizId = insertQuiz(userOneId, "Science Quiz");
        Question question = insertQuestion(quizId, "Planet?", "Earth", "Mars", "Venus", "Jupiter", "A");

        Result aliceResult = new Result(0, quizId, userOneId, 1, "2026-05-04 12:00:00");
        aliceResult.setUserAnswers(List.of("A"));
        resultDao.createResult(aliceResult, List.of(question));

        Result bobResult = new Result(0, quizId, userTwoId, 0, "2026-05-04 12:05:00");
        bobResult.setUserAnswers(List.of("B"));
        resultDao.createResult(bobResult, List.of(question));

        List<QuizHistoryEntry> history = resultDao.getQuizHistoryByUserId(userOneId);
        List<LeaderboardEntry> globalLeaderboard = resultDao.getGlobalLeaderboard();
        List<LeaderboardEntry> quizLeaderboard = resultDao.getQuizLeaderboard(quizId);

        assertEquals(1, history.size(), "User history should include the saved result");
        assertEquals(aliceResult.getResultId(), history.getFirst().resultId());
        assertEquals("Science Quiz", history.getFirst().quizTitle());

        assertEquals(2, globalLeaderboard.size(), "Global leaderboard should include both players");
        assertEquals("alice", globalLeaderboard.getFirst().username());
        assertEquals(1, globalLeaderboard.getFirst().score());

        assertEquals(2, quizLeaderboard.size(), "Quiz leaderboard should include both quiz attempts");
        assertEquals("alice", quizLeaderboard.getFirst().username());
        assertEquals(1, quizLeaderboard.getFirst().score());
        assertEquals("2026-05-04 12:00:00", quizLeaderboard.getFirst().completionTime());
    }

    private int insertUser(String username, String email) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, "placeholder");
            stmt.setString(3, "user");
            stmt.setString(4, email);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int insertQuiz(int createdBy, String title) throws SQLException {
        String sql = "INSERT INTO quizzes (title, description, created_by, time_limit) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, title);
            stmt.setString(2, "Generated for tests");
            stmt.setInt(3, createdBy);
            stmt.setInt(4, 30);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private Question insertQuestion(int quizId, String prompt, String optionA, String optionB,
                                    String optionC, String optionD, String correctAnswer) throws SQLException {
        String sql = """
                INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, quizId);
            stmt.setString(2, prompt);
            stmt.setString(3, optionA);
            stmt.setString(4, optionB);
            stmt.setString(5, optionC);
            stmt.setString(6, optionD);
            stmt.setString(7, correctAnswer);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return new Question(rs.getInt(1), quizId, prompt, optionA, optionB, optionC, optionD, correctAnswer);
            }
        }
    }
}
