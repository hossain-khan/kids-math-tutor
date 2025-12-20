package dev.hossain.mathtutor.domain.model

import java.time.Instant

/**
 * Aggregated statistics for a specific game type.
 * Provides an overview of the user's performance and progress for a particular game.
 *
 * @property game The game type these statistics are for
 * @property personalBest The highest score ever achieved in this game
 * @property totalGamesPlayed Total number of times this game has been played
 * @property averageScore Average score across all game sessions
 * @property bestAccuracy Highest accuracy percentage achieved
 * @property lastPlayedAt Timestamp of when this game was last played, null if never played
 * @property totalCorrectAnswers Total correct answers across all sessions
 * @property totalAttempts Total problems attempted across all sessions
 */
data class GameStats(
    val game: Game,
    val personalBest: Int,
    val totalGamesPlayed: Int,
    val averageScore: Float,
    val bestAccuracy: Float,
    val lastPlayedAt: Instant?,
    val totalCorrectAnswers: Int = 0,
    val totalAttempts: Int = 0,
) {
    /**
     * Calculates the overall accuracy across all game sessions.
     *
     * @return Overall accuracy as a percentage (0.0 - 100.0), or 0.0 if no attempts
     */
    val overallAccuracy: Float
        get() =
            if (totalAttempts > 0) {
                (totalCorrectAnswers.toFloat() / totalAttempts) * 100f
            } else {
                0f
            }

    /**
     * Checks if the user has ever played this game.
     *
     * @return true if the game has been played at least once
     */
    val hasPlayed: Boolean
        get() = totalGamesPlayed > 0

    /**
     * Returns a star rating (1-5) based on best accuracy achieved.
     *
     * @return Number of stars (1-5), or 0 if never played
     */
    fun getStarRating(): Int =
        when {
            !hasPlayed -> 0
            bestAccuracy >= 90f -> 5
            bestAccuracy >= 80f -> 4
            bestAccuracy >= 70f -> 3
            bestAccuracy >= 60f -> 2
            else -> 1
        }

    companion object {
        /**
         * Creates empty stats for a game that has never been played.
         *
         * @param game The game type
         * @return GameStats with all zeroed values
         */
        fun empty(game: Game): GameStats =
            GameStats(
                game = game,
                personalBest = 0,
                totalGamesPlayed = 0,
                averageScore = 0f,
                bestAccuracy = 0f,
                lastPlayedAt = null,
                totalCorrectAnswers = 0,
                totalAttempts = 0,
            )
    }
}
