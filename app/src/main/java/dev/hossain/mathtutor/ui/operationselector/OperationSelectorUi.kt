package dev.hossain.mathtutor.ui.operationselector

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.ui.component.FeatureTopAppBar
import dev.hossain.mathtutor.ui.component.OperationCard
import dev.hossain.mathtutor.ui.component.TopBarFeature
import dev.hossain.mathtutor.ui.icons.CustomIcons
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope

// Max width for content centering on larger screens
private val MAX_CONTENT_WIDTH: Dp = 700.dp

// Minimum card width for adaptive grid
private val MIN_CARD_WIDTH: Dp = 280.dp

/**
 * Data class to hold operation card information.
 */
private data class OperationInfo(
    val title: String,
    val icon: ImageVector,
    val examples: List<String>,
    val operation: MathOperation,
)

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
            FeatureTopAppBar(
                title = {
                    Text("Math Time")
                },
                feature = TopBarFeature.PRACTICE,
                navigationIcon = {
                    IconButton(
                        onClick = {
                            state.eventSink(OperationSelectorScreen.Event.NavigateBack)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        // Center content on wide screens
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = MAX_CONTENT_WIDTH)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header with mascot
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Mascot image
                    Image(
                        painter = painterResource(id = R.drawable.pup_tutor_sticker_teaching),
                        contentDescription = "Math Pup Tutor",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(100.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    // Text column
                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = "Choose Your Practice",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "What would you like to work on?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Build list of operations dynamically based on grade level
                val operationsList =
                    buildList {
                        // Addition - always available
                        add(
                            OperationInfo(
                                title = "Addition",
                                icon = Icons.Default.Add,
                                examples = listOf("1 + 1 = ?", "5 + 3 = ?"),
                                operation = MathOperation.ADDITION,
                            ),
                        )

                        // Subtraction - always available
                        add(
                            OperationInfo(
                                title = "Subtraction",
                                icon = Icons.Default.Remove,
                                examples = listOf("10 - 5 = ?", "7 - 2 = ?"),
                                operation = MathOperation.SUBTRACTION,
                            ),
                        )

                        // Multiplication - only for Grade 1 and Grade 2
                        if (state.gradeLevel in listOf(GradeLevel.GRADE_1, GradeLevel.GRADE_2)) {
                            add(
                                OperationInfo(
                                    title = "Multiplication",
                                    icon = Icons.Default.Close,
                                    examples =
                                        when (state.gradeLevel) {
                                            GradeLevel.GRADE_1 -> listOf("2 × 5 = ?", "5 × 10 = ?")
                                            GradeLevel.GRADE_2 -> listOf("3 × 7 = ?", "8 × 6 = ?")
                                            else -> listOf()
                                        },
                                    operation = MathOperation.MULTIPLICATION,
                                ),
                            )
                        }

                        // Division - only for Grade 2
                        if (state.gradeLevel == GradeLevel.GRADE_2) {
                            add(
                                OperationInfo(
                                    title = "Division",
                                    icon = CustomIcons.Division,
                                    examples = listOf("20 ÷ 5 = ?", "15 ÷ 3 = ?"),
                                    operation = MathOperation.DIVISION,
                                ),
                            )
                        }

                        // Mix It Up - always available
                        add(
                            OperationInfo(
                                title = "Mix It Up!",
                                icon = Icons.Default.Shuffle,
                                examples = listOf("Random problems"),
                                operation = MathOperation.MIXED,
                            ),
                        )
                    }

                // Adaptive grid of operation cards
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = MIN_CARD_WIDTH),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(operationsList.size) { index ->
                        val opInfo = operationsList[index]
                        OperationCard(
                            title = opInfo.title,
                            icon = opInfo.icon,
                            examples = opInfo.examples,
                            operation = opInfo.operation,
                            onClick = {
                                // Handle Mix It Up special case
                                val selectedOperation =
                                    if (opInfo.operation == MathOperation.MIXED) {
                                        // Temporary: Using ADDITION until MathOperation.MIXED is implemented
                                        MathOperation.ADDITION
                                    } else {
                                        opInfo.operation
                                    }
                                state.eventSink(
                                    OperationSelectorScreen.Event.OperationSelected(selectedOperation),
                                )
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
}

@Preview(showBackground = true)
@Composable
private fun OperationSelectorUiPreview() {
    KidsMathTutorAppTheme {
        OperationSelectorUi(
            state =
                OperationSelectorScreen.State(
                    gradeLevel = GradeLevel.KINDERGARTEN,
                    hasSessionHistory = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OperationSelectorUiGrade1Preview() {
    KidsMathTutorAppTheme {
        OperationSelectorUi(
            state =
                OperationSelectorScreen.State(
                    gradeLevel = GradeLevel.GRADE_1,
                    hasSessionHistory = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OperationSelectorUiGrade2Preview() {
    KidsMathTutorAppTheme {
        OperationSelectorUi(
            state =
                OperationSelectorScreen.State(
                    gradeLevel = GradeLevel.GRADE_2,
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
                    gradeLevel = GradeLevel.GRADE_2,
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
                    gradeLevel = GradeLevel.GRADE_2,
                    hasSessionHistory = true,
                    eventSink = {},
                ),
        )
    }
}

// Adaptive layout previews
@Preview(
    name = "Compact (411dp × 891dp)",
    showBackground = true,
    widthDp = 411,
    heightDp = 891,
)
@Composable
private fun OperationSelectorUiCompactPreview() {
    KidsMathTutorAppTheme {
        OperationSelectorUi(
            state =
                OperationSelectorScreen.State(
                    gradeLevel = GradeLevel.GRADE_2,
                    hasSessionHistory = true,
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    name = "Medium (700dp × 500dp)",
    showBackground = true,
    widthDp = 700,
    heightDp = 500,
)
@Composable
private fun OperationSelectorUiMediumPreview() {
    KidsMathTutorAppTheme {
        OperationSelectorUi(
            state =
                OperationSelectorScreen.State(
                    gradeLevel = GradeLevel.GRADE_2,
                    hasSessionHistory = true,
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    name = "Expanded (1100dp × 600dp)",
    showBackground = true,
    widthDp = 1100,
    heightDp = 600,
)
@Composable
private fun OperationSelectorUiExpandedPreview() {
    KidsMathTutorAppTheme {
        OperationSelectorUi(
            state =
                OperationSelectorScreen.State(
                    gradeLevel = GradeLevel.GRADE_2,
                    hasSessionHistory = true,
                    eventSink = {},
                ),
        )
    }
}
