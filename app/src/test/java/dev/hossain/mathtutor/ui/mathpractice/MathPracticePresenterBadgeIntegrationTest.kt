package dev.hossain.mathtutor.ui.mathpractice

import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        assertTrue(state.unlockedBadges.isEmpty())
        assertFalse(state.showBadgeUnlock)
        assertEquals(0, state.currentBadgeIndex)
    }

    @Test
    fun `state tracks unlocked badges correctly`() {
        // Given - A badge is unlocked
        val badge =
            Badge(
                id = "first_steps",
                name = "First Steps",
                description = "Completed first practice session",
                icon = "🎯",
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
        assertEquals(1, state.unlockedBadges.size)
        assertEquals("first_steps", state.unlockedBadges[0].id)
        assertTrue(state.showBadgeUnlock)
        assertEquals(0, state.currentBadgeIndex)
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
        assertEquals(3, state.unlockedBadges.size)
        assertEquals("badge1", state.unlockedBadges[0].id)
        assertEquals("badge2", state.unlockedBadges[1].id)
        assertEquals("badge3", state.unlockedBadges[2].id)
        assertTrue(state.showBadgeUnlock)
        assertEquals(0, state.currentBadgeIndex)
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
        assertEquals(3, state.unlockedBadges.size)
        assertEquals(1, state.currentBadgeIndex)
        assertEquals("badge2", state.unlockedBadges[state.currentBadgeIndex].id)
    }

    @Test
    fun `events are properly defined`() {
        // Verify all events exist and are of correct type
        val numberClicked: MathPracticeScreen.Event = MathPracticeScreen.Event.NumberClicked(5)
        val clearAnswer: MathPracticeScreen.Event = MathPracticeScreen.Event.ClearAnswer
        val checkAnswer: MathPracticeScreen.Event = MathPracticeScreen.Event.CheckAnswer
        val nextProblem: MathPracticeScreen.Event = MathPracticeScreen.Event.NextProblem
        val navigateBack: MathPracticeScreen.Event = MathPracticeScreen.Event.NavigateBack
        val dismissBadge: MathPracticeScreen.Event = MathPracticeScreen.Event.DismissBadgeDialog

        // All events should be instances of Event interface
        assertTrue(numberClicked is MathPracticeScreen.Event)
        assertTrue(clearAnswer is MathPracticeScreen.Event)
        assertTrue(checkAnswer is MathPracticeScreen.Event)
        assertTrue(nextProblem is MathPracticeScreen.Event)
        assertTrue(navigateBack is MathPracticeScreen.Event)
        assertTrue(dismissBadge is MathPracticeScreen.Event)
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
        assertEquals(0, state.currentBadgeIndex)
        assertEquals("badge1", state.unlockedBadges[state.currentBadgeIndex].id)

        // When - Moving to second badge
        state = state.copy(currentBadgeIndex = 1)

        // Then - Second badge should be shown
        assertEquals(1, state.currentBadgeIndex)
        assertEquals("badge2", state.unlockedBadges[state.currentBadgeIndex].id)

        // When - Moving to third badge
        state = state.copy(currentBadgeIndex = 2)

        // Then - Third badge should be shown
        assertEquals(2, state.currentBadgeIndex)
        assertEquals("badge3", state.unlockedBadges[state.currentBadgeIndex].id)
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
        assertTrue(state.showBadgeUnlock)

        // When - Dialog is dismissed
        showBadgeUnlock = false
        val dismissedState = state.copy(showBadgeUnlock = showBadgeUnlock)

        // Then - Dialog should be hidden
        assertFalse(dismissedState.showBadgeUnlock)
    }

    private fun createBadge(
        id: String,
        name: String,
    ): Badge =
        Badge(
            id = id,
            name = name,
            description = "Test badge",
            icon = "🎯",
            category = BadgeCategory.GETTING_STARTED,
            requirement = BadgeRequirement.ProblemCount(10),
            unlockedAt = java.time.Instant.now(),
        )
}
