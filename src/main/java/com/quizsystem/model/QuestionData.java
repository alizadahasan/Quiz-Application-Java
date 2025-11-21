package com.quizsystem.model;

/**
 * Represents the data for a quiz question, used for creating questions without IDs.
 */
public class QuestionData {
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
     * Constructs a new QuestionData with the specified attributes.
     *
     * @param questionText  The text of the question.
     * @param optionA       The text of option A.
     * @param optionB       The text of option B.
     * @param optionC       The text of option C.
     * @param optionD       The text of option D.
     * @param correctAnswer The correct answer.
     */
    public QuestionData(String questionText, String optionA, String optionB, String optionC, String optionD, String correctAnswer) {
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
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
     * Gets the text of option A.
     *
     * @return The text of option A.
     */
    public String getOptionA() {
        return optionA;
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
     * Gets the text of option C.
     *
     * @return The text of option C.
     */
    public String getOptionC() {
        return optionC;
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
     * Gets the correct answer.
     *
     * @return The correct answer.
     */
    public String getCorrectAnswer() {
        return correctAnswer;
    }
}