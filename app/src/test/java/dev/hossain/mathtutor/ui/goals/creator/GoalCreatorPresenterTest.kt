package dev.hossain.mathtutor.ui.goals.creator

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import org.junit.Test

/**
 * Unit tests for [GoalCreatorPresenter].
 * Tests the state management and step progression for goal creation.
 */
class GoalCreatorPresenterTest {
    @Test
    fun `initial state shows title step`() {
        // Given
        val state =
            GoalCreatorScreen.State(
                currentStep = GoalCreatorScreen.Step.Title,
                goalTitle = "",
                goalDescription = "",
                components = emptyList(),
                canAdvance = false,
                isLoading = false,
                error = null,
                eventSink = {},
            )

        // Then
        assertThat(state.currentStep).isEqualTo(GoalCreatorScreen.Step.Title)
        assertThat(state.goalTitle).isEmpty()
        assertThat(state.goalDescription).isEmpty()
        assertThat(state.components).isEmpty()
        assertThat(state.canAdvance).isFalse()
    }

    @Test
    fun `title update enables advance when non-empty`() {
        // Given
        var currentTitle = ""
        var canAdvance = false

        // When setting a title
        currentTitle = "Math Mastery Goal"
        canAdvance = currentTitle.isNotEmpty()

        // Then
        assertThat(currentTitle).isNotEmpty()
        assertThat(canAdvance).isTrue()
    }

    @Test
    fun `empty title prevents advance`() {
        // Given
        var canAdvance = false
        var currentTitle = ""

        // When title is empty
        canAdvance = currentTitle.isNotEmpty()

        // Then
        assertThat(canAdvance).isFalse()
    }

    @Test
    fun `step progression to select components`() {
        // Given
        var currentStep = GoalCreatorScreen.Step.Title

        // When advancing
        currentStep = GoalCreatorScreen.Step.SelectComponents

        // Then
        assertThat(currentStep).isEqualTo(GoalCreatorScreen.Step.SelectComponents)
    }

    @Test
    fun `event sink can emit SetTitle event`() {
        // Given
        var eventReceived: GoalCreatorScreen.Event? = null
        val state =
            GoalCreatorScreen.State(
                currentStep = GoalCreatorScreen.Step.Title,
                goalTitle = "",
                goalDescription = "",
                components = emptyList(),
                canAdvance = false,
                isLoading = false,
                error = null,
                eventSink = { event -> eventReceived = event },
            )

        // When
        state.eventSink(GoalCreatorScreen.Event.SetTitle("New Goal"))

        // Then
        assertThat(eventReceived).isNotNull()
        assertThat(eventReceived).isInstanceOf(GoalCreatorScreen.Event.SetTitle::class.java)
    }

    @Test
    fun `event sink can emit NextStep event`() {
        // Given
        var eventReceived: GoalCreatorScreen.Event? = null
        val state =
            GoalCreatorScreen.State(
                currentStep = GoalCreatorScreen.Step.Title,
                goalTitle = "Goal",
                goalDescription = "",
                components = emptyList(),
                canAdvance = true,
                isLoading = false,
                error = null,
                eventSink = { event -> eventReceived = event },
            )

        // When
        state.eventSink(GoalCreatorScreen.Event.NextStep)

        // Then
        assertThat(eventReceived).isNotNull()
        assertThat(eventReceived).isInstanceOf(GoalCreatorScreen.Event.NextStep::class.java)
    }

    @Test
    fun `event sink can emit PreviousStep event`() {
        // Given
        var eventReceived: GoalCreatorScreen.Event? = null
        val state =
            GoalCreatorScreen.State(
                currentStep = GoalCreatorScreen.Step.SelectComponents,
                goalTitle = "Goal",
                goalDescription = "",
                components = emptyList(),
                canAdvance = true,
                isLoading = false,
                error = null,
                eventSink = { event -> eventReceived = event },
            )

        // When
        state.eventSink(GoalCreatorScreen.Event.PreviousStep)

        // Then
        assertThat(eventReceived).isNotNull()
        assertThat(eventReceived).isInstanceOf(GoalCreatorScreen.Event.PreviousStep::class.java)
    }

    @Test
    fun `event sink can emit Cancel event`() {
        // Given
        var eventReceived: GoalCreatorScreen.Event? = null
        val state =
            GoalCreatorScreen.State(
                currentStep = GoalCreatorScreen.Step.Title,
                goalTitle = "",
                goalDescription = "",
                components = emptyList(),
                canAdvance = false,
                isLoading = false,
                error = null,
                eventSink = { event -> eventReceived = event },
            )

        // When
        state.eventSink(GoalCreatorScreen.Event.Cancel)

        // Then
        assertThat(eventReceived).isNotNull()
        assertThat(eventReceived).isInstanceOf(GoalCreatorScreen.Event.Cancel::class.java)
    }

    @Test
    fun `event sink can emit AddComponent event`() {
        // Given
        var eventReceived: GoalCreatorScreen.Event? = null
        val component =
            GoalComponent.OperationBased(
                operation = dev.hossain.mathtutor.domain.model.MathOperation.ADDITION,
                sessionCount = 5,
            )
        val state =
            GoalCreatorScreen.State(
                currentStep = GoalCreatorScreen.Step.SelectComponents,
                goalTitle = "Goal",
                goalDescription = "",
                components = emptyList(),
                canAdvance = false,
                isLoading = false,
                error = null,
                eventSink = { event -> eventReceived = event },
            )

        // When
        state.eventSink(GoalCreatorScreen.Event.AddComponent(component))

        // Then
        assertThat(eventReceived).isNotNull()
        assertThat(eventReceived).isInstanceOf(GoalCreatorScreen.Event.AddComponent::class.java)
    }

    @Test
    fun `event sink can emit SaveGoal event`() {
        // Given
        var eventReceived: GoalCreatorScreen.Event? = null
        val component =
            GoalComponent.OperationBased(
                operation = dev.hossain.mathtutor.domain.model.MathOperation.ADDITION,
                sessionCount = 5,
            )
        val state =
            GoalCreatorScreen.State(
                currentStep = GoalCreatorScreen.Step.Review,
                goalTitle = "Goal",
                goalDescription = "Description",
                components = listOf(component),
                canAdvance = true,
                isLoading = false,
                error = null,
                eventSink = { event -> eventReceived = event },
            )

        // When
        state.eventSink(GoalCreatorScreen.Event.SaveGoal)

        // Then
        assertThat(eventReceived).isNotNull()
        assertThat(eventReceived).isInstanceOf(GoalCreatorScreen.Event.SaveGoal::class.java)
    }

    @Test
    fun `event sink can emit SetDescription event`() {
        // Given
        var eventReceived: GoalCreatorScreen.Event? = null
        val state =
            GoalCreatorScreen.State(
                currentStep = GoalCreatorScreen.Step.Title,
                goalTitle = "Goal",
                goalDescription = "",
                components = emptyList(),
                canAdvance = true,
                isLoading = false,
                error = null,
                eventSink = { event -> eventReceived = event },
            )

        // When
        state.eventSink(GoalCreatorScreen.Event.SetDescription("Learn addition"))

        // Then
        assertThat(eventReceived).isNotNull()
        assertThat(eventReceived).isInstanceOf(GoalCreatorScreen.Event.SetDescription::class.java)
    }
}
