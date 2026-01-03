package dev.hossain.mathtutor.ui.practiceresults

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.ui.component.BadgeDetailDialog
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.EXPANDED_WIDTH_BREAKPOINT
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MEDIUM_WIDTH_BREAKPOINT
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.RESULTS_GRID_MAX_WIDTH
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.RESULTS_SUMMARY_WIDTH_COMPACT
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.RESULTS_SUMMARY_WIDTH_EXPANDED
import dev.zacsweers.metro.AppScope

// Problem card dimensions
private val PROBLEM_CARD_HEIGHT: Dp = 140.dp
private val PROBLEM_CARD_MIN_WIDTH: Dp = 300.dp

/**
 * UI for [ResultsScreen].
 *
 * Displays practice session results including summary statistics and problem list.
 *
 * Adaptive Layout:
 * - Compact (<600dp): Single-column problem review, centered summary (max 700dp)
 * - Medium (600-840dp): 2-column problem review grid, wider summary (max 800dp)
 * - Expanded (>840dp): 2-3 column problem review grid, widest summary (max 800dp)
 */
@CircuitInject(ResultsScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsUi(
    state: ResultsScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Practice Results")
                },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(ResultsScreen.Event.TryAgain) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        modifier = modifier.fillMaxSize().systemBarsPadding(),
    ) { paddingValues ->
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            val screenWidth = maxWidth
            val isWideScreen = screenWidth >= MEDIUM_WIDTH_BREAKPOINT
            val isExpandedScreen = screenWidth >= EXPANDED_WIDTH_BREAKPOINT

            // Determine max width for summary card
            val summaryMaxWidth =
                if (isExpandedScreen) RESULTS_SUMMARY_WIDTH_EXPANDED else RESULTS_SUMMARY_WIDTH_COMPACT

            // Center content on wide screens
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                LazyColumn(
                    modifier =
                        Modifier
                            .widthIn(max = RESULTS_GRID_MAX_WIDTH)
                            .fillMaxSize()
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Summary statistics
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            SummaryCard(
                                totalProblems = state.totalProblems,
                                correctCount = state.correctCount,
                                accuracyPercentage = state.accuracyPercentage,
                                userName = state.userName,
                                customChallengeTitle = state.customChallengeTitle,
                                modifier = Modifier.widthIn(max = summaryMaxWidth),
                            )
                        }
                    }

                    // Problem review header
                    item {
                        Text(
                            text = "Problem Review",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }

                    // Problem results - adaptive grid using LazyColumn items
                    if (isWideScreen) {
                        // Calculate number of columns for grid layout
                        val columns = (screenWidth / PROBLEM_CARD_MIN_WIDTH).toInt().coerceAtLeast(1)
                        val rows = (state.problemResults.size + columns - 1) / columns

                        // Add grid rows as separate items in LazyColumn
                        for (rowIndex in 0 until rows) {
                            item(key = "row_$rowIndex") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    for (colIndex in 0 until columns) {
                                        val itemIndex = rowIndex * columns + colIndex
                                        if (itemIndex < state.problemResults.size) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                ProblemResultCard(result = state.problemResults[itemIndex])
                                            }
                                        } else {
                                            // Empty space for incomplete rows
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Single column for compact screens
                        items(state.problemResults) { result ->
                            ProblemResultCard(result = result)
                        }
                    }

                    // Action buttons
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionButtonsSection(
                            onTryAgain = { state.eventSink(ResultsScreen.Event.TryAgain) },
                        )
                    }
                }
            }
        }

        // Badge unlock dialog - shown if badges were unlocked
        if (state.showBadgeUnlock && state.unlockedBadges.isNotEmpty()) {
            val currentBadge = state.unlockedBadges.getOrNull(state.currentBadgeIndex)
            if (currentBadge != null) {
                BadgeDetailDialog(
                    badge = currentBadge,
                    onDismiss = { state.eventSink(ResultsScreen.Event.DismissBadgeDialog) },
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totalProblems: Int,
    correctCount: Int,
    accuracyPercentage: Float,
    userName: String?,
    customChallengeTitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Show custom challenge completion message if applicable
            if (customChallengeTitle != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Parent Challenge Complete!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = customChallengeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Personalized congratulations message
            val congratsMessage =
                if (customChallengeTitle != null) {
                    getCustomChallengeCongratsMessage(accuracyPercentage, userName)
                } else {
                    getCongratsMessage(accuracyPercentage, userName)
                }
            Text(
                text = congratsMessage,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )

            // Accuracy percentage
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${accuracyPercentage.toInt()}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "Correct",
                    value = correctCount.toString(),
                    color = MaterialTheme.colorScheme.primary,
                )
                StatItem(
                    label = "Total",
                    value = totalProblems.toString(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                StatItem(
                    label = "Incorrect",
                    value = (totalProblems - correctCount).toString(),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun ProblemResultCard(
    result: ResultsScreen.ProblemResult,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (result.isCorrect) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    },
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status icon
            Icon(
                imageVector =
                    if (result.isCorrect) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Clear
                    },
                contentDescription = if (result.isCorrect) "Correct" else "Incorrect",
                tint =
                    if (result.isCorrect) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                modifier = Modifier.size(32.dp),
            )

            Spacer(modifier = Modifier.padding(8.dp))

            // Problem and answers
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.problem.getDisplayString().replace(" = ?", ""),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (result.isCorrect) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    Text(
                        text = "Your answer: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            if (result.isCorrect) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            },
                    )
                    Text(
                        text = result.userAnswer?.toString() ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color =
                            if (result.isCorrect) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            },
                    )
                }

                if (!result.isCorrect) {
                    Row {
                        Text(
                            text = "Correct answer: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            text = result.problem.correctAnswer.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onTryAgain,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Let's Practice More")
        }
    }
}

/**
 * Generate personalized congratulations message based on accuracy and user name.
 */
private fun getCongratsMessage(
    accuracyPercentage: Float,
    userName: String?,
): String {
    val nameSuffix = if (userName != null) ", $userName" else ""

    return when {
        accuracyPercentage == 100f -> {
            if (userName != null) {
                "Perfect score, $userName! 🎉"
            } else {
                "Perfect score! 🎉"
            }
        }

        accuracyPercentage >= 90f -> {
            if (userName != null) {
                "Excellent work, $userName! ⭐"
            } else {
                "Excellent work! ⭐"
            }
        }

        accuracyPercentage >= 75f -> {
            if (userName != null) {
                "Great job, $userName! 👍"
            } else {
                "Great job! 👍"
            }
        }

        accuracyPercentage >= 50f -> {
            "Good effort$nameSuffix! 💪"
        }

        else -> {
            "Keep practicing$nameSuffix! 📚"
        }
    }
}

/**
 * Returns a custom challenge-specific congratulations message based on accuracy.
 */
private fun getCustomChallengeCongratsMessage(
    accuracyPercentage: Float,
    userName: String?,
): String {
    val nameSuffix = if (userName != null) ", $userName" else ""

    return when {
        accuracyPercentage == 100f -> {
            if (userName != null) {
                "Perfect, $userName! 🌟"
            } else {
                "Perfect! 🌟"
            }
        }

        accuracyPercentage >= 90f -> {
            if (userName != null) {
                "Awesome, $userName! 🎯"
            } else {
                "Awesome! 🎯"
            }
        }

        accuracyPercentage >= 75f -> {
            if (userName != null) {
                "Well done, $userName! 👏"
            } else {
                "Well done! 👏"
            }
        }

        accuracyPercentage >= 50f -> {
            "Nice try$nameSuffix! 💫"
        }

        else -> {
            "Good effort$nameSuffix! 🌈"
        }
    }
}

@Preview(name = "Compact Phone", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun ResultsUiPreview() {
    KidsMathTutorAppTheme {
        ResultsUi(
            state =
                ResultsScreen.State(
                    totalProblems = 5,
                    correctCount = 4,
                    accuracyPercentage = 80f,
                    problemResults =
                        listOf(
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 5,
                                        num2 = 3,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 8,
                                    ),
                                userAnswer = 8,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 7,
                                        num2 = 2,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 9,
                                    ),
                                userAnswer = 10,
                                isCorrect = false,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 4,
                                        num2 = 6,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 10,
                                    ),
                                userAnswer = 10,
                                isCorrect = true,
                            ),
                        ),
                    eventSink = {},
                ),
        )
    }
}

