package dev.hossain.mathtutor.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for grade selection during onboarding.
 *
 * Allows users to select their grade level (K, 1, or 2) which determines
 * the difficulty and number ranges for math problems.
 */
@Parcelize
data object GradeSelectionScreen : Screen {
    /**
     * State for [GradeSelectionScreen].
     *
     * @property selectedGrade Currently selected grade level, null if none selected
     * @property eventSink Handler for screen events
     */
    data class State(
        val selectedGrade: GradeLevel?,
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
    }
}

/**
 * Presenter for [GradeSelectionScreen].
 *
 * Manages grade selection state and navigation to name entry screen.
 */
@AssistedInject
class GradeSelectionPresenter
    constructor(
        @Assisted private val navigator: Navigator,
    ) : Presenter<GradeSelectionScreen.State> {
        @CircuitInject(GradeSelectionScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): GradeSelectionPresenter
        }

        @Composable
        override fun present(): GradeSelectionScreen.State {
            var selectedGrade by remember { mutableStateOf<GradeLevel?>(null) }

            return GradeSelectionScreen.State(
                selectedGrade = selectedGrade,
            ) { event ->
                when (event) {
                    is GradeSelectionScreen.Event.GradeSelected -> {
                        selectedGrade = event.grade
                    }

                    is GradeSelectionScreen.Event.ContinueClicked -> {
                        // Only navigate if grade is selected
                        selectedGrade?.let {
                            navigator.goTo(NameEntryScreen(gradeLevel = it))
                        }
                    }
                }
            }
        }
    }

/**
 * UI for [GradeSelectionScreen].
 *
 * Displays three grade cards with descriptions and example problems.
 */
@CircuitInject(GradeSelectionScreen::class, AppScope::class)
@Composable
fun GradeSelectionUi(
    state: GradeSelectionScreen.State,
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
                text = "Which grade are you in? 🐶",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Kindergarten Card
            GradeCard(
                gradeLevel = GradeLevel.KINDERGARTEN,
                description = "Numbers 1-10, Simple addition",
                isSelected = state.selectedGrade == GradeLevel.KINDERGARTEN,
                onClick = {
                    state.eventSink(GradeSelectionScreen.Event.GradeSelected(GradeLevel.KINDERGARTEN))
                },
            )

            // Grade 1 Card
            GradeCard(
                gradeLevel = GradeLevel.GRADE_1,
                description = "Numbers 1-20, Add, subtract",
                isSelected = state.selectedGrade == GradeLevel.GRADE_1,
                onClick = {
                    state.eventSink(GradeSelectionScreen.Event.GradeSelected(GradeLevel.GRADE_1))
                },
            )

            // Grade 2 Card
            GradeCard(
                gradeLevel = GradeLevel.GRADE_2,
                description = "Numbers 1-100, All operations",
                isSelected = state.selectedGrade == GradeLevel.GRADE_2,
                onClick = {
                    state.eventSink(GradeSelectionScreen.Event.GradeSelected(GradeLevel.GRADE_2))
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Continue Button
            Button(
                onClick = { state.eventSink(GradeSelectionScreen.Event.ContinueClicked) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                enabled = state.selectedGrade != null,
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
