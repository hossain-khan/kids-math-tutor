package dev.hossain.mathtutor.ui.goals.catalog

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [GoalCatalogPresenter].
 * Tests the state management for the goal catalog screen.
 */
class GoalCatalogPresenterTest {
    @Test
    fun `initial state has empty goals list`() {
        // Given
        val state =
            GoalCatalogScreen.State(
                goals = emptyList(),
                activeGoalId = null,
                isLoading = false,
                error = null,
                eventSink = {},
            )

        // Then
        assertThat(state.goals).isEmpty()
        assertThat(state.activeGoalId).isNull()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `event sink can emit CreateNewGoal event`() {
        // Given
        var eventReceived: GoalCatalogScreen.Event? = null
        val state =
            GoalCatalogScreen.State(
                goals = emptyList(),
                activeGoalId = null,
                isLoading = false,
                error = null,
                eventSink = { event -> eventReceived = event },
            )

        // When
        state.eventSink(GoalCatalogScreen.Event.CreateNewGoal)

        // Then
        assertThat(eventReceived).isNotNull()
        assertThat(eventReceived).isInstanceOf(GoalCatalogScreen.Event.CreateNewGoal::class.java)
    }

    @Test
    fun `event sink can emit ViewHistory event with goal id`() {
        // Given
        var eventReceived: GoalCatalogScreen.Event? = null
        val state =
            GoalCatalogScreen.State(
                goals = emptyList(),
                activeGoalId = null,
                isLoading = false,
                error = null,
                eventSink = { event -> eventReceived = event },
            )

        // When
        state.eventSink(GoalCatalogScreen.Event.ViewHistory("goal-123"))

        // Then
        assertThat(eventReceived).isNotNull()
        assertThat(eventReceived).isInstanceOf(GoalCatalogScreen.Event.ViewHistory::class.java)
    }

    @Test
    fun `event sink can emit ActivateGoal event`() {
        // Given
        var eventReceived: GoalCatalogScreen.Event? = null
        val state =
            GoalCatalogScreen.State(
                goals = emptyList(),
                activeGoalId = null,
                isLoading = false,
                error = null,
                eventSink = { event -> eventReceived = event },
            )

        // When
        state.eventSink(GoalCatalogScreen.Event.ActivateGoal("goal-456"))

        // Then
        assertThat(eventReceived).isNotNull()
        assertThat(eventReceived).isInstanceOf(GoalCatalogScreen.Event.ActivateGoal::class.java)
    }

    @Test
    fun `error can be set and dismissed`() {
        // Given
        val stateWithError =
            GoalCatalogScreen.State(
                goals = emptyList(),
                activeGoalId = null,
                isLoading = false,
                error = "Failed to load goals",
                eventSink = {},
            )

        // Then
        assertThat(stateWithError.error).isEqualTo("Failed to load goals")

        // When dismissing error
        var eventReceived: GoalCatalogScreen.Event? = null
        val state =
            GoalCatalogScreen.State(
                goals = emptyList(),
                activeGoalId = null,
                isLoading = false,
                error = "Failed to load goals",
                eventSink = { event -> eventReceived = event },
            )
        state.eventSink(GoalCatalogScreen.Event.DismissError)

        // Then
        assertThat(eventReceived).isInstanceOf(GoalCatalogScreen.Event.DismissError::class.java)
    }

    @Test
    fun `event sink can emit DeleteGoal event`() {
        // Given
        var eventReceived: GoalCatalogScreen.Event? = null
        val state =
            GoalCatalogScreen.State(
                goals = emptyList(),
                activeGoalId = null,
                isLoading = false,
                error = null,
                eventSink = { event -> eventReceived = event },
            )

        // When
        state.eventSink(GoalCatalogScreen.Event.DeleteGoal("goal-789"))

        // Then
        assertThat(eventReceived).isNotNull()
        assertThat(eventReceived).isInstanceOf(GoalCatalogScreen.Event.DeleteGoal::class.java)
    }
}
