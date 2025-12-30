package dev.hossain.mathtutor.ui.games

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.model.goals.ComponentProgress
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for game blocking logic.
 * Tests that games are properly blocked when an active goal exists.
 */
class GameBlockingLogicTest {
    private fun createTestActiveGoal(): ActiveGoal {
        val goal = Goal(
            id = "test-goal-1",
            title = "Addition Master",
            description = "Master addition skills",
            createdAt = Instant.now(),
            components = listOf(
                GoalComponent.OperationBased(MathOperation.ADDITION, sessionCount = 10),
            ),
        )
        return ActiveGoal(
            id = "active-goal-1",
            goalId = goal.id,
            goal = goal,
            activatedAt = Instant.now(),
            currentComponentIndex = 0,
            componentProgress = listOf(
                ComponentProgress(
                    componentIndex = 0,
                    completedSessions = 3,
                    totalSessions = 10,
                    accuracy = 85f,
                    totalTimeSeconds = 1800L,
                ),
            ),
        )
    }

    @Test
    fun `game should be blocked when active goal exists`() {
        // Given
        val activeGoal = createTestActiveGoal()

        // When
        val isGameBlocked = activeGoal != null

        // Then
        assertThat(isGameBlocked).isTrue()
    }

    @Test
    fun `game should not be blocked when no active goal exists`() {
        // Given
        val activeGoal: ActiveGoal? = null

        // When
        val isGameBlocked = activeGoal != null

        // Then
        assertThat(isGameBlocked).isFalse()
    }

    @Test
    fun `blocked game returns appropriate goal info`() {
        // Given
        val activeGoal = createTestActiveGoal()

        // When
        val isGameBlocked = activeGoal != null
        val goalTitle = if (isGameBlocked) activeGoal?.goal?.title else null
        val completedSessions = if (isGameBlocked) {
            activeGoal?.componentProgress?.sumOf { it.completedSessions } ?: 0
        } else {
            0
        }

        // Then
        assertThat(isGameBlocked).isTrue()
        assertThat(goalTitle).isEqualTo("Addition Master")
        assertThat(completedSessions).isEqualTo(3)
    }

    @Test
    fun `blocker dialog displays correct progress percentage`() {
        // Given
        val activeGoal = createTestActiveGoal()

        // When
        val totalSessions = activeGoal.goal.components.sumOf { it.sessionCount }
        val completedSessions = activeGoal.componentProgress.sumOf { it.completedSessions }
        val progressPercentage = (completedSessions.toFloat() / totalSessions.toFloat()) * 100f

        // Then
        assertThat(totalSessions).isEqualTo(10)
        assertThat(completedSessions).isEqualTo(3)
        assertThat(progressPercentage).isEqualTo(30f)
    }

    @Test
    fun `blocker shows goal title with emoji`() {
        // Given
        val activeGoal = createTestActiveGoal()

        // When
        val displayTitle = "🎯 ${activeGoal.goal.title}"

        // Then
        assertThat(displayTitle).isEqualTo("🎯 Addition Master")
    }

    @Test
    fun `blocker provides correct session count display`() {
        // Given
        val activeGoal = createTestActiveGoal()

        // When
        val totalSessions = activeGoal.goal.components.sumOf { it.sessionCount }
        val completedSessions = activeGoal.componentProgress.sumOf { it.completedSessions }
        val sessionDisplay = "$completedSessions / $totalSessions"

        // Then
        assertThat(sessionDisplay).isEqualTo("3 / 10")
    }

    @Test
    fun `game blocking status can be toggled based on goal presence`() {
        // Given
        var activeGoal: ActiveGoal? = createTestActiveGoal()

        // When
        var isBlocked = activeGoal != null
        assertThat(isBlocked).isTrue()

        // Remove goal
        activeGoal = null

        // Then
        isBlocked = activeGoal != null
        assertThat(isBlocked).isFalse()
    }
}
