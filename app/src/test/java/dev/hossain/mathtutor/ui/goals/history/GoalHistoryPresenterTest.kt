package dev.hossain.mathtutor.ui.goals.history

import com.slack.circuit.test.FakeNavigator
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.goals.ComponentResult
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import dev.hossain.mathtutor.domain.repository.GoalRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for [GoalHistoryPresenter].
 * Tests analytics calculation, history filtering, and event handling.
 */
class GoalHistoryPresenterTest {
    @Mock
    private lateinit var goalRepository: GoalRepository

    private lateinit var navigator: FakeNavigator
    private lateinit var presenter: GoalHistoryPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        navigator = FakeNavigator()
    }

    private fun createPresenter(goalId: String = "goal-1") {
        presenter =
            GoalHistoryPresenter(
                screen = GoalHistoryScreen(goalId),
                navigator = navigator,
                goalRepository = goalRepository,
            )
    }

    @Test
    fun initialState_withNoHistory_showsEmptyState() =
        runTest {
            // Arrange
            val goalId = "goal-1"
            val goal =
                Goal(
                    id = goalId,
                    title = "Addition Practice",
                    description = "Learn addition",
                    components =
                        listOf(
                            GoalComponent.OperationBased(
                                operation = MathOperation.ADDITION,
                                sessionCount = 5,
                            ),
                        ),
                )

            whenever(goalRepository.getGoalById(goalId)).thenReturn(flowOf(goal))
            whenever(goalRepository.getGoalHistory()).thenReturn(flowOf(emptyList()))

            createPresenter(goalId)

            // Act & Assert
            val state = presenter.present()
            assertEquals(goal, state.goal)
            assertEquals(emptyList<GoalHistory>(), state.histories)
            assertEquals(0, state.totalCompleted)
            assertEquals(0f, state.averageAccuracy)
        }

    @Test
    fun initialState_withHistory_calculatesAnalytics() =
        runTest {
            // Arrange
            val goalId = "goal-1"
            val goal =
                Goal(
                    id = goalId,
                    title = "Addition Practice",
                    description = "Learn addition",
                    components =
                        listOf(
                            GoalComponent.OperationBased(
                                operation = MathOperation.ADDITION,
                                sessionCount = 5,
                            ),
                        ),
                )

            val history1 =
                GoalHistory(
                    id = "history-1",
                    goal = goal,
                    completedAt = Instant.now(),
                    overallAccuracy = 80f,
                    totalTimeSeconds = 600L,
                    componentResults =
                        listOf(
                            ComponentResult(
                                componentIndex = 0,
                                sessionsCompleted = 5,
                                averageAccuracy = 80f,
                                totalTimeSeconds = 600L,
                            ),
                        ),
                )

            val history2 =
                GoalHistory(
                    id = "history-2",
                    goal = goal,
                    completedAt = Instant.now(),
                    overallAccuracy = 90f,
                    totalTimeSeconds = 500L,
                    componentResults =
                        listOf(
                            ComponentResult(
                                componentIndex = 0,
                                sessionsCompleted = 5,
                                averageAccuracy = 90f,
                                totalTimeSeconds = 500L,
                            ),
                        ),
                )

            whenever(goalRepository.getGoalById(goalId)).thenReturn(flowOf(goal))
            whenever(goalRepository.getGoalHistory()).thenReturn(
                flowOf(listOf(history1, history2)),
            )

            createPresenter(goalId)

            // Act & Assert
            val state = presenter.present()
            assertEquals(2, state.totalCompleted)
            assertEquals(85f, state.averageAccuracy) // (80 + 90) / 2
            assertEquals(18, state.totalTimeMins) // (600 + 500) / 60
        }

    @Test
    fun selectHistory_storesSelectedHistory() =
        runTest {
            // Arrange
            val goalId = "goal-1"
            val goal =
                Goal(
                    id = goalId,
                    title = "Addition Practice",
                    description = "",
                    components = emptyList(),
                )
            val history =
                GoalHistory(
                    id = "history-1",
                    goal = goal,
                    completedAt = Instant.now(),
                    overallAccuracy = 80f,
                    totalTimeSeconds = 600L,
                    componentResults = emptyList(),
                )

            whenever(goalRepository.getGoalById(goalId)).thenReturn(flowOf(goal))
            whenever(goalRepository.getGoalHistory()).thenReturn(flowOf(listOf(history)))

            createPresenter(goalId)

            // Act
            val state = presenter.present()
            state.eventSink(GoalHistoryScreen.Event.SelectHistory(history))

            // Assert
            assertEquals(history, state.selectedHistory)
        }

    @Test
    fun clearSelection_removesSelectedHistory() =
        runTest {
            // Arrange
            val goalId = "goal-1"
            val goal =
                Goal(
                    id = goalId,
                    title = "Addition Practice",
                    description = "",
                    components = emptyList(),
                )
            val history =
                GoalHistory(
                    id = "history-1",
                    goal = goal,
                    completedAt = Instant.now(),
                    overallAccuracy = 80f,
                    totalTimeSeconds = 600L,
                    componentResults = emptyList(),
                )

            whenever(goalRepository.getGoalById(goalId)).thenReturn(flowOf(goal))
            whenever(goalRepository.getGoalHistory()).thenReturn(flowOf(listOf(history)))

            createPresenter(goalId)

            // Act
            val state = presenter.present()
            state.eventSink(GoalHistoryScreen.Event.SelectHistory(history))
            state.eventSink(GoalHistoryScreen.Event.ClearSelection)

            // Assert
            assertNull(state.selectedHistory)
        }

    @Test
    fun back_navigatesBack() =
        runTest {
            // Arrange
            val goalId = "goal-1"
            val goal =
                Goal(
                    id = goalId,
                    title = "Addition Practice",
                    description = "",
                    components = emptyList(),
                )

            whenever(goalRepository.getGoalById(goalId)).thenReturn(flowOf(goal))
            whenever(goalRepository.getGoalHistory()).thenReturn(flowOf(emptyList()))

            createPresenter(goalId)

            // Act
            val state = presenter.present()
            state.eventSink(GoalHistoryScreen.Event.Back)

            // Assert
            assertEquals(1, navigator.popCount)
        }

    @Test
    fun filtersHistoryByGoalId() =
        runTest {
            // Arrange
            val goalId1 = "goal-1"
            val goalId2 = "goal-2"

            val goal1 =
                Goal(
                    id = goalId1,
                    title = "Addition Practice",
                    description = "",
                    components = emptyList(),
                )
            val goal2 =
                Goal(
                    id = goalId2,
                    title = "Subtraction Practice",
                    description = "",
                    components = emptyList(),
                )

            val history1 =
                GoalHistory(
                    id = "history-1",
                    goal = goal1,
                    completedAt = Instant.now(),
                    overallAccuracy = 80f,
                    totalTimeSeconds = 600L,
                    componentResults = emptyList(),
                )
            val history2 =
                GoalHistory(
                    id = "history-2",
                    goal = goal2,
                    completedAt = Instant.now(),
                    overallAccuracy = 85f,
                    totalTimeSeconds = 500L,
                    componentResults = emptyList(),
                )

            whenever(goalRepository.getGoalById(goalId1)).thenReturn(flowOf(goal1))
            whenever(goalRepository.getGoalHistory()).thenReturn(
                flowOf(listOf(history1, history2)),
            )

            createPresenter(goalId1)

            // Act & Assert
            val state = presenter.present()
            assertEquals(1, state.histories.size)
            assertEquals(history1, state.histories[0])
        }
}
