package dev.hossain.mathtutor.ui.goals.creator

import com.slack.circuit.test.FakeNavigator
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.usecase.goals.CreateGoalUseCase
import dev.hossain.mathtutor.ui.goals.creator.GoalCreatorScreen.Step
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [GoalCreatorPresenter].
 * Tests the step progression, validation, and event handling for goal creation.
 */
class GoalCreatorPresenterTest {
    @Mock
    private lateinit var createGoalUseCase: CreateGoalUseCase

    private lateinit var navigator: FakeNavigator
    private lateinit var presenter: GoalCreatorPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        navigator = FakeNavigator()
    }

    private fun createPresenter() {
        presenter =
            GoalCreatorPresenter(
                screen = GoalCreatorScreen,
                navigator = navigator,
                createGoalUseCase = createGoalUseCase,
            )
    }

    @Test
    fun initialState_showsTitleStep() =
        runTest {
            // Arrange
            createPresenter()

            // Act & Assert
            val state = presenter.present()
            assertEquals(Step.Title, state.currentStep)
            assertTrue(state.goalTitle.isEmpty())
            assertTrue(state.goalDescription.isEmpty())
            assertTrue(state.components.isEmpty())
            assertFalse(state.canAdvance)
        }

    @Test
    fun setTitle_updatesTitle() =
        runTest {
            // Arrange
            createPresenter()

            // Act
            val state = presenter.present()
            state.eventSink(GoalCreatorScreen.Event.SetTitle("Addition Practice"))

            // Assert
            assertEquals("Addition Practice", state.goalTitle)
        }

    @Test
    fun setTitle_emptyTitle_cannotAdvance() =
        runTest {
            // Arrange
            createPresenter()

            // Act
            val state = presenter.present()
            state.eventSink(GoalCreatorScreen.Event.SetTitle(""))

            // Assert
            assertFalse(state.canAdvance)
        }

    @Test
    fun setTitle_withTitle_canAdvance() =
        runTest {
            // Arrange
            createPresenter()

            // Act
            val state = presenter.present()
            state.eventSink(GoalCreatorScreen.Event.SetTitle("Addition Practice"))

            // Assert
            assertTrue(state.canAdvance)
        }

    @Test
    fun nextStep_fromTitleToSelectComponents() =
        runTest {
            // Arrange
            createPresenter()

            // Act
            val state = presenter.present()
            state.eventSink(GoalCreatorScreen.Event.SetTitle("Addition Practice"))
            state.eventSink(GoalCreatorScreen.Event.NextStep)

            // Assert
            assertEquals(Step.SelectComponents, state.currentStep)
        }

    @Test
    fun addComponent_addsToComponentsList() =
        runTest {
            // Arrange
            createPresenter()
            val component =
                GoalComponent.OperationBased(
                    operation = MathOperation.ADDITION,
                    sessionCount = 5,
                )

            // Act
            val state = presenter.present()
            state.eventSink(GoalCreatorScreen.Event.AddComponent(component))

            // Assert
            assertEquals(1, state.components.size)
            assertEquals(component, state.components[0])
        }

    @Test
    fun addComponent_enablesAdvanceOnSelectStep() =
        runTest {
            // Arrange
            createPresenter()
            val component =
                GoalComponent.OperationBased(
                    operation = MathOperation.ADDITION,
                    sessionCount = 1,
                )

            // Act
            val state = presenter.present()
            state.eventSink(GoalCreatorScreen.Event.SetTitle("Addition Practice"))
            state.eventSink(GoalCreatorScreen.Event.NextStep)
            state.eventSink(GoalCreatorScreen.Event.AddComponent(component))

            // Assert
            assertTrue(state.canAdvance)
        }

    @Test
    fun previousStep_goesBackToPreviousStep() =
        runTest {
            // Arrange
            createPresenter()

            // Act
            val state = presenter.present()
            state.eventSink(GoalCreatorScreen.Event.SetTitle("Addition Practice"))
            state.eventSink(GoalCreatorScreen.Event.NextStep)
            state.eventSink(GoalCreatorScreen.Event.PreviousStep)

            // Assert
            assertEquals(Step.Title, state.currentStep)
        }

    @Test
    fun removeComponent_removesFromList() =
        runTest {
            // Arrange
            createPresenter()
            val component =
                GoalComponent.OperationBased(
                    operation = MathOperation.ADDITION,
                    sessionCount = 1,
                )

            // Act
            val state = presenter.present()
            state.eventSink(GoalCreatorScreen.Event.AddComponent(component))
            state.eventSink(GoalCreatorScreen.Event.RemoveComponent(0))

            // Assert
            assertTrue(state.components.isEmpty())
        }

    @Test
    fun cancel_navigatesBack() =
        runTest {
            // Arrange
            createPresenter()

            // Act
            val state = presenter.present()
            state.eventSink(GoalCreatorScreen.Event.Cancel)

            // Assert
            assertEquals(1, navigator.popCount)
        }

    @Test
    fun setDescription_updatesDescription() =
        runTest {
            // Arrange
            createPresenter()

            // Act
            val state = presenter.present()
            state.eventSink(GoalCreatorScreen.Event.SetDescription("Learn basic addition"))

            // Assert
            assertEquals("Learn basic addition", state.goalDescription)
        }
}
