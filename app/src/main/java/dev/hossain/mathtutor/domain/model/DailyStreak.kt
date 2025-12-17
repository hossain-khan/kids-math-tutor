package dev.hossain.mathtutor.domain.model

import java.time.LocalDate

/**
 * Domain model representing daily practice streak statistics.
 * Tracks consecutive days of practice to encourage daily engagement.
 *
 * @property currentStreak Number of consecutive days the user has practiced (starts at 1)
 * @property longestStreak The longest streak the user has achieved
 * @property lastPracticeDate The last date the user completed a practice session
 * @property totalDaysPracticed Total number of unique days the user has practiced
 */
data class DailyStreak(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastPracticeDate: LocalDate? = null,
    val totalDaysPracticed: Int = 0,
) {
    /**
     * Updates the streak based on today's practice.
     * Handles three scenarios:
     * 1. First practice ever: Initialize streak to 1
     * 2. Same day practice: No change to streak
     * 3. Next consecutive day: Increment streak
     * 4. Missed day(s): Reset streak to 1
     *
     * @param today The current date when practice occurred
     * @return Updated DailyStreak with new values
     */
    fun updateStreak(today: LocalDate): DailyStreak {
        // First practice ever
        if (lastPracticeDate == null) {
            return DailyStreak(
                currentStreak = 1,
                longestStreak = 1,
                lastPracticeDate = today,
                totalDaysPracticed = 1,
            )
        }

        // Same day practice - no change
        if (lastPracticeDate == today) {
            return this
        }

        // Next consecutive day - increment streak
        val yesterday = today.minusDays(1)
        val newStreak =
            if (lastPracticeDate == yesterday) {
                currentStreak + 1
            } else {
                // Missed day(s) - reset to 1
                1
            }

        return DailyStreak(
            currentStreak = newStreak,
            longestStreak = maxOf(longestStreak, newStreak),
            lastPracticeDate = today,
            totalDaysPracticed = totalDaysPracticed + 1,
        )
    }

    /**
     * Checks if the streak is still alive based on today's date.
     * A streak is alive if:
     * - User practiced today, OR
     * - User practiced yesterday (still have time to continue today)
     *
     * @param today The current date to check against
     * @return true if streak is alive, false if it has expired
     */
    fun isStreakAlive(today: LocalDate): Boolean {
        if (lastPracticeDate == null) return false

        val yesterday = today.minusDays(1)
        return lastPracticeDate == today || lastPracticeDate == yesterday
    }

    companion object {
        /**
         * Empty initial state for a new user with no practice history.
         */
        val EMPTY =
            DailyStreak(
                currentStreak = 0,
                longestStreak = 0,
                lastPracticeDate = null,
                totalDaysPracticed = 0,
            )
    }
}
