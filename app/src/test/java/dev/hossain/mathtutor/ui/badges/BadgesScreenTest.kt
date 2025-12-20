package dev.hossain.mathtutor.ui.badges

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.repository.BadgeProgress
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
        assertThat(state.badgesByCategory).isEqualTo(badgesByCategory)
        assertThat(state.progressSummary).isEqualTo(progressSummary)
        assertThat(state.selectedBadge).isEqualTo(selectedBadge)
        assertThat(state.eventSink).isNotNull()
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
        assertThat(state.badgesByCategory.isEmpty()).isTrue()
        assertThat(state.progressSummary.unlockedCount).isEqualTo(0)
        assertThat(state.progressSummary.totalCount).isEqualTo(0)
        assertThat(state.selectedBadge).isNull()
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
        assertThat(state.badgesByCategory.size).isEqualTo(3)
        assertThat(state.badgesByCategory[BadgeCategory.GETTING_STARTED]?.size).isEqualTo(1)
        assertThat(state.badgesByCategory[BadgeCategory.VOLUME]?.size).isEqualTo(1)
        assertThat(state.badgesByCategory[BadgeCategory.SPEED_ACCURACY]?.size).isEqualTo(1)
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
        assertThat(event.badge).isEqualTo(badge)
    }

    @Test
    fun event_closeDialog_createsCorrectEvent() {
        // When
        val event = BadgesScreen.Event.CloseDialog

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(BadgesScreen.Event.CloseDialog)
    }

    @Test
    fun event_backPressed_createsCorrectEvent() {
        // When
        val event = BadgesScreen.Event.BackPressed

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(BadgesScreen.Event.BackPressed)
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
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is BadgesScreen.Event.BadgeClicked).isTrue()
        assertThat((receivedEvent as BadgesScreen.Event.BadgeClicked).isEqualTo(badge).badge)
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
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is BadgesScreen.Event.CloseDialog).isTrue()
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
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is BadgesScreen.Event.BackPressed).isTrue()
    }

    @Test
    fun progressSummary_calculatesPercentageCorrectly() {
        // Given
        val progress = BadgeProgress(unlockedCount = 5, totalCount = 15)

        // When
        val percentage = progress.percentage

        // Then
        assertThat(percentage).isWithin(0.01f).of(33.333332f)
    }

    @Test
    fun progressSummary_withZeroBadges_returnsZeroPercentage() {
        // Given
        val progress = BadgeProgress(unlockedCount = 0, totalCount = 0)

        // When
        val percentage = progress.percentage

        // Then
        assertThat(percentage).isWithin(0.01f).of(0f)
    }
}
