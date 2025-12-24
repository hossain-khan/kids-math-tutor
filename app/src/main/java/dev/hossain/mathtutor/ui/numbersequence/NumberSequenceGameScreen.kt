package dev.hossain.mathtutor.ui.numbersequence

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.generator.SequenceQuestion
import dev.hossain.mathtutor.haptic.HapticService
import dev.hossain.mathtutor.ui.component.AnswerField
import dev.hossain.mathtutor.ui.component.NumberPad
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme

/**
 * Main game screen for Number Sequence.
 *
 * Shows timer, score, current sequence with missing number, answer field, and number pad.
 * Timer changes color and pulses when below 10 seconds.
 *
 * @param currentSequence Current sequence puzzle to solve
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
fun NumberSequenceGameScreen(
    currentSequence: SequenceQuestion?,
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
        Column(
            modifier =
                Modifier
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

            // Sequence display
            if (currentSequence != null) {
                SequenceDisplay(
                    sequence = currentSequence,
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

/**
 * Header showing timer and score.
 */
@Composable
private fun GameHeader(
    timeRemaining: Int,
    score: Int,
) {
    // Timer turns red and pulses when below 10 seconds
    val isWarning = timeRemaining <= 10
    val timerColor by animateColorAsState(
        targetValue =
            if (isWarning) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        label = "timer_color",
    )

    // Pulsing animation for warning state
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isWarning) 1.1f else 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "timer_scale",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Timer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .scale(scale)
                    .semantics {
                        liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite
                        contentDescription = "$timeRemaining seconds remaining"
                    },
        ) {
            Icon(
                imageVector = Icons.Filled.AccessTime,
                contentDescription = null,
                tint = timerColor,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatTime(timeRemaining),
                style = MaterialTheme.typography.headlineMedium,
                color = timerColor,
                fontWeight = FontWeight.Bold,
            )
        }

        // Score
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.semantics {
                    contentDescription = "Score: $score"
                },
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Displays the sequence with a missing number highlighted.
 */
@Composable
private fun SequenceDisplay(
    sequence: SequenceQuestion,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Find the missing number",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sequence numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                sequence.numbers.forEachIndexed { index, number ->
                    if (index > 0) {
                        // Comma separator
                        Text(
                            text = ",",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }

                    if (number != null) {
                        // Regular number
                        SequenceNumberCard(
                            number = number,
                            isMissing = false,
                        )
                    } else {
                        // Missing number (question mark)
                        SequenceNumberCard(
                            number = null,
                            isMissing = true,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pattern hint
            Text(
                text = "Pattern: ${sequence.sequenceType}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

/**
 * Individual number card in the sequence.
 */
@Composable
private fun SequenceNumberCard(
    number: Int?,
    isMissing: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        if (isMissing) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }

    val borderColor =
        if (isMissing) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }

    Card(
        modifier =
            modifier
                .size(56.dp)
                .semantics {
                    if (isMissing) {
                        contentDescription = "Missing number"
                    } else {
                        contentDescription = "Number $number"
                    }
                },
        colors =
            CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
        border = BorderStroke(2.dp, borderColor),
        shape = RoundedCornerShape(8.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number?.toString() ?: "?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color =
                    if (isMissing) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
        }
    }
}

/**
 * Action buttons for backspace and submit.
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
                        contentDescription = "Backspace"
                        role = Role.Button
                    },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            enabled = hasAnswer,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
        }

        // Submit button
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
                        contentDescription = "Submit answer"
                        role = Role.Button
                    },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            enabled = hasAnswer,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CHECK",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Personal best footer display.
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
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Best: $personalBest",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

/**
 * Formats seconds into MM:SS format.
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}

@Preview(showBackground = true)
@Composable
private fun NumberSequenceGameScreenPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceGameScreen(
            currentSequence =
                SequenceQuestion(
                    numbers = listOf(2, 4, null, 8, 10),
                    correctAnswer = 6,
                    missingIndex = 2,
                    sequenceType = "+2",
                ),
            currentAnswer = "6",
            score = 5,
            timeRemaining = 45,
            personalBest = 12,
            lastAnswerCorrect = null,
            onNumberEntered = {},
            onBackspace = {},
            onCheckAnswer = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NumberSequenceGameScreenWarningPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceGameScreen(
            currentSequence =
                SequenceQuestion(
                    numbers = listOf(1, 2, 4, null, 16),
                    correctAnswer = 8,
                    missingIndex = 3,
                    sequenceType = "×2",
                ),
            currentAnswer = "",
            score = 8,
            timeRemaining = 8,
            personalBest = 10,
            lastAnswerCorrect = null,
            onNumberEntered = {},
            onBackspace = {},
            onCheckAnswer = {},
        )
    }
}
