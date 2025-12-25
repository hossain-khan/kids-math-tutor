package dev.hossain.mathtutor.ui.devportal

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.Badge
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
        val showResetOnboardingConfirm: Boolean = false,
        val resetOnboardingInProgress: Boolean = false,
        val resetOnboardingResultMessage: String? = null,
        val seedInProgress: Boolean = false,
        val seedResultMessage: String? = null,
        val importChallengesInProgress: Boolean = false,
        val importChallengesResultMessage: String? = null,
        val badges: List<Badge> = emptyList(),
        val forceUnlockInProgress: Boolean = false,
        val forceUnlockResultMessage: String? = null,
        val isAnalyticsEnabled: Boolean = true,
        val isBackgroundMusicPlaying: Boolean = false,
        val soundHapticFeedback: String? = null,
        val currentProfileName: String? = null,
        val currentGradeLevel: GradeLevel? = null,
        val currentAdaptiveDifficulty: Boolean = true,
        val profileUpdateResultMessage: String? = null,
        val soundsLoaded: Boolean = false,
        val soundSampleIds: Map<String, Int> = emptyMap(),
        val totalSessionCount: Int = 0,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object ToggleAnalyticsOverride : Event

        data object ClearAppDataClicked : Event

        data class ConfirmClear(
            val confirmationText: String,
        ) : Event

        data object CancelClear : Event

        data object ResetOnboardingClicked : Event

        data object ConfirmResetOnboarding : Event

        data object CancelResetOnboarding : Event

        data class SeedSessionsRequested(
            val count: Int,
            val operation: MathOperation,
            val grade: GradeLevel,
        ) : Event

        data object SeedSessionsClicked : Event

        data object ImportSampleChallengesClicked : Event

        data object ForceBadgeCheckClicked : Event

        data class ForceUnlockBadge(
            val badgeId: String,
        ) : Event

        data object UnlockAllBadges : Event

        data object PlaySuccessSound : Event

        data object PlayErrorSound : Event

        data object PlayLevelUpSound : Event

        data object PlayBadgeUnlockSound : Event

        data object PlayCountdownSound : Event

        data object PlayGoSound : Event

        data object PlayPerfectScore : Event

        data object PlayStreakContinue : Event

        data object PlayWarning : Event

        data object ToggleBackgroundMusic : Event

        data class UpdateGradeLevel(
            val gradeLevel: GradeLevel,
        ) : Event

        data class UpdateAdaptiveDifficulty(
            val enabled: Boolean,
        ) : Event

        data class UpdateProfileName(
            val name: String?,
        ) : Event

        // Quick Navigation events for testing games directly
        data object NavigateToMathRace : Event

        data object NavigateToMemoryMatch : Event

        data object NavigateToNumberSequence : Event

        // Color Palette Viewer
        data object ViewColorPalette : Event

        data object NavigateBack : Event
    }
}
