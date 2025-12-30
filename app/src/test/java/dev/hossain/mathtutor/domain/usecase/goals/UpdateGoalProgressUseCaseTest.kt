package dev.hossain.mathtutor.domain.usecase.goals

import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.model.goals.ComponentProgress
import dev.hossain.mathtutor.domain.model.goals.GoalError
import dev.hossain.mathtutor.domain.repository.GoalRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class UpdateGoalProgressUseCaseTest {
    @Mock
    private lateinit var goalRepository: GoalRepository

    private lateinit var useCase: UpdateGoalProgressUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = UpdateGoalProgressUseCase(goalRepository)
    }

    @Test
    fun `invoke with valid inputs updates progress successfully`() =
        runTest {
            val mockActiveGoal =
                ActiveGoal(
                    id = "active-goal-1",
                    goalId = "goal-123",
                    currentComponentIndex = 0,
                    componentProgress =
                        listOf(
                            ComponentProgress(
                                componentIndex = 0,
                                completedSessions = 1,
                                totalSessions = 3,
                                accuracy = 85f,
                                totalTimeSeconds = 120L,
                            ),
                        ),
                    startedAt = Instant.now(),
                )

            whenever(
                goalRepository.updateComponentProgress(
                    eq(0),
                    eq(1),
                    eq(85f),
                    eq(120L),
                ),
            ).thenReturn(Result.success(mockActiveGoal))

            val result = useCase(0, 1, 85f, 120L)

            assertTrue(result.isSuccess)
            assertEquals(mockActiveGoal, result.getOrNull())
        }

    @Test
    fun `invoke with negative componentIndex returns InvalidComponent error`() =
        runTest {
            val result = useCase(-1, 1, 85f, 120L)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidComponent)
        }

    @Test
    fun `invoke with negative completedSessions returns InvalidComponent error`() =
        runTest {
            val result = useCase(0, -1, 85f, 120L)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidComponent)
        }

    @Test
    fun `invoke with accuracy below 0 returns InvalidAccuracy error`() =
        runTest {
            val result = useCase(0, 1, -0.1f, 120L)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidAccuracy)
        }

    @Test
    fun `invoke with accuracy above 100 returns InvalidAccuracy error`() =
        runTest {
            val result = useCase(0, 1, 100.1f, 120L)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidAccuracy)
        }

    @Test
    fun `invoke with accuracy exactly 0 succeeds`() =
        runTest {
            val mockActiveGoal =
                ActiveGoal(
                    id = "active-goal-1",
                    goalId = "goal-123",
                    currentComponentIndex = 0,
                    componentProgress = listOf(),
                    startedAt = Instant.now(),
                )

            whenever(
                goalRepository.updateComponentProgress(
                    eq(0),
                    eq(1),
                    eq(0f),
                    eq(120L),
                ),
            ).thenReturn(Result.success(mockActiveGoal))

            val result = useCase(0, 1, 0f, 120L)

            assertTrue(result.isSuccess)
        }

    @Test
    fun `invoke with accuracy exactly 100 succeeds`() =
        runTest {
            val mockActiveGoal =
                ActiveGoal(
                    id = "active-goal-1",
                    goalId = "goal-123",
                    currentComponentIndex = 0,
                    componentProgress = listOf(),
                    startedAt = Instant.now(),
                )

            whenever(
                goalRepository.updateComponentProgress(
                    eq(0),
                    eq(1),
                    eq(100f),
                    eq(120L),
                ),
            ).thenReturn(Result.success(mockActiveGoal))

            val result = useCase(0, 1, 100f, 120L)

            assertTrue(result.isSuccess)
        }

    @Test
    fun `invoke with negative time returns InvalidComponent error`() =
        runTest {
            val result = useCase(0, 1, 85f, -1L)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidComponent)
        }

    @Test
    fun `invoke with zero time succeeds`() =
        runTest {
            val mockActiveGoal =
                ActiveGoal(
                    id = "active-goal-1",
                    goalId = "goal-123",
                    currentComponentIndex = 0,
                    componentProgress = listOf(),
                    startedAt = Instant.now(),
                )

            whenever(
                goalRepository.updateComponentProgress(
                    eq(0),
                    eq(1),
                    eq(85f),
                    eq(0L),
                ),
            ).thenReturn(Result.success(mockActiveGoal))

            val result = useCase(0, 1, 85f, 0L)

            assertTrue(result.isSuccess)
        }

    @Test
    fun `invoke delegates to repository with correct parameters`() =
        runTest {
            val mockActiveGoal =
                ActiveGoal(
                    id = "active-goal-1",
                    goalId = "goal-123",
                    currentComponentIndex = 1,
                    componentProgress = listOf(),
                    startedAt = Instant.now(),
                )

            whenever(
                goalRepository.updateComponentProgress(
                    eq(2),
                    eq(5),
                    eq(92.5f),
                    eq(450L),
                ),
            ).thenReturn(Result.success(mockActiveGoal))

            useCase(2, 5, 92.5f, 450L)

            verify(goalRepository).updateComponentProgress(
                eq(2),
                eq(5),
                eq(92.5f),
                eq(450L),
            )
        }
}
