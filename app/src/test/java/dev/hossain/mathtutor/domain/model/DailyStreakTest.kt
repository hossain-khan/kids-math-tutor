package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class DailyStreakTest {
    @Test
    fun `EMPTY constant has zero values`() {
        assertThat(DailyStreak.EMPTY.currentStreak).isEqualTo(0)
        assertThat(DailyStreak.EMPTY.longestStreak).isEqualTo(0)
        assertThat(DailyStreak.EMPTY.lastPracticeDate).isNull()
        assertThat(DailyStreak.EMPTY.totalDaysPracticed).isEqualTo(0)
    }

    @Test
    fun `updateStreak on first practice initializes to 1`() {
        val today = LocalDate.of(2025, 1, 15)
        val streak = DailyStreak.EMPTY.updateStreak(today)

        assertThat(streak.currentStreak).isEqualTo(1)
        assertThat(streak.longestStreak).isEqualTo(1)
        assertThat(streak.lastPracticeDate).isEqualTo(today)
        assertThat(streak.totalDaysPracticed).isEqualTo(1)
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

        assertThat(updated).isEqualTo(existingStreak)
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

        assertThat(updated.currentStreak).isEqualTo(6)
        assertThat(updated.longestStreak).isEqualTo(10) // Unchanged since 6 < 10
        assertThat(updated.lastPracticeDate).isEqualTo(today)
        assertThat(updated.totalDaysPracticed).isEqualTo(16)
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

        assertThat(updated.currentStreak).isEqualTo(11)
        assertThat(updated.longestStreak).isEqualTo(11) // Updated to match current
        assertThat(updated.lastPracticeDate).isEqualTo(today)
        assertThat(updated.totalDaysPracticed).isEqualTo(21)
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

        assertThat(updated.currentStreak).isEqualTo(1) // Reset
        assertThat(updated.longestStreak).isEqualTo(10) // Longest unchanged
        assertThat(updated.lastPracticeDate).isEqualTo(today)
        assertThat(updated.totalDaysPracticed).isEqualTo(16)
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

        assertThat(updated.currentStreak).isEqualTo(1)
        assertThat(updated.longestStreak).isEqualTo(7)
        assertThat(updated.lastPracticeDate).isEqualTo(today)
        assertThat(updated.totalDaysPracticed).isEqualTo(11)
    }

    @Test
    fun `isStreakAlive returns false when no practice date`() {
        val today = LocalDate.of(2025, 1, 15)
        val streak = DailyStreak.EMPTY

        assertThat(streak.isStreakAlive(today)).isFalse()
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

        assertThat(streak.isStreakAlive(today)).isTrue()
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

        assertThat(streak.isStreakAlive(today)).isTrue()
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

        assertThat(streak.isStreakAlive(today)).isFalse()
    }

    @Test
    fun `building a streak over multiple consecutive days`() {
        var streak = DailyStreak.EMPTY
        val startDate = LocalDate.of(2025, 1, 1)

        // Practice for 7 consecutive days
        for (i in 0 until 7) {
            val date = startDate.plusDays(i.toLong())
            streak = streak.updateStreak(date)
            assertThat(streak.currentStreak).isEqualTo(i + 1)
            assertThat(streak.longestStreak).isEqualTo(i + 1)
            assertThat(streak.lastPracticeDate).isEqualTo(date)
            assertThat(streak.totalDaysPracticed).isEqualTo(i + 1)
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
        assertThat(streak.currentStreak).isEqualTo(7)
        assertThat(streak.longestStreak).isEqualTo(7)

        // Miss 2 days and restart
        val restartDate = startDate.plusDays(9) // 2 days gap
        streak = streak.updateStreak(restartDate)
        assertThat(streak.currentStreak).isEqualTo(1)
        assertThat(streak.longestStreak).isEqualTo(7) // Longest preserved

        // Build a new 5-day streak
        for (i in 1 until 5) {
            streak = streak.updateStreak(restartDate.plusDays(i.toLong()))
        }
        assertThat(streak.currentStreak).isEqualTo(5)
        assertThat(streak.longestStreak).isEqualTo(7) // Still the longest
        assertThat(streak.totalDaysPracticed).isEqualTo(12) // 7 + 1 + 4
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
        assertThat(streak.currentStreak).isEqualTo(4)
        assertThat(streak.totalDaysPracticed).isEqualTo(11)

        // Second practice same day
        val secondUpdate = streak.updateStreak(today)
        assertThat(secondUpdate.currentStreak).isEqualTo(4)
        assertThat(secondUpdate.totalDaysPracticed).isEqualTo(11)
        assertThat(secondUpdate).isEqualTo(streak)
    }

    @Test
    fun `totalDaysPracticed accumulates correctly with gaps`() {
        var streak = DailyStreak.EMPTY

        // Day 1
        streak = streak.updateStreak(LocalDate.of(2025, 1, 1))
        assertThat(streak.totalDaysPracticed).isEqualTo(1)

        // Day 2 (consecutive)
        streak = streak.updateStreak(LocalDate.of(2025, 1, 2))
        assertThat(streak.totalDaysPracticed).isEqualTo(2)

        // Skip to Day 5 (2 days gap)
        streak = streak.updateStreak(LocalDate.of(2025, 1, 5))
        assertThat(streak.totalDaysPracticed).isEqualTo(3)

        // Skip to Day 10 (4 days gap)
        streak = streak.updateStreak(LocalDate.of(2025, 1, 10))
        assertThat(streak.totalDaysPracticed).isEqualTo(4)
    }
}
