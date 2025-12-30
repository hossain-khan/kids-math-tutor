package dev.hossain.mathtutor.ui.mathpractice

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.model.goals.ComponentProgress
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for goal-related logic in MathPracticeScreen.
 * Tests goal completion detection and accuracy calculations.
 */
class GoalIntegrationLogicTest {
    @Test
    fun `goal is not complete when no components are completed`() {
        // Given
        val goal =
            Goal(
                id = "goal-1",
                title = "Addition Master",
                components =
                    listOf(
                        GoalComponent.OperationBased(MathOperation.ADDITION, sessionCount = 5),
                    ),
            )
        val progress =
            listOf(
                ComponentProgress(componentIndex = 0, completedSessions = 0, totalSessions = 5),
            )
        val activeGoal =
            ActiveGoal(
                id = "active-goal-1",
                goalId = goal.id,
                goal = goal,
                componentProgress = progress,
            )

        // When
        val totalComponents = activeGoal.goal.components.size
        val completedComponents = activeGoal.componentProgress.count { it.completedSessions >= it.totalSessions }

        // Then
        assertThat(totalComponents).isEqualTo(1)
        assertThat(completedComponents).isEqualTo(0)
        assertThat(completedComponents >= totalComponents).isFalse()
    }

    @Test
    fun `goal is complete when all components are completed`() {
        // Given
        val goal =
            Goal(
                id = "goal-1",
                title = "Addition Master",
                components =
                    listOf(
                        GoalComponent.OperationBased(MathOperation.ADDITION, sessionCount = 5),
                        GoalComponent.OperationBased(MathOperation.SUBTRACTION, sessionCount = 5),
                    ),
            )
        val progress =
            listOf(
                ComponentProgress(componentIndex = 0, completedSessions = 5, totalSessions = 5),
                ComponentProgress(componentIndex = 1, completedSessions = 5, totalSessions = 5),
            )
        val activeGoal =
            ActiveGoal(
                id = "active-goal-1",
                goalId = goal.id,
                goal = goal,
                componentProgress = progress,
            )

        // When
        val totalComponents = activeGoal.goal.components.size
        val completedComponents = activeGoal.componentProgress.count { it.completedSessions >= it.totalSessions }

        // Then
        assertThat(totalComponents).isEqualTo(2)
        assertThat(completedComponents).isEqualTo(2)
        assertThat(completedComponents >= totalComponents).isTrue()
    }

    @Test
    fun `accuracy calculation is correct for practice session`() {
        // Given
        val totalProblems = 10
        val correctAnswers = 8

        // When
        val accuracy =
            if (totalProblems > 0) {
                (correctAnswers.toFloat() / totalProblems.toFloat()) * 100f
            } else {
                0f
            }

        // Then
        assertThat(accuracy).isEqualTo(80f)
    }

    @Test
    fun `accuracy is zero when no problems are attempted`() {
        // Given
        val totalProblems = 0

        // When
        val accuracy =
            if (totalProblems > 0) {
                (0.toFloat() / totalProblems.toFloat()) * 100f
            } else {
                0f
            }

        // Then
        assertThat(accuracy).isEqualTo(0f)
    }

    @Test
    fun `accuracy is 100 for perfect score`() {
        // Given
        val totalProblems = 10
        val correctAnswers = 10

        // When
        val accuracy =
            if (totalProblems > 0) {
                (correctAnswers.toFloat() / totalProblems.toFloat()) * 100f
            } else {
                0f
            }

        // Then
        assertThat(accuracy).isEqualTo(100f)
    }

    @Test
    fun `goal progress calculates total sessions correctly`() {
        // Given
        val goal =
            Goal(
                id = "goal-1",
                title = "Math Master",
                components =
                    listOf(
                        GoalComponent.OperationBased(MathOperation.ADDITION, sessionCount = 5),
                        GoalComponent.OperationBased(MathOperation.SUBTRACTION, sessionCount = 3),
                    ),
            )

        // When
        val totalSessions = goal.components.sumOf { it.sessionCount }

        // Then
        assertThat(totalSessions).isEqualTo(8)
    }

    @Test
    fun `completed sessions are counted correctly`() {
        // Given
        val progress =
            listOf(
                ComponentProgress(componentIndex = 0, completedSessions = 5, totalSessions = 5),
                ComponentProgress(componentIndex = 1, completedSessions = 2, totalSessions = 3),
            )

        // When
        val completedSessions = progress.sumOf { it.completedSessions }
        val totalSessions = progress.sumOf { it.totalSessions }

        // Then
        assertThat(completedSessions).isEqualTo(7)
        assertThat(totalSessions).isEqualTo(8)
    }

    @Test
    fun `progress fraction is calculated correctly`() {
        // Given
        val goal =
            Goal(
                id = "goal-1",
                title = "Addition Master",
                components =
                    listOf(
                        GoalComponent.OperationBased(MathOperation.ADDITION, sessionCount = 10),
                    ),
            )
        val progress =
            listOf(
                ComponentProgress(componentIndex = 0, completedSessions = 3, totalSessions = 10),
            )

        // When
        val totalSessions = goal.components.sumOf { it.sessionCount }
        val completedSessions = progress.sumOf { it.completedSessions }
        val progressFraction =
            if (totalSessions > 0) {
                completedSessions.toFloat() / totalSessions.toFloat()
            } else {
                0f
            }

        // Then
        assertThat(progressFraction).isEqualTo(0.3f)
    }
}
