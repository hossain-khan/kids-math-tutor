package dev.hossain.mathtutor.domain.usecase

import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class UpdateStreakUseCaseTest {
    private lateinit var fakeRepository: FakeStreakRepository
    private lateinit var useCase: UpdateStreakUseCase

    @Before
    fun setup() {
        fakeRepository = FakeStreakRepository()
        useCase = UpdateStreakUseCase(fakeRepository)
    }

    @Test
    fun `updateStreak on first practice initializes to 1`() =
        runTest {
            val today = LocalDate.of(2025, 1, 15)

            val result = useCase.updateStreak(today)

            assertEquals(1, result.currentStreak)
            assertEquals(1, result.longestStreak)
            assertEquals(today, result.lastPracticeDate)
            assertEquals(1, result.totalDaysPracticed)
            assertEquals(1, fakeRepository.savedStreaks.size)
        }

    @Test
    fun `updateStreak on consecutive day increments streak`() =
        runTest {
            val yesterday = LocalDate.of(2025, 1, 14)
            val today = LocalDate.of(2025, 1, 15)
            fakeRepository.currentStreak =
                DailyStreak(
                    currentStreak = 5,
                    longestStreak = 10,
                    lastPracticeDate = yesterday,
                    totalDaysPracticed = 15,
                )

            val result = useCase.updateStreak(today)

            assertEquals(6, result.currentStreak)
            assertEquals(10, result.longestStreak)
            assertEquals(today, result.lastPracticeDate)
            assertEquals(16, result.totalDaysPracticed)
            assertEquals(1, fakeRepository.savedStreaks.size)
        }

    @Test
    fun `updateStreak on same day returns unchanged streak`() =
        runTest {
            val today = LocalDate.of(2025, 1, 15)
            fakeRepository.currentStreak =
                DailyStreak(
                    currentStreak = 5,
                    longestStreak = 10,
                    lastPracticeDate = today,
                    totalDaysPracticed = 15,
                )

            val result = useCase.updateStreak(today)

            // No change expected, no save should occur
            assertEquals(5, result.currentStreak)
            assertEquals(10, result.longestStreak)
            assertEquals(today, result.lastPracticeDate)
            assertEquals(15, result.totalDaysPracticed)
            assertEquals(0, fakeRepository.savedStreaks.size) // No save for same-day
        }

    @Test
    fun `updateStreak after missing day resets to 1`() =
        runTest {
            val twoDaysAgo = LocalDate.of(2025, 1, 13)
            val today = LocalDate.of(2025, 1, 15)
            fakeRepository.currentStreak =
                DailyStreak(
                    currentStreak = 5,
                    longestStreak = 10,
                    lastPracticeDate = twoDaysAgo,
                    totalDaysPracticed = 15,
                )

            val result = useCase.updateStreak(today)

            assertEquals(1, result.currentStreak) // Reset
            assertEquals(10, result.longestStreak) // Preserved
            assertEquals(today, result.lastPracticeDate)
            assertEquals(16, result.totalDaysPracticed)
            assertEquals(1, fakeRepository.savedStreaks.size)
        }

    @Test
    fun `updateStreak updates longestStreak when exceeded`() =
        runTest {
            val yesterday = LocalDate.of(2025, 1, 14)
            val today = LocalDate.of(2025, 1, 15)
            fakeRepository.currentStreak =
                DailyStreak(
                    currentStreak = 10,
                    longestStreak = 10,
                    lastPracticeDate = yesterday,
                    totalDaysPracticed = 20,
                )

            val result = useCase.updateStreak(today)

            assertEquals(11, result.currentStreak)
            assertEquals(11, result.longestStreak) // Updated
            assertEquals(today, result.lastPracticeDate)
            assertEquals(21, result.totalDaysPracticed)
            assertEquals(1, fakeRepository.savedStreaks.size)
        }

    @Test
    fun `getCurrentStreak returns EMPTY when no streak exists`() =
        runTest {
            val result = useCase.getCurrentStreak()

            assertEquals(DailyStreak.EMPTY, result)
        }

    @Test
    fun `getCurrentStreak returns existing streak`() =
        runTest {
            val expected =
                DailyStreak(
                    currentStreak = 7,
                    longestStreak = 12,
                    lastPracticeDate = LocalDate.of(2025, 1, 15),
                    totalDaysPracticed = 25,
                )
            fakeRepository.currentStreak = expected

            val result = useCase.getCurrentStreak()

            assertEquals(expected, result)
        }

    @Test
    fun `updateStreak uses current date by default`() =
        runTest {
            // Don't pass today parameter - use default
            val result = useCase.updateStreak()

            assertEquals(1, result.currentStreak)
            assertEquals(LocalDate.now(), result.lastPracticeDate)
            assertEquals(1, fakeRepository.savedStreaks.size)
        }

    @Test
    fun `multiple updateStreak calls build streak correctly`() =
        runTest {
            val day1 = LocalDate.of(2025, 1, 1)
            val day2 = LocalDate.of(2025, 1, 2)
            val day3 = LocalDate.of(2025, 1, 3)

            val streak1 = useCase.updateStreak(day1)
            assertEquals(1, streak1.currentStreak)

            fakeRepository.currentStreak = streak1
            val streak2 = useCase.updateStreak(day2)
            assertEquals(2, streak2.currentStreak)

            fakeRepository.currentStreak = streak2
            val streak3 = useCase.updateStreak(day3)
            assertEquals(3, streak3.currentStreak)
            assertEquals(3, streak3.longestStreak)

            assertEquals(3, fakeRepository.savedStreaks.size)
        }

    @Test
    fun `updateStreak after gap resets but preserves longest`() =
        runTest {
            // Build initial streak
            fakeRepository.currentStreak =
                DailyStreak(
                    currentStreak = 7,
                    longestStreak = 7,
                    lastPracticeDate = LocalDate.of(2025, 1, 1),
                    totalDaysPracticed = 7,
                )

            // Practice after gap
            val result = useCase.updateStreak(LocalDate.of(2025, 1, 5))

            assertEquals(1, result.currentStreak)
            assertEquals(7, result.longestStreak) // Longest preserved
            assertEquals(8, result.totalDaysPracticed)
        }
}

/**
 * Fake implementation of StreakRepository for testing.
 */
class FakeStreakRepository : StreakRepository {
    var currentStreak: DailyStreak? = null
    val savedStreaks = mutableListOf<DailyStreak>()
    private val streakFlow = MutableStateFlow<DailyStreak?>(null)

    override fun getStreak(): Flow<DailyStreak?> {
        streakFlow.value = currentStreak
        return streakFlow
    }

    override suspend fun saveStreak(streak: DailyStreak) {
        savedStreaks.add(streak)
        currentStreak = streak
        streakFlow.value = streak
    }
}
