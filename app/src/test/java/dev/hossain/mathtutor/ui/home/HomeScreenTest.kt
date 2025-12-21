package dev.hossain.mathtutor.ui.home

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.model.SessionStats
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * Unit tests for [HomeScreen].
 *
 * Tests the screen state and events for the home dashboard.
 */
class HomeScreenTest {
    @Test
    fun state_withAllData_hasCorrectProperties() {
        // Given
        val userName = "Alex"
        val streakData =
            DailyStreak(
                currentStreak = 5,
                longestStreak = 7,
                lastPracticeDate = LocalDate.now(),
                totalDaysPracticed = 10,
            )
        val overallStats =
            SessionStats(
                totalProblems = 150,
                correctCount = 135,
                accuracy = 90f,
                sessionCount = 15,
            )
        val recentBadges =
            listOf(
                Badge(
                    id = "badge1",
                    name = "Badge 1",
                    description = "Test badge 1",
                    icon = dev.hossain.mathtutor.domain.model.BadgeIcon.FIRST_STEPS.name,
                    category = BadgeCategory.GETTING_STARTED,
                    requirement = BadgeRequirement.ProblemCount(1),
                    unlockedAt = Instant.now(),
                ),
            )
        var eventReceived: HomeScreen.Event? = null
        val eventSink: (HomeScreen.Event) -> Unit = { event ->
            eventReceived = event
        }

        // When
        val state =
            HomeScreen.State(
                userName = userName,
                gradeLevel = null,
                streakData = streakData,
                overallStats = overallStats,
                recentBadges = recentBadges,
                eventSink = eventSink,
            )

        // Then
        assertThat(state.userName).isEqualTo(userName)
        assertThat(state.gradeLevel).isEqualTo(null)
        assertThat(state.streakData).isEqualTo(streakData)
        assertThat(state.overallStats).isEqualTo(overallStats)
        assertThat(state.recentBadges).isEqualTo(recentBadges)
        assertThat(state.eventSink).isNotNull()
    }

    @Test
    fun state_withNoUserName_hasNullUserName() {
        // Given
        val eventSink: (HomeScreen.Event) -> Unit = { }

        // When
        val state =
            HomeScreen.State(
                userName = null,
                gradeLevel = null,
                streakData = null,
                overallStats = SessionStats.EMPTY,
                recentBadges = emptyList(),
                eventSink = eventSink,
            )

        // Then
        assertThat(state.userName).isNull()
    }

    @Test
    fun state_withNoStreakData_hasNullStreakData() {
        // Given
        val eventSink: (HomeScreen.Event) -> Unit = { }

        // When
        val state =
            HomeScreen.State(
                userName = null,
                gradeLevel = null,
                streakData = null,
                overallStats = SessionStats.EMPTY,
                recentBadges = emptyList(),
                eventSink = eventSink,
            )

        // Then
        assertThat(state.streakData).isNull()
    }

    @Test
    fun state_withEmptyStats_hasEmptyStats() {
        // Given
        val eventSink: (HomeScreen.Event) -> Unit = { }

        // When
        val state =
            HomeScreen.State(
                userName = null,
                gradeLevel = null,
                streakData = null,
                overallStats = SessionStats.EMPTY,
                recentBadges = emptyList(),
                eventSink = eventSink,
            )

        // Then
        assertThat(state.overallStats).isEqualTo(SessionStats.EMPTY)
        assertThat(state.overallStats.sessionCount).isEqualTo(0)
        assertThat(state.overallStats.totalProblems).isEqualTo(0)
    }

    @Test
    fun state_withNoBadges_hasEmptyBadgesList() {
        // Given
        val eventSink: (HomeScreen.Event) -> Unit = { }

        // When
        val state =
            HomeScreen.State(
                userName = null,
                gradeLevel = null,
                streakData = null,
                overallStats = SessionStats.EMPTY,
                recentBadges = emptyList(),
                eventSink = eventSink,
            )

        // Then
        assertThat(state.recentBadges.isEmpty()).isTrue()
    }

    @Test
    fun state_withMultipleBadges_hasCorrectBadgesList() {
        // Given
        val badge1 =
            Badge(
                id = "badge1",
                name = "Badge 1",
                description = "Test badge 1",
                icon = "🎯",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(1),
                unlockedAt = Instant.now(),
            )
        val badge2 =
            Badge(
                id = "badge2",
                name = "Badge 2",
                description = "Test badge 2",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.PERFECT_RACE.name,
                category = BadgeCategory.VOLUME,
                requirement = BadgeRequirement.ProblemCount(10),
                unlockedAt = Instant.now(),
            )
        val badge3 =
            Badge(
                id = "badge3",
                name = "Badge 3",
                description = "Test badge 3",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.QUICK_THINKER.name,
                category = BadgeCategory.SPEED_ACCURACY,
                requirement = BadgeRequirement.ProblemSpeed(3),
                unlockedAt = Instant.now(),
            )
        val recentBadges = listOf(badge1, badge2, badge3)
        val eventSink: (HomeScreen.Event) -> Unit = { }

        // When
        val state =
            HomeScreen.State(
                userName = null,
                gradeLevel = null,
                streakData = null,
                overallStats = SessionStats.EMPTY,
                recentBadges = recentBadges,
                eventSink = eventSink,
            )

        // Then
        assertThat(state.recentBadges.size).isEqualTo(3)
        assertThat(state.recentBadges[0]).isEqualTo(badge1)
        assertThat(state.recentBadges[1]).isEqualTo(badge2)
        assertThat(state.recentBadges[2]).isEqualTo(badge3)
    }

