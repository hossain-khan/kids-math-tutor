package dev.hossain.mathtutor.domain.model.goals

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Test

/**
 * Unit tests for goal completion logic.
 * Tests the logic for determining when a goal is complete.
 */
class GoalCompletionLogicTest {
    @Test
    fun `single component goal is complete when component sessions match required sessions`() {
        // Given
        val goal = Goal(
            id = "goal-1",
            title = "Addition Practice",
            components = listOf(
                GoalComponent.OperationBased(MathOperation.ADDITION, sessionCount = 5),
            ),
        )
        val componentProgress = listOf(
            ComponentProgress(
                componentIndex = 0,
                completedSessions = 5,
                totalSessions = 5,
            ),
        )

        // When
        val isGoalComplete = componentProgress.all { it.completedSessions >= it.totalSessions } &&
                             componentProgress.size == goal.components.size

        // Then
        assertThat(isGoalComplete).isTrue()
    }

    @Test
    fun `single component goal is incomplete when sessions are less than required`() {
        // Given
        val goal = Goal(
            id = "goal-1",
            title = "Addition Practice",
            components = listOf(
                GoalComponent.OperationBased(MathOperation.ADDITION, sessionCount = 5),
            ),
        )
        val componentProgress = listOf(
            ComponentProgress(
                componentIndex = 0,
                completedSessions = 3,
                totalSessions = 5,
            ),
        )

        // When
        val isGoalComplete = componentProgress.all { it.completedSessions >= it.totalSessions } &&
                             componentProgress.size == goal.components.size

        // Then
        assertThat(isGoalComplete).isFalse()
    }

    @Test
    fun `multi-component goal is complete only when all components are complete`() {
        // Given
        val goal = Goal(
            id = "goal-1",
            title = "Math Master",
            components = listOf(
                GoalComponent.OperationBased(MathOperation.ADDITION, sessionCount = 5),
                GoalComponent.OperationBased(MathOperation.SUBTRACTION, sessionCount = 5),
                GoalComponent.OperationBased(MathOperation.MULTIPLICATION, sessionCount = 5),
            ),
        )
        val componentProgress = listOf(
            ComponentProgress(componentIndex = 0, completedSessions = 5, totalSessions = 5),
            ComponentProgress(componentIndex = 1, completedSessions = 5, totalSessions = 5),
            ComponentProgress(componentIndex = 2, completedSessions = 3, totalSessions = 5),
        )

        // When
        val isGoalComplete = componentProgress.all { it.completedSessions >= it.totalSessions } &&
                             componentProgress.size == goal.components.size

        // Then
        assertThat(isGoalComplete).isFalse()
    }

    @Test
    fun `multi-component goal is complete when all components are complete`() {
        // Given
        val goal = Goal(
            id = "goal-1",
            title = "Math Master",
            components = listOf(
                GoalComponent.OperationBased(MathOperation.ADDITION, sessionCount = 5),
                GoalComponent.OperationBased(MathOperation.SUBTRACTION, sessionCount = 5),
                GoalComponent.OperationBased(MathOperation.MULTIPLICATION, sessionCount = 5),
            ),
        )
        val componentProgress = listOf(
            ComponentProgress(componentIndex = 0, completedSessions = 5, totalSessions = 5),
            ComponentProgress(componentIndex = 1, completedSessions = 5, totalSessions = 5),
            ComponentProgress(componentIndex = 2, completedSessions = 5, totalSessions = 5),
        )

        // When
        val isGoalComplete = componentProgress.all { it.completedSessions >= it.totalSessions } &&
                             componentProgress.size == goal.components.size

        // Then
        assertThat(isGoalComplete).isTrue()
    }

    @Test
    fun `goal with custom challenge component can be completed`() {
        // Given
        val goal = Goal(
            id = "goal-1",
            title = "Challenge Master",
            components = listOf(
                GoalComponent.CustomChallengeBased(
                    challengeId = "challenge-1",
                    challengeTitle = "Worksheet 1",
                    sessionCount = 3,
                ),
            ),
        )
        val componentProgress = listOf(
            ComponentProgress(
                componentIndex = 0,
                completedSessions = 3,
                totalSessions = 3,
            ),
        )

        // When
        val isGoalComplete = componentProgress.all { it.completedSessions >= it.totalSessions } &&
                             componentProgress.size == goal.components.size

        // Then
        assertThat(isGoalComplete).isTrue()
    }

    @Test
    fun `goal with mixed component types tracks completion correctly`() {
        // Given
        val goal = Goal(
            id = "goal-1",
            title = "Mixed Practice",
            components = listOf(
                GoalComponent.OperationBased(MathOperation.ADDITION, sessionCount = 5),
                GoalComponent.CustomChallengeBased(
                    challengeId = "challenge-1",
                    challengeTitle = "Worksheet 1",
                    sessionCount = 3,
                ),
            ),
        )
        val componentProgress = listOf(
            ComponentProgress(componentIndex = 0, completedSessions = 5, totalSessions = 5),
            ComponentProgress(componentIndex = 1, completedSessions = 3, totalSessions = 3),
        )

        // When
        val isGoalComplete = componentProgress.all { it.completedSessions >= it.totalSessions } &&
                             componentProgress.size == goal.components.size

        // Then
        assertThat(isGoalComplete).isTrue()
    }
}
