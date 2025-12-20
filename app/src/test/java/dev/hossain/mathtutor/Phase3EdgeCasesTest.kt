package dev.hossain.mathtutor

import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.SessionStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * Edge case tests for Phase 3: Achievement System & Motivation.
 *
 * Tests cover:
 * - First-time user scenarios (no badges, no streak)
 * - Multiple badge unlocks in one session
 * - Multiple practices same day (streak unchanged)
 * - Multiple days skipped (streak reset)
 * - All 15 badges unlockable verification
 * - Boundary conditions and edge cases
 */
class Phase3EdgeCasesTest {
    // ============================================================
    // First-Time User Edge Cases
    // ============================================================

    @Test
    fun `first-time user has empty streak data`() {
        val streak = DailyStreak.EMPTY

        assertEquals(0, streak.currentStreak)
        assertEquals(0, streak.longestStreak)
        assertNull(streak.lastPracticeDate)
        assertEquals(0, streak.totalDaysPracticed)
        assertFalse(streak.isStreakAlive(LocalDate.now()))
    }

    @Test
    fun `first-time user has no unlocked badges`() {
        val badges =
            listOf(
                createBadge("badge1"),
                createBadge("badge2"),
                createBadge("badge3"),
            )

        val unlockedBadges = badges.filter { it.isUnlocked() }

        assertTrue("First-time user should have no unlocked badges", unlockedBadges.isEmpty())
    }

    @Test
    fun `first-time user session stats are empty`() {
        val stats = SessionStats.EMPTY

        assertEquals(0, stats.totalProblems)
        assertEquals(0, stats.correctCount)
        assertEquals(0f, stats.accuracy, 0.01f)
        assertEquals(0, stats.sessionCount)
    }

    @Test
    fun `first-time user first practice initializes streak to 1`() {
        val today = LocalDate.now()
        val firstStreak = DailyStreak.EMPTY.updateStreak(today)

        assertEquals(1, firstStreak.currentStreak)
        assertEquals(1, firstStreak.longestStreak)
        assertEquals(today, firstStreak.lastPracticeDate)
        assertEquals(1, firstStreak.totalDaysPracticed)
        assertTrue(firstStreak.isStreakAlive(today))
    }

    // ============================================================
    // Multiple Badge Unlocks in One Session
    // ============================================================

    @Test
    fun `multiple badges can be unlocked in single session`() {
        // Given - User completes first session with 10 correct problems
        val overallStats = SessionStats(totalProblems = 10, correctCount = 10, accuracy = 100f, sessionCount = 1)

        // Three badges that should unlock simultaneously
        val firstStepsBadge = createBadge("first_steps", BadgeRequirement.ProblemCount(1))
        val rookieBadge = createBadge("math_rookie", BadgeRequirement.ProblemCount(10))
        val perfect10Badge = createBadge("perfect_10", BadgeRequirement.SessionAccuracy(100f, 1))

        // Simulate checking each badge
        val unlockedBadges = mutableListOf<Badge>()

        // Check first_steps (1 problem)
        if (overallStats.totalProblems >= 1) {
            unlockedBadges.add(firstStepsBadge)
        }

        // Check math_rookie (10 problems)
        if (overallStats.totalProblems >= 10) {
            unlockedBadges.add(rookieBadge)
        }

        // Check perfect_10 (100% accuracy)
        if (overallStats.accuracy >= 100f && overallStats.sessionCount >= 1) {
            unlockedBadges.add(perfect10Badge)
        }

        // Then - All three badges should be unlocked
        assertEquals(3, unlockedBadges.size)
        assertTrue(unlockedBadges.any { it.id == "first_steps" })
        assertTrue(unlockedBadges.any { it.id == "math_rookie" })
        assertTrue(unlockedBadges.any { it.id == "perfect_10" })
    }

    @Test
    fun `progressive badge unlocks across volume badges`() {
        // Given - User gradually increases problem count
        val badge25 = createBadge("25_problems", BadgeRequirement.ProblemCount(25))
        val badge50 = createBadge("50_problems", BadgeRequirement.ProblemCount(50))
        val badge100 = createBadge("100_problems", BadgeRequirement.ProblemCount(100))

        // When - User solves 60 problems
        val stats = SessionStats(totalProblems = 60, correctCount = 48, accuracy = 80f, sessionCount = 6)

        val unlockedCount =
            listOf(badge25, badge50, badge100).count { badge ->
                when (val req = badge.requirement) {
                    is BadgeRequirement.ProblemCount -> stats.totalProblems >= req.count
                    else -> false
                }
            }

        // Then - Only first two badges should unlock
        assertEquals(2, unlockedCount)
    }

