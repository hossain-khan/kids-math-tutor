package dev.hossain.mathtutor.domain.usecase

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
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

            assertThat(result.currentStreak).isEqualTo(1)
            assertThat(result.longestStreak).isEqualTo(1)
            assertThat(result.lastPracticeDate).isEqualTo(today)
            assertThat(result.totalDaysPracticed).isEqualTo(1)
            assertThat(fakeRepository.savedStreaks.size).isEqualTo(1)
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

            assertThat(result.currentStreak).isEqualTo(6)
            assertThat(result.longestStreak).isEqualTo(10)
            assertThat(result.lastPracticeDate).isEqualTo(today)
            assertThat(result.totalDaysPracticed).isEqualTo(16)
            assertThat(fakeRepository.savedStreaks.size).isEqualTo(1)
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
            assertThat(result.currentStreak).isEqualTo(5)
            assertThat(result.longestStreak).isEqualTo(10)
            assertThat(result.lastPracticeDate).isEqualTo(today)
            assertThat(result.totalDaysPracticed).isEqualTo(15)
            assertThat(fakeRepository.savedStreaks.size).isEqualTo(0) // No save for same-day
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

            assertThat(result.currentStreak).isEqualTo(1) // Reset
            assertThat(result.longestStreak).isEqualTo(10) // Preserved
            assertThat(result.lastPracticeDate).isEqualTo(today)
            assertThat(result.totalDaysPracticed).isEqualTo(16)
            assertThat(fakeRepository.savedStreaks.size).isEqualTo(1)
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

            assertThat(result.currentStreak).isEqualTo(11)
            assertThat(result.longestStreak).isEqualTo(11) // Updated
            assertThat(result.lastPracticeDate).isEqualTo(today)
            assertThat(result.totalDaysPracticed).isEqualTo(21)
            assertThat(fakeRepository.savedStreaks.size).isEqualTo(1)
        }

    @Test
    fun `getCurrentStreak returns EMPTY when no streak exists`() =
        runTest {
            val result = useCase.getCurrentStreak()

            assertThat(result).isEqualTo(DailyStreak.EMPTY)
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

            assertThat(result).isEqualTo(expected)
        }

    @Test
    fun `updateStreak uses current date by default`() =
        runTest {
            // Don't pass today parameter - use default
            val result = useCase.updateStreak()

            assertThat(result.currentStreak).isEqualTo(1)
            assertThat(result.lastPracticeDate).isEqualTo(LocalDate.now())
            assertThat(fakeRepository.savedStreaks.size).isEqualTo(1)
        }

    @Test
    fun `multiple updateStreak calls build streak correctly`() =
        runTest {
            val day1 = LocalDate.of(2025, 1, 1)
            val day2 = LocalDate.of(2025, 1, 2)
            val day3 = LocalDate.of(2025, 1, 3)

            val streak1 = useCase.updateStreak(day1)
            assertThat(streak1.currentStreak).isEqualTo(1)

            fakeRepository.currentStreak = streak1
            val streak2 = useCase.updateStreak(day2)
            assertThat(streak2.currentStreak).isEqualTo(2)

            fakeRepository.currentStreak = streak2
            val streak3 = useCase.updateStreak(day3)
            assertThat(streak3.currentStreak).isEqualTo(3)
            assertThat(streak3.longestStreak).isEqualTo(3)

            assertThat(fakeRepository.savedStreaks.size).isEqualTo(3)
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

            assertThat(result.currentStreak).isEqualTo(1)
            assertThat(result.longestStreak).isEqualTo(7) // Longest preserved
            assertThat(result.totalDaysPracticed).isEqualTo(8)
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
