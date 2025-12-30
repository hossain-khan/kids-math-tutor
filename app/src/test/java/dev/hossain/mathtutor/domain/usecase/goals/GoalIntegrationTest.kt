package dev.hossain.mathtutor.domain.usecase.goals

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.analytics.GoalAnalyticsTracker
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.model.goals.GoalError
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import dev.hossain.mathtutor.domain.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

/**
 * Integration test suite for goal feature end-to-end workflows.
 * Tests the complete lifecycle: creation, activation, progress tracking, and completion.
 *
 * This suite validates that:
 * - Goals can be created with multiple components
 * - Goals can be activated and progress tracked
 * - Multiple sessions can be completed for a goal
 * - Goals are properly marked as complete and moved to history
 * - Error conditions are handled correctly
 * - Analytics events are fired at appropriate points
 */
class GoalIntegrationTest {
    @Mock
    private lateinit var goalRepository: GoalRepository

    @Mock
    private lateinit var analyticsTracker: GoalAnalyticsTracker

    private lateinit var createGoalUseCase: CreateGoalUseCase
    private lateinit var activateGoalUseCase: ActivateGoalUseCase
    private lateinit var updateProgressUseCase: UpdateGoalProgressUseCase
    private lateinit var completeGoalUseCase: CompleteGoalUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        createGoalUseCase = CreateGoalUseCase(goalRepository, analyticsTracker)
        activateGoalUseCase = ActivateGoalUseCase(goalRepository, analyticsTracker)
        updateProgressUseCase = UpdateGoalProgressUseCase(goalRepository, analyticsTracker)
        completeGoalUseCase = CompleteGoalUseCase(goalRepository, analyticsTracker)
    }

    @Test
    fun `complete goal workflow from creation to completion`() =
        runTest {
            // Step 1: Create a goal with 2 components
            val title = "Addition Master"
            val description = "Master addition skills"
            val components =
                listOf(
                    GoalComponent.OperationBased(operation = MathOperation.ADDITION, sessionCount = 2),
                    GoalComponent.OperationBased(operation = MathOperation.ADDITION, sessionCount = 2),
                )

            val createdGoal =
                Goal(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    components = components,
                    createdAt = Instant.now(),
                    isArchived = false,
                )

            whenever(goalRepository.createGoal(eq(title), eq(description), eq(components)))
                .thenReturn(Result.success(createdGoal))

            val createResult = createGoalUseCase(title, description, components)
            assertThat(createResult.isSuccess).isTrue()
            assertThat(createResult.getOrNull()?.id).isEqualTo(createdGoal.id)

            // Verify analytics was called
            verify(analyticsTracker).trackGoalCreated(
                goal = eq(createdGoal),
                componentCount = eq(2),
                componentTypes = any(),
            )

            // Step 2: Activate the goal
            val activeGoalId = UUID.randomUUID().toString()
            val mockActiveGoal =
                dev.hossain.mathtutor.domain.model.goals.ActiveGoal(
                    id = activeGoalId,
                    goalId = createdGoal.id,
                    goal = createdGoal,
                    activatedAt = Instant.now(),
                    currentComponentIndex = 0,
                    componentProgress = emptyList(),
                )

            whenever(goalRepository.activateGoal(eq(createdGoal.id)))
                .thenReturn(Result.success(mockActiveGoal))

            val activateResult = activateGoalUseCase(createdGoal.id)
            assertThat(activateResult.isSuccess).isTrue()

            // Verify analytics was called for activation
            verify(analyticsTracker).trackGoalActivated(
                goalId = eq(createdGoal.id),
                goalTitle = eq(title),
                totalSessions = eq(4),
            )

            // Step 3: Update progress for first component (first session)
            val updatedActiveGoal =
                mockActiveGoal.copy(
                    componentProgress =
                        listOf(
                            dev.hossain.mathtutor.domain.model.goals.ComponentProgress(
                                componentIndex = 0,
                                completedSessions = 1,
                                accuracy = 85.5f,
                                totalTimeSeconds = 300L,
                            ),
                        ),
                )

            whenever(
                goalRepository.updateComponentProgress(
                    componentIndex = eq(0),
                    completedSessions = eq(1),
                    accuracy = eq(85.5f),
                    timeSeconds = eq(300L),
                ),
            ).thenReturn(Result.success(updatedActiveGoal))

            val progressResult1 = updateProgressUseCase(0, 1, 85.5f, 300L, 10)
            assertThat(progressResult1.isSuccess).isTrue()

            // Verify analytics was called for session completion
            verify(analyticsTracker).trackSessionCompleted(
                goalId = eq(createdGoal.id),
                componentIndex = eq(0),
                accuracy = eq(85.5f),
                durationSeconds = eq(300L),
                problemsCompleted = eq(10),
            )

            // Step 4: Update progress for first component (second session)
            val updatedActiveGoal2 =
                updatedActiveGoal.copy(
                    componentProgress =
                        listOf(
                            updatedActiveGoal.componentProgress[0].copy(completedSessions = 2),
                        ),
                )

            whenever(
                goalRepository.updateComponentProgress(
                    componentIndex = eq(0),
                    completedSessions = eq(2),
                    accuracy = eq(92.0f),
                    timeSeconds = eq(280L),
                ),
            ).thenReturn(Result.success(updatedActiveGoal2))

            val progressResult2 = updateProgressUseCase(0, 2, 92.0f, 280L, 10)
            assertThat(progressResult2.isSuccess).isTrue()

            // Step 5: Complete the goal
            val goalHistory =
                GoalHistory(
                    id = UUID.randomUUID().toString(),
                    goalId = createdGoal.id,
                    goal = createdGoal,
                    activatedAt = mockActiveGoal.activatedAt,
                    completedAt = Instant.now(),
                    totalTimeSeconds = 1200L,
                    overallAccuracy = 90.0f,
                    componentResults = emptyList(),
                )

            whenever(goalRepository.completeActiveGoal())
                .thenReturn(Result.success(goalHistory))

            val completeResult = completeGoalUseCase()
            assertThat(completeResult.isSuccess).isTrue()

            // Verify analytics was called for completion
            verify(analyticsTracker).trackGoalCompleted(
                goalHistory = eq(goalHistory),
                totalDaysActive = any(),
                achievedAccuracy = eq(90.0f),
                gameLevelsUnlocked = any(),
            )
        }

    @Test
    fun `cannot create goal with empty components list`() =
        runTest {
            val result = createGoalUseCase("Math Goal", "Learn math", emptyList())

            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(GoalError.InvalidGoal::class.java)
        }

    @Test
    fun `cannot create goal with empty title`() =
        runTest {
            val components =
                listOf(
                    GoalComponent.OperationBased(MathOperation.ADDITION, 3),
                )
            val result = createGoalUseCase("", "", components)

            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(GoalError.InvalidGoal::class.java)
        }

    @Test
    fun `goal with multiple different operations`() =
        runTest {
            val components =
                listOf(
                    GoalComponent.OperationBased(operation = MathOperation.ADDITION, sessionCount = 2),
                    GoalComponent.OperationBased(operation = MathOperation.SUBTRACTION, sessionCount = 2),
                    GoalComponent.OperationBased(operation = MathOperation.MULTIPLICATION, sessionCount = 1),
                )

            val createdGoal =
                Goal(
                    id = UUID.randomUUID().toString(),
                    title = "Math Mastery",
                    description = "Learn all operations",
                    components = components,
                    createdAt = Instant.now(),
                    isArchived = false,
                )

            whenever(goalRepository.createGoal("Math Mastery", "Learn all operations", components))
                .thenReturn(Result.success(createdGoal))

            val result = createGoalUseCase("Math Mastery", "Learn all operations", components)
            assertThat(result.isSuccess).isTrue()

            // Verify correct component types are tracked
            val captor = argumentCaptor<List<String>>()
            verify(analyticsTracker).trackGoalCreated(
                goal = any(),
                componentCount = eq(3),
                componentTypes = captor.capture(),
            )
            assertThat(captor.firstValue).contains("Addition")
            assertThat(captor.firstValue).contains("Subtraction")
            assertThat(captor.firstValue).contains("Multiplication")
        }

    @Test
    fun `progress tracking with high accuracy`() =
        runTest {
            val goal =
                Goal(
                    id = "goal-1",
                    title = "Expert Level",
                    components =
                        listOf(
                            GoalComponent.OperationBased(MathOperation.DIVISION, 2),
                        ),
                )

            val activeGoal =
                dev.hossain.mathtutor.domain.model.goals.ActiveGoal(
                    id = "active-1",
                    goalId = goal.id,
                    goal = goal,
                    activatedAt = Instant.now(),
                    currentComponentIndex = 0,
                    componentProgress = emptyList(),
                )

            whenever(
                goalRepository.updateComponentProgress(
                    componentIndex = eq(0),
                    completedSessions = eq(1),
                    accuracy = eq(100.0f),
                    timeSeconds = eq(180L),
                ),
            ).thenReturn(Result.success(activeGoal))

            val result = updateProgressUseCase(0, 1, 100.0f, 180L, 10)
            assertThat(result.isSuccess).isTrue()

            // Verify perfect accuracy is tracked
            verify(analyticsTracker).trackSessionCompleted(
                goalId = eq(goal.id),
                componentIndex = eq(0),
                accuracy = eq(100.0f),
                durationSeconds = eq(180L),
                problemsCompleted = eq(10),
            )
        }

    @Test
    fun `progress tracking with low accuracy still succeeds`() =
        runTest {
            val goal =
                Goal(
                    id = "goal-2",
                    title = "Learning Goal",
                    components =
                        listOf(
                            GoalComponent.OperationBased(MathOperation.SUBTRACTION, 3),
                        ),
                )

            val activeGoal =
                dev.hossain.mathtutor.domain.model.goals.ActiveGoal(
                    id = "active-2",
                    goalId = goal.id,
                    goal = goal,
                    activatedAt = Instant.now(),
                    currentComponentIndex = 0,
                    componentProgress = emptyList(),
                )

            whenever(
                goalRepository.updateComponentProgress(
                    componentIndex = eq(0),
                    completedSessions = eq(1),
                    accuracy = eq(45.0f),
                    timeSeconds = eq(600L),
                ),
            ).thenReturn(Result.success(activeGoal))

            val result = updateProgressUseCase(0, 1, 45.0f, 600L, 10)
            assertThat(result.isSuccess).isTrue()

            // Verify low accuracy is still tracked (learning in progress)
            verify(analyticsTracker).trackSessionCompleted(
                goalId = eq(goal.id),
                componentIndex = eq(0),
                accuracy = eq(45.0f),
                durationSeconds = eq(600L),
                problemsCompleted = eq(10),
            )
        }

    @Test
    fun `invalid accuracy values are rejected`() =
        runTest {
            // Accuracy > 100
            var result = updateProgressUseCase(0, 1, 150.0f, 300L)
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(GoalError.InvalidAccuracy::class.java)

            // Negative accuracy
            result = updateProgressUseCase(0, 1, -10.0f, 300L)
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(GoalError.InvalidAccuracy::class.java)
        }

    @Test
    fun `negative component index is rejected`() =
        runTest {
            val result = updateProgressUseCase(-1, 1, 80.0f, 300L)

            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(GoalError.InvalidComponent::class.java)
        }
}
