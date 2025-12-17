package dev.hossain.mathtutor.domain.usecase

import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.repository.StreakRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate

/**
 * Use case for updating daily practice streak based on practice completion.
 * Handles streak continuation, reset, and longest streak tracking.
 */
@SingleIn(AppScope::class)
@Inject
class UpdateStreakUseCase
    constructor(
        private val streakRepository: StreakRepository,
    ) {
        /**
         * Updates the streak based on today's practice session.
         * Handles four scenarios:
         * 1. First practice ever: Initialize streak to 1
         * 2. Same day practice: No change (returns current streak)
         * 3. Consecutive day: Increment streak
         * 4. Missed day(s): Reset streak to 1
         *
         * @param today The date when practice occurred (defaults to today)
         * @return Updated DailyStreak after processing
         */
        suspend fun updateStreak(today: LocalDate = LocalDate.now()): DailyStreak {
            val currentStreak = streakRepository.getStreak().first() ?: DailyStreak.EMPTY

            Timber.d(
                "Updating streak - Current: ${currentStreak.currentStreak}, " +
                    "Last: ${currentStreak.lastPracticeDate}, Today: $today",
            )

            val updatedStreak = currentStreak.updateStreak(today)

            // Only save if there was a change
            if (updatedStreak != currentStreak) {
                streakRepository.saveStreak(updatedStreak)
                Timber.d(
                    "Streak updated - New: ${updatedStreak.currentStreak}, " +
                        "Longest: ${updatedStreak.longestStreak}",
                )
            } else {
                Timber.d("Streak unchanged (same day practice)")
            }

            return updatedStreak
        }

        /**
         * Gets the current streak without updating it.
         *
         * @return Current DailyStreak, or EMPTY if no streak exists
         */
        suspend fun getCurrentStreak(): DailyStreak = streakRepository.getStreak().first() ?: DailyStreak.EMPTY
    }