    @Test
    fun event_startPracticeClicked_createsCorrectEvent() {
        // When
        val event = HomeScreen.Event.StartPracticeClicked

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(HomeScreen.Event.StartPracticeClicked)
    }

    @Test
    fun event_viewStatsClicked_createsCorrectEvent() {
        // When
        val event = HomeScreen.Event.ViewStatsClicked

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(HomeScreen.Event.ViewStatsClicked)
    }

    @Test
    fun event_viewBadgesClicked_createsCorrectEvent() {
        // When
        val event = HomeScreen.Event.ViewBadgesClicked

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(HomeScreen.Event.ViewBadgesClicked)
    }

    @Test
    fun event_viewSettingsClicked_createsCorrectEvent() {
        // When
        val event = HomeScreen.Event.ViewSettingsClicked

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(HomeScreen.Event.ViewSettingsClicked)
    }

    @Test
    fun eventSink_startPracticeClicked_receivesEvent() {
        // Given
        var receivedEvent: HomeScreen.Event? = null
        val state =
            HomeScreen.State(
                userName = null,
                gradeLevel = null,
                streakData = null,
                overallStats = SessionStats.EMPTY,
                recentBadges = emptyList(),
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(HomeScreen.Event.StartPracticeClicked)

        // Then
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is HomeScreen.Event.StartPracticeClicked).isTrue()
    }

    @Test
    fun eventSink_viewStatsClicked_receivesEvent() {
        // Given
        var receivedEvent: HomeScreen.Event? = null
        val state =
            HomeScreen.State(
                userName = null,
                gradeLevel = null,
                streakData = null,
                overallStats = SessionStats.EMPTY,
                recentBadges = emptyList(),
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(HomeScreen.Event.ViewStatsClicked)

        // Then
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is HomeScreen.Event.ViewStatsClicked).isTrue()
    }

    @Test
    fun eventSink_viewBadgesClicked_receivesEvent() {
        // Given
        var receivedEvent: HomeScreen.Event? = null
        val state =
            HomeScreen.State(
                userName = null,
                gradeLevel = null,
                streakData = null,
                overallStats = SessionStats.EMPTY,
                recentBadges = emptyList(),
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(HomeScreen.Event.ViewBadgesClicked)

        // Then
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is HomeScreen.Event.ViewBadgesClicked).isTrue()
    }

    @Test
    fun eventSink_viewSettingsClicked_receivesEvent() {
        // Given
        var receivedEvent: HomeScreen.Event? = null
        val state =
            HomeScreen.State(
                userName = null,
                gradeLevel = null,
                streakData = null,
                overallStats = SessionStats.EMPTY,
                recentBadges = emptyList(),
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(HomeScreen.Event.ViewSettingsClicked)

        // Then
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is HomeScreen.Event.ViewSettingsClicked).isTrue()
    }

    @Test
    fun state_withActiveStreak_hasCorrectStreakData() {
        // Given
        val today = LocalDate.now()
        val streakData =
            DailyStreak(
                currentStreak = 5,
                longestStreak = 7,
                lastPracticeDate = today,
                totalDaysPracticed = 10,
            )
        val eventSink: (HomeScreen.Event) -> Unit = { }

        // When
        val state =
            HomeScreen.State(
                userName = null,
                gradeLevel = null,
                streakData = streakData,
                overallStats = SessionStats.EMPTY,
                recentBadges = emptyList(),
                eventSink = eventSink,
            )

        // Then
        assertThat(state.streakData?.currentStreak).isEqualTo(5)
        assertThat(state.streakData?.longestStreak).isEqualTo(7)
        assertThat(state.streakData?.lastPracticeDate).isEqualTo(today)
        assertThat(state.streakData?.isStreakAlive(today)).isTrue()
    }

    @Test
    fun state_withAtRiskStreak_hasCorrectStreakData() {
        // Given
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val streakData =
            DailyStreak(
                currentStreak = 3,
                longestStreak = 5,
                lastPracticeDate = yesterday,
                totalDaysPracticed = 8,
            )
        val eventSink: (HomeScreen.Event) -> Unit = { }

        // When
        val state =
            HomeScreen.State(
                userName = null,
                gradeLevel = null,
                streakData = streakData,
                overallStats = SessionStats.EMPTY,
                recentBadges = emptyList(),
                eventSink = eventSink,
            )

        // Then
        assertThat(state.streakData?.currentStreak).isEqualTo(3)
        assertThat(state.streakData?.lastPracticeDate).isEqualTo(yesterday)
        assertThat(state.streakData?.isStreakAlive(today)).isTrue()
    }

    @Test
    fun state_withStatsData_hasCorrectStats() {
        // Given
        val stats =
            SessionStats(
                totalProblems = 100,
                correctCount = 85,
                accuracy = 85f,
                sessionCount = 10,
            )
        val eventSink: (HomeScreen.Event) -> Unit = { }

        // When
        val state =
            HomeScreen.State(
                userName = null,
                gradeLevel = null,
                streakData = null,
                overallStats = stats,
                recentBadges = emptyList(),
                eventSink = eventSink,
            )

        // Then
        assertThat(state.overallStats.totalProblems).isEqualTo(100)
        assertThat(state.overallStats.correctCount).isEqualTo(85)
        assertThat(state.overallStats.accuracy).isWithin(0.01f).of(85f)
        assertThat(state.overallStats.sessionCount).isEqualTo(10)
    }
}
