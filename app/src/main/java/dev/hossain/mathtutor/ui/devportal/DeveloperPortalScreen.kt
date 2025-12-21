package dev.hossain.mathtutor.ui.devportal

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import kotlinx.parcelize.Parcelize

/**
 * Debug-only Developer Portal screen. Exposes developer tools and helpers for testing.
 */
@Parcelize
data object DeveloperPortalScreen : Screen {
    data class State(
        val showSeedSection: Boolean = true,
        val showDataOpsSection: Boolean = true,
        val showDiagnosticsSection: Boolean = true,
        val showClearConfirm: Boolean = false,
        val clearInProgress: Boolean = false,
        val clearResultMessage: String? = null,
        val seedInProgress: Boolean = false,
        val seedResultMessage: String? = null,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object ToggleAnalyticsOverride : Event

        data object ClearAppDataClicked : Event

        data class ConfirmClear(
            val confirmationText: String,
        ) : Event

        data object CancelClear : Event

        data class SeedSessionsRequested(
            val count: Int,
            val operation: MathOperation,
            val grade: GradeLevel,
        ) : Event

        data object SeedSessionsClicked : Event

        data object ForceBadgeCheckClicked : Event

        data object PlaySuccessSound : Event

        data object NavigateBack : Event
    }
}
