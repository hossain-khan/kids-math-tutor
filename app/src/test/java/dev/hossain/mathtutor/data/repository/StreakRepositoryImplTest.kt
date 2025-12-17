package dev.hossain.mathtutor.data.repository

import dev.hossain.mathtutor.data.local.dao.StreakDao
import dev.hossain.mathtutor.data.local.entity.StreakEntity
import dev.hossain.mathtutor.domain.model.DailyStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

            assertNull(result)
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

            assertEquals(5, result?.currentStreak)
            assertEquals(10, result?.longestStreak)
            assertEquals(date, result?.lastPracticeDate)
            assertEquals(20, result?.totalDaysPracticed)
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

            assertEquals(1, fakeDao.insertedEntities.size)
            val inserted = fakeDao.insertedEntities[0]
            assertEquals(1, inserted.id) // Singleton ID
            assertEquals(7, inserted.currentStreak)
            assertEquals(12, inserted.longestStreak)
            assertEquals(date, inserted.lastPracticeDate)
            assertEquals(25, inserted.totalDaysPracticed)
        }

    @Test
    fun `saveStreak with EMPTY streak`() =
        runTest {
            repository.saveStreak(DailyStreak.EMPTY)

            assertEquals(1, fakeDao.insertedEntities.size)
            val inserted = fakeDao.insertedEntities[0]
            assertEquals(0, inserted.currentStreak)
            assertEquals(0, inserted.longestStreak)
            assertNull(inserted.lastPracticeDate)
            assertEquals(0, inserted.totalDaysPracticed)
        }

    @Test
    fun `getStreak emits updates when streak changes`() =
        runTest {
            // Initial state
            fakeDao.streakFlow.value = null
            val firstResult = repository.getStreak().first()
            assertNull(firstResult)

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
            assertEquals(3, secondResult?.currentStreak)
            assertEquals(5, secondResult?.longestStreak)
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
            assertEquals(2, fakeDao.insertedEntities.size)
            val latest = fakeDao.insertedEntities[1]
            assertEquals(2, latest.currentStreak)
            assertEquals(date2, latest.lastPracticeDate)
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
