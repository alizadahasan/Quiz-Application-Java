package com.quizsystem.model;

/**
 * Represents one completed quiz attempt in a user's history.
 */
public record QuizHistoryEntry(int resultId, String quizTitle, int score, String completionTime) {
}
