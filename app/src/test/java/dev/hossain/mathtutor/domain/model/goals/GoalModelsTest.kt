package dev.hossain.mathtutor.domain.model.goals

import dev.hossain.mathtutor.domain.model.MathOperation
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ComponentProgressTest {
    @Test
    fun `isComplete returns true when completedSessions equals totalSessions`() {
        val progress =
            ComponentProgress(
                componentIndex = 0,
                completedSessions = 2,
                totalSessions = 2,
                accuracy = 0f,
                totalTimeSeconds = 0L,
                sessionResults = emptyList(),
            )
        assertTrue(progress.isComplete())
    }

    @Test
    fun `isComplete returns false when completedSessions less than totalSessions`() {
        val progress =
            ComponentProgress(
                componentIndex = 0,
                completedSessions = 1,
                totalSessions = 2,
                accuracy = 0f,
                totalTimeSeconds = 0L,
                sessionResults = emptyList(),
            )
        assertFalse(progress.isComplete())
    }

    @Test
    fun `getProgressFraction returns correct value`() {
        val progress =
            ComponentProgress(
                componentIndex = 0,
                completedSessions = 2,
                totalSessions = 4,
                accuracy = 0f,
                totalTimeSeconds = 0L,
                sessionResults = emptyList(),
            )
        assertEquals(0.5f, progress.getProgressFraction())
    }

    @Test
    fun `getProgressFraction returns 0 when totalSessions is 0`() {
        val progress =
            ComponentProgress(
                componentIndex = 0,
                completedSessions = 0,
                totalSessions = 0,
                accuracy = 0f,
                totalTimeSeconds = 0L,
                sessionResults = emptyList(),
            )
        assertEquals(0f, progress.getProgressFraction())
    }

    @Test
    fun `getAverageTimePerSession returns correct value`() {
        val progress =
            ComponentProgress(
                componentIndex = 0,
                completedSessions = 2,
                totalSessions = 4,
                accuracy = 0f,
                totalTimeSeconds = 600L,
                sessionResults = emptyList(),
            )
        assertEquals(300L, progress.getAverageTimePerSession())
    }

    @Test
    fun `getAverageTimePerSession returns 0 when no sessions completed`() {
        val progress =
            ComponentProgress(
                componentIndex = 0,
                completedSessions = 0,
                totalSessions = 0,
                accuracy = 0f,
                totalTimeSeconds = 0L,
                sessionResults = emptyList(),
            )
        assertEquals(0L, progress.getAverageTimePerSession())
    }
}

class GoalComponentTest {
    @Test
    fun `OperationBased getDescription returns formatted string`() {
        val component =
            GoalComponent.OperationBased(
                operation = MathOperation.ADDITION,
                sessionCount = 2,
            )
        assertEquals("Addition (2 x Sessions)", component.getDescription())
    }

    @Test
    fun `CustomChallengeBased getDescription returns formatted string`() {
        val component =
            GoalComponent.CustomChallengeBased(
                challengeId = "1",
                challengeTitle = "Worksheet 1",
                sessionCount = 3,
            )
        assertEquals("Worksheet 1 (3 x Sessions)", component.getDescription())
    }
}

class GoalTest {
    @Test
    fun `getTotalSessions returns sum of all component sessionCounts`() {
        val goal =
            Goal(
                title = "Test Goal",
                components =
                    listOf(
                        GoalComponent.OperationBased(MathOperation.ADDITION, 2),
                        GoalComponent.OperationBased(MathOperation.SUBTRACTION, 3),
                    ),
            )
        assertEquals(5, goal.getTotalSessions())
    }

    @Test
    fun `getTotalSessions returns 0 for empty components`() {
        val goal =
            Goal(
                title = "Test Goal",
                components = emptyList(),
            )
        assertEquals(0, goal.getTotalSessions())
    }

