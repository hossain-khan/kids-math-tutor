package dev.hossain.mathtutor.data.repository

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.data.local.dao.StreakDao
import dev.hossain.mathtutor.data.local.entity.StreakEntity
import dev.hossain.mathtutor.domain.model.DailyStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class StreakRepositoryImplTest {
    private lateinit var fakeDao: FakeStreakDao
    private lateinit var repository: StreakRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeStreakDao()
        repository = StreakRepositoryImpl(fakeDao)
    }

    @Test
    fun `getStreak returns null when no streak exists`() =
        runTest {
            fakeDao.streakFlow.value = null

            val result = repository.getStreak().first()

            assertThat(result).isNull()
        }

    @Test
    fun `getStreak returns mapped domain model`() =
        runTest {
            val date = LocalDate.of(2025, 1, 15)
            val entity =
                StreakEntity(
                    id = 1,
                    currentStreak = 5,
                    longestStreak = 10,
                    lastPracticeDate = date,
                    totalDaysPracticed = 20,
                )
            fakeDao.streakFlow.value = entity

            val result = repository.getStreak().first()

            assertThat(result?.currentStreak).isEqualTo(5)
            assertThat(result?.longestStreak).isEqualTo(10)
            assertThat(result?.lastPracticeDate).isEqualTo(date)
            assertThat(result?.totalDaysPracticed).isEqualTo(20)
        }

    @Test
    fun `saveStreak inserts entity correctly`() =
        runTest {
            val date = LocalDate.of(2025, 1, 15)
            val streak =
                DailyStreak(
                    currentStreak = 7,
                    longestStreak = 12,
                    lastPracticeDate = date,
                    totalDaysPracticed = 25,
                )

            repository.saveStreak(streak)

            assertThat(fakeDao.insertedEntities.size).isEqualTo(1)
            val inserted = fakeDao.insertedEntities[0]
            assertThat(inserted.id).isEqualTo(1) // Singleton ID
            assertThat(inserted.currentStreak).isEqualTo(7)
            assertThat(inserted.longestStreak).isEqualTo(12)
            assertThat(inserted.lastPracticeDate).isEqualTo(date)
            assertThat(inserted.totalDaysPracticed).isEqualTo(25)
        }

    @Test
    fun `saveStreak with EMPTY streak`() =
        runTest {
            repository.saveStreak(DailyStreak.EMPTY)

            assertThat(fakeDao.insertedEntities.size).isEqualTo(1)
            val inserted = fakeDao.insertedEntities[0]
            assertThat(inserted.currentStreak).isEqualTo(0)
            assertThat(inserted.longestStreak).isEqualTo(0)
            assertThat(inserted.lastPracticeDate).isNull()
            assertThat(inserted.totalDaysPracticed).isEqualTo(0)
        }

    @Test
    fun `getStreak emits updates when streak changes`() =
        runTest {
            // Initial state
            fakeDao.streakFlow.value = null
            val firstResult = repository.getStreak().first()
            assertThat(firstResult).isNull()

            // Update streak
            val date = LocalDate.of(2025, 1, 15)
            fakeDao.streakFlow.value =
                StreakEntity(
                    id = 1,
                    currentStreak = 3,
                    longestStreak = 5,
                    lastPracticeDate = date,
                    totalDaysPracticed = 10,
                )

            val secondResult = repository.getStreak().first()
            assertThat(secondResult?.currentStreak).isEqualTo(3)
            assertThat(secondResult?.longestStreak).isEqualTo(5)
        }

    @Test
    fun `multiple saveStreak calls replace previous data`() =
        runTest {
            val date1 = LocalDate.of(2025, 1, 15)
            val streak1 = DailyStreak(currentStreak = 1, longestStreak = 1, lastPracticeDate = date1, totalDaysPracticed = 1)
            repository.saveStreak(streak1)

            val date2 = LocalDate.of(2025, 1, 16)
            val streak2 = DailyStreak(currentStreak = 2, longestStreak = 2, lastPracticeDate = date2, totalDaysPracticed = 2)
            repository.saveStreak(streak2)

            // Both should be inserted (Room REPLACE strategy)
            assertThat(fakeDao.insertedEntities.size).isEqualTo(2)
            val latest = fakeDao.insertedEntities[1]
            assertThat(latest.currentStreak).isEqualTo(2)
            assertThat(latest.lastPracticeDate).isEqualTo(date2)
        }
}

/**
 * Fake implementation of StreakDao for testing.
 */
class FakeStreakDao : StreakDao {
    val streakFlow = MutableStateFlow<StreakEntity?>(null)
    val insertedEntities = mutableListOf<StreakEntity>()

    override fun getStreak(): Flow<StreakEntity?> = streakFlow

    override suspend fun insertStreak(streak: StreakEntity) {
        insertedEntities.add(streak)
        streakFlow.value = streak
    }

    override suspend fun deleteStreak() {
        insertedEntities.clear()
        streakFlow.value = null
    }
}
