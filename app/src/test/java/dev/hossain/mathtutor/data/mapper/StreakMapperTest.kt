package dev.hossain.mathtutor.data.mapper

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.data.local.entity.StreakEntity
import dev.hossain.mathtutor.domain.model.DailyStreak
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

        assertThat(entity.id).isEqualTo(1) // Singleton ID
        assertThat(entity.currentStreak).isEqualTo(5)
        assertThat(entity.longestStreak).isEqualTo(10)
        assertThat(entity.lastPracticeDate).isEqualTo(date)
        assertThat(entity.totalDaysPracticed).isEqualTo(20)
    }

    @Test
    fun `toEntity with null lastPracticeDate`() {
        val domain = DailyStreak.EMPTY

        val entity = StreakMapper.toEntity(domain)

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.currentStreak).isEqualTo(0)
        assertThat(entity.longestStreak).isEqualTo(0)
        assertThat(entity.lastPracticeDate).isNull()
        assertThat(entity.totalDaysPracticed).isEqualTo(0)
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

        assertThat(domain.currentStreak).isEqualTo(7)
        assertThat(domain.longestStreak).isEqualTo(12)
        assertThat(domain.lastPracticeDate).isEqualTo(date)
        assertThat(domain.totalDaysPracticed).isEqualTo(25)
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

        assertThat(domain.currentStreak).isEqualTo(0)
        assertThat(domain.longestStreak).isEqualTo(0)
        assertThat(domain.lastPracticeDate).isNull()
        assertThat(domain.totalDaysPracticed).isEqualTo(0)
    }

    @Test
    fun `toEntity always uses ID 1 for singleton`() {
        val domain1 = DailyStreak(currentStreak = 5, longestStreak = 10, lastPracticeDate = LocalDate.now(), totalDaysPracticed = 15)
        val domain2 = DailyStreak(currentStreak = 3, longestStreak = 8, lastPracticeDate = LocalDate.now(), totalDaysPracticed = 12)

        val entity1 = StreakMapper.toEntity(domain1)
        val entity2 = StreakMapper.toEntity(domain2)

        assertThat(entity1.id).isEqualTo(1)
        assertThat(entity2.id).isEqualTo(1)
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

        assertThat(result).isEqualTo(original)
    }
}
