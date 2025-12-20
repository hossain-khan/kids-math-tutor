package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BadgeRequirementTest {
    @Test
    fun `ProblemCount requirement has correct count`() {
        val requirement = BadgeRequirement.ProblemCount(25)

        assertThat(requirement.count).isEqualTo(25)
    }

    @Test
    fun `OperationCount requirement has correct operation and count`() {
        val requirement = BadgeRequirement.OperationCount(MathOperation.ADDITION, 50)

        assertThat(requirement.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(requirement.count).isEqualTo(50)
    }

    @Test
    fun `ConsecutiveCorrect requirement has correct count`() {
        val requirement = BadgeRequirement.ConsecutiveCorrect(5)

        assertThat(requirement.count).isEqualTo(5)
    }

    @Test
    fun `SessionAccuracy requirement has correct percentage and default sessionCount`() {
        val requirement = BadgeRequirement.SessionAccuracy(90f)

        assertThat(requirement.percentage).isWithin(0.01f).of(90f)
        assertThat(requirement.sessionCount).isEqualTo(1)
    }

    @Test
    fun `SessionAccuracy requirement has correct percentage and custom sessionCount`() {
        val requirement = BadgeRequirement.SessionAccuracy(100f, 3)

        assertThat(requirement.percentage).isWithin(0.01f).of(100f)
        assertThat(requirement.sessionCount).isEqualTo(3)
    }

    @Test
    fun `DailyStreak requirement has correct days`() {
        val requirement = BadgeRequirement.DailyStreak(7)

        assertThat(requirement.days).isEqualTo(7)
    }

    @Test
    fun `ProblemSpeed requirement has correct maxSeconds`() {
        val requirement = BadgeRequirement.ProblemSpeed(3)

        assertThat(requirement.maxSeconds).isEqualTo(3)
    }

    @Test
    fun `MixedSessions requirement has correct count`() {
        val requirement = BadgeRequirement.MixedSessions(10)

        assertThat(requirement.count).isEqualTo(10)
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
        assertThat(problemCount.count).isEqualTo(10)
        assertThat(operationCount.operation).isEqualTo(MathOperation.SUBTRACTION)
        assertThat(operationCount.count).isEqualTo(20)
        assertThat(consecutiveCorrect.count).isEqualTo(5)
        assertThat(sessionAccuracy.percentage).isEqualTo(85f)
        assertThat(sessionAccuracy.sessionCount).isEqualTo(2)
        assertThat(dailyStreak.days).isEqualTo(3)
        assertThat(problemSpeed.maxSeconds).isEqualTo(5)
        assertThat(mixedSessions.count).isEqualTo(8)
    }
}
