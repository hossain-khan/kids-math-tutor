package dev.hossain.mathtutor.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem

/**
 * Visual hint card that combines text hints with animated dot visualizers.
 *
 * Shows operation-specific visual representations to help children understand
 * the math concept while providing text guidance.
 *
 * @param hintText The text hint to display
 * @param problem The math problem being hinted
 * @param onDismiss Callback when hint is dismissed
 * @param modifier Optional modifier
 */
@Composable
fun VisualHintCard(
    hintText: String,
    problem: MathProblem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Card(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Header with emoji and dismiss button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "💡 Visual Hint",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.then(Modifier),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Dismiss hint",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }

                // Math Pup juggling balls sticker
                Image(
                    painter = painterResource(R.drawable.pup_tutor_sticker_juggling_balls),
                    contentDescription = "Math Pup with visual dots",
                    modifier =
                        Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally),
                )

                // Text hint
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Visual representation
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    DotVisualizer(
                        operation = problem.operation,
                        firstNumber = problem.num1,
                        secondNumber = problem.num2,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Encouragement text
                Text(
                    text = getEncouragementText(problem.operation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Returns operation-specific encouragement text.
 */
private fun getEncouragementText(operation: MathOperation): String =
    when (operation) {
        MathOperation.ADDITION -> "Count all the dots together!"
        MathOperation.SUBTRACTION -> "Count the bright dots! The dim ones are taken away!"
        MathOperation.MULTIPLICATION -> "Count the dots in all groups!"
        MathOperation.DIVISION -> "Count the dots in each group!"
        MathOperation.MIXED -> "Give it a try!"
    }
