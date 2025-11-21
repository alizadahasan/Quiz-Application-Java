package com.quizsystem.model;

/**
 * Represents a quiz with a title, description, creator, and time limit.
 */
public class Quiz {
    /** The unique ID of the quiz. */
    private int quizId;

    /** The title of the quiz. */
    private String title;

    /** The description of the quiz. */
    private String description;

    /** The ID of the user who created the quiz. */
    private int createdBy;

    /** The time limit for the quiz in seconds. */
    private int timeLimit;

    /**
     * Constructs a new Quiz with the specified attributes.
     *
     * @param quizId      The unique ID of the quiz.
     * @param title       The title of the quiz.
     * @param description The description of the quiz.
     * @param createdBy   The ID of the user who created the quiz.
     * @param timeLimit   The time limit in seconds.
     */
    public Quiz(int quizId, String title, String description, int createdBy, int timeLimit) {
        this.quizId = quizId;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.timeLimit = timeLimit;
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
     * Sets the quiz ID.
     *
     * @param quizId The quiz ID to set.
     */
    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    /**
     * Gets the quiz title.
     *
     * @return The quiz title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the quiz title.
     *
     * @param title The quiz title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the quiz description.
     *
     * @return The quiz description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the quiz description.
     *
     * @param description The quiz description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the ID of the user who created the quiz.
     *
     * @return The creator’s user ID.
     */
    public int getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the ID of the user who created the quiz.
     *
     * @param createdBy The creator’s user ID to set.
     */
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Gets the time limit for the quiz.
     *
     * @return The time limit in seconds.
     */
    public int getTimeLimit() {
        return timeLimit;
    }

    /**
     * Sets the time limit for the quiz.
     *
     * @param timeLimit The time limit in seconds to set.
     */
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
}