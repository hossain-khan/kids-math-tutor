package dev.hossain.mathtutor.ui.operationselector

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.ui.component.OperationCard
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope

/**
 * UI for [OperationSelectorScreen].
 *
 * Displays three operation cards (Addition, Subtraction, Mix It Up) and a stats button.
 * The stats button is only enabled when session history exists.
 */
@CircuitInject(OperationSelectorScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationSelectorUi(
    state: OperationSelectorScreen.State,
    modifier: Modifier = Modifier,
) {
    /*
     * IMPORTANT: Explicit BackHandler to prevent ANR on system back button press.
     *
     * Without this BackHandler, pressing the system back button causes a 5+ second freeze
     * with high CPU usage on the main thread, triggering an ANR (Application Not Responding).
     * The BackHandler ensures immediate navigation response by handling the back event directly
     * and triggering navigation without blocking the UI thread.
     *
     * See: Similar fix in GameSelectionUi (PR #143) for the same ANR issue.
     */
    BackHandler {
        state.eventSink(OperationSelectorScreen.Event.NavigateBack)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Math Time")
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "🐶 Choose Your Practice",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "What would you like to work on?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            // Addition Card
            OperationCard(
                title = "Addition",
                icon = Icons.Default.Add,
                examples = listOf("1 + 1 = ?", "5 + 3 = ?"),
                onClick = {
                    state.eventSink(
                        OperationSelectorScreen.Event.OperationSelected(
                            MathOperation.ADDITION,
                        ),
                    )
                },
            )

            // Subtraction Card
            OperationCard(
                title = "Subtraction",
                icon = Icons.Default.Remove,
                examples = listOf("10 - 5 = ?", "7 - 2 = ?"),
                onClick = {
                    state.eventSink(
                        OperationSelectorScreen.Event.OperationSelected(
                            MathOperation.SUBTRACTION,
                        ),
                    )
                },
            )

            // Mix It Up Card
            // TODO: Implement MathOperation.MIXED type and mixed problem generation
            // For now, using ADDITION as placeholder until mixed operation mode is implemented
            OperationCard(
                title = "Mix It Up!",
                icon = Icons.Default.Shuffle,
                examples = listOf("Random problems"),
                onClick = {
                    // Temporary: Using ADDITION until MathOperation.MIXED is implemented
                    state.eventSink(
                        OperationSelectorScreen.Event.OperationSelected(
                            MathOperation.ADDITION,
                        ),
                    )
                },
            )

            Spacer(modifier = Modifier.weight(1f))

            // Stats Button
            Button(
                onClick = {
                    state.eventSink(OperationSelectorScreen.Event.ViewStatsClicked)
                },
                enabled = state.hasSessionHistory,
                modifier =
                    Modifier
                        .width(250.dp)
                        .height(48.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
            ) {
                Text(
                    text = "View My Stats",
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OperationSelectorUiPreview() {
    KidsMathTutorAppTheme {
        OperationSelectorUi(
            state =
                OperationSelectorScreen.State(
                    hasSessionHistory = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OperationSelectorUiWithHistoryPreview() {
    KidsMathTutorAppTheme {
        OperationSelectorUi(
            state =
                OperationSelectorScreen.State(
                    hasSessionHistory = true,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OperationSelectorUiDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        OperationSelectorUi(
            state =
                OperationSelectorScreen.State(
                    hasSessionHistory = true,
                    eventSink = {},
                ),
        )
    }
}
