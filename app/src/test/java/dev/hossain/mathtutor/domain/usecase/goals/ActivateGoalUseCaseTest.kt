package dev.hossain.mathtutor.domain.usecase.goals

import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.model.goals.ComponentProgress
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalError
import dev.hossain.mathtutor.domain.repository.GoalRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class ActivateGoalUseCaseTest {
    @Mock
    private lateinit var goalRepository: GoalRepository

    private lateinit var useCase: ActivateGoalUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = ActivateGoalUseCase(goalRepository)
    }

    @Test
    fun `invoke with valid goalId activates goal successfully`() =
        runTest {
            val goalId = "goal-123"
            val mockActiveGoal =
                ActiveGoal(
                    id = "active-goal-1",
                    goalId = goalId,
                    goal = Goal(title = "Test Goal", components = emptyList()),
                    currentComponentIndex = 0,
                    componentProgress =
                        listOf(
                            ComponentProgress(
                                componentIndex = 0,
                                completedSessions = 0,
                                totalSessions = 3,
                                accuracy = 0f,
                                totalTimeSeconds = 0L,
                            ),
                        ),
                    activatedAt = Instant.now(),
                )

            whenever(goalRepository.activateGoal(eq(goalId)))
                .thenReturn(Result.success(mockActiveGoal))

            val result = useCase(goalId)

            assertTrue(result.isSuccess)
            assertEquals(mockActiveGoal, result.getOrNull())
            verify(goalRepository).activateGoal(eq(goalId))
        }

    @Test
    fun `invoke with empty goalId returns InvalidGoal error`() =
        runTest {
            val result = useCase("")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidGoal)
        }

    @Test
    fun `invoke with blank goalId returns InvalidGoal error`() =
        runTest {
            val result = useCase("   ")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidGoal)
        }

    @Test
    fun `invoke with nonexistent goalId returns GoalNotFound error`() =
        runTest {
            val goalId = "nonexistent-goal"

            whenever(goalRepository.activateGoal(eq(goalId)))
                .thenReturn(Result.failure(GoalError.GoalNotFound(goalId)))

            val result = useCase(goalId)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.GoalNotFound)
        }

    @Test
    fun `invoke when active goal already exists returns ActiveGoalExists error`() =
        runTest {
            val goalId = "goal-123"

            whenever(goalRepository.activateGoal(eq(goalId)))
                .thenReturn(Result.failure(GoalError.ActiveGoalExists(goalId)))

            val result = useCase(goalId)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.ActiveGoalExists)
        }

    @Test
    fun `invoke initializes component progress for new active goal`() =
        runTest {
            val goalId = "goal-123"
            val mockActiveGoal =
                ActiveGoal(
                    id = "active-goal-1",
                    goalId = goalId,
                    goal = Goal(title = "Test Goal", components = emptyList()),
                    currentComponentIndex = 0,
                    componentProgress =
                        listOf(
                            ComponentProgress(
                                componentIndex = 0,
                                completedSessions = 0,
                                totalSessions = 5,
                                accuracy = 0f,
                                totalTimeSeconds = 0L,
                            ),
                        ),
                    activatedAt = Instant.now(),
                )

            whenever(goalRepository.activateGoal(eq(goalId)))
                .thenReturn(Result.success(mockActiveGoal))

            val result = useCase(goalId)

            val activeGoal = result.getOrNull()
            assertTrue(activeGoal!!.componentProgress[0].completedSessions == 0)
            assertTrue(activeGoal.currentComponentIndex == 0)
        }
}
