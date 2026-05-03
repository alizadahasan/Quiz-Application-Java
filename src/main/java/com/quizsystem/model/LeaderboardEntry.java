package com.quizsystem.model;

/**
 * Represents a leaderboard row for either a quiz-specific or global ranking.
 */
public record LeaderboardEntry(String username, int score, String completionTime) {
}
