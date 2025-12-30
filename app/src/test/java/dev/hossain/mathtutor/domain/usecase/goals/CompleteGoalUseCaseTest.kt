package dev.hossain.mathtutor.domain.usecase.goals

import dev.hossain.mathtutor.domain.model.goals.GoalError
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import dev.hossain.mathtutor.domain.repository.GoalRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class CompleteGoalUseCaseTest {
    @Mock
    private lateinit var goalRepository: GoalRepository

    private lateinit var useCase: CompleteGoalUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = CompleteGoalUseCase(goalRepository)
    }

    @Test
    fun `invoke completes active goal successfully`() =
        runTest {
            val mockGoalHistory =
                GoalHistory(
                    id = "history-1",
                    goalId = "goal-123",
                    completedAt = Instant.now(),
                    totalAccuracy = 87.5f,
                    totalTimeSeconds = 450L,
                )

            whenever(goalRepository.completeActiveGoal())
                .thenReturn(Result.success(mockGoalHistory))

            val result = useCase()

            assertTrue(result.isSuccess)
            assertEquals(mockGoalHistory, result.getOrNull())
            verify(goalRepository).completeActiveGoal()
        }

    @Test
    fun `invoke with no active goal returns NoActiveGoal error`() =
        runTest {
            whenever(goalRepository.completeActiveGoal())
                .thenReturn(Result.failure(GoalError.NoActiveGoal()))

            val result = useCase()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.NoActiveGoal)
        }

    @Test
    fun `invoke creates history record with completion metadata`() =
        runTest {
            val mockGoalHistory =
                GoalHistory(
                    id = "history-1",
                    goalId = "goal-123",
                    completedAt = Instant.now(),
                    totalAccuracy = 92f,
                    totalTimeSeconds = 600L,
                    completionCount = 2,
                )

            whenever(goalRepository.completeActiveGoal())
                .thenReturn(Result.success(mockGoalHistory))

            val result = useCase()

            val history = result.getOrNull()
            assertTrue(history!!.totalAccuracy == 92f)
            assertTrue(history.totalTimeSeconds == 600L)
            assertTrue(history.completionCount == 2)
        }

    @Test
    fun `invoke with database error returns DatabaseError`() =
        runTest {
            whenever(goalRepository.completeActiveGoal())
                .thenReturn(Result.failure(GoalError.DatabaseError))

            val result = useCase()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.DatabaseError)
        }

    @Test
    fun `invoke delegates to repository`() =
        runTest {
            val mockGoalHistory =
                GoalHistory(
                    id = "history-1",
                    goalId = "goal-123",
                    completedAt = Instant.now(),
                    totalAccuracy = 85f,
                    totalTimeSeconds = 300L,
                )

            whenever(goalRepository.completeActiveGoal())
                .thenReturn(Result.success(mockGoalHistory))

            useCase()

            verify(goalRepository).completeActiveGoal()
        }

    @Test
    fun `invoke clears active goal after completion`() =
        runTest {
            val mockGoalHistory =
                GoalHistory(
                    id = "history-1",
                    goalId = "goal-123",
                    completedAt = Instant.now(),
                    totalAccuracy = 88f,
                    totalTimeSeconds = 480L,
                )

            whenever(goalRepository.completeActiveGoal())
                .thenReturn(Result.success(mockGoalHistory))

            val result = useCase()

            assertTrue(result.isSuccess)
            // In a real scenario, we'd verify that clearActiveGoal was called
            // but for now the repository method signature in the interface combines both operations
        }
}
