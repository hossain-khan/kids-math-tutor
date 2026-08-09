package dev.hossain.mathtutor.ui.operationselector

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import kotlinx.parcelize.Parcelize
import com.slack.circuit.serialization.CircuitSerializable
import dev.zacsweers.metro.AppScope

/**
 * Circuit screen for operation selection.
 *
 * Displays available math operations filtered by user's grade level:
 * - Grade K: Addition, Subtraction
 * - Grade 1: Addition, Subtraction, Multiplication (limited), Mix It Up
 * - Grade 2: Addition, Subtraction, Multiplication (full), Division, Mix It Up
 *
 * Also provides access to stats screen when session history exists.
 */
@Parcelize
@CircuitSerializable(AppScope::class)
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
