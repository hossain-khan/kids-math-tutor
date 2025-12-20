package dev.hossain.mathtutor.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.ui.onboarding.GradeSelectionScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Presenter for [SettingsScreen].
 *
 * Manages the state and business logic for user settings and profile management.
 * Handles profile loading, name editing, grade level changes, and adaptive difficulty toggle.
 */
@AssistedInject
class SettingsPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val userProfileRepository: UserProfileRepository,
    ) : Presenter<SettingsScreen.State> {
        @CircuitInject(SettingsScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): SettingsPresenter
        }

        @Composable
        override fun present(): SettingsScreen.State {
            val scope = rememberCoroutineScope()

            // Collect user profile
            val profile by userProfileRepository.getProfile().collectAsState(initial = null)
            Timber.d(
                "SettingsScreen: Profile loaded - name=${profile?.name}, grade=${profile?.gradeLevel}, adaptive=${profile?.adaptiveDifficultyEnabled}",
            )

            // Dialog visibility states
            var showNameDialog by remember { mutableStateOf(false) }
            var showGradeDialog by remember { mutableStateOf(false) }

            return SettingsScreen.State(
                profile = profile,
                showNameDialog = showNameDialog,
                showGradeDialog = showGradeDialog,
            ) { event ->
                when (event) {
                    is SettingsScreen.Event.EditNameClicked -> {
                        Timber.d("SettingsScreen: Edit name clicked")
                        showNameDialog = true
                    }

                    is SettingsScreen.Event.ChangeGradeClicked -> {
                        Timber.d("SettingsScreen: Change grade clicked - navigating to GradeSelectionScreen")
                        navigator.goTo(GradeSelectionScreen(isFromSettings = true))
                    }

                    is SettingsScreen.Event.ToggleAdaptiveDifficulty -> {
                        Timber.d("SettingsScreen: Toggle adaptive difficulty - enabled=${event.enabled}")
                        scope.launch {
                            userProfileRepository.updateAdaptiveDifficulty(event.enabled)
                        }
                    }

                    is SettingsScreen.Event.SaveName -> {
                        Timber.d("SettingsScreen: Saving name - ${event.name}")
                        showNameDialog = false
                        scope.launch {
                            if (profile != null) {
                                userProfileRepository.updateName(event.name)
                            } else {
                                // No profile exists, create one with default grade
                                userProfileRepository.saveProfile(
                                    UserProfile(
                                        name = event.name,
                                        gradeLevel = GradeLevel.KINDERGARTEN,
                                        createdAt = java.time.Instant.now(),
                                        adaptiveDifficultyEnabled = true,
                                    ),
                                )
                            }
                        }
                    }

                    is SettingsScreen.Event.CancelNameEdit -> {
                        Timber.d("SettingsScreen: Cancel name edit")
                        showNameDialog = false
                    }

                    is SettingsScreen.Event.SaveGrade -> {
                        Timber.d("SettingsScreen: Saving grade - ${event.gradeLevel}")
                        showGradeDialog = false
                        scope.launch {
                            userProfileRepository.updateGradeLevel(event.gradeLevel)
                        }
                    }

                    is SettingsScreen.Event.CancelGradeChange -> {
                        Timber.d("SettingsScreen: Cancel grade change")
                        showGradeDialog = false
                    }

                    is SettingsScreen.Event.BackClicked -> {
                        Timber.d("SettingsScreen: Back clicked")
                        navigator.pop()
                    }

                    is SettingsScreen.Event.AudioHapticsClicked -> {
                        Timber.d("SettingsScreen: Audio & Haptics clicked")
                        navigator.goTo(AudioHapticSettingsScreen)
                    }
                }
            }
        }
    }
