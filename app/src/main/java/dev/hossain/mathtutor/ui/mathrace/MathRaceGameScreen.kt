package dev.hossain.mathtutor.ui.mathrace

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.haptic.HapticService
import dev.hossain.mathtutor.ui.component.AnswerField
import dev.hossain.mathtutor.ui.component.NumberPad
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme

// Width breakpoints for adaptive layouts
private val MAX_CONTENT_WIDTH: Dp = 700.dp

/**
 * Main game screen for Math Race.
 *
 * Shows timer, score, current problem, answer field, number pad, and personal best.
 * Timer changes color and pulses when below 10 seconds.
 *
 * @param currentProblem Current math problem to solve
 * @param currentAnswer Player's current answer input
 * @param score Current score (correct answers)
 * @param timeRemaining Seconds remaining in the game
 * @param personalBest Player's personal best score
 * @param lastAnswerCorrect Whether the last answer was correct (for feedback)
 * @param onNumberEntered Callback when a digit is entered
 * @param onBackspace Callback when backspace is pressed
 * @param onCheckAnswer Callback when check/submit is pressed
 * @param modifier Optional modifier
 * @param hapticService Optional haptic service for feedback
 */
@Composable
fun MathRaceGameScreen(
    currentProblem: MathProblem?,
    currentAnswer: String,
    score: Int,
    timeRemaining: Int,
    personalBest: Int,
    lastAnswerCorrect: Boolean?,
    onNumberEntered: (Int) -> Unit,
    onBackspace: () -> Unit,
    onCheckAnswer: () -> Unit,
    modifier: Modifier = Modifier,
    hapticService: HapticService? = null,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Center content on tablets
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier =
                        Modifier
                            .widthIn(max = MAX_CONTENT_WIDTH)
                            .fillMaxSize()
                            .systemBarsPadding()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Header: Timer and Score
                    GameHeader(
                        timeRemaining = timeRemaining,
                        score = score,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Problem display
                    if (currentProblem != null) {
                        ProblemDisplay(
                            problem = currentProblem,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Answer field
                    AnswerField(
                        answer = currentAnswer,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Number pad
                    NumberPad(
                        onNumberClick = onNumberEntered,
                        modifier = Modifier.fillMaxWidth(),
                        hapticService = hapticService,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons row
                    ActionButtonsRow(
                        hasAnswer = currentAnswer.isNotEmpty(),
                        onBackspace = onBackspace,
                        onCheckAnswer = onCheckAnswer,
                        hapticService = hapticService,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Personal best at bottom
                    if (personalBest > 0) {
                        PersonalBestFooter(personalBest = personalBest)
                    }
                }
            }
        }
    }
}

/**
 * Header row showing timer and score with appropriate styling.
 */
@Composable
private fun GameHeader(
    timeRemaining: Int,
    score: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Timer display with warning state
        TimerDisplay(
            timeRemaining = timeRemaining,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Score display
        ScoreDisplay(
            score = score,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Timer display with color change and pulse animation when < 10 seconds.
 */
@Composable
private fun TimerDisplay(
    timeRemaining: Int,
    modifier: Modifier = Modifier,
) {
    val isWarning = timeRemaining <= 10

    // Animate color between primary and error
    val timerColor by animateColorAsState(
        targetValue =
            if (isWarning) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        animationSpec = tween(durationMillis = 300),
        label = "timer_color",
    )

    // Pulse animation when warning
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isWarning) 1.1f else 1.0f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing,
                    ),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "timer_scale",
    )

    // Format time as M:SS
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timeText = String.format("%d:%02d", minutes, seconds)

    Row(
        modifier =
            modifier
                .scale(scale)
                .semantics {
                    contentDescription =
                        if (isWarning) {
                            "Warning: $timeRemaining seconds remaining"
                        } else {
                            "$timeRemaining seconds remaining"
                        }
                    liveRegion =
                        if (isWarning) {
                            androidx.compose.ui.semantics.LiveRegionMode.Assertive
                        } else {
                            androidx.compose.ui.semantics.LiveRegionMode.Polite
                        }
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.AccessTime,
            contentDescription = null,
            tint = timerColor,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = timeText,
            style = MaterialTheme.typography.headlineMedium,
            color = timerColor,
        )
    }
}

/**
 * Score display with star icon.
 */
@Composable
private fun ScoreDisplay(
    score: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier.semantics {
                contentDescription = "Score: $score"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Score: $score",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

/**
 * Displays the current math problem in a card.
 */
@Composable
private fun ProblemDisplay(
    problem: MathProblem,
    modifier: Modifier = Modifier,
) {
    val displayText = problem.getDisplayString()
    val spokenText = problem.getSpokenString()

    Card(
        modifier =
            modifier.semantics(mergeDescendants = true) {
                contentDescription = spokenText
                heading()
            },
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
                text = displayText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Action buttons row with backspace and check buttons.
 */
@Composable
private fun ActionButtonsRow(
    hasAnswer: Boolean,
    onBackspace: () -> Unit,
    onCheckAnswer: () -> Unit,
    modifier: Modifier = Modifier,
    hapticService: HapticService? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Backspace button
        Button(
            onClick = {
                hapticService?.triggerButtonClick()
                onBackspace()
            },
            modifier =
                Modifier
                    .weight(1f)
                    .height(56.dp)
                    .semantics {
                        contentDescription = "Delete last digit"
                        role = Role.Button
                    },
            enabled = hasAnswer,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }

        // Check answer button
        Button(
            onClick = {
                hapticService?.triggerButtonClick()
                onCheckAnswer()
            },
            modifier =
                Modifier
                    .weight(2f)
                    .height(56.dp)
                    .semantics {
                        contentDescription = "Check answer"
                        role = Role.Button
                    },
            enabled = hasAnswer,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Check",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

/**
 * Personal best display at the bottom of the screen.
 */
@Composable
private fun PersonalBestFooter(
    personalBest: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier.semantics {
                contentDescription = "Personal best: $personalBest"
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Personal Best: $personalBest",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, name = "Compact Phone", widthDp = 411, heightDp = 891)
@Composable
private fun MathRaceGameScreenPreview() {
    KidsMathTutorAppTheme {
        MathRaceGameScreen(
            currentProblem =
                MathProblem(
                    num1 = 8,
                    num2 = 4,
                    operation = MathOperation.ADDITION,
                    correctAnswer = 12,
                ),
            currentAnswer = "12",
            score = 15,
            timeRemaining = 47,
            personalBest = 18,
            lastAnswerCorrect = null,
            onNumberEntered = {},
            onBackspace = {},
            onCheckAnswer = {},
        )
    }
}

@Preview(showBackground = true, name = "Medium Tablet", widthDp = 700, heightDp = 500)
@Composable
private fun MathRaceGameScreenMediumPreview() {
    KidsMathTutorAppTheme {
        MathRaceGameScreen(
            currentProblem =
                MathProblem(
                    num1 = 8,
                    num2 = 4,
                    operation = MathOperation.ADDITION,
                    correctAnswer = 12,
                ),
            currentAnswer = "12",
            score = 15,
            timeRemaining = 47,
            personalBest = 18,
            lastAnswerCorrect = null,
            onNumberEntered = {},
            onBackspace = {},
            onCheckAnswer = {},
        )
    }
}

@Preview(showBackground = true, name = "Expanded Tablet Landscape", widthDp = 1100, heightDp = 600)
@Composable
private fun MathRaceGameScreenExpandedPreview() {
    KidsMathTutorAppTheme {
        MathRaceGameScreen(
            currentProblem =
                MathProblem(
                    num1 = 8,
                    num2 = 4,
                    operation = MathOperation.ADDITION,
                    correctAnswer = 12,
                ),
            currentAnswer = "12",
            score = 15,
            timeRemaining = 47,
            personalBest = 18,
            lastAnswerCorrect = null,
            onNumberEntered = {},
            onBackspace = {},
            onCheckAnswer = {},
        )
    }
}

@Preview(showBackground = true, name = "Timer Warning State")
@Composable
private fun MathRaceGameScreenWarningPreview() {
    KidsMathTutorAppTheme {
        MathRaceGameScreen(
            currentProblem =
                MathProblem(
                    num1 = 5,
                    num2 = 3,
                    operation = MathOperation.SUBTRACTION,
                    correctAnswer = 2,
                ),
            currentAnswer = "",
            score = 21,
            timeRemaining = 8,
            personalBest = 18,
            lastAnswerCorrect = true,
            onNumberEntered = {},
            onBackspace = {},
            onCheckAnswer = {},
        )
    }
}

@Preview(showBackground = true, name = "Dark Theme")
@Composable
private fun MathRaceGameScreenDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        MathRaceGameScreen(
            currentProblem =
                MathProblem(
                    num1 = 7,
                    num2 = 6,
                    operation = MathOperation.MULTIPLICATION,
                    correctAnswer = 42,
                ),
            currentAnswer = "42",
            score = 10,
            timeRemaining = 35,
            personalBest = 25,
            lastAnswerCorrect = null,
            onNumberEntered = {},
            onBackspace = {},
            onCheckAnswer = {},
        )
    }
}
