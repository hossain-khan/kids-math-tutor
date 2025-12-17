package dev.hossain.mathtutor.domain.model

/**
 * Represents aggregated statistics from practice sessions.
 *
 * @property totalProblems Total number of problems attempted across all sessions
 * @property correctCount Total number of correct answers across all sessions
 * @property accuracy Calculated accuracy percentage (0-100)
 * @property sessionCount Total number of completed practice sessions
 */
data class SessionStats(
    val totalProblems: Int,
    val correctCount: Int,
    val accuracy: Float,
    val sessionCount: Int,
) {
    /**
     * Calculates star rating (1-5 stars) based on accuracy percentage.
     *
     * Rating scale:
     * - 5 stars: 90-100% accuracy
     * - 4 stars: 80-89% accuracy
     * - 3 stars: 70-79% accuracy
     * - 2 stars: 60-69% accuracy
     * - 1 star: <60% accuracy
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
         * Returns an empty stats object (all zeros) when no sessions exist.
         */
        val EMPTY =
            SessionStats(
                totalProblems = 0,
                correctCount = 0,
                accuracy = 0f,
                sessionCount = 0,
            )
    }
}