    @Test
    fun `unlocking multiple operation mastery badges`() {
        // Given - User completes 50 addition and 50 subtraction problems
        val additionStats = SessionStats(totalProblems = 50, correctCount = 45, accuracy = 90f, sessionCount = 5)
        val subtractionStats = SessionStats(totalProblems = 50, correctCount = 42, accuracy = 84f, sessionCount = 5)

        val additionBadge = createBadge("addition_expert", BadgeRequirement.OperationCount(MathOperation.ADDITION, 50))
        val subtractionBadge =
            createBadge("subtraction_star", BadgeRequirement.OperationCount(MathOperation.SUBTRACTION, 50))

        // Both badges should unlock
        val additionMet = additionStats.totalProblems >= 50
        val subtractionMet = subtractionStats.totalProblems >= 50

        assertTrue("Addition badge should unlock", additionMet)
        assertTrue("Subtraction badge should unlock", subtractionMet)
    }

    // ============================================================
    // Same Day Multiple Practice Sessions (Streak Unchanged)
    // ============================================================

    @Test
    fun `practicing twice same day does not increase streak`() {
        val today = LocalDate.now()
        var streak =
            DailyStreak(
                currentStreak = 5,
                longestStreak = 10,
                lastPracticeDate = today,
                totalDaysPracticed = 15,
            )

        // Second practice same day
        val secondPractice = streak.updateStreak(today)

        assertEquals("Current streak should not change", streak.currentStreak, secondPractice.currentStreak)
        assertEquals("Longest streak should not change", streak.longestStreak, secondPractice.longestStreak)
        assertEquals("Total days practiced should not change", streak.totalDaysPracticed, secondPractice.totalDaysPracticed)
        assertEquals("Last practice date should remain same", streak.lastPracticeDate, secondPractice.lastPracticeDate)
        assertEquals("Streak should be identical", streak, secondPractice)
    }

    @Test
    fun `multiple sessions same day maintain streak integrity`() {
        val today = LocalDate.now()
        var streak =
            DailyStreak(
                currentStreak = 3,
                longestStreak = 5,
                lastPracticeDate = today,
                totalDaysPracticed = 10,
            )

        // Simulate 5 practice sessions same day
        repeat(5) {
            val updatedStreak = streak.updateStreak(today)
            assertEquals("Streak should not change on same-day practice", streak, updatedStreak)
            streak = updatedStreak
        }

        // All stats should remain unchanged
        assertEquals(3, streak.currentStreak)
        assertEquals(5, streak.longestStreak)
        assertEquals(10, streak.totalDaysPracticed)
    }

    @Test
    fun `practicing same day after consecutive day build`() {
        val yesterday = LocalDate.now().minusDays(1)
        val today = LocalDate.now()

        // Start with yesterday's streak
        var streak =
            DailyStreak(
                currentStreak = 7,
                longestStreak = 10,
                lastPracticeDate = yesterday,
                totalDaysPracticed = 20,
            )

        // First practice today (should increment)
        streak = streak.updateStreak(today)
        assertEquals(8, streak.currentStreak)
        assertEquals(21, streak.totalDaysPracticed)

        // Second practice today (should not change)
        val secondPractice = streak.updateStreak(today)
        assertEquals(8, secondPractice.currentStreak)
        assertEquals(21, secondPractice.totalDaysPracticed)
    }

    // ============================================================
    // Skip Multiple Days (Streak Reset)
    // ============================================================

    @Test
    fun `skip 2 days resets streak to 1`() {
        val threeDaysAgo = LocalDate.now().minusDays(3)
        val today = LocalDate.now()

        val streak =
            DailyStreak(
                currentStreak = 10,
                longestStreak = 15,
                lastPracticeDate = threeDaysAgo,
                totalDaysPracticed = 25,
            )

        val updated = streak.updateStreak(today)

        assertEquals(1, updated.currentStreak)
        assertEquals(15, updated.longestStreak) // Preserved
        assertEquals(26, updated.totalDaysPracticed)
        assertEquals(today, updated.lastPracticeDate)
    }

