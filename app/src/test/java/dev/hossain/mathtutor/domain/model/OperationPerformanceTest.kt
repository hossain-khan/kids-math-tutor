package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [OperationPerformance].
 */
class OperationPerformanceTest {
    @Test
    fun `shouldIncreaseDifficulty returns true when accuracy high and sufficient attempts`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_1,
                totalAttempts = 25,
                correctAnswers = 22,
                recentAccuracy = 0.90f, // 90%
                recentAttempts = 20,
            )

        assertThat(performance.shouldIncreaseDifficulty()).isTrue()
    }

    @Test
    fun `shouldIncreaseDifficulty returns false when at max grade level`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_2, // Max level
                totalAttempts = 25,
                correctAnswers = 22,
                recentAccuracy = 0.90f,
                recentAttempts = 20,
            )

        assertThat(performance.shouldIncreaseDifficulty()).isFalse()
    }

    @Test
    fun `shouldIncreaseDifficulty returns false when not enough attempts`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_1,
                totalAttempts = 15,
                correctAnswers = 14,
                recentAccuracy = 0.93f,
                recentAttempts = 15, // Less than 20
            )

        assertThat(performance.shouldIncreaseDifficulty()).isFalse()
    }

    @Test
    fun `shouldIncreaseDifficulty returns false when accuracy below threshold`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_1,
                totalAttempts = 25,
                correctAnswers = 18,
                recentAccuracy = 0.72f, // Below 85%
                recentAttempts = 25,
            )

        assertThat(performance.shouldIncreaseDifficulty()).isFalse()
    }

    @Test
    fun `shouldDecreaseDifficulty returns true when accuracy low and sufficient attempts`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_1,
                totalAttempts = 15,
                correctAnswers = 5,
                recentAccuracy = 0.40f, // 40%, below 50%
                recentAttempts = 10,
            )

        assertThat(performance.shouldDecreaseDifficulty()).isTrue()
    }

    @Test
    fun `shouldDecreaseDifficulty returns false when at min grade level`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.KINDERGARTEN, // Min level
                totalAttempts = 15,
                correctAnswers = 5,
                recentAccuracy = 0.30f,
                recentAttempts = 15,
            )

        assertThat(performance.shouldDecreaseDifficulty()).isFalse()
    }

    @Test
    fun `shouldDecreaseDifficulty returns false when not enough attempts`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_1,
                totalAttempts = 5,
                correctAnswers = 2,
                recentAccuracy = 0.40f,
                recentAttempts = 5, // Less than 10
            )

        assertThat(performance.shouldDecreaseDifficulty()).isFalse()
    }

    @Test
    fun `shouldDecreaseDifficulty returns false when accuracy at threshold`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_1,
                totalAttempts = 15,
                correctAnswers = 7,
                recentAccuracy = 0.50f, // Exactly 50%, should NOT decrease
                recentAttempts = 15,
            )

        assertThat(performance.shouldDecreaseDifficulty()).isFalse()
    }

    @Test
    fun `getRecommendedAdjustment returns HARDER when should increase`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.KINDERGARTEN,
                totalAttempts = 25,
                correctAnswers = 22,
                recentAccuracy = 0.88f,
                recentAttempts = 20,
            )

        assertThat(performance.getRecommendedAdjustment().isEqualTo(DifficultyAdjustment.HARDER))
    }

    @Test
    fun `getRecommendedAdjustment returns EASIER when should decrease`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_2,
                totalAttempts = 15,
                correctAnswers = 5,
                recentAccuracy = 0.35f,
                recentAttempts = 10,
            )

        assertThat(performance.getRecommendedAdjustment().isEqualTo(DifficultyAdjustment.EASIER))
    }

    @Test
    fun `getRecommendedAdjustment returns CURRENT when no change needed`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_1,
                totalAttempts = 15,
                correctAnswers = 10,
                recentAccuracy = 0.67f, // Between 50% and 85%
                recentAttempts = 15,
            )

        assertThat(performance.getRecommendedAdjustment().isEqualTo(DifficultyAdjustment.CURRENT))
    }

    @Test
    fun `overallAccuracy calculates correctly`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_1,
                totalAttempts = 20,
                correctAnswers = 15,
                recentAccuracy = 0.75f,
                recentAttempts = 10,
            )

        assertThat(performance.overallAccuracy).isWithin(0.01f).of(75f)
    }

    @Test
    fun `overallAccuracy returns 0 when no attempts`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_1,
                totalAttempts = 0,
                correctAnswers = 0,
                recentAccuracy = 0f,
                recentAttempts = 0,
            )

        assertThat(performance.overallAccuracy).isWithin(0.01f).of(0f)
    }

    @Test
    fun `empty creates performance with zero stats`() {
        val performance =
            OperationPerformance.empty(
                operation = MathOperation.SUBTRACTION,
                gradeLevel = GradeLevel.GRADE_2,
            )

        assertThat(performance.operation).isEqualTo(MathOperation.SUBTRACTION)
        assertThat(performance.gradeLevel).isEqualTo(GradeLevel.GRADE_2)
        assertThat(performance.totalAttempts).isEqualTo(0)
        assertThat(performance.correctAnswers).isEqualTo(0)
        assertThat(performance.recentAccuracy).isWithin(0.01f).of(0f)
        assertThat(performance.recentAttempts).isEqualTo(0)
    }

    @Test
    fun `shouldIncreaseDifficulty boundary test - exactly 85 percent and 20 attempts`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_1,
                totalAttempts = 20,
                correctAnswers = 17,
                recentAccuracy = 0.85f, // Exactly 85%
                recentAttempts = 20, // Exactly 20
            )

        assertThat(performance.shouldIncreaseDifficulty()).isTrue()
    }

    @Test
    fun `shouldIncreaseDifficulty boundary test - just below threshold`() {
        val performance =
            OperationPerformance(
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_1,
                totalAttempts = 20,
                correctAnswers = 17,
                recentAccuracy = 0.849f, // Just below 85%
                recentAttempts = 20,
            )

        assertThat(performance.shouldIncreaseDifficulty()).isFalse()
    }
}
