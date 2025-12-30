package dev.hossain.mathtutor.domain.usecase.goals

import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
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

class CreateGoalUseCaseTest {
    @Mock
    private lateinit var goalRepository: GoalRepository

    private lateinit var useCase: CreateGoalUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = CreateGoalUseCase(goalRepository)
    }

    @Test
    fun `invoke with valid inputs creates goal successfully`() =
        runTest {
            val title = "Math Master"
            val description = "Learn addition"
            val components =
                listOf(
                    GoalComponent.OperationBased(operation = MathOperation.ADDITION, sessionCount = 3),
                )
            val mockGoal = Goal(title = title, description = description, components = components)

            whenever(goalRepository.createGoal(eq(title), eq(description), eq(components)))
                .thenReturn(Result.success(mockGoal))

            val result = useCase(title, description, components)

            assertTrue(result.isSuccess)
            assertEquals(mockGoal, result.getOrNull())
            verify(goalRepository).createGoal(eq(title), eq(description), eq(components))
        }

    @Test
    fun `invoke with empty title returns InvalidGoal error`() =
        runTest {
            val result = useCase("", "Description", listOf())

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidGoal)
        }

    @Test
    fun `invoke with blank title returns InvalidGoal error`() =
        runTest {
            val result = useCase("   ", "Description", listOf())

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidGoal)
        }

    @Test
    fun `invoke with title exceeding 100 chars returns InvalidGoal error`() =
        runTest {
            val longTitle = "a".repeat(101)
            val result = useCase(longTitle, "Description", listOf())

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidGoal)
        }

    @Test
    fun `invoke with title exactly 100 chars succeeds`() =
        runTest {
            val title = "a".repeat(100)
            val components =
                listOf(
                    GoalComponent.OperationBased(operation = MathOperation.ADDITION, sessionCount = 1),
                )
            val mockGoal = Goal(title = title, components = components)

            whenever(goalRepository.createGoal(eq(title), any(), eq(components)))
                .thenReturn(Result.success(mockGoal))

            val result = useCase(title, "Desc", components)

            assertTrue(result.isSuccess)
        }

    @Test
    fun `invoke with empty components returns InvalidGoal error`() =
        runTest {
            val result = useCase("Title", "Description", emptyList())

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidGoal)
        }

    @Test
    fun `invoke with component sessionCount zero returns InvalidComponent error`() =
        runTest {
            val components =
                listOf(
                    GoalComponent.OperationBased(operation = MathOperation.ADDITION, sessionCount = 0),
                )
            val result = useCase("Title", "Description", components)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidComponent)
        }

    @Test
    fun `invoke with component sessionCount exceeding 10 returns InvalidComponent error`() =
        runTest {
            val components =
                listOf(
                    GoalComponent.OperationBased(operation = MathOperation.ADDITION, sessionCount = 11),
                )
            val result = useCase("Title", "Description", components)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GoalError.InvalidComponent)
        }

    @Test
    fun `invoke with multiple valid components succeeds`() =
        runTest {
            val title = "Multi Component Goal"
            val components =
                listOf(
                    GoalComponent.OperationBased(operation = MathOperation.ADDITION, sessionCount = 3),
                    GoalComponent.OperationBased(operation = MathOperation.SUBTRACTION, sessionCount = 2),
                )
            val mockGoal = Goal(title = title, components = components)

            whenever(goalRepository.createGoal(eq(title), any(), eq(components)))
                .thenReturn(Result.success(mockGoal))

            val result = useCase(title, "", components)

            assertTrue(result.isSuccess)
        }

    @Test
    fun `invoke delegates to repository with correct parameters`() =
        runTest {
            val title = "Test Goal"
            val description = "Test Description"
            val components =
                listOf(
                    GoalComponent.OperationBased(operation = MathOperation.ADDITION, sessionCount = 1),
                )
            val mockGoal = Goal(title = title, description = description, components = components)

            whenever(goalRepository.createGoal(eq(title), eq(description), eq(components)))
                .thenReturn(Result.success(mockGoal))

            useCase(title, description, components)

            verify(goalRepository).createGoal(eq(title), eq(description), eq(components))
        }

    @Test
    fun `invoke with repository failure propagates error`() =
        runTest {
            val title = "Test Goal"
            val components =
                listOf(
                    GoalComponent.OperationBased(operation = MathOperation.ADDITION, sessionCount = 1),
                )
            val error = GoalError.DatabaseError

            whenever(goalRepository.createGoal(eq(title), any(), eq(components)))
                .thenReturn(Result.failure(error))

            val result = useCase(title, "", components)

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }
}
