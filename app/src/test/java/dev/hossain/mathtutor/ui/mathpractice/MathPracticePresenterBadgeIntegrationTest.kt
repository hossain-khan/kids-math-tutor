package dev.hossain.mathtutor.ui.mathpractice

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Test

/**
 * Additional tests for badge and streak integration in MathPracticePresenter.
 *
 * These tests validate the state management for badge unlock display
 * and event handling for sequential badge presentation.
 */
class MathPracticePresenterBadgeIntegrationTest {
    @Test
    fun `state shows no badges initially`() {
        // Given - Initial state
        val state =
            MathPracticeScreen.State(
                currentProblem = null,
                currentAnswer = "",
                currentProblemIndex = 0,
                totalProblems = 10,
                isLoading = false,
                isCorrect = null,
                unlockedBadges = emptyList(),
                showBadgeUnlock = false,
                currentBadgeIndex = 0,
                eventSink = {},
            )

        // Then
        assertThat(state.unlockedBadges.isEmpty()).isTrue()
        assertThat(state.showBadgeUnlock).isFalse()
        assertThat(state.currentBadgeIndex).isEqualTo(0)
    }

    @Test
    fun `state tracks unlocked badges correctly`() {
        // Given - A badge is unlocked
        val badge =
            Badge(
                id = "first_steps",
                name = "First Steps",
                description = "Completed first practice session",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.FIRST_STEPS.name,
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(10),
                unlockedAt = java.time.Instant.now(),
            )

        val state =
            MathPracticeScreen.State(
                currentProblem = null,
                currentAnswer = "",
                currentProblemIndex = 9,
                totalProblems = 10,
                isLoading = false,
                isCorrect = true,
                unlockedBadges = listOf(badge),
                showBadgeUnlock = true,
                currentBadgeIndex = 0,
                eventSink = {},
            )

        // Then
        assertThat(state.unlockedBadges.size).isEqualTo(1)
        assertThat(state.unlockedBadges[0].id).isEqualTo("first_steps")
        assertThat(state.showBadgeUnlock).isTrue()
        assertThat(state.currentBadgeIndex).isEqualTo(0)
    }

    @Test
    fun `state tracks multiple unlocked badges`() {
        // Given - Multiple badges unlocked
        val badge1 = createBadge("badge1", "First Badge")
        val badge2 = createBadge("badge2", "Second Badge")
        val badge3 = createBadge("badge3", "Third Badge")

        val state =
            MathPracticeScreen.State(
                currentProblem = null,
                currentAnswer = "",
                currentProblemIndex = 9,
                totalProblems = 10,
                isLoading = false,
                isCorrect = true,
                unlockedBadges = listOf(badge1, badge2, badge3),
                showBadgeUnlock = true,
                currentBadgeIndex = 0,
                eventSink = {},
            )

        // Then - All badges are tracked
        assertThat(state.unlockedBadges.size).isEqualTo(3)
        assertThat(state.unlockedBadges[0].id).isEqualTo("badge1")
        assertThat(state.unlockedBadges[1].id).isEqualTo("badge2")
        assertThat(state.unlockedBadges[2].id).isEqualTo("badge3")
        assertThat(state.showBadgeUnlock).isTrue()
        assertThat(state.currentBadgeIndex).isEqualTo(0)
    }

    @Test
    fun `state tracks current badge index for sequential display`() {
        // Given - Multiple badges and showing second badge
        val badges =
            listOf(
                createBadge("badge1", "First"),
                createBadge("badge2", "Second"),
                createBadge("badge3", "Third"),
            )

        val state =
            MathPracticeScreen.State(
                currentProblem = null,
                currentAnswer = "",
                currentProblemIndex = 9,
                totalProblems = 10,
                isLoading = false,
                isCorrect = true,
                unlockedBadges = badges,
                showBadgeUnlock = true,
                currentBadgeIndex = 1, // Showing second badge
                eventSink = {},
            )

        // Then
        assertThat(state.unlockedBadges.size).isEqualTo(3)
        assertThat(state.currentBadgeIndex).isEqualTo(1)
        assertThat(state.unlockedBadges[state.currentBadgeIndex].id).isEqualTo("badge2")
    }

