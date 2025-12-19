package dev.hossain.mathtutor.domain.model

/**
 * Represents performance statistics for a specific operation and grade level combination.
 *
 * This data class tracks how well a user is performing on a particular type of math problem,
 * and provides recommendations for difficulty adjustments based on recent performance.
 *
 * @property operation The math operation being tracked
 * @property gradeLevel The grade level at which problems are being practiced
 * @property totalAttempts Total number of problems attempted for this operation/grade
 * @property correctAnswers Total number of correct answers
 * @property averageTimeSeconds Average time in seconds to answer a problem
 * @property recentAccuracy Accuracy percentage of the last N problems (typically last 10)
 * @property recentAttempts Number of recent attempts used to calculate recentAccuracy
 */
data class OperationPerformance(
    val operation: MathOperation,
    val gradeLevel: GradeLevel,
    val totalAttempts: Int = 0,
    val correctAnswers: Int = 0,
    val averageTimeSeconds: Float = 0f,
    val recentAccuracy: Float = 0f,
    val recentAttempts: Int = 0,
) {
    companion object {
        /**
         * Minimum number of attempts required before considering difficulty increase.
         */
        const val MIN_ATTEMPTS_FOR_INCREASE = 20

        /**
         * Minimum number of attempts required before considering difficulty decrease.
         */
        const val MIN_ATTEMPTS_FOR_DECREASE = 10

        /**
         * Accuracy threshold (percentage) at or above which difficulty should increase.
         * 85% = 0.85
         */
        const val INCREASE_THRESHOLD = 0.85f

        /**
         * Accuracy threshold (percentage) below which difficulty should decrease.
         * 50% = 0.50
         */
        const val DECREASE_THRESHOLD = 0.50f

        /**
         * Returns an empty performance instance for a given operation and grade level.
         *
         * @param operation The math operation
         * @param gradeLevel The grade level
         * @return An [OperationPerformance] with zero stats
         */
        fun empty(
            operation: MathOperation,
            gradeLevel: GradeLevel,
        ): OperationPerformance =
            OperationPerformance(
                operation = operation,
                gradeLevel = gradeLevel,
            )
    }

    /**
     * Returns the overall accuracy as a percentage (0-100).
     */
    val overallAccuracy: Float
        get() = if (totalAttempts > 0) (correctAnswers.toFloat() / totalAttempts) * 100 else 0f

    /**
     * Determines if the user should move to a harder difficulty level.
     *
     * Criteria:
     * - Recent accuracy >= 85%
     * - Total attempts >= 20
     * - Not already at maximum grade level (Grade 2)
     *
     * @return true if difficulty should be increased, false otherwise
     */
    fun shouldIncreaseDifficulty(): Boolean =
        recentAccuracy >= INCREASE_THRESHOLD &&
            recentAttempts >= MIN_ATTEMPTS_FOR_INCREASE &&
            gradeLevel != GradeLevel.GRADE_2

    /**
     * Determines if the user should move to an easier difficulty level.
     *
     * Criteria:
     * - Recent accuracy < 50%
     * - Total attempts >= 10
     * - Not already at minimum grade level (Kindergarten)
     *
     * @return true if difficulty should be decreased, false otherwise
     */
    fun shouldDecreaseDifficulty(): Boolean =
        recentAccuracy < DECREASE_THRESHOLD &&
            recentAttempts >= MIN_ATTEMPTS_FOR_DECREASE &&
            gradeLevel != GradeLevel.KINDERGARTEN

    /**
     * Gets the recommended difficulty adjustment based on performance.
     *
     * @return [DifficultyAdjustment] indicating the recommended action
     */
    fun getRecommendedAdjustment(): DifficultyAdjustment =
        when {
            shouldIncreaseDifficulty() -> DifficultyAdjustment.HARDER
            shouldDecreaseDifficulty() -> DifficultyAdjustment.EASIER
            else -> DifficultyAdjustment.CURRENT
        }
}
