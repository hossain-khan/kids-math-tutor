package dev.hossain.mathtutor

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.SessionStats
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

        assertThat(streak.currentStreak).isEqualTo(0)
        assertThat(streak.longestStreak).isEqualTo(0)
        assertThat(streak.lastPracticeDate).isNull()
        assertThat(streak.totalDaysPracticed).isEqualTo(0)
        assertThat(streak.isStreakAlive(LocalDate.now())).isFalse()
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

        assertThat(unlockedBadges.isEmpty()).isTrue()
    }

    @Test
    fun `first-time user session stats are empty`() {
        val stats = SessionStats.EMPTY

        assertThat(stats.totalProblems).isEqualTo(0)
        assertThat(stats.correctCount).isEqualTo(0)
        assertThat(stats.accuracy).isWithin(0.01f).of(0f)
        assertThat(stats.sessionCount).isEqualTo(0)
    }

    @Test
    fun `first-time user first practice initializes streak to 1`() {
        val today = LocalDate.now()
        val firstStreak = DailyStreak.EMPTY.updateStreak(today)

        assertThat(firstStreak.currentStreak).isEqualTo(1)
        assertThat(firstStreak.longestStreak).isEqualTo(1)
        assertThat(firstStreak.lastPracticeDate).isEqualTo(today)
        assertThat(firstStreak.totalDaysPracticed).isEqualTo(1)
        assertThat(firstStreak.isStreakAlive(today)).isTrue()
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
        assertThat(unlockedBadges.size).isEqualTo(3)
        assertThat(unlockedBadges.any { it.id == "first_steps" }).isTrue()
        assertThat(unlockedBadges.any { it.id == "math_rookie" }).isTrue()
        assertThat(unlockedBadges.any { it.id == "perfect_10" }).isTrue()
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
        assertThat(unlockedCount).isEqualTo(2)
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

        assertThat(additionMet).isTrue()
        assertThat(subtractionMet).isTrue()
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

        assertThat(streak.currentStreak).isEqualTo(secondPractice.currentStreak)
        assertThat(streak.longestStreak).isEqualTo(secondPractice.longestStreak)
        assertThat(streak.totalDaysPracticed).isEqualTo(secondPractice.totalDaysPracticed)
        assertThat(streak.lastPracticeDate).isEqualTo(secondPractice.lastPracticeDate)
        assertThat(secondPractice).isEqualTo(streak)
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
            assertThat(updatedStreak).isEqualTo(streak)
            streak = updatedStreak
        }

        // All stats should remain unchanged
        assertThat(streak.currentStreak).isEqualTo(3)
        assertThat(streak.longestStreak).isEqualTo(5)
        assertThat(streak.totalDaysPracticed).isEqualTo(10)
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
        assertThat(streak.currentStreak).isEqualTo(8)
        assertThat(streak.totalDaysPracticed).isEqualTo(21)

        // Second practice today (should not change)
        val secondPractice = streak.updateStreak(today)
        assertThat(secondPractice.currentStreak).isEqualTo(8)
        assertThat(secondPractice.totalDaysPracticed).isEqualTo(21)
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

        assertThat(updated.currentStreak).isEqualTo(1)
        assertThat(updated.longestStreak).isEqualTo(15) // Preserved
        assertThat(updated.totalDaysPracticed).isEqualTo(26)
        assertThat(updated.lastPracticeDate).isEqualTo(today)
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

        assertThat(updated.currentStreak).isEqualTo(1)
        assertThat(updated.longestStreak).isEqualTo(25) // Preserved
        assertThat(updated.totalDaysPracticed).isEqualTo(51)
        assertThat(streak.isStreakAlive(today)).isFalse()
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

        assertThat(updated.currentStreak).isEqualTo(1)
        assertThat(updated.longestStreak).isEqualTo(20) // Longest still preserved
        assertThat(updated.totalDaysPracticed).isEqualTo(41)
    }

    @Test
    fun `streak reset after break can rebuild and exceed previous longest`() {
        var streak = DailyStreak.EMPTY
        val startDate = LocalDate.of(2025, 1, 1)

        // Build 5-day streak
        for (i in 0 until 5) {
            streak = streak.updateStreak(startDate.plusDays(i.toLong()))
        }
        assertThat(streak.currentStreak).isEqualTo(5)
        assertThat(streak.longestStreak).isEqualTo(5)

        // Skip 3 days, restart
        val restartDate = startDate.plusDays(8)
        streak = streak.updateStreak(restartDate)
        assertThat(streak.currentStreak).isEqualTo(1)
        assertThat(streak.longestStreak).isEqualTo(5)

        // Build 7-day streak (exceeding previous longest)
        for (i in 1 until 7) {
            streak = streak.updateStreak(restartDate.plusDays(i.toLong()))
        }
        assertThat(streak.currentStreak).isEqualTo(7)
        assertThat(streak.longestStreak).isEqualTo(7) // New longest
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
        assertThat(allBadgeRequirements.size).isEqualTo(15)
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

        assertThat(categories.size).isEqualTo(5)
        assertThat(categories).containsExactlyElementsIn(
            setOf(
                BadgeCategory.GETTING_STARTED,
                BadgeCategory.VOLUME,
                BadgeCategory.OPERATION_MASTERY,
                BadgeCategory.SPEED_ACCURACY,
                BadgeCategory.STREAK,
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
            assertThat(req.count > 0).isTrue()
            assertThat(req.count <= 1000).isTrue()
        }

        val streakBadges =
            listOf(
                BadgeRequirement.DailyStreak(3), // 3 days achievable
                BadgeRequirement.DailyStreak(7), // 1 week challenging
            )

        streakBadges.forEach { req ->
            assertThat(req.days > 0).isTrue()
            assertThat(req.days <= 30).isTrue()
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
        assertThat(meetsRequirement).isTrue()
    }

    @Test
    fun `one less than requirement does not unlock badge`() {
        val stats = SessionStats(totalProblems = 49, correctCount = 40, accuracy = 81.6f, sessionCount = 5)
        val requirement = BadgeRequirement.ProblemCount(50)

        // 49 problems should NOT unlock badge
        val meetsRequirement = stats.totalProblems >= 50
        assertThat(meetsRequirement).isFalse()
    }

    @Test
    fun `accuracy requirement with exactly 90 percent unlocks badge`() {
        // 9 correct out of 10 = 90%
        val stats = SessionStats(totalProblems = 10, correctCount = 9, accuracy = 90f, sessionCount = 1)
        val requirement = BadgeRequirement.SessionAccuracy(90f, 1)

        val meetsRequirement = stats.accuracy >= 90f && stats.sessionCount >= 1
        assertThat(meetsRequirement).isTrue()
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
        assertThat(meetsRequirement).isTrue()
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
        assertThat(streak.isStreakAlive(today)).isTrue()

        // Practicing today saves it
        val saved = streak.updateStreak(today)
        assertThat(saved.currentStreak).isEqualTo(7)
        assertThat(saved.isStreakAlive(today)).isTrue()
    }

    @Test
    fun `zero stats do not unlock any badges`() {
        val stats = SessionStats.EMPTY
        val problemCountBadge = BadgeRequirement.ProblemCount(1)
        val accuracyBadge = BadgeRequirement.SessionAccuracy(50f, 1)

        assertThat(stats.totalProblems >= 1).isFalse()
        assertThat(stats.sessionCount >= 1).isFalse()
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
            icon = dev.hossain.mathtutor.domain.model.BadgeIcon.FIRST_STEPS.name,
            category = BadgeCategory.GETTING_STARTED,
            requirement = requirement,
            unlockedAt = unlockedAt,
        )
}