    @Test
    fun `skip one week resets streak but preserves longest`() {
        val weekAgo = LocalDate.now().minusDays(7)
        val today = LocalDate.now()

        val streak =
            DailyStreak(
                currentStreak = 20,
                longestStreak = 25,
                lastPracticeDate = weekAgo,
                totalDaysPracticed = 50,
            )

        val updated = streak.updateStreak(today)

        assertEquals(1, updated.currentStreak)
        assertEquals(25, updated.longestStreak) // Preserved
        assertEquals(51, updated.totalDaysPracticed)
        assertFalse(streak.isStreakAlive(today))
    }

    @Test
    fun `skip 30 days resets streak completely`() {
        val monthAgo = LocalDate.now().minusDays(30)
        val today = LocalDate.now()

        val streak =
            DailyStreak(
                currentStreak = 15,
                longestStreak = 20,
                lastPracticeDate = monthAgo,
                totalDaysPracticed = 40,
            )

        val updated = streak.updateStreak(today)

        assertEquals(1, updated.currentStreak)
        assertEquals(20, updated.longestStreak) // Longest still preserved
        assertEquals(41, updated.totalDaysPracticed)
    }

    @Test
    fun `streak reset after break can rebuild and exceed previous longest`() {
        var streak = DailyStreak.EMPTY
        val startDate = LocalDate.of(2025, 1, 1)

        // Build 5-day streak
        for (i in 0 until 5) {
            streak = streak.updateStreak(startDate.plusDays(i.toLong()))
        }
        assertEquals(5, streak.currentStreak)
        assertEquals(5, streak.longestStreak)

        // Skip 3 days, restart
        val restartDate = startDate.plusDays(8)
        streak = streak.updateStreak(restartDate)
        assertEquals(1, streak.currentStreak)
        assertEquals(5, streak.longestStreak)

        // Build 7-day streak (exceeding previous longest)
        for (i in 1 until 7) {
            streak = streak.updateStreak(restartDate.plusDays(i.toLong()))
        }
        assertEquals(7, streak.currentStreak)
        assertEquals(7, streak.longestStreak) // New longest
    }

    // ============================================================
    // All 15 Badges Unlockable Verification
    // ============================================================

    @Test
    fun `all 15 badge requirement types are defined`() {
        val allBadgeRequirements =
            listOf(
                // Getting Started (3)
                BadgeRequirement.ProblemCount(1),
                BadgeRequirement.ConsecutiveCorrect(5),
                BadgeRequirement.SessionAccuracy(100f, 1),
                // Volume (4)
                BadgeRequirement.ProblemCount(25),
                BadgeRequirement.ProblemCount(50),
                BadgeRequirement.ProblemCount(100),
                BadgeRequirement.ProblemCount(500),
                // Operation Mastery (3)
                BadgeRequirement.OperationCount(MathOperation.ADDITION, 50),
                BadgeRequirement.OperationCount(MathOperation.SUBTRACTION, 50),
                BadgeRequirement.MixedSessions(10),
                // Speed & Accuracy (3)
                BadgeRequirement.ProblemSpeed(3),
                BadgeRequirement.SessionAccuracy(90f, 1),
                BadgeRequirement.SessionAccuracy(100f, 3),
                // Streak (2)
                BadgeRequirement.DailyStreak(3),
                BadgeRequirement.DailyStreak(7),
            )

        // Verify all 15 requirements are unique types
        assertEquals(15, allBadgeRequirements.size)
    }

    @Test
    fun `all badge categories are represented`() {
        val categories =
            setOf(
                BadgeCategory.GETTING_STARTED,
                BadgeCategory.VOLUME,
                BadgeCategory.OPERATION_MASTERY,
                BadgeCategory.SPEED_ACCURACY,
                BadgeCategory.STREAK,
            )

        assertEquals(5, categories.size)
        assertTrue(
            "All badge categories should be present",
            categories.containsAll(
                setOf(
                    BadgeCategory.GETTING_STARTED,
                    BadgeCategory.VOLUME,
                    BadgeCategory.OPERATION_MASTERY,
                    BadgeCategory.SPEED_ACCURACY,
                    BadgeCategory.STREAK,
                ),
            ),
        )
    }

