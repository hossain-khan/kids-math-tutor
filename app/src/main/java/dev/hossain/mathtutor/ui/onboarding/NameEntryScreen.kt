package dev.hossain.mathtutor.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.ui.home.HomeScreen
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.time.Instant

/**
 * Circuit screen for optional name entry during onboarding.
 *
 * Allows users to optionally enter their name for personalized greetings.
 * Users can skip this step and go directly to the home screen.
 */
@Parcelize
data class NameEntryScreen(
    val gradeLevel: GradeLevel,
) : Screen {
    /**
     * State for [NameEntryScreen].
     *
     * @property name Current text in the name field
     * @property eventSink Handler for screen events
     */
    data class State(
        val name: String,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [NameEntryScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User changed the name text.
         */
        data class NameChanged(
            val name: String,
        ) : Event

        /**
         * User tapped Skip button.
         */
        data object SkipClicked : Event

        /**
         * User tapped Continue button.
         */
        data object ContinueClicked : Event
    }
}

/**
 * Presenter for [NameEntryScreen].
 *
 * Manages name input state and saves complete profile before navigating to home.
 */
@AssistedInject
class NameEntryPresenter
    constructor(
        @Assisted private val screen: NameEntryScreen,
        @Assisted private val navigator: Navigator,
        private val userProfileRepository: UserProfileRepository,
    ) : Presenter<NameEntryScreen.State> {
        @CircuitInject(NameEntryScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(
                screen: NameEntryScreen,
                navigator: Navigator,
            ): NameEntryPresenter
        }

        @Composable
        override fun present(): NameEntryScreen.State {
            var name by remember { mutableStateOf("") }
            val coroutineScope = rememberCoroutineScope()

            return NameEntryScreen.State(
                name = name,
            ) { event ->
                when (event) {
                    is NameEntryScreen.Event.NameChanged -> {
                        name = event.name
                    }

                    is NameEntryScreen.Event.SkipClicked -> {
                        // Save profile without name
                        coroutineScope.launch {
                            userProfileRepository.saveProfile(
                                UserProfile(
                                    name = null,
                                    gradeLevel = screen.gradeLevel,
                                    createdAt = Instant.now(),
                                    adaptiveDifficultyEnabled = true,
                                ),
                            )
                            navigator.resetRoot(HomeScreen)
                        }
                    }

                    is NameEntryScreen.Event.ContinueClicked -> {
                        // Save profile with name (or null if empty)
                        coroutineScope.launch {
                            userProfileRepository.saveProfile(
                                UserProfile(
                                    name = name.trim().ifBlank { null },
                                    gradeLevel = screen.gradeLevel,
                                    createdAt = Instant.now(),
                                    adaptiveDifficultyEnabled = true,
                                ),
                            )
                            navigator.resetRoot(HomeScreen)
                        }
                    }
                }
            }
        }
    }

/**
 * UI for [NameEntryScreen].
 *
 * Displays a text field for name entry with skip and continue buttons.
 */
@CircuitInject(NameEntryScreen::class, AppScope::class)
@Composable
fun NameEntryUi(
    state: NameEntryScreen.State,
    modifier: Modifier = Modifier,
) {
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(systemBarsPadding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "What's your name? 🐶",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "(Optional - we'll cheer for you!)",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = { state.eventSink(NameEntryScreen.Event.NameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Your Name") },
                placeholder = { Text("Enter your name") },
                singleLine = true,
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TextButton(
                    onClick = { state.eventSink(NameEntryScreen.Event.SkipClicked) },
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(56.dp),
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Button(
                    onClick = { state.eventSink(NameEntryScreen.Event.ContinueClicked) },
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(56.dp),
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NameEntryUiPreview() {
    KidsMathTutorAppTheme {
        NameEntryUi(
            state =
                NameEntryScreen.State(
                    name = "",
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NameEntryUiWithTextPreview() {
    KidsMathTutorAppTheme {
        NameEntryUi(
            state =
                NameEntryScreen.State(
                    name = "Alex",
                    eventSink = {},
                ),
        )
    }
}
