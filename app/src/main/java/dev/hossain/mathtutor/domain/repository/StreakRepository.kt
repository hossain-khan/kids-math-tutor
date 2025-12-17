package dev.hossain.mathtutor.domain.repository

import dev.hossain.mathtutor.domain.model.DailyStreak
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for streak data management.
 * Provides methods to retrieve and update daily practice streak information.
 */
interface StreakRepository {
    /**
     * Retrieves the current streak data as a Flow.
     * Emits updates whenever the streak data changes.
     *
     * @return Flow of DailyStreak, or null if no streak data exists
     */
    fun getStreak(): Flow<DailyStreak?>

    /**
     * Saves or updates the streak data in persistent storage.
     *
     * @param streak The streak data to save
     */
    suspend fun saveStreak(streak: DailyStreak)
}
