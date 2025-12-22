package dev.hossain.mathtutor.ui.practiceresults

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import dev.zacsweers.metro.AppScope

// Width breakpoints for adaptive layouts
private val MAX_CONTENT_WIDTH: Dp = 700.dp

/**
 * UI for [ResultsScreen].
 *
 * Displays practice session results including summary statistics and problem list.
 *
 * Adaptive Layout:
 * - Compact: Full width results
 * - Medium/Expanded: Centered content with max width
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
                    IconButton(onClick = { state.eventSink(ResultsScreen.Event.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Center content on wide screens
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                LazyColumn(
                    modifier =
                        Modifier
                            .widthIn(max = MAX_CONTENT_WIDTH)
                            .fillMaxSize()
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Summary statistics
                    item {
                        SummaryCard(
                            totalProblems = state.totalProblems,
                            correctCount = state.correctCount,
                            accuracyPercentage = state.accuracyPercentage,
                            userName = state.userName,
                            customChallengeTitle = state.customChallengeTitle,
                        )
                    }

                    // Problem results list
                    item {
                        Text(
                            text = "Problem Review",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }

                    items(state.problemResults) { result ->
                        ProblemResultCard(result = result)
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
            Text("Try Again")
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

@Preview(showBackground = true)
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

@Preview(showBackground = true)
@Composable
private fun ResultsUiDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
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
