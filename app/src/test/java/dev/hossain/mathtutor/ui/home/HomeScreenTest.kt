package dev.hossain.mathtutor.ui.home

import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.model.SessionStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
                    icon = "🎯",
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
        assertEquals(userName, state.userName)
        assertEquals(null, state.gradeLevel)
        assertEquals(streakData, state.streakData)
        assertEquals(overallStats, state.overallStats)
        assertEquals(recentBadges, state.recentBadges)
        assertNotNull(state.eventSink)
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
        assertNull(state.userName)
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
        assertNull(state.streakData)
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
        assertEquals(SessionStats.EMPTY, state.overallStats)
        assertEquals(0, state.overallStats.sessionCount)
        assertEquals(0, state.overallStats.totalProblems)
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
        assertTrue(state.recentBadges.isEmpty())
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
                icon = "🚀",
                category = BadgeCategory.VOLUME,
                requirement = BadgeRequirement.ProblemCount(10),
                unlockedAt = Instant.now(),
            )
        val badge3 =
            Badge(
                id = "badge3",
                name = "Badge 3",
                description = "Test badge 3",
                icon = "⚡",
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
        assertEquals(3, state.recentBadges.size)
        assertEquals(badge1, state.recentBadges[0])
        assertEquals(badge2, state.recentBadges[1])
        assertEquals(badge3, state.recentBadges[2])
    }

    @Test
    fun event_startPracticeClicked_createsCorrectEvent() {
        // When
        val event = HomeScreen.Event.StartPracticeClicked

        // Then
        assertTrue(event is HomeScreen.Event.StartPracticeClicked)
    }

    @Test
    fun event_viewStatsClicked_createsCorrectEvent() {
        // When
        val event = HomeScreen.Event.ViewStatsClicked

        // Then
        assertTrue(event is HomeScreen.Event.ViewStatsClicked)
    }

    @Test
    fun event_viewBadgesClicked_createsCorrectEvent() {
        // When
        val event = HomeScreen.Event.ViewBadgesClicked

        // Then
        assertTrue(event is HomeScreen.Event.ViewBadgesClicked)
    }

    @Test
    fun event_viewSettingsClicked_createsCorrectEvent() {
        // When
        val event = HomeScreen.Event.ViewSettingsClicked

        // Then
        assertTrue(event is HomeScreen.Event.ViewSettingsClicked)
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
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is HomeScreen.Event.StartPracticeClicked)
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
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is HomeScreen.Event.ViewStatsClicked)
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
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is HomeScreen.Event.ViewBadgesClicked)
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
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is HomeScreen.Event.ViewSettingsClicked)
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
        assertEquals(5, state.streakData?.currentStreak)
        assertEquals(7, state.streakData?.longestStreak)
        assertEquals(today, state.streakData?.lastPracticeDate)
        assertTrue(state.streakData?.isStreakAlive(today) == true)
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
        assertEquals(3, state.streakData?.currentStreak)
        assertEquals(yesterday, state.streakData?.lastPracticeDate)
        assertTrue(state.streakData?.isStreakAlive(today) == true)
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
        assertEquals(100, state.overallStats.totalProblems)
        assertEquals(85, state.overallStats.correctCount)
        assertEquals(85f, state.overallStats.accuracy, 0.01f)
        assertEquals(10, state.overallStats.sessionCount)
    }
}
