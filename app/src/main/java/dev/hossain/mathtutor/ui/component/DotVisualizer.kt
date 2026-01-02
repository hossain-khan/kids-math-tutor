package dev.hossain.mathtutor.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme

/**
 * Visual representation using animated dots to represent grouped quantities.
 *
 * For example:
 * - Addition: Shows dots grouped by first number, then added dots
 * - Subtraction: Shows initial dots, then crossed-out removed dots
 * - Multiplication: Shows groups of dots
 * - Division: Shows dots being shared into groups
 *
 * @param operation The math operation to visualize
 * @param firstNumber The first number in the operation
 * @param secondNumber The second number in the operation
 * @param modifier Optional modifier
 */
@Composable
fun DotVisualizer(
    operation: MathOperation,
    firstNumber: Int,
    secondNumber: Int,
    modifier: Modifier = Modifier,
) {
    val animationDurationMs = 800
    val staggerDelayMs = 100

    // Use vertical layout that wraps naturally for portrait mode with vertical scroll support
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (operation) {
            MathOperation.ADDITION -> {
                AdditionDotVisualizer(firstNumber, secondNumber, animationDurationMs, staggerDelayMs)
            }

            MathOperation.SUBTRACTION -> {
                SubtractionDotVisualizer(firstNumber, secondNumber, animationDurationMs, staggerDelayMs)
            }

            MathOperation.MULTIPLICATION -> {
                MultiplicationDotVisualizer(firstNumber, secondNumber, animationDurationMs, staggerDelayMs)
            }

            MathOperation.DIVISION -> {
                DivisionDotVisualizer(firstNumber, secondNumber, animationDurationMs, staggerDelayMs)
            }

            MathOperation.MIXED -> {}
        }
    }
}

/**
 * Addition visualization: shows num1 dots, then num2 additional dots being added
 */
@Composable
private fun AdditionDotVisualizer(
    firstNumber: Int,
    secondNumber: Int,
    durationMs: Int,
    delayMs: Int,
) {
    // Combine all dots into one visualization with a plus sign in between
    val totalDots = firstNumber + secondNumber
    var dotIndex = 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // First group of dots - wrapped in rows
        WrappedDotGrid(
            count = firstNumber,
            color = MaterialTheme.colorScheme.primary,
            startDelayMs = 0,
            durationMs = durationMs,
            staggerMs = delayMs,
        )

        // Plus sign
        AnimatedText(
            text = "+",
            delayMs = durationMs,
            durationMs = 300,
        )

        // Second group of dots - wrapped in rows
        WrappedDotGrid(
            count = secondNumber,
            color = MaterialTheme.colorScheme.tertiary,
            startDelayMs = durationMs + 300,
            durationMs = durationMs,
            staggerMs = delayMs,
        )
    }
}

/**
 * Subtraction visualization: shows num1 dots with the last num2 dots dimmed with animation.
 *
 * For example, 13 - 3 shows 13 dots initially bright, then after 1 second,
 * the last 3 dots animate to dimmed state over 1 second, making it intuitive
 * for kids to see "taking away" from the original number.
 */
@Composable
private fun SubtractionDotVisualizer(
    firstNumber: Int,
    secondNumber: Int,
    durationMs: Int,
    delayMs: Int,
) {
    val remainingCount = (firstNumber - secondNumber).coerceAtLeast(0)
    val dotsPerRow = 6
    val rows = (firstNumber + dotsPerRow - 1) / dotsPerRow

    // Calculate when all dots finish appearing
    val lastDotStartTime = (firstNumber - 1) * delayMs
    val allDotsAppearTime = lastDotStartTime + durationMs
    val dimStartDelay = allDotsAppearTime + 1000 // Wait 1 second after all dots appear

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Show all dots in a grid, with the last 'secondNumber' dots animating to dim
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(rows) { rowIndex ->
                val startDot = rowIndex * dotsPerRow
                val endDot = (startDot + dotsPerRow).coerceAtMost(firstNumber)
                val dotsInRow = endDot - startDot

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(dotsInRow) { dotIndex ->
                        val globalDotIndex = startDot + dotIndex
                        // Determine if this dot should be dimmed
                        val shouldDim = globalDotIndex >= remainingCount

                        SubtractionAnimatedDot(
                            color = MaterialTheme.colorScheme.primary,
                            dimColor = MaterialTheme.colorScheme.secondary,
                            shouldDim = shouldDim,
                            appearDelayMs = globalDotIndex * delayMs,
                            appearDurationMs = durationMs,
                            dimDelayMs = dimStartDelay,
                            dimDurationMs = 1000, // Dim animation takes 1 second
                        )
                    }
                }
            }
        }
    }
}

/**
 * A single dot for subtraction that appears first, then optionally dims with animation.
 */
@Composable
private fun SubtractionAnimatedDot(
    color: androidx.compose.ui.graphics.Color,
    dimColor: androidx.compose.ui.graphics.Color,
    shouldDim: Boolean,
    appearDelayMs: Int,
    appearDurationMs: Int,
    dimDelayMs: Int,
    dimDurationMs: Int,
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    // Animate dot appearance
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = appearDurationMs,
                    delayMillis = appearDelayMs,
                    easing = LinearEasing,
                ),
        )

        // After dot appears, wait and then animate to dim if needed
        if (shouldDim) {
            alpha.animateTo(
                targetValue = 0.15f,
                animationSpec =
                    tween(
                        durationMillis = dimDurationMs,
                        delayMillis = dimDelayMs,
                        easing = LinearEasing,
                    ),
            )
        }
    }

    Box(
        modifier =
            Modifier
                .size(20.dp)
                .background(
                    color = color,
                    shape = CircleShape,
                ).graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                },
    )
}