    @Test
    fun `getComponentsSummary returns comma separated descriptions`() {
        val goal =
            Goal(
                title = "Test Goal",
                components =
                    listOf(
                        GoalComponent.OperationBased(MathOperation.ADDITION, 2),
                        GoalComponent.OperationBased(MathOperation.SUBTRACTION, 1),
                    ),
            )
        assertEquals("Addition (2 x Sessions), Subtraction (1 x Sessions)", goal.getComponentsSummary())
    }
}

class ActiveGoalTest {
    @Test
    fun `getOverallProgress returns correct fraction`() {
        val componentProgress1 =
            ComponentProgress(
                componentIndex = 0,
                completedSessions = 2,
                totalSessions = 2,
                accuracy = 0f,
                totalTimeSeconds = 0L,
                sessionResults = emptyList(),
            )
        val componentProgress2 =
            ComponentProgress(
                componentIndex = 1,
                completedSessions = 1,
                totalSessions = 3,
                accuracy = 0f,
                totalTimeSeconds = 0L,
                sessionResults = emptyList(),
            )

        val activeGoal =
            ActiveGoal(
                goalId = "1",
                goal =
                    Goal(
                        title = "Test",
                        components =
                            listOf(
                                GoalComponent.OperationBased(MathOperation.ADDITION, 2),
                                GoalComponent.OperationBased(MathOperation.SUBTRACTION, 3),
                            ),
                    ),
                componentProgress = listOf(componentProgress1, componentProgress2),
            )

        val expected = 3f / 5f // 3 completed out of 5 total
        assertEquals(expected, activeGoal.getOverallProgress())
    }

    @Test
    fun `getTotalCompletedSessions returns sum of all component completions`() {
        val componentProgress1 =
            ComponentProgress(
                componentIndex = 0,
                completedSessions = 2,
                totalSessions = 2,
                accuracy = 0f,
                totalTimeSeconds = 0L,
                sessionResults = emptyList(),
            )
        val componentProgress2 =
            ComponentProgress(
                componentIndex = 1,
                completedSessions = 1,
                totalSessions = 3,
                accuracy = 0f,
                totalTimeSeconds = 0L,
                sessionResults = emptyList(),
            )

        val activeGoal =
            ActiveGoal(
                goalId = "1",
                goal =
                    Goal(
                        title = "Test",
                        components = emptyList(),
                    ),
                componentProgress = listOf(componentProgress1, componentProgress2),
            )

        assertEquals(3, activeGoal.getTotalCompletedSessions())
    }

    @Test
    fun `getOverallAccuracy returns weighted average`() {
        val componentProgress1 =
            ComponentProgress(
                componentIndex = 0,
                completedSessions = 2,
                totalSessions = 2,
                accuracy = 90f,
                totalTimeSeconds = 0L,
                sessionResults = emptyList(),
            )
        val componentProgress2 =
            ComponentProgress(
                componentIndex = 1,
                completedSessions = 2,
                totalSessions = 3,
                accuracy = 80f,
                totalTimeSeconds = 0L,
                sessionResults = emptyList(),
            )

        val activeGoal =
            ActiveGoal(
                goalId = "1",
                goal = Goal(title = "Test", components = emptyList()),
                componentProgress = listOf(componentProgress1, componentProgress2),
            )

        val expected = ((90f * 2) + (80f * 2)) / 4
        assertEquals(expected, activeGoal.getOverallAccuracy())
    }
}

class GoalErrorTest {
    @Test
    fun `InvalidGoal error message contains provided message`() {
        val error = GoalError.InvalidGoal("Empty title")
        assertEquals("Empty title", error.message)
    }

    @Test
    fun `GoalNotFound error message includes goalId`() {
        val error = GoalError.GoalNotFound("goal123")
        assertEquals("Goal not found: goal123", error.message)
    }

    @Test
    fun `InvalidAccuracy error message includes accuracy value`() {
        val error = GoalError.InvalidAccuracy(150f)
        assertEquals("Invalid accuracy: 150.0. Expected 0-100.", error.message)
    }
}
