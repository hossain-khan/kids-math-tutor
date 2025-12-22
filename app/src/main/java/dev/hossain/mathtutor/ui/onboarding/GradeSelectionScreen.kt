package dev.hossain.mathtutor.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsParam
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.time.Instant

/**
 * Circuit screen for grade selection during onboarding or from settings.
 *
 * Allows users to select their grade level (K, 1, or 2) which determines
 * the difficulty and number ranges for math problems.
 *
 * @property isFromSettings When true, saves grade and navigates back instead of going to name entry.
 *                          Used when accessing from Settings screen to change grade level.
 */
@Parcelize
data class GradeSelectionScreen(
    val isFromSettings: Boolean = false,
) : Screen {
    /**
     * State for [GradeSelectionScreen].
     *
     * @property selectedGrade Currently selected grade level, null if none selected
     * @property isFromSettings Whether this screen was opened from settings
     * @property eventSink Handler for screen events
     */
    data class State(
        val selectedGrade: GradeLevel?,
        val isFromSettings: Boolean = false,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [GradeSelectionScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User selected a grade level.
         */
        data class GradeSelected(
            val grade: GradeLevel,
        ) : Event

        /**
         * User tapped Continue button (requires grade selection).
         */
        data object ContinueClicked : Event

        /**
         * User tapped back button.
         */
        data object BackClicked : Event
    }
}

/**
 * Presenter for [GradeSelectionScreen].
 *
 * Manages grade selection state and navigation. Handles two modes:
 * - Onboarding mode: Navigates to name entry screen after selection
 * - Settings mode: Saves grade level and navigates back
 */
@AssistedInject
class GradeSelectionPresenter
    constructor(
        @Assisted private val screen: GradeSelectionScreen,
        @Assisted private val navigator: Navigator,
        private val userProfileRepository: UserProfileRepository,
        private val analyticsService: AnalyticsService,
    ) : Presenter<GradeSelectionScreen.State> {
        @CircuitInject(GradeSelectionScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(
                screen: GradeSelectionScreen,
                navigator: Navigator,
            ): GradeSelectionPresenter
        }

        @Composable
        override fun present(): GradeSelectionScreen.State {
            val scope = rememberCoroutineScope()
            var selectedGrade by remember { mutableStateOf<GradeLevel?>(null) }

            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Grade Selection",
                    screenClass = GradeSelectionScreen::class.java.name,
                    parameters =
                        mapOf(
                            "is_from_settings" to screen.isFromSettings,
                        ),
                )
            }

            // Collect current profile to check if it exists (for settings mode)
            val currentProfile by userProfileRepository.getProfile().collectAsState(initial = null)

            return GradeSelectionScreen.State(
                selectedGrade = selectedGrade,
                isFromSettings = screen.isFromSettings,
            ) { event ->
                when (event) {
                    is GradeSelectionScreen.Event.GradeSelected -> {
                        selectedGrade = event.grade
                        // Track grade selection
                        analyticsService.logEvent(
                            AnalyticsEvent.GRADE_SELECTED,
                            mapOf(
                                AnalyticsParam.GRADE_LEVEL to event.grade.name,
                            ),
                        )
                    }

                    is GradeSelectionScreen.Event.ContinueClicked -> {
                        // Only navigate if grade is selected
                        selectedGrade?.let { grade ->
                            if (screen.isFromSettings) {
                                // From settings: save grade asynchronously and navigate back immediately
                                // Don't block navigation on database save to prevent ANR
                                scope.launch {
                                    if (currentProfile != null) {
                                        // Profile exists, just update the grade
                                        userProfileRepository.updateGradeLevel(grade)
                                    } else {
                                        // No profile exists, create a new one
                                        // This handles edge cases where profile wasn't created properly
                                        userProfileRepository.saveProfile(
                                            UserProfile(
                                                name = null,
                                                gradeLevel = grade,
                                                createdAt = Instant.now(),
                                                adaptiveDifficultyEnabled = true,
                                            ),
                                        )
                                    }
                                }
                                // Navigate back immediately without waiting for database save
                                navigator.pop()
                            } else {
                                // Onboarding: navigate to name entry
                                navigator.goTo(NameEntryScreen(gradeLevel = grade))
                            }
                        }
                    }

                    is GradeSelectionScreen.Event.BackClicked -> {
                        navigator.pop()
                    }
                }
            }
        }
    }

/**
 * UI for [GradeSelectionScreen].
 *
 * Displays three grade cards with descriptions and example problems.
 * Shows a TopAppBar with back button when accessed from settings.
 */
@CircuitInject(GradeSelectionScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeSelectionUi(
    state: GradeSelectionScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (state.isFromSettings) {
                TopAppBar(
                    title = { Text("Change Grade") },
                    navigationIcon = {
                        IconButton(onClick = { state.eventSink(GradeSelectionScreen.Event.BackClicked) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    modifier = Modifier.shadow(elevation = 4.dp),
                )
            }
        },
        bottomBar = {
            // Floating button at bottom for both onboarding and settings
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Button(
                    onClick = { state.eventSink(GradeSelectionScreen.Event.ContinueClicked) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                            .height(56.dp),
                    enabled = state.selectedGrade != null,
                ) {
                    Text(
                        text = if (state.isFromSettings) "Save" else "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0.dp),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .then(
                        if (!state.isFromSettings) {
                            // Onboarding: apply top status bar padding (no TopAppBar)
                            Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                        } else {
                            Modifier
                        },
                    ).padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (state.isFromSettings) "Select your grade level 🐶" else "Which grade are you in? 🐶",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Kindergarten Card
            GradeCard(
                gradeLevel = GradeLevel.KINDERGARTEN,
                description = "Numbers 1-5, Simple addition",
                isSelected = state.selectedGrade == GradeLevel.KINDERGARTEN,
                onClick = {
                    state.eventSink(GradeSelectionScreen.Event.GradeSelected(GradeLevel.KINDERGARTEN))
                },
            )

            // Grade 1 Card
            GradeCard(
                gradeLevel = GradeLevel.GRADE_1,
                description = "Numbers 1-10, Add, subtract",
                isSelected = state.selectedGrade == GradeLevel.GRADE_1,
                onClick = {
                    state.eventSink(GradeSelectionScreen.Event.GradeSelected(GradeLevel.GRADE_1))
                },
            )

            // Grade 2 Card
            GradeCard(
                gradeLevel = GradeLevel.GRADE_2,
                description = "Numbers 1-20, All operations",
                isSelected = state.selectedGrade == GradeLevel.GRADE_2,
                onClick = {
                    state.eventSink(GradeSelectionScreen.Event.GradeSelected(GradeLevel.GRADE_2))
                },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Individual grade card component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradeCard(
    gradeLevel: GradeLevel,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(150.dp),
        border =
            if (isSelected) {
                BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            } else {
                null
            },
        elevation =
            CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp,
            ),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = gradeLevel.displayName,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GradeSelectionUiPreview() {
    KidsMathTutorAppTheme {
        GradeSelectionUi(
            state =
                GradeSelectionScreen.State(
                    selectedGrade = null,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GradeSelectionUiSelectedPreview() {
    KidsMathTutorAppTheme {
        GradeSelectionUi(
            state =
                GradeSelectionScreen.State(
                    selectedGrade = GradeLevel.GRADE_1,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GradeSelectionUiDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        GradeSelectionUi(
            state =
                GradeSelectionScreen.State(
                    selectedGrade = GradeLevel.GRADE_2,
                    eventSink = {},
                ),
        )
    }
}
