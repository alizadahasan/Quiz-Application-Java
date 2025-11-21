package com.quizsystem.model;

/**
 * Represents a quiz question with multiple-choice options and a correct answer.
 */
public class Question {
    /** The unique ID of the question. */
    private int questionId;

    /** The ID of the quiz this question belongs to. */
    private int quizId;

    /** The text of the question. */
    private String questionText;

    /** The text of option A. */
    private String optionA;

    /** The text of option B. */
    private String optionB;

    /** The text of option C. */
    private String optionC;

    /** The text of option D. */
    private String optionD;

    /** The correct answer (e.g., "A", "B", "C", or "D"). */
    private String correctAnswer;

    /**
     * Constructs a new Question with the specified attributes.
     *
     * @param questionId    The unique ID of the question.
     * @param quizId        The ID of the quiz.
     * @param questionText  The text of the question.
     * @param optionA       The text of option A.
     * @param optionB       The text of option B.
     * @param optionC       The text of option C.
     * @param optionD       The text of option D.
     * @param correctAnswer The correct answer.
     */
    public Question(int questionId, int quizId, String questionText, String optionA, String optionB, String optionC, String optionD, String correctAnswer) {
        this.questionId = questionId;
        this.quizId = quizId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
    }

    /**
     * Gets the question ID.
     *
     * @return The question ID.
     */
    public int getQuestionId() {
        return questionId;
    }

    /**
     * Sets the question ID.
     *
     * @param questionId The question ID to set.
     */
    public void setQuestionId(int questionId) {
        this.questionId = questionId;
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
     * Gets the question text.
     *
     * @return The question text.
     */
    public String getQuestionText() {
        return questionText;
    }

    /**
     * Sets the question text.
     *
     * @param questionText The question text to set.
     */
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    /**
     * Gets the text of option A.
     *
     * @return The text of option A.
     */
    public String getOptionA() {
        return optionA;
    }

    /**
     * Sets the text of option A.
     *
     * @param optionA The text of option A to set.
     */
    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    /**
     * Gets the text of option B.
     *
     * @return The text of option B.
     */
    public String getOptionB() {
        return optionB;
    }

    /**
     * Sets the text of option B.
     *
     * @param optionB The text of option B to set.
     */
    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    /**
     * Gets the text of option C.
     *
     * @return The text of option C.
     */
    public String getOptionC() {
        return optionC;
    }

    /**
     * Sets the text of option C.
     *
     * @param optionC The text of option C to set.
     */
    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    /**
     * Gets the text of option D.
     *
     * @return The text of option D.
     */
    public String getOptionD() {
        return optionD;
    }

    /**
     * Sets the text of option D.
     *
     * @param optionD The text of option D to set.
     */
    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    /**
     * Gets the correct answer.
     *
     * @return The correct answer.
     */
    public String getCorrectAnswer() {
        return correctAnswer;
    }

    /**
     * Sets the correct answer.
     *
     * @param correctAnswer The correct answer to set.
     */
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}