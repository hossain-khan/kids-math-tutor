package dev.hossain.mathtutor.domain.model

/**
 * Represents a user's answer to a specific problem in a practice session.
 *
 * @property problemId The ID of the problem being answered
 * @property userAnswer The answer provided by the user (null if skipped/unanswered)
 * @property isCorrect Whether the answer was correct
 * @property attemptCount Number of attempts made (for analytics)
 * @property timeSpentSeconds Time spent on this problem in seconds
 */
data class SessionAnswer(
    val problemId: String,
    val userAnswer: Int?,
    val isCorrect: Boolean,
    val attemptCount: Int = 1,
    val timeSpentSeconds: Long = 0,
)
