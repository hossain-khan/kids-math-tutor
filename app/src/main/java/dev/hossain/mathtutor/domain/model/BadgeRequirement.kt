package dev.hossain.mathtutor.domain.model

/**
 * Sealed class representing different types of badge requirements.
 * Each requirement type defines specific criteria that must be met to unlock a badge.
 */
sealed class BadgeRequirement {
    /**
     * Badge requirement based on total number of problems solved.
     *
     * @property count The number of problems that must be solved
     */
    data class ProblemCount(
        val count: Int,
    ) : BadgeRequirement()

    /**
     * Badge requirement based on number of problems solved for a specific operation.
     *
     * @property operation The math operation that must be practiced
     * @property count The number of problems that must be solved for this operation
     */
    data class OperationCount(
        val operation: MathOperation,
        val count: Int,
    ) : BadgeRequirement()

    /**
     * Badge requirement based on consecutive correct answers.
     *
     * @property count The number of consecutive correct answers required
     */
    data class ConsecutiveCorrect(
        val count: Int,
    ) : BadgeRequirement()

    /**
     * Badge requirement based on session accuracy percentage.
     *
     * @property percentage The accuracy percentage required (0.0-100.0, where 100.0 = perfect score)
     * @property sessionCount The number of sessions that must meet the accuracy requirement
     */
    data class SessionAccuracy(
        val percentage: Float,
        val sessionCount: Int = 1,
    ) : BadgeRequirement()

    /**
     * Badge requirement based on daily practice streak.
     *
     * @property days The number of consecutive days that must be practiced
     */
    data class DailyStreak(
        val days: Int,
    ) : BadgeRequirement()

    /**
     * Badge requirement based on problem solving speed.
     *
     * @property maxSeconds The maximum time in seconds to solve a problem
     */
    data class ProblemSpeed(
        val maxSeconds: Int,
    ) : BadgeRequirement()

    /**
     * Badge requirement based on number of mixed operation sessions.
     *
     * @property count The number of mixed operation sessions that must be completed
     */
    data class MixedSessions(
        val count: Int,
    ) : BadgeRequirement()
}
