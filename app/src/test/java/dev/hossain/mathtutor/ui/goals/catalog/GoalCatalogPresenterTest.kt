package dev.hossain.mathtutor.ui.goals.catalog

import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.test.FakeNavigator
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.repository.GoalRepository
import dev.hossain.mathtutor.domain.usecase.goals.ActivateGoalUseCase
import dev.hossain.mathtutor.ui.goals.creator.GoalCreatorScreen
import dev.hossain.mathtutor.ui.goals.history.GoalHistoryScreen
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for [GoalCatalogPresenter].
 * Tests the state management and event handling for the goal catalog screen.
 */
class GoalCatalogPresenterTest {
    @Mock
    private lateinit var goalRepository: GoalRepository

    @Mock
    private lateinit var activateGoalUseCase: ActivateGoalUseCase

    private lateinit var navigator: FakeNavigator
    private lateinit var presenter: GoalCatalogPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        navigator = FakeNavigator()
    }

    private fun createPresenter() {
        presenter =
            GoalCatalogPresenter(
                screen = GoalCatalogScreen,
                navigator = navigator,
                goalRepository = goalRepository,
                activateGoalUseCase = activateGoalUseCase,
            )
    }

    @Test
    fun initialState_showsEmptyGoalsList() =
        runTest {
            // Arrange
            whenever(goalRepository.getAllGoals()).thenReturn(
                flowOf(emptyList()),
            )
            whenever(goalRepository.getActiveGoal()).thenReturn(
                flowOf(null),
            )

            createPresenter()

            // Act & Assert
            val state = presenter.present()
            assertEquals(emptyList<Goal>(), state.goals)
            assertNull(state.activeGoalId)
            assertEquals(false, state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun initialState_withGoals_showsGoalsList() =
        runTest {
            // Arrange
            val goals =
                listOf(
                    Goal(
                        id = "goal1",
                        title = "Addition Practice",
                        description = "Practice basic addition",
                        components =
                            listOf(
                                GoalComponent.OperationBased(
                                    operation = MathOperation.ADDITION,
                                    sessionCount = 5,
                                ),
                            ),
                    ),
                )

            whenever(goalRepository.getAllGoals()).thenReturn(flowOf(goals))
            whenever(goalRepository.getActiveGoal()).thenReturn(flowOf(null))

            createPresenter()

            // Act & Assert
            val state = presenter.present()
            assertEquals(1, state.goals.size)
            assertEquals("Addition Practice", state.goals[0].title)
        }

    @Test
    fun createNewGoal_navigatesToGoalCreatorScreen() =
        runTest {
            // Arrange
            whenever(goalRepository.getAllGoals()).thenReturn(flowOf(emptyList()))
            whenever(goalRepository.getActiveGoal()).thenReturn(flowOf(null))

            createPresenter()

            // Act
            val state = presenter.present()
            state.eventSink(GoalCatalogScreen.Event.CreateNewGoal)

            // Assert
            val navigation = navigator.awaitNextScreen()
            assertEquals(GoalCreatorScreen, navigation)
        }

    @Test
    fun viewHistory_navigatesToHistoryScreen() =
        runTest {
            // Arrange
            whenever(goalRepository.getAllGoals()).thenReturn(flowOf(emptyList()))
            whenever(goalRepository.getActiveGoal()).thenReturn(flowOf(null))

            createPresenter()
            val goalId = "test-goal-id"

            // Act
            val state = presenter.present()
            state.eventSink(GoalCatalogScreen.Event.ViewHistory(goalId))

            // Assert
            val navigation = navigator.awaitNextScreen()
            assertEquals(GoalHistoryScreen(goalId), navigation)
        }

    @Test
    fun dismissError_clearsErrorMessage() =
        runTest {
            // Arrange
            whenever(goalRepository.getAllGoals()).thenReturn(flowOf(emptyList()))
            whenever(goalRepository.getActiveGoal()).thenReturn(flowOf(null))

            createPresenter()

            // Act
            val state = presenter.present()
            state.eventSink(GoalCatalogScreen.Event.DismissError)

            // Assert - error should be cleared
            assertNull(state.error)
        }
}
