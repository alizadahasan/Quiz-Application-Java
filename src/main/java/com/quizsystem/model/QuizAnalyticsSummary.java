package com.quizsystem.model;

/**
 * Aggregated analytics for a single quiz.
 */
public record QuizAnalyticsSummary(
        int attemptCount,
        int participantCount,
        int highestScore,
        double averageScore,
        String latestCompletionTime
) {
}
