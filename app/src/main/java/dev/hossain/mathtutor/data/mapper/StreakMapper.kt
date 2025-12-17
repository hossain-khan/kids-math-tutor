package dev.hossain.mathtutor.data.mapper

import dev.hossain.mathtutor.data.local.entity.StreakEntity
import dev.hossain.mathtutor.domain.model.DailyStreak

/**
 * Mapper for converting between StreakEntity (data layer) and DailyStreak (domain layer).
 */
object StreakMapper {
    /**
     * Converts domain model to database entity.
     *
     * @param streak The domain model to convert
     * @return StreakEntity ready for database storage
     */
    fun toEntity(streak: DailyStreak): StreakEntity =
        StreakEntity(
            id = 1, // Singleton - always use ID 1
            currentStreak = streak.currentStreak,
            longestStreak = streak.longestStreak,
            lastPracticeDate = streak.lastPracticeDate,
            totalDaysPracticed = streak.totalDaysPracticed,
        )

    /**
     * Converts database entity to domain model.
     *
     * @param entity The database entity to convert
     * @return DailyStreak domain model
     */
    fun toDomain(entity: StreakEntity): DailyStreak =
        DailyStreak(
            currentStreak = entity.currentStreak,
            longestStreak = entity.longestStreak,
            lastPracticeDate = entity.lastPracticeDate,
            totalDaysPracticed = entity.totalDaysPracticed,
        )
}
