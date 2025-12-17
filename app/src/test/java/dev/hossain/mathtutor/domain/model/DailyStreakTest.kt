package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyStreakTest {
    @Test
    fun `EMPTY constant has zero values`() {
        assertEquals(0, DailyStreak.EMPTY.currentStreak)
        assertEquals(0, DailyStreak.EMPTY.longestStreak)
        assertNull(DailyStreak.EMPTY.lastPracticeDate)
        assertEquals(0, DailyStreak.EMPTY.totalDaysPracticed)
    }

    @Test
    fun `updateStreak on first practice initializes to 1`() {
        val today = LocalDate.of(2025, 1, 15)
        val streak = DailyStreak.EMPTY.updateStreak(today)

        assertEquals(1, streak.currentStreak)
        assertEquals(1, streak.longestStreak)
        assertEquals(today, streak.lastPracticeDate)
        assertEquals(1, streak.totalDaysPracticed)
    }

    @Test
    fun `updateStreak on same day returns unchanged streak`() {
        val today = LocalDate.of(2025, 1, 15)
        val existingStreak =
            DailyStreak(
                currentStreak = 5,
                longestStreak = 10,
                lastPracticeDate = today,
                totalDaysPracticed = 15,
            )

        val updated = existingStreak.updateStreak(today)

        assertEquals(existingStreak, updated)
    }

    @Test
    fun `updateStreak on consecutive day increments streak`() {
        val yesterday = LocalDate.of(2025, 1, 14)
        val today = LocalDate.of(2025, 1, 15)
        val existingStreak =
            DailyStreak(
                currentStreak = 5,
                longestStreak = 10,
                lastPracticeDate = yesterday,
                totalDaysPracticed = 15,
            )

        val updated = existingStreak.updateStreak(today)

        assertEquals(6, updated.currentStreak)
        assertEquals(10, updated.longestStreak) // Unchanged since 6 < 10
        assertEquals(today, updated.lastPracticeDate)
        assertEquals(16, updated.totalDaysPracticed)
    }

    @Test
    fun `updateStreak updates longestStreak when current exceeds it`() {
        val yesterday = LocalDate.of(2025, 1, 14)
        val today = LocalDate.of(2025, 1, 15)
        val existingStreak =
            DailyStreak(
                currentStreak = 10,
                longestStreak = 10,
                lastPracticeDate = yesterday,
                totalDaysPracticed = 20,
            )

        val updated = existingStreak.updateStreak(today)

        assertEquals(11, updated.currentStreak)
        assertEquals(11, updated.longestStreak) // Updated to match current
        assertEquals(today, updated.lastPracticeDate)
        assertEquals(21, updated.totalDaysPracticed)
    }

    @Test
    fun `updateStreak resets to 1 after missing one day`() {
        val twoDaysAgo = LocalDate.of(2025, 1, 13)
        val today = LocalDate.of(2025, 1, 15)
        val existingStreak =
            DailyStreak(
                currentStreak = 5,
                longestStreak = 10,
                lastPracticeDate = twoDaysAgo,
                totalDaysPracticed = 15,
            )

        val updated = existingStreak.updateStreak(today)

        assertEquals(1, updated.currentStreak) // Reset
        assertEquals(10, updated.longestStreak) // Longest unchanged
        assertEquals(today, updated.lastPracticeDate)
        assertEquals(16, updated.totalDaysPracticed)
    }

    @Test
    fun `updateStreak resets to 1 after missing multiple days`() {
        val weekAgo = LocalDate.of(2025, 1, 8)
        val today = LocalDate.of(2025, 1, 15)
        val existingStreak =
            DailyStreak(
                currentStreak = 3,
                longestStreak = 7,
                lastPracticeDate = weekAgo,
                totalDaysPracticed = 10,
            )

        val updated = existingStreak.updateStreak(today)

        assertEquals(1, updated.currentStreak)
        assertEquals(7, updated.longestStreak)
        assertEquals(today, updated.lastPracticeDate)
        assertEquals(11, updated.totalDaysPracticed)
    }

    @Test
    fun `isStreakAlive returns false when no practice date`() {
        val today = LocalDate.of(2025, 1, 15)
        val streak = DailyStreak.EMPTY

        assertFalse(streak.isStreakAlive(today))
    }

    @Test
    fun `isStreakAlive returns true when practiced today`() {
        val today = LocalDate.of(2025, 1, 15)
        val streak =
            DailyStreak(
                currentStreak = 5,
                longestStreak = 10,
                lastPracticeDate = today,
                totalDaysPracticed = 15,
            )

        assertTrue(streak.isStreakAlive(today))
    }

    @Test
    fun `isStreakAlive returns true when practiced yesterday`() {
        val yesterday = LocalDate.of(2025, 1, 14)
        val today = LocalDate.of(2025, 1, 15)
        val streak =
            DailyStreak(
                currentStreak = 5,
                longestStreak = 10,
                lastPracticeDate = yesterday,
                totalDaysPracticed = 15,
            )

        assertTrue(streak.isStreakAlive(today))
    }

    @Test
    fun `isStreakAlive returns false when practiced two days ago`() {
        val twoDaysAgo = LocalDate.of(2025, 1, 13)
        val today = LocalDate.of(2025, 1, 15)
        val streak =
            DailyStreak(
                currentStreak = 5,
                longestStreak = 10,
                lastPracticeDate = twoDaysAgo,
                totalDaysPracticed = 15,
            )

        assertFalse(streak.isStreakAlive(today))
    }

    @Test
    fun `building a streak over multiple consecutive days`() {
        var streak = DailyStreak.EMPTY
        val startDate = LocalDate.of(2025, 1, 1)

        // Practice for 7 consecutive days
        for (i in 0 until 7) {
            val date = startDate.plusDays(i.toLong())
            streak = streak.updateStreak(date)
            assertEquals(i + 1, streak.currentStreak)
            assertEquals(i + 1, streak.longestStreak)
            assertEquals(date, streak.lastPracticeDate)
            assertEquals(i + 1, streak.totalDaysPracticed)
        }
    }

    @Test
    fun `streak broken and rebuilt maintains longest`() {
        var streak = DailyStreak.EMPTY
        val startDate = LocalDate.of(2025, 1, 1)

        // Build initial 7-day streak
        for (i in 0 until 7) {
            streak = streak.updateStreak(startDate.plusDays(i.toLong()))
        }
        assertEquals(7, streak.currentStreak)
        assertEquals(7, streak.longestStreak)

        // Miss 2 days and restart
        val restartDate = startDate.plusDays(9) // 2 days gap
        streak = streak.updateStreak(restartDate)
        assertEquals(1, streak.currentStreak)
        assertEquals(7, streak.longestStreak) // Longest preserved

        // Build a new 5-day streak
        for (i in 1 until 5) {
            streak = streak.updateStreak(restartDate.plusDays(i.toLong()))
        }
        assertEquals(5, streak.currentStreak)
        assertEquals(7, streak.longestStreak) // Still the longest
        assertEquals(12, streak.totalDaysPracticed) // 7 + 1 + 4
    }

    @Test
    fun `multiple practices on same day do not affect stats`() {
        val today = LocalDate.of(2025, 1, 15)
        var streak =
            DailyStreak(
                currentStreak = 3,
                longestStreak = 5,
                lastPracticeDate = today.minusDays(1),
                totalDaysPracticed = 10,
            )

        // First practice today
        streak = streak.updateStreak(today)
        assertEquals(4, streak.currentStreak)
        assertEquals(11, streak.totalDaysPracticed)

        // Second practice same day
        val secondUpdate = streak.updateStreak(today)
        assertEquals(4, secondUpdate.currentStreak)
        assertEquals(11, secondUpdate.totalDaysPracticed)
        assertEquals(streak, secondUpdate)
    }

    @Test
    fun `totalDaysPracticed accumulates correctly with gaps`() {
        var streak = DailyStreak.EMPTY

        // Day 1
        streak = streak.updateStreak(LocalDate.of(2025, 1, 1))
        assertEquals(1, streak.totalDaysPracticed)

        // Day 2 (consecutive)
        streak = streak.updateStreak(LocalDate.of(2025, 1, 2))
        assertEquals(2, streak.totalDaysPracticed)

        // Skip to Day 5 (2 days gap)
        streak = streak.updateStreak(LocalDate.of(2025, 1, 5))
        assertEquals(3, streak.totalDaysPracticed)

        // Skip to Day 10 (4 days gap)
        streak = streak.updateStreak(LocalDate.of(2025, 1, 10))
        assertEquals(4, streak.totalDaysPracticed)
    }
}
