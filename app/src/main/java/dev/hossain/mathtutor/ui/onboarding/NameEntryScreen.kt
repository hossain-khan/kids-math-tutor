package dev.hossain.mathtutor.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.ui.home.HomeScreen
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.EXPANDED_WIDTH_BREAKPOINT
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MAX_CONTENT_WIDTH_SMALL
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MEDIUM_WIDTH_BREAKPOINT
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.time.Instant

/**
 * Circuit screen for optional name entry during onboarding.
 *
 * Allows users to optionally enter their name for personalized greetings.
 * Users can skip this step and go directly to the home screen.
 */
@Parcelize
@CircuitSerializable(AppScope::class)
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
        private val userPreferencesRepository: UserPreferencesRepository,
        private val analyticsService: AnalyticsService,
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

            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Name Entry",
                    screenClass = NameEntryScreen::class.java.name,
                )
            }

            return NameEntryScreen.State(
                name = name,
            ) { event ->
                when (event) {
                    is NameEntryScreen.Event.NameChanged -> {
                        name = event.name
                    }

                    is NameEntryScreen.Event.SkipClicked -> {
                        Timber.d("NameEntry: Skip clicked - saving profile without name")
                        // Track name skipped
                        analyticsService.logEvent(
                            AnalyticsEvent.NAME_ENTERED,
                            mapOf("skipped" to true),
                        )
                        // Save profile without name
                        coroutineScope.launch {
                            userProfileRepository.saveProfile(
                                UserProfile(
                                    name = null,
                                    gradeLevel = screen.gradeLevel,
                                    createdAt = Instant.now(),
                                    adaptiveDifficultyEnabled = false,
                                ),
                            )
                            // Mark onboarding as completed
                            userPreferencesRepository.setOnboardingCompleted(true)
                            Timber.d("NameEntry: Profile saved (skipped), navigating to Home")
                            navigator.resetRoot(HomeScreen)
                        }
                    }

                    is NameEntryScreen.Event.ContinueClicked -> {
                        Timber.d("NameEntry: Continue clicked - name='${name.trim()}')")
                        // Track name entered
                        val hasName = name.trim().isNotBlank()
                        analyticsService.logEvent(
                            AnalyticsEvent.NAME_ENTERED,
                            mapOf("skipped" to !hasName),
                        )
                        // Save profile with name (or null if empty)
                        coroutineScope.launch {
                            userProfileRepository.saveProfile(
                                UserProfile(
                                    name = name.trim().ifBlank { null },
                                    gradeLevel = screen.gradeLevel,
                                    createdAt = Instant.now(),
                                    adaptiveDifficultyEnabled = false,
                                ),
                            )
                            // Mark onboarding as completed
                            userPreferencesRepository.setOnboardingCompleted(true)
                            Timber.d("NameEntry: Profile saved, navigating to Home")
                            navigator.resetRoot(HomeScreen)
                        }
                    }
                }
            }
        }
    }

/**
 * UI for [NameEntryScreen] with adaptive layout for tablets.
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
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
    ) { paddingValues ->
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            val screenWidth = maxWidth

            // Center content on wider screens
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier =
                        Modifier
                            .widthIn(max = MAX_CONTENT_WIDTH_SMALL)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Hero image with gradient overlays
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 32.dp)
                                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hero_onboarding_name_entry),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )

                        // Gradient overlay at top (10%)
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.1f)
                                    .align(Alignment.TopCenter)
                                    .background(
                                        brush =
                                            Brush.verticalGradient(
                                                colors =
                                                    listOf(
                                                        MaterialTheme.colorScheme.surface,
                                                        MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                                    ),
                                            ),
                                    ),
                        )

                        // Gradient overlay at bottom (1%)
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.01f)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        brush =
                                            Brush.verticalGradient(
                                                colors =
                                                    listOf(
                                                        MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                                        MaterialTheme.colorScheme.surface,
                                                    ),
                                            ),
                                    ),
                        )
                    }

                    Column(
                        modifier =
                            Modifier
                                .padding(systemBarsPadding)
                                .padding(24.dp)
                                .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "What's your name? 🐶",
                            style =
                                when {
                                    screenWidth >= MEDIUM_WIDTH_BREAKPOINT -> MaterialTheme.typography.displaySmall
                                    else -> MaterialTheme.typography.headlineLarge
                                },
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "(Optional - we'll cheer for you!)",
                            style =
                                when {
                                    screenWidth >= MEDIUM_WIDTH_BREAKPOINT -> MaterialTheme.typography.titleLarge
                                    else -> MaterialTheme.typography.bodyLarge
                                },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { state.eventSink(NameEntryScreen.Event.NameChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Your Name", style = MaterialTheme.typography.titleMedium) },
                            placeholder = { Text("Enter your name", style = MaterialTheme.typography.titleMedium) },
                            textStyle =
                                when {
                                    screenWidth >= MEDIUM_WIDTH_BREAKPOINT -> {
                                        MaterialTheme.typography.displaySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }

                                    else -> {
                                        MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                                    }
                                },
                            singleLine = false,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions =
                                KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done,
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                    },
                                ),
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

@Preview(showBackground = true)
@Composable
private fun NameEntryUiDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        NameEntryUi(
            state =
                NameEntryScreen.State(
                    name = "Jordan",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=891dp,height=411dp,dpi=420,isRound=false,orientation=landscape",
    name = "Phone Landscape",
)
@Composable
private fun NameEntryUiPhoneLandscapePreview() {
    KidsMathTutorAppTheme {
        NameEntryUi(
            state =
                NameEntryScreen.State(
                    name = "Sam",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240,isRound=false,orientation=portrait",
    name = "Tablet Portrait",
)
@Composable
private fun NameEntryUiTabletPortraitPreview() {
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

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,isRound=false,orientation=landscape",
    name = "Tablet Landscape",
)
@Composable
private fun NameEntryUiTabletLandscapePreview() {
    KidsMathTutorAppTheme {
        NameEntryUi(
            state =
                NameEntryScreen.State(
                    name = "Casey",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=673dp,height=841dp,dpi=373,isRound=false,orientation=portrait",
    name = "Foldable Portrait (Pixel Fold Unfolded)",
)
@Composable
private fun NameEntryUiFoldablePortraitPreview() {
    KidsMathTutorAppTheme {
        NameEntryUi(
            state =
                NameEntryScreen.State(
                    name = "Taylor",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=841dp,height=673dp,dpi=373,isRound=false,orientation=landscape",
    name = "Foldable Landscape (Pixel Fold Unfolded)",
)
@Composable
private fun NameEntryUiFoldableLandscapePreview() {
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