/**
 * Multiplication visualization: shows groups of dots.
 *
 * Displays repeated addition: 2 × 5 becomes 5 + 5 (two groups of five dots)
 * This helps children understand multiplication as equal groups being combined.
 */
@Composable
private fun MultiplicationDotVisualizer(
    firstNumber: Int,
    secondNumber: Int,
    durationMs: Int,
    delayMs: Int,
) {
    val maxGroups = 12 // Increased limit for better accuracy
    val groupsToShow = firstNumber.coerceAtMost(maxGroups)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(groupsToShow) { groupIndex ->
            DotGroup(
                count = secondNumber.coerceAtMost(6), // Increased limit for better accuracy
                color = MaterialTheme.colorScheme.primary,
                delayMs = groupIndex * delayMs,
                durationMs = durationMs,
                staggerMs = delayMs / 2,
            )

            // Show "+" between groups except after last (represents repeated addition)
            if (groupIndex < groupsToShow - 1) {
                AnimatedText(
                    text = "+",
                    delayMs = (groupIndex + 1) * delayMs,
                    durationMs = 300,
                )
            }
        }
    }
}

/**
 * Division visualization: shows dots being distributed into groups
 */
@Composable
private fun DivisionDotVisualizer(
    firstNumber: Int,
    secondNumber: Int,
    durationMs: Int,
    delayMs: Int,
) {
    val maxGroups = 10 // Increased limit for better accuracy
    val groupsToShow = secondNumber.coerceAtMost(maxGroups)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(groupsToShow) { groupIndex ->
            val dotsPerGroup = firstNumber / secondNumber
            DotGroup(
                count = dotsPerGroup,
                color = MaterialTheme.colorScheme.primary,
                delayMs = groupIndex * delayMs,
                durationMs = durationMs,
                staggerMs = delayMs,
            )
        }
    }
}

/**
 * Wrapped grid of dots that breaks into multiple rows for better portrait mode support.
 * Maximum 6 dots per row.
 */
@Composable
private fun WrappedDotGrid(
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    startDelayMs: Int,
    durationMs: Int,
    staggerMs: Int,
) {
    val dotsPerRow = 6
    val rows = (count + dotsPerRow - 1) / dotsPerRow

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(rows) { rowIndex ->
            val startDot = rowIndex * dotsPerRow
            val endDot = (startDot + dotsPerRow).coerceAtMost(count)
            val dotsInRow = endDot - startDot

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(dotsInRow) { dotIndex ->
                    AnimatedDot(
                        color = color,
                        delayMs = startDelayMs + ((startDot + dotIndex) * staggerMs),
                        durationMs = durationMs,
                    )
                }
            }
        }
    }
}

/**
 * A group of animated dots that appear one by one with staggered timing.
 */
@Composable
private fun DotGroup(
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    delayMs: Int,
    durationMs: Int,
    staggerMs: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(count) { dotIndex ->
            AnimatedDot(
                color = color,
                delayMs = delayMs + (dotIndex * staggerMs),
                durationMs = durationMs,
            )
        }
    }
}

/**
 * A single dot that animates in with scale effect.
 */
@Composable
private fun AnimatedDot(
    color: androidx.compose.ui.graphics.Color,
    delayMs: Int,
    durationMs: Int,
) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = durationMs,
                    delayMillis = delayMs,
                    easing = LinearEasing,
                ),
        )
    }

    Box(
        modifier =
            Modifier
                .size(20.dp)
                .background(color, CircleShape)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
    )
}

/**
 * Animated text that fades in.
 */
@Composable
private fun AnimatedText(
    text: String,
    delayMs: Int,
    durationMs: Int,
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = durationMs,
                    delayMillis = delayMs,
                    easing = LinearEasing,
                ),
        )
    }

    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.graphicsLayer { this.alpha = alpha.value },
    )
}

/**
 * Preview of addition visualization showing 8 + 5 with animated dots.
 */
@Preview(showBackground = true, name = "Addition 8 + 5")
@Composable
private fun AdditionVisualizerPreview() {
    KidsMathTutorAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "8 + 5 = ?",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            DotVisualizer(
                operation = MathOperation.ADDITION,
                firstNumber = 8,
                secondNumber = 5,
            )
        }
    }
}

/**
 * Preview of subtraction visualization showing 13 - 3 with dimming animation.
 */
@Preview(showBackground = true, name = "Subtraction 13 - 3")
@Composable
private fun SubtractionVisualizerPreview() {
    KidsMathTutorAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "13 - 3 = ?",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            DotVisualizer(
                operation = MathOperation.SUBTRACTION,
                firstNumber = 13,
                secondNumber = 3,
            )
        }
    }
}

/**
 * Preview of multiplication visualization showing 4 × 6 with groups.
 */
@Preview(showBackground = true, name = "Multiplication 4 × 6")
@Composable
private fun MultiplicationVisualizerPreview() {
    KidsMathTutorAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "4 × 6 = ?",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            DotVisualizer(
                operation = MathOperation.MULTIPLICATION,
                firstNumber = 4,
                secondNumber = 6,
            )
        }
    }
}

/**
 * Preview of division visualization showing 12 ÷ 3 with equal groups.
 */
@Preview(showBackground = true, name = "Division 12 ÷ 3")
@Composable
private fun DivisionVisualizerPreview() {
    KidsMathTutorAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "12 ÷ 3 = ?",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            DotVisualizer(
                operation = MathOperation.DIVISION,
                firstNumber = 12,
                secondNumber = 3,
            )
        }
    }
}
