package dev.hossain.mathtutor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Room entity representing daily practice streak statistics.
 * Stores a single row of streak data (singleton table).
 *
 * @property id Always 1 (singleton - only one row in table)
 * @property currentStreak Number of consecutive days the user has practiced
 * @property longestStreak The longest streak the user has achieved
 * @property lastPracticeDate The last date the user completed a practice session (stored as epoch day)
 * @property totalDaysPracticed Total number of unique days the user has practiced
 */
@Entity(tableName = "streak")
data class StreakEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton - only one row
    val currentStreak: Int,
    val longestStreak: Int,
    val lastPracticeDate: LocalDate?,
    val totalDaysPracticed: Int,
)
