package com.quizsystem.service;

import com.quizsystem.dao.QuestionDao;
import com.quizsystem.model.Question;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for managing question-related operations, interacting with the QuestionDao.
 */
public class QuestionService {
    /** Data access object for question database operations. */
    private final QuestionDao questionDao;

    /**
     * Constructs a QuestionService with a new QuestionDao instance.
     */
    public QuestionService() {
        this.questionDao = new QuestionDao();
    }

    /**
     * Creates a new question for the specified quiz in the database.
     *
     * @param quizId        The ID of the quiz.
     * @param questionText  The text of the question.
     * @param optionA       The text of option A.
     * @param optionB       The text of option B.
     * @param optionC       The text of option C.
     * @param optionD       The text of option D.
     * @param correctAnswer The correct answer (e.g., "A", "B", "C", or "D").
     * @throws SQLException If a database error occurs during creation.
     */
    public void createQuestion(int quizId, String questionText, String optionA, String optionB,
                               String optionC, String optionD, String correctAnswer) throws SQLException {
        Question question = new Question(0, quizId, questionText, optionA, optionB, optionC, optionD, correctAnswer);
        questionDao.createQuestion(question);
    }

    /**
     * Retrieves all questions associated with the specified quiz ID.
     *
     * @param quizId The ID of the quiz.
     * @return A list of questions for the quiz.
     * @throws SQLException If a database error occurs during retrieval.
     */
    public List<Question> getQuestionsByQuizId(int quizId) throws SQLException {
        return questionDao.getQuestionsByQuizId(quizId);
    }
}