package dev.hossain.mathtutor.domain.model

import java.time.Instant

/**
 * Represents a complete practice session containing multiple problems and their answers.
 *
 * @property totalProblems The total number of problems in this session
 * @property problems List of math problems for this session
 * @property answers Map of problem IDs to user answers
 * @property operation The math operation practiced in this session (null for mixed operations)
 * @property durationSeconds Duration of the session in seconds (null if not completed)
 * @property completedAt Timestamp when the session was completed (null if not completed)
 */
data class PracticeSession(
    val totalProblems: Int = 10,
    val problems: List<MathProblem>,
    val answers: MutableMap<String, SessionAnswer> = mutableMapOf(),
    val operation: MathOperation? = null,
    val durationSeconds: Long? = null,
    val completedAt: Instant? = null,
) {
    /**
     * Gets the count of correct answers in this session.
     *
     * @return Number of correct answers
     */
    fun getCorrectCount(): Int = answers.values.count { it.isCorrect }

    /**
     * Gets the count of incorrect answers in this session.
     * Includes both answered incorrectly and skipped (unanswered) problems.
     *
     * @return Number of incorrect answers
     */
    fun getIncorrectCount(): Int = answers.values.count { !it.isCorrect }

    /**
     * Calculates the accuracy percentage for this session.
     *
     * @return Accuracy as a percentage (0-100), or 0 if no answers yet
     */
    fun getAccuracy(): Float {
        if (answers.isEmpty()) return 0f
        return (getCorrectCount().toFloat() / answers.size) * 100
    }

    /**
     * Checks if the session is complete.
     *
     * A session is considered complete when it has a completion timestamp.
     *
     * @return true if the session is complete, false otherwise
     */
    fun isComplete(): Boolean = completedAt != null
}
