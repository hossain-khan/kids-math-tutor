package dev.hossain.mathtutor.circuit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.ui.component.AnswerField
import dev.hossain.mathtutor.ui.component.NumberPad
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope

/**
 * UI for [MathPracticeScreen].
 *
 * Displays the math problem, answer input field, number pad, and action buttons.
 */
@CircuitInject(MathPracticeScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathPracticeUi(
    state: MathPracticeScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Math Practice")
                },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(MathPracticeScreen.Event.NavigateBack) }) {
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
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Progress indicator
            ProgressSection(
                currentIndex = state.currentProblemIndex,
                totalProblems = state.totalProblems,
            )

            // Problem display
            state.currentProblem?.let { problem ->
                ProblemCard(problem = problem)
            }

            // Answer field
            AnswerField(
                answer = state.currentAnswer,
                modifier = Modifier.fillMaxWidth(),
            )

            // Feedback display
            FeedbackSection(isCorrect = state.isCorrect)

            Spacer(modifier = Modifier.weight(1f))

            // Number pad
            NumberPad(
                onNumberClick = { number ->
                    state.eventSink(MathPracticeScreen.Event.NumberClicked(number))
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // Action buttons
            ActionButtons(
                hasAnswer = state.currentAnswer.isNotEmpty(),
                isCorrect = state.isCorrect,
                onClear = { state.eventSink(MathPracticeScreen.Event.ClearAnswer) },
                onCheck = { state.eventSink(MathPracticeScreen.Event.CheckAnswer) },
                onNext = { state.eventSink(MathPracticeScreen.Event.NextProblem) },
            )
        }
    }
}

@Composable
private fun ProgressSection(
    currentIndex: Int,
    totalProblems: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Problem ${currentIndex + 1} of $totalProblems",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalProblems },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ProblemCard(
    problem: MathProblem,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = problem.getDisplayString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FeedbackSection(
    isCorrect: Boolean?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.height(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (isCorrect) {
            true -> {
                Text(
                    text = "✓ Correct!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            false -> {
                Text(
                    text = "✗ Try again",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            null -> {
                // Empty space when no feedback
            }
        }
    }
}

@Composable
private fun ActionButtons(
    hasAnswer: Boolean,
    isCorrect: Boolean?,
    onClear: () -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Clear button
        Button(
            onClick = onClear,
            enabled = hasAnswer,
            modifier = Modifier.weight(1f),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear",
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear")
        }

        // Check/Next button
        Button(
            onClick = if (isCorrect == true) onNext else onCheck,
            enabled = hasAnswer,
            modifier = Modifier.weight(1f),
        ) {
            Text(if (isCorrect == true) "Next" else "Check")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MathPracticeUiPreview() {
    KidsMathTutorAppTheme {
        MathPracticeUi(
            state =
                MathPracticeScreen.State(
                    currentProblem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8),
                    currentAnswer = "8",
                    currentProblemIndex = 0,
                    totalProblems = 10,
                    isCorrect = null,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MathPracticeUiCorrectPreview() {
    KidsMathTutorAppTheme {
        MathPracticeUi(
            state =
                MathPracticeScreen.State(
                    currentProblem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8),
                    currentAnswer = "8",
                    currentProblemIndex = 2,
                    totalProblems = 10,
                    isCorrect = true,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MathPracticeUiIncorrectPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        MathPracticeUi(
            state =
                MathPracticeScreen.State(
                    currentProblem = MathProblem(num1 = 7, num2 = 4, operation = MathOperation.ADDITION, correctAnswer = 11),
                    currentAnswer = "10",
                    currentProblemIndex = 5,
                    totalProblems = 10,
                    isCorrect = false,
                    eventSink = {},
                ),
        )
    }
}
