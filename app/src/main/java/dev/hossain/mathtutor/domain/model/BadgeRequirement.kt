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

    // ==================== Game Badges ====================

    /**
     * Badge requirement based on total number of games played.
     *
     * @property count The number of games that must be played (any game type)
     */
    data class GameCount(
        val count: Int,
    ) : BadgeRequirement()

    /**
     * Badge requirement based on Math Race score.
     *
     * @property minScore The minimum score required in a single Math Race game
     */
    data class MathRaceScore(
        val minScore: Int,
    ) : BadgeRequirement()

    /**
     * Badge requirement for achieving 100% accuracy in any game.
     * Requires all answers in a single game session to be correct.
     */
    data object PerfectGameAccuracy : BadgeRequirement()

    // ==================== Memory Match Badges ====================

    /**
     * Badge requirement for completing Memory Match games.
     *
     * @property count The number of Memory Match games that must be completed
     */
    data class MemoryMatchCount(
        val count: Int,
    ) : BadgeRequirement()

    /**
     * Badge requirement for completing Memory Match with limited moves.
     *
     * @property maxMoves The maximum number of moves allowed to unlock this badge
     */
    data class MemoryMatchMoves(
        val maxMoves: Int,
    ) : BadgeRequirement()

    /**
     * Badge requirement for completing Memory Match quickly.
     *
     * @property maxSeconds The maximum time in seconds to complete the game
     */
    data class MemoryMatchTime(
        val maxSeconds: Int,
    ) : BadgeRequirement()

    /**
     * Badge requirement for completing Memory Match with perfect moves (8 moves = minimum possible).
     * Requires completing the game in exactly 8 moves (matching all 8 pairs on first try).
     */
    data object PerfectMemoryMatch : BadgeRequirement()
}
