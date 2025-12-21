package dev.hossain.mathtutor.ui.devportal

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
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
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object ToggleAnalyticsOverride : Event
        data object ClearAppDataClicked : Event
        data object SeedSessionsClicked : Event
        data object ForceBadgeCheckClicked : Event
        data object PlaySuccessSound : Event
        data object NavigateBack : Event
    }
}