@Preview(name = "Medium Tablet", showBackground = true, widthDp = 700, heightDp = 500)
@Composable
private fun ResultsUiMediumPreview() {
    KidsMathTutorAppTheme {
        ResultsUi(
            state =
                ResultsScreen.State(
                    totalProblems = 6,
                    correctCount = 5,
                    accuracyPercentage = 83.33f,
                    problemResults =
                        listOf(
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 5,
                                        num2 = 3,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 8,
                                    ),
                                userAnswer = 8,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 7,
                                        num2 = 2,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 5,
                                    ),
                                userAnswer = 4,
                                isCorrect = false,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 4,
                                        num2 = 6,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 10,
                                    ),
                                userAnswer = 10,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 9,
                                        num2 = 3,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 6,
                                    ),
                                userAnswer = 6,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 8,
                                        num2 = 2,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 10,
                                    ),
                                userAnswer = 10,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 5,
                                        num2 = 5,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 10,
                                    ),
                                userAnswer = 10,
                                isCorrect = true,
                            ),
                        ),
                    eventSink = {},
                ),
        )
    }
}

@Preview(name = "Expanded Tablet", showBackground = true, widthDp = 1100, heightDp = 600)
@Composable
private fun ResultsUiExpandedPreview() {
    KidsMathTutorAppTheme {
        ResultsUi(
            state =
                ResultsScreen.State(
                    totalProblems = 6,
                    correctCount = 5,
                    accuracyPercentage = 83.33f,
                    problemResults =
                        listOf(
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 5,
                                        num2 = 3,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 8,
                                    ),
                                userAnswer = 8,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 7,
                                        num2 = 2,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 5,
                                    ),
                                userAnswer = 4,
                                isCorrect = false,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 4,
                                        num2 = 6,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 10,
                                    ),
                                userAnswer = 10,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 9,
                                        num2 = 3,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 6,
                                    ),
                                userAnswer = 6,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 8,
                                        num2 = 2,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 10,
                                    ),
                                userAnswer = 10,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 5,
                                        num2 = 5,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 10,
                                    ),
                                userAnswer = 10,
                                isCorrect = true,
                            ),
                        ),
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
private fun ResultsUiPhoneLandscapePreview() {
    KidsMathTutorAppTheme {
        ResultsUi(
            state =
                ResultsScreen.State(
                    totalProblems = 5,
                    correctCount = 3,
                    accuracyPercentage = 60f,
                    problemResults =
                        listOf(
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 6,
                                        num2 = 3,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 9,
                                    ),
                                userAnswer = 9,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 8,
                                        num2 = 4,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 4,
                                    ),
                                userAnswer = 3,
                                isCorrect = false,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 5,
                                        num2 = 5,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 10,
                                    ),
                                userAnswer = 10,
                                isCorrect = true,
                            ),
                        ),
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
private fun ResultsUiTabletPortraitPreview() {
    KidsMathTutorAppTheme {
        ResultsUi(
            state =
                ResultsScreen.State(
                    totalProblems = 10,
                    correctCount = 10,
                    accuracyPercentage = 100f,
                    problemResults =
                        listOf(
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 3,
                                        num2 = 2,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 5,
                                    ),
                                userAnswer = 5,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 7,
                                        num2 = 3,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 4,
                                    ),
                                userAnswer = 4,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 6,
                                        num2 = 4,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 10,
                                    ),
                                userAnswer = 10,
                                isCorrect = true,
                            ),
                        ),
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
private fun ResultsUiTabletLandscapePreview() {
    KidsMathTutorAppTheme {
        ResultsUi(
            state =
                ResultsScreen.State(
                    totalProblems = 10,
                    correctCount = 7,
                    accuracyPercentage = 70f,
                    problemResults =
                        listOf(
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 4,
                                        num2 = 3,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 7,
                                    ),
                                userAnswer = 7,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 9,
                                        num2 = 5,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 4,
                                    ),
                                userAnswer = 5,
                                isCorrect = false,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 2,
                                        num2 = 8,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 10,
                                    ),
                                userAnswer = 10,
                                isCorrect = true,
                            ),
                        ),
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
private fun ResultsUiFoldablePortraitPreview() {
    KidsMathTutorAppTheme {
        ResultsUi(
            state =
                ResultsScreen.State(
                    totalProblems = 5,
                    correctCount = 2,
                    accuracyPercentage = 40f,
                    problemResults =
                        listOf(
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 5,
                                        num2 = 3,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 8,
                                    ),
                                userAnswer = 8,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 7,
                                        num2 = 4,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 3,
                                    ),
                                userAnswer = 4,
                                isCorrect = false,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 6,
                                        num2 = 3,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 9,
                                    ),
                                userAnswer = 9,
                                isCorrect = true,
                            ),
                        ),
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
private fun ResultsUiFoldableLandscapePreview() {
    KidsMathTutorAppTheme {
        ResultsUi(
            state =
                ResultsScreen.State(
                    totalProblems = 10,
                    correctCount = 8,
                    accuracyPercentage = 80f,
                    problemResults =
                        listOf(
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 4,
                                        num2 = 2,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 6,
                                    ),
                                userAnswer = 6,
                                isCorrect = true,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 8,
                                        num2 = 3,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 5,
                                    ),
                                userAnswer = 4,
                                isCorrect = false,
                            ),
                            ResultsScreen.ProblemResult(
                                problem =
                                    MathProblem(
                                        num1 = 5,
                                        num2 = 5,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 10,
                                    ),
                                userAnswer = 10,
                                isCorrect = true,
                            ),
                        ),
                    eventSink = {},
                ),
        )
    }
}
