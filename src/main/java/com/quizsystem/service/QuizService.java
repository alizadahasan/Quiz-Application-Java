package com.quizsystem.service;

import com.quizsystem.dao.QuizDao;
import com.quizsystem.model.Quiz;
import com.quizsystem.model.QuestionData;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for managing quiz-related operations, interacting with the QuizDao.
 */
public class QuizService {
    /** Data access object for quiz database operations. */
    private final QuizDao quizDao;

    /**
     * Constructs a QuizService with a new QuizDao instance.
     */
    public QuizService() {
        this.quizDao = new QuizDao();
    }

    /**
     * Creates a new quiz in the database.
     *
     * @param title       The title of the quiz.
     * @param description The description of the quiz.
     * @param createdBy   The ID of the admin who created the quiz.
     * @param timeLimit   The time limit for the quiz in seconds.
     * @throws SQLException If a database error occurs during creation.
     */
    public void createQuiz(String title, String description, int createdBy, int timeLimit) throws SQLException {
        Quiz quiz = new Quiz(0, title, description, createdBy, timeLimit);
        quizDao.createQuiz(quiz);
    }

    /**
     * Retrieves all quizzes from the database.
     *
     * @return A list of all quizzes.
     * @throws SQLException If a database error occurs during retrieval.
     */
    public List<Quiz> getAllQuizzes() throws SQLException {
        return quizDao.getAllQuizzes();
    }

    /**
     * Deletes a quiz and its associated data from the database.
     *
     * @param quizId The ID of the quiz to delete.
     * @throws SQLException If a database error occurs or the quiz is not found.
     */
    public void deleteQuiz(int quizId) throws SQLException {
        quizDao.deleteQuiz(quizId);
    }

    /**
     * Creates a new quiz with associated questions in the database.
     *
     * @param title       The title of the quiz.
     * @param description The description of the quiz.
     * @param createdBy   The ID of the admin who created the quiz.
     * @param timeLimit   The time limit for the quiz in seconds.
     * @param questions   The list of questions for the quiz.
     * @return The ID of the created quiz.
     * @throws SQLException If a database error occurs during creation.
     */
    public int createQuizWithQuestions(String title, String description, int createdBy, int timeLimit, List<QuestionData> questions) throws SQLException {
        Quiz quiz = new Quiz(0, title, description, createdBy, timeLimit);
        return quizDao.createQuizWithQuestions(quiz, questions);
    }
}