package dev.hossain.mathtutor.data.mapper

import dev.hossain.mathtutor.data.local.entity.StreakEntity
import dev.hossain.mathtutor.domain.model.DailyStreak
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class StreakMapperTest {
    @Test
    fun `toEntity converts domain model to entity`() {
        val date = LocalDate.of(2025, 1, 15)
        val domain =
            DailyStreak(
                currentStreak = 5,
                longestStreak = 10,
                lastPracticeDate = date,
                totalDaysPracticed = 20,
            )

        val entity = StreakMapper.toEntity(domain)

        assertEquals(1, entity.id) // Singleton ID
        assertEquals(5, entity.currentStreak)
        assertEquals(10, entity.longestStreak)
        assertEquals(date, entity.lastPracticeDate)
        assertEquals(20, entity.totalDaysPracticed)
    }

    @Test
    fun `toEntity with null lastPracticeDate`() {
        val domain = DailyStreak.EMPTY

        val entity = StreakMapper.toEntity(domain)

        assertEquals(1, entity.id)
        assertEquals(0, entity.currentStreak)
        assertEquals(0, entity.longestStreak)
        assertNull(entity.lastPracticeDate)
        assertEquals(0, entity.totalDaysPracticed)
    }

    @Test
    fun `toDomain converts entity to domain model`() {
        val date = LocalDate.of(2025, 1, 15)
        val entity =
            StreakEntity(
                id = 1,
                currentStreak = 7,
                longestStreak = 12,
                lastPracticeDate = date,
                totalDaysPracticed = 25,
            )

        val domain = StreakMapper.toDomain(entity)

        assertEquals(7, domain.currentStreak)
        assertEquals(12, domain.longestStreak)
        assertEquals(date, domain.lastPracticeDate)
        assertEquals(25, domain.totalDaysPracticed)
    }

    @Test
    fun `toDomain with null lastPracticeDate`() {
        val entity =
            StreakEntity(
                id = 1,
                currentStreak = 0,
                longestStreak = 0,
                lastPracticeDate = null,
                totalDaysPracticed = 0,
            )

        val domain = StreakMapper.toDomain(entity)

        assertEquals(0, domain.currentStreak)
        assertEquals(0, domain.longestStreak)
        assertNull(domain.lastPracticeDate)
        assertEquals(0, domain.totalDaysPracticed)
    }

    @Test
    fun `toEntity always uses ID 1 for singleton`() {
        val domain1 = DailyStreak(currentStreak = 5, longestStreak = 10, lastPracticeDate = LocalDate.now(), totalDaysPracticed = 15)
        val domain2 = DailyStreak(currentStreak = 3, longestStreak = 8, lastPracticeDate = LocalDate.now(), totalDaysPracticed = 12)

        val entity1 = StreakMapper.toEntity(domain1)
        val entity2 = StreakMapper.toEntity(domain2)

        assertEquals(1, entity1.id)
        assertEquals(1, entity2.id)
    }

    @Test
    fun `roundtrip conversion preserves data`() {
        val original =
            DailyStreak(
                currentStreak = 8,
                longestStreak = 15,
                lastPracticeDate = LocalDate.of(2025, 2, 20),
                totalDaysPracticed = 30,
            )

        val entity = StreakMapper.toEntity(original)
        val result = StreakMapper.toDomain(entity)

        assertEquals(original, result)
    }
}