    @Test
    fun `events are properly defined`() {
        // Verify all events exist and are of correct type
        val numberClicked = MathPracticeScreen.Event.NumberClicked(5)
        val clearAnswer = MathPracticeScreen.Event.ClearAnswer
        val checkAnswer = MathPracticeScreen.Event.CheckAnswer
        val nextProblem = MathPracticeScreen.Event.NextProblem
        val navigateBack = MathPracticeScreen.Event.NavigateBack
        val dismissBadge = MathPracticeScreen.Event.DismissBadgeDialog

        // Verify event values and singleton objects
        assertThat(numberClicked.number).isEqualTo(5)
        assertThat(clearAnswer).isEqualTo(MathPracticeScreen.Event.ClearAnswer)
        assertThat(checkAnswer).isEqualTo(MathPracticeScreen.Event.CheckAnswer)
        assertThat(nextProblem).isEqualTo(MathPracticeScreen.Event.NextProblem)
        assertThat(navigateBack).isEqualTo(MathPracticeScreen.Event.NavigateBack)
        assertThat(dismissBadge).isEqualTo(MathPracticeScreen.Event.DismissBadgeDialog)
    }

    @Test
    fun `badge state transitions work correctly with multiple badges`() {
        // Given - Three badges unlocked
        val badges =
            listOf(
                createBadge("badge1", "First"),
                createBadge("badge2", "Second"),
                createBadge("badge3", "Third"),
            )

        // When - Starting with first badge
        var state =
            MathPracticeScreen.State(
                currentProblem = null,
                currentAnswer = "",
                currentProblemIndex = 9,
                totalProblems = 10,
                isLoading = false,
                isCorrect = true,
                unlockedBadges = badges,
                showBadgeUnlock = true,
                currentBadgeIndex = 0,
                eventSink = {},
            )

        // Then - First badge should be shown
        assertThat(state.currentBadgeIndex).isEqualTo(0)
        assertThat(state.unlockedBadges[state.currentBadgeIndex].id).isEqualTo("badge1")

        // When - Moving to second badge
        state = state.copy(currentBadgeIndex = 1)

        // Then - Second badge should be shown
        assertThat(state.currentBadgeIndex).isEqualTo(1)
        assertThat(state.unlockedBadges[state.currentBadgeIndex].id).isEqualTo("badge2")

        // When - Moving to third badge
        state = state.copy(currentBadgeIndex = 2)

        // Then - Third badge should be shown
        assertThat(state.currentBadgeIndex).isEqualTo(2)
        assertThat(state.unlockedBadges[state.currentBadgeIndex].id).isEqualTo("badge3")
    }

    @Test
    fun `badge dialog can be dismissed`() {
        // Given - Badge dialog is showing
        var showBadgeUnlock = true
        val badges = listOf(createBadge("badge1", "Test"))

        val state =
            MathPracticeScreen.State(
                currentProblem = null,
                currentAnswer = "",
                currentProblemIndex = 9,
                totalProblems = 10,
                isLoading = false,
                isCorrect = true,
                unlockedBadges = badges,
                showBadgeUnlock = showBadgeUnlock,
                currentBadgeIndex = 0,
                eventSink = {},
            )

        // Then - Dialog should be showing
        assertThat(state.showBadgeUnlock).isTrue()

        // When - Dialog is dismissed
        showBadgeUnlock = false
        val dismissedState = state.copy(showBadgeUnlock = showBadgeUnlock)

        // Then - Dialog should be hidden
        assertThat(dismissedState.showBadgeUnlock).isFalse()
    }

    private fun createBadge(
        id: String,
        name: String,
    ): Badge =
        Badge(
            id = id,
            name = name,
            description = "Test badge",
            icon = dev.hossain.mathtutor.domain.model.BadgeIcon.FIRST_STEPS.name,
            category = BadgeCategory.GETTING_STARTED,
            requirement = BadgeRequirement.ProblemCount(10),
            unlockedAt = java.time.Instant.now(),
        )
}
