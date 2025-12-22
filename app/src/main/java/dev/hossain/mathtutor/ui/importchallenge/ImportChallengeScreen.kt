package dev.hossain.mathtutor.ui.importchallenge

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.PreviewData
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for importing custom challenges.
 *
 * This screen allows parents to paste JSON challenge specifications,
 * validate them, preview the problems, and save the challenge.
 *
 * @property prefilledJson Optional JSON content shared from another app
 */
@Parcelize
data class ImportChallengeScreen(
    val prefilledJson: String? = null,
) : Screen {
    /**
     * State for [ImportChallengeScreen].
     *
     * @property jsonInput Current JSON input text
     * @property validationState Current validation state
     * @property previewData Preview data if validation is successful
     * @property isLoading Whether a save operation is in progress
     * @property detectedJsonFromShare Whether JSON was detected and extracted from shared content
     * @property eventSink Handler for screen events
     */
    data class State(
        val jsonInput: String,
        val validationState: ValidationState,
        val previewData: PreviewData?,
        val isLoading: Boolean,
        val detectedJsonFromShare: Boolean,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [ImportChallengeScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User changed the JSON input text.
         */
        data class JsonInputChanged(
            val input: String,
        ) : Event

        /**
         * User requested validation and preview generation.
         */
        data object ValidateAndPreview : Event

        /**
         * User requested to save the challenge.
         */
        data object SaveChallenge : Event

        /**
         * User requested to clear the input.
         */
        data object ClearInput : Event

        /**
         * User requested to navigate back.
         */
        data object NavigateBack : Event
    }
}

/**
 * Represents the validation state of the JSON input.
 */
sealed class ValidationState {
    /**
     * Initial state, no validation has been performed.
     */
    data object Idle : ValidationState()

    /**
     * Validation passed successfully.
     */
    data object Valid : ValidationState()

    /**
     * Validation failed with field-specific errors.
     *
     * @property fieldErrors Map of field names to error messages
     */
    data class Invalid(
        val fieldErrors: Map<String, String>,
    ) : ValidationState()
}
