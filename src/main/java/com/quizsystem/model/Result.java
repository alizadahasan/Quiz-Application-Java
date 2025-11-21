package com.quizsystem.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a quiz attempt by a user, including score and answers.
 */
public class Result {
    /** The unique ID of the result. */
    private int resultId;

    /** The ID of the quiz. */
    private int quizId;

    /** The ID of the user who took the quiz. */
    private int userId;

    /** The score achieved in the quiz. */
    private int score;

    /** The time when the quiz was completed. */
    private String completionTime;

    /** The list of user answers for the quiz questions. */
    private List<String> userAnswers;

    /**
     * Constructs a new Result with the specified attributes.
     *
     * @param resultId       The unique ID of the result.
     * @param quizId         The ID of the quiz.
     * @param userId         The ID of the user.
     * @param score          The score achieved.
     * @param completionTime The completion time.
     */
    public Result(int resultId, int quizId, int userId, int score, String completionTime) {
        this.resultId = resultId;
        this.quizId = quizId;
        this.userId = userId;
        this.score = score;
        this.completionTime = completionTime;
        this.userAnswers = new ArrayList<>();
    }

    /**
     * Gets the result ID.
     *
     * @return The result ID.
     */
    public int getResultId() {
        return resultId;
    }

    /**
     * Sets the result ID.
     *
     * @param resultId The result ID to set.
     */
    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    /**
     * Gets the quiz ID.
     *
     * @return The quiz ID.
     */
    public int getQuizId() {
        return quizId;
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
     * Gets the score.
     *
     * @return The score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets the completion time.
     *
     * @return The completion time.
     */
    public String getCompletionTime() {
        return completionTime;
    }

    /**
     * Gets the list of user answers.
     *
     * @return The list of user answers.
     */
    public List<String> getUserAnswers() {
        return userAnswers;
    }

    /**
     * Sets the list of user answers.
     *
     * @param userAnswers The list of user answers to set.
     */
    public void setUserAnswers(List<String> userAnswers) {
        this.userAnswers = userAnswers;
    }
}