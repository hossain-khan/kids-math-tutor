package dev.hossain.mathtutor.domain.model

import java.time.Instant

/**
 * Domain model representing a completed game session.
 * Captures all the statistics and metadata from a single game play.
 *
 * @property id Unique identifier for this game session (0 for new sessions)
 * @property game The type of game that was played
 * @property startTime When the game session started
 * @property endTime When the game session ended
 * @property score Number of correct answers (points earned)
 * @property correctAnswers Number of problems answered correctly
 * @property totalAttempts Total number of problems attempted (correct + incorrect)
 * @property durationSeconds Actual duration of the game session in seconds
 * @property gradeLevel The grade level at which the game was played
 * @property isNewRecord Whether this session achieved a new personal best
 */
data class GameSession(
    val id: Long = 0,
    val game: Game,
    val startTime: Instant,
    val endTime: Instant,
    val score: Int,
    val correctAnswers: Int,
    val totalAttempts: Int,
    val durationSeconds: Int,
    val gradeLevel: GradeLevel,
    val isNewRecord: Boolean = false,
) {
    /**
     * Calculates the accuracy percentage for this game session.
     *
     * @return Accuracy as a percentage (0.0 - 100.0), or 0.0 if no attempts
     */
    val accuracy: Float
        get() =
            if (totalAttempts > 0) {
                (correctAnswers.toFloat() / totalAttempts) * 100f
            } else {
                0f
            }

    /**
     * Calculates the average time spent per problem.
     *
     * @return Average seconds per problem, or 0.0 if no attempts
     */
    val averageTimePerProblem: Float
        get() =
            if (totalAttempts > 0) {
                durationSeconds.toFloat() / totalAttempts
            } else {
                0f
            }

    /**
     * Calculates the problems solved per minute rate.
     *
     * @return Problems per minute, or 0.0 if duration is 0
     */
    val problemsPerMinute: Float
        get() =
            if (durationSeconds > 0) {
                (totalAttempts.toFloat() / durationSeconds) * 60f
            } else {
                0f
            }

    /**
     * Checks if the player achieved a perfect game (100% accuracy).
     *
     * @return true if all attempts were correct, false otherwise
     */
    val isPerfectGame: Boolean
        get() = totalAttempts > 0 && correctAnswers == totalAttempts

    /**
     * Returns a star rating (1-5) based on accuracy.
     * Uses the same scale as SessionStats for consistency.
     *
     * @return Number of stars (1-5)
     */
    fun getStarRating(): Int =
        when {
            accuracy >= 90f -> 5
            accuracy >= 80f -> 4
            accuracy >= 70f -> 3
            accuracy >= 60f -> 2
            else -> 1
        }

    companion object {
        /**
         * Creates a new GameSession with the current time as start time.
         * Useful for starting a new game.
         *
         * @param game The type of game to play
         * @param gradeLevel The grade level to use for problem generation
         * @return A new GameSession with default values and current start time
         */
        fun startNew(
            game: Game,
            gradeLevel: GradeLevel,
        ): GameSession =
            GameSession(
                game = game,
                startTime = Instant.now(),
                endTime = Instant.now(),
                score = 0,
                correctAnswers = 0,
                totalAttempts = 0,
                durationSeconds = 0,
                gradeLevel = gradeLevel,
            )
    }
}
