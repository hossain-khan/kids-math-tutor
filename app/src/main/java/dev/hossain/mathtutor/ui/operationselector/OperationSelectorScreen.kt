package dev.hossain.mathtutor.ui.operationselector

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for selecting math operation to practice.
 *
 * Dynamically displays operation options based on the user's grade level:
 * - Kindergarten: Addition, Subtraction, Mix It Up
 * - Grade 1: Addition, Subtraction, Multiplication (limited), Mix It Up
 * - Grade 2: Addition, Subtraction, Multiplication (full), Division, Mix It Up
 *
 * Also provides access to stats screen when session history exists.
 */
@Parcelize
data object OperationSelectorScreen : Screen {
    /**
     * State for [OperationSelectorScreen].
     */
    data class State(
        val gradeLevel: GradeLevel,
        val hasSessionHistory: Boolean,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [OperationSelectorScreen].
     */
    sealed interface Event : CircuitUiEvent {
        data class OperationSelected(
            val operation: MathOperation,
        ) : Event

        data object ViewStatsClicked : Event

        data object NavigateBack : Event
    }
}
