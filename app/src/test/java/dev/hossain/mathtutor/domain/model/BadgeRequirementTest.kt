package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BadgeRequirementTest {
    @Test
    fun `ProblemCount requirement has correct count`() {
        val requirement = BadgeRequirement.ProblemCount(25)

        assertEquals(25, requirement.count)
    }

    @Test
    fun `OperationCount requirement has correct operation and count`() {
        val requirement = BadgeRequirement.OperationCount(MathOperation.ADDITION, 50)

        assertEquals(MathOperation.ADDITION, requirement.operation)
        assertEquals(50, requirement.count)
    }

    @Test
    fun `ConsecutiveCorrect requirement has correct count`() {
        val requirement = BadgeRequirement.ConsecutiveCorrect(5)

        assertEquals(5, requirement.count)
    }

    @Test
    fun `SessionAccuracy requirement has correct percentage and default sessionCount`() {
        val requirement = BadgeRequirement.SessionAccuracy(90f)

        assertEquals(90f, requirement.percentage, 0.01f)
        assertEquals(1, requirement.sessionCount)
    }

    @Test
    fun `SessionAccuracy requirement has correct percentage and custom sessionCount`() {
        val requirement = BadgeRequirement.SessionAccuracy(100f, 3)

        assertEquals(100f, requirement.percentage, 0.01f)
        assertEquals(3, requirement.sessionCount)
    }

    @Test
    fun `DailyStreak requirement has correct days`() {
        val requirement = BadgeRequirement.DailyStreak(7)

        assertEquals(7, requirement.days)
    }

    @Test
    fun `ProblemSpeed requirement has correct maxSeconds`() {
        val requirement = BadgeRequirement.ProblemSpeed(3)

        assertEquals(3, requirement.maxSeconds)
    }

    @Test
    fun `MixedSessions requirement has correct count`() {
        val requirement = BadgeRequirement.MixedSessions(10)

        assertEquals(10, requirement.count)
    }

    @Test
    fun `requirement types are correctly distinguished`() {
        val problemCount = BadgeRequirement.ProblemCount(10)
        val operationCount = BadgeRequirement.OperationCount(MathOperation.SUBTRACTION, 20)
        val consecutiveCorrect = BadgeRequirement.ConsecutiveCorrect(5)
        val sessionAccuracy = BadgeRequirement.SessionAccuracy(85f, 2)
        val dailyStreak = BadgeRequirement.DailyStreak(3)
        val problemSpeed = BadgeRequirement.ProblemSpeed(5)
        val mixedSessions = BadgeRequirement.MixedSessions(8)

        // Verify each subtype can be constructed and has expected values
        assertEquals(10, problemCount.count)
        assertEquals(MathOperation.SUBTRACTION, operationCount.operation)
        assertEquals(20, operationCount.count)
        assertEquals(5, consecutiveCorrect.count)
        assertEquals(85f, sessionAccuracy.percentage)
        assertEquals(2, sessionAccuracy.sessionCount)
        assertEquals(3, dailyStreak.days)
        assertEquals(5, problemSpeed.maxSeconds)
        assertEquals(8, mixedSessions.count)
    }
}
