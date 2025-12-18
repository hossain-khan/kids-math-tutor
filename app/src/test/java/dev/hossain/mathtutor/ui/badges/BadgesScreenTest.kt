package dev.hossain.mathtutor.ui.badges

import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.repository.BadgeProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for [BadgesScreen].
 *
 * Tests the screen state and events.
 */
class BadgesScreenTest {
    @Test
    fun state_hasCorrectProperties() {
        // Given
        val badge1 =
            Badge(
                id = "test_badge_1",
                name = "Test Badge 1",
                description = "Test description",
                icon = "🎯",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(10),
                unlockedAt = Instant.now(),
            )
        val badge2 =
            Badge(
                id = "test_badge_2",
                name = "Test Badge 2",
                description = "Test description 2",
                icon = "🚀",
                category = BadgeCategory.VOLUME,
                requirement = BadgeRequirement.ProblemCount(25),
            )
        val badgesByCategory =
            mapOf(
                BadgeCategory.GETTING_STARTED to listOf(badge1),
                BadgeCategory.VOLUME to listOf(badge2),
            )
        val progressSummary = BadgeProgress(unlockedCount = 1, totalCount = 2)
        val selectedBadge = badge1
        var eventReceived: BadgesScreen.Event? = null
        val eventSink: (BadgesScreen.Event) -> Unit = { event ->
            eventReceived = event
        }

        // When
        val state =
            BadgesScreen.State(
                badgesByCategory = badgesByCategory,
                progressSummary = progressSummary,
                selectedBadge = selectedBadge,
                eventSink = eventSink,
            )

        // Then
        assertEquals(badgesByCategory, state.badgesByCategory)
        assertEquals(progressSummary, state.progressSummary)
        assertEquals(selectedBadge, state.selectedBadge)
        assertNotNull(state.eventSink)
    }

    @Test
    fun state_withNoBadges_hasCorrectProperties() {
        // Given
        val badgesByCategory = emptyMap<BadgeCategory, List<Badge>>()
        val progressSummary = BadgeProgress(unlockedCount = 0, totalCount = 0)
        val eventSink: (BadgesScreen.Event) -> Unit = { }

        // When
        val state =
            BadgesScreen.State(
                badgesByCategory = badgesByCategory,
                progressSummary = progressSummary,
                selectedBadge = null,
                eventSink = eventSink,
            )

        // Then
        assertTrue(state.badgesByCategory.isEmpty())
        assertEquals(0, state.progressSummary.unlockedCount)
        assertEquals(0, state.progressSummary.totalCount)
        assertNull(state.selectedBadge)
    }

    @Test
    fun state_withMultipleCategories_groupsBadgesCorrectly() {
        // Given
        val gettingStartedBadge =
            Badge(
                id = "first_steps",
                name = "First Steps",
                description = "Solve your first problem",
                icon = "🎯",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(1),
            )
        val volumeBadge =
            Badge(
                id = "math_rookie",
                name = "Math Rookie",
                description = "Solve 25 total problems",
                icon = "🐣",
                category = BadgeCategory.VOLUME,
                requirement = BadgeRequirement.ProblemCount(25),
            )
        val speedBadge =
            Badge(
                id = "quick_thinker",
                name = "Quick Thinker",
                description = "Solve a problem in under 3 seconds",
                icon = "⚡",
                category = BadgeCategory.SPEED_ACCURACY,
                requirement = BadgeRequirement.ProblemSpeed(3),
            )

        val badgesByCategory =
            mapOf(
                BadgeCategory.GETTING_STARTED to listOf(gettingStartedBadge),
                BadgeCategory.VOLUME to listOf(volumeBadge),
                BadgeCategory.SPEED_ACCURACY to listOf(speedBadge),
            )

        // When
        val state =
            BadgesScreen.State(
                badgesByCategory = badgesByCategory,
                progressSummary = BadgeProgress(unlockedCount = 0, totalCount = 3),
                selectedBadge = null,
                eventSink = {},
            )

        // Then
        assertEquals(3, state.badgesByCategory.size)
        assertEquals(1, state.badgesByCategory[BadgeCategory.GETTING_STARTED]?.size)
        assertEquals(1, state.badgesByCategory[BadgeCategory.VOLUME]?.size)
        assertEquals(1, state.badgesByCategory[BadgeCategory.SPEED_ACCURACY]?.size)
    }

    @Test
    fun event_badgeClicked_createsCorrectEvent() {
        // Given
        val badge =
            Badge(
                id = "test",
                name = "Test",
                description = "Test",
                icon = "🎯",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(1),
            )

        // When
        val event = BadgesScreen.Event.BadgeClicked(badge)

        // Then
        assertTrue(event is BadgesScreen.Event.BadgeClicked)
        assertEquals(badge, event.badge)
    }

    @Test
    fun event_closeDialog_createsCorrectEvent() {
        // When
        val event = BadgesScreen.Event.CloseDialog

        // Then
        assertTrue(event is BadgesScreen.Event.CloseDialog)
    }

    @Test
    fun event_backPressed_createsCorrectEvent() {
        // When
        val event = BadgesScreen.Event.BackPressed

        // Then
        assertTrue(event is BadgesScreen.Event.BackPressed)
    }

    @Test
    fun eventSink_badgeClicked_receivesEvent() {
        // Given
        val badge =
            Badge(
                id = "test",
                name = "Test",
                description = "Test",
                icon = "🎯",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(1),
            )
        var receivedEvent: BadgesScreen.Event? = null
        val state =
            BadgesScreen.State(
                badgesByCategory = emptyMap(),
                progressSummary = BadgeProgress(0, 0),
                selectedBadge = null,
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(BadgesScreen.Event.BadgeClicked(badge))

        // Then
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is BadgesScreen.Event.BadgeClicked)
        assertEquals(badge, (receivedEvent as BadgesScreen.Event.BadgeClicked).badge)
    }

    @Test
    fun eventSink_closeDialog_receivesEvent() {
        // Given
        var receivedEvent: BadgesScreen.Event? = null
        val state =
            BadgesScreen.State(
                badgesByCategory = emptyMap(),
                progressSummary = BadgeProgress(0, 0),
                selectedBadge = null,
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(BadgesScreen.Event.CloseDialog)

        // Then
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is BadgesScreen.Event.CloseDialog)
    }

    @Test
    fun eventSink_backPressed_receivesEvent() {
        // Given
        var receivedEvent: BadgesScreen.Event? = null
        val state =
            BadgesScreen.State(
                badgesByCategory = emptyMap(),
                progressSummary = BadgeProgress(0, 0),
                selectedBadge = null,
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(BadgesScreen.Event.BackPressed)

        // Then
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is BadgesScreen.Event.BackPressed)
    }

    @Test
    fun progressSummary_calculatesPercentageCorrectly() {
        // Given
        val progress = BadgeProgress(unlockedCount = 5, totalCount = 15)

        // When
        val percentage = progress.percentage

        // Then
        assertEquals(33.333332f, percentage, 0.01f)
    }

    @Test
    fun progressSummary_withZeroBadges_returnsZeroPercentage() {
        // Given
        val progress = BadgeProgress(unlockedCount = 0, totalCount = 0)

        // When
        val percentage = progress.percentage

        // Then
        assertEquals(0f, percentage, 0.01f)
    }
}