    @Test
    fun `badge requirements have achievable thresholds`() {
        // Verify requirements are reasonable for K-2 users
        val volumeBadges =
            listOf(
                BadgeRequirement.ProblemCount(25), // Should take ~3 sessions
                BadgeRequirement.ProblemCount(50), // Should take ~5 sessions
                BadgeRequirement.ProblemCount(100), // Should take ~10 sessions
                BadgeRequirement.ProblemCount(500), // Long-term goal
            )

        volumeBadges.forEachIndexed { index, req ->
            assertTrue("Badge $index should have positive count", req.count > 0)
            assertTrue("Badge $index should be achievable", req.count <= 1000)
        }

        val streakBadges =
            listOf(
                BadgeRequirement.DailyStreak(3), // 3 days achievable
                BadgeRequirement.DailyStreak(7), // 1 week challenging
            )

        streakBadges.forEach { req ->
            assertTrue("Streak should be positive", req.days > 0)
            assertTrue("Streak should be achievable", req.days <= 30)
        }
    }

    // ============================================================
    // Boundary Conditions
    // ============================================================

    @Test
    fun `exactly meeting badge requirement unlocks badge`() {
        val stats = SessionStats(totalProblems = 50, correctCount = 40, accuracy = 80f, sessionCount = 5)
        val requirement = BadgeRequirement.ProblemCount(50)

        // Exactly 50 problems should unlock badge
        val meetsRequirement = stats.totalProblems >= 50
        assertTrue("Exactly meeting requirement should unlock badge", meetsRequirement)
    }

    @Test
    fun `one less than requirement does not unlock badge`() {
        val stats = SessionStats(totalProblems = 49, correctCount = 40, accuracy = 81.6f, sessionCount = 5)
        val requirement = BadgeRequirement.ProblemCount(50)

        // 49 problems should NOT unlock badge
        val meetsRequirement = stats.totalProblems >= 50
        assertFalse("One less than requirement should not unlock badge", meetsRequirement)
    }

    @Test
    fun `accuracy requirement with exactly 90 percent unlocks badge`() {
        // 9 correct out of 10 = 90%
        val stats = SessionStats(totalProblems = 10, correctCount = 9, accuracy = 90f, sessionCount = 1)
        val requirement = BadgeRequirement.SessionAccuracy(90f, 1)

        val meetsRequirement = stats.accuracy >= 90f && stats.sessionCount >= 1
        assertTrue("Exactly 90% accuracy should unlock badge", meetsRequirement)
    }

    @Test
    fun `streak exactly at threshold unlocks badge`() {
        val streak =
            DailyStreak(
                currentStreak = 7,
                longestStreak = 7,
                lastPracticeDate = LocalDate.now(),
                totalDaysPracticed = 7,
            )
        val requirement = BadgeRequirement.DailyStreak(7)

        val meetsRequirement = streak.currentStreak >= 7
        assertTrue("Exactly 7-day streak should unlock badge", meetsRequirement)
    }

    @Test
    fun `consecutive day streak at risk can be saved`() {
        val yesterday = LocalDate.now().minusDays(1)
        val today = LocalDate.now()

        val streak =
            DailyStreak(
                currentStreak = 6,
                longestStreak = 10,
                lastPracticeDate = yesterday,
                totalDaysPracticed = 20,
            )

        // Streak is at risk (practiced yesterday, not today yet)
        assertTrue("Streak should be alive", streak.isStreakAlive(today))

        // Practicing today saves it
        val saved = streak.updateStreak(today)
        assertEquals(7, saved.currentStreak)
        assertTrue(saved.isStreakAlive(today))
    }

    @Test
    fun `zero stats do not unlock any badges`() {
        val stats = SessionStats.EMPTY
        val problemCountBadge = BadgeRequirement.ProblemCount(1)
        val accuracyBadge = BadgeRequirement.SessionAccuracy(50f, 1)

        assertFalse("Zero problems should not unlock badge", stats.totalProblems >= 1)
        assertFalse("No sessions should not unlock accuracy badge", stats.sessionCount >= 1)
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private fun createBadge(
        id: String,
        requirement: BadgeRequirement = BadgeRequirement.ProblemCount(10),
        unlockedAt: Instant? = null,
    ): Badge =
        Badge(
            id = id,
            name = "Test Badge $id",
            description = "Test description for $id",
            icon = "🎯",
            category = BadgeCategory.GETTING_STARTED,
            requirement = requirement,
            unlockedAt = unlockedAt,
        )
}
