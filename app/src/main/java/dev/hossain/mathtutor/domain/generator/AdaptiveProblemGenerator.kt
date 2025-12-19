package dev.hossain.mathtutor.domain.generator

import dev.hossain.mathtutor.domain.model.DifficultyAdjustment
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.OperationPerformance
import dev.hossain.mathtutor.domain.repository.PerformanceRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Adaptive problem generator that adjusts difficulty based on user performance.
 *
 * This generator tracks user performance for each operation and automatically
 * adjusts the grade level of generated problems:
 * - When accuracy is high (≥85%) with sufficient attempts (≥20), difficulty increases
 * - When accuracy is low (<50%) with sufficient attempts (≥10), difficulty decreases
 * - Otherwise, difficulty stays at the user's configured grade level
 *
 * Grade level boundaries are respected:
 * - Kindergarten is the minimum (cannot decrease below)
 * - Grade 2 is the maximum (cannot increase above)
 */
@SingleIn(AppScope::class)
@Inject
class AdaptiveProblemGenerator
    constructor(
        private val gradeAwareProblemGenerator: GradeAwareProblemGenerator,
        private val performanceRepository: PerformanceRepository,
    ) {
        /**
         * Generates problems with adaptive difficulty based on user performance.
         *
         * @param count Number of problems to generate
         * @param operation The math operation to generate problems for
         * @param baseGradeLevel The user's configured grade level (baseline)
         * @return Pair of generated problems and the actual grade level used (may be adjusted)
         */
        suspend fun generateAdaptiveProblems(
            count: Int,
            operation: MathOperation,
            baseGradeLevel: GradeLevel,
        ): AdaptiveProblemsResult {
            // Get current performance for this operation
            val performance = performanceRepository.getPerformance(operation, baseGradeLevel).first()

            // Determine adjusted grade level based on performance
            val adjustedGradeLevel = getAdjustedGradeLevel(baseGradeLevel, performance)
            val adjustment = performance.getRecommendedAdjustment()

            Timber.d(
                "Adaptive difficulty: operation=$operation, baseGrade=$baseGradeLevel, " +
                    "adjustedGrade=$adjustedGradeLevel, recentAccuracy=${performance.recentAccuracy}, " +
                    "recentAttempts=${performance.recentAttempts}, adjustment=$adjustment",
            )

            // Generate problems at the adjusted level
            val problems =
                gradeAwareProblemGenerator.generateProblems(
                    count = count,
                    operation = operation,
                    gradeLevel = adjustedGradeLevel,
                )

            return AdaptiveProblemsResult(
                problems = problems,
                actualGradeLevel = adjustedGradeLevel,
                baseGradeLevel = baseGradeLevel,
                adjustment = adjustment,
                performance = performance,
            )
        }

        /**
         * Gets the adjusted grade level based on performance.
         *
         * @param baseGradeLevel The user's configured grade level
         * @param performance The performance data for the operation
         * @return The adjusted grade level
         */
        private fun getAdjustedGradeLevel(
            baseGradeLevel: GradeLevel,
            performance: OperationPerformance,
        ): GradeLevel =
            when (performance.getRecommendedAdjustment()) {
                DifficultyAdjustment.HARDER -> getNextGradeLevel(baseGradeLevel)
                DifficultyAdjustment.EASIER -> getPreviousGradeLevel(baseGradeLevel)
                DifficultyAdjustment.CURRENT -> baseGradeLevel
            }

        /**
         * Gets the next higher grade level.
         * Grade 2 is the maximum, so Grade 2 returns Grade 2.
         *
         * @param gradeLevel Current grade level
         * @return Next higher grade level, or current if at maximum
         */
        fun getNextGradeLevel(gradeLevel: GradeLevel): GradeLevel =
            when (gradeLevel) {
                GradeLevel.KINDERGARTEN -> GradeLevel.GRADE_1
                GradeLevel.GRADE_1 -> GradeLevel.GRADE_2
                GradeLevel.GRADE_2 -> GradeLevel.GRADE_2 // Max level
            }

        /**
         * Gets the previous lower grade level.
         * Kindergarten is the minimum, so Kindergarten returns Kindergarten.
         *
         * @param gradeLevel Current grade level
         * @return Previous lower grade level, or current if at minimum
         */
        fun getPreviousGradeLevel(gradeLevel: GradeLevel): GradeLevel =
            when (gradeLevel) {
                GradeLevel.KINDERGARTEN -> GradeLevel.KINDERGARTEN

                // Min level
                GradeLevel.GRADE_1 -> GradeLevel.KINDERGARTEN

                GradeLevel.GRADE_2 -> GradeLevel.GRADE_1
            }
    }

/**
 * Result of adaptive problem generation.
 *
 * @property problems The generated math problems
 * @property actualGradeLevel The grade level actually used for generation (may be adjusted)
 * @property baseGradeLevel The user's configured base grade level
 * @property adjustment The difficulty adjustment that was applied
 * @property performance The performance data used to make the adjustment decision
 */
data class AdaptiveProblemsResult(
    val problems: List<MathProblem>,
    val actualGradeLevel: GradeLevel,
    val baseGradeLevel: GradeLevel,
    val adjustment: DifficultyAdjustment,
    val performance: OperationPerformance,
) {
    /**
     * Returns true if the difficulty was adjusted (either increased or decreased).
     */
    val wasAdjusted: Boolean
        get() = adjustment != DifficultyAdjustment.CURRENT

    /**
     * Returns true if difficulty was increased.
     */
    val wasIncreased: Boolean
        get() = adjustment == DifficultyAdjustment.HARDER

    /**
     * Returns true if difficulty was decreased.
     */
    val wasDecreased: Boolean
        get() = adjustment == DifficultyAdjustment.EASIER
}
