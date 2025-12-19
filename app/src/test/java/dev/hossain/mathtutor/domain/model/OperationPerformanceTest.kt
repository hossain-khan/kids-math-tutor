package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

        assertTrue(performance.shouldIncreaseDifficulty())
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

        assertFalse(performance.shouldIncreaseDifficulty())
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

        assertFalse(performance.shouldIncreaseDifficulty())
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

        assertFalse(performance.shouldIncreaseDifficulty())
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

        assertTrue(performance.shouldDecreaseDifficulty())
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

        assertFalse(performance.shouldDecreaseDifficulty())
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

        assertFalse(performance.shouldDecreaseDifficulty())
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

        assertFalse(performance.shouldDecreaseDifficulty())
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

        assertEquals(DifficultyAdjustment.HARDER, performance.getRecommendedAdjustment())
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

        assertEquals(DifficultyAdjustment.EASIER, performance.getRecommendedAdjustment())
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

        assertEquals(DifficultyAdjustment.CURRENT, performance.getRecommendedAdjustment())
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

        assertEquals(75f, performance.overallAccuracy, 0.01f)
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

        assertEquals(0f, performance.overallAccuracy, 0.01f)
    }

    @Test
    fun `empty creates performance with zero stats`() {
        val performance =
            OperationPerformance.empty(
                operation = MathOperation.SUBTRACTION,
                gradeLevel = GradeLevel.GRADE_2,
            )

        assertEquals(MathOperation.SUBTRACTION, performance.operation)
        assertEquals(GradeLevel.GRADE_2, performance.gradeLevel)
        assertEquals(0, performance.totalAttempts)
        assertEquals(0, performance.correctAnswers)
        assertEquals(0f, performance.recentAccuracy, 0.01f)
        assertEquals(0, performance.recentAttempts)
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

        assertTrue(performance.shouldIncreaseDifficulty())
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

        assertFalse(performance.shouldIncreaseDifficulty())
    }
}
