package dev.hossain.mathtutor.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.MathOperation

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

    when (operation) {
        MathOperation.ADDITION -> {
            AdditionDotVisualizer(firstNumber, secondNumber, animationDurationMs, staggerDelayMs, modifier)
        }

        MathOperation.SUBTRACTION -> {
            SubtractionDotVisualizer(firstNumber, secondNumber, animationDurationMs, staggerDelayMs, modifier)
        }

        MathOperation.MULTIPLICATION -> {
            MultiplicationDotVisualizer(firstNumber, secondNumber, animationDurationMs, staggerDelayMs, modifier)
        }

        MathOperation.DIVISION -> {
            DivisionDotVisualizer(firstNumber, secondNumber, animationDurationMs, staggerDelayMs, modifier)
        }

        MathOperation.MIXED -> {}
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
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // First group of dots
        DotGroup(
            count = firstNumber,
            color = MaterialTheme.colorScheme.primary,
            delayMs = 0,
            durationMs = durationMs,
            staggerMs = delayMs,
        )

        // Plus sign
        AnimatedText(
            text = "+",
            delayMs = durationMs,
            durationMs = 300,
        )

        // Second group of dots
        DotGroup(
            count = secondNumber,
            color = MaterialTheme.colorScheme.tertiary,
            delayMs = durationMs + 300,
            durationMs = durationMs,
            staggerMs = delayMs,
        )
    }
}

/**
 * Subtraction visualization: shows num1 dots with num2 becoming faded
 */
@Composable
private fun SubtractionDotVisualizer(
    firstNumber: Int,
    secondNumber: Int,
    durationMs: Int,
    delayMs: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // All dots initially
        DotGroup(
            count = firstNumber,
            color = MaterialTheme.colorScheme.primary,
            delayMs = 0,
            durationMs = durationMs,
            staggerMs = delayMs,
        )

        // Minus sign
        AnimatedText(
            text = "-",
            delayMs = durationMs,
            durationMs = 300,
        )

        // Remaining dots (shown in a different style)
        val remainingCount = (firstNumber - secondNumber).coerceAtLeast(0)
        DotGroup(
            count = remainingCount,
            color = MaterialTheme.colorScheme.secondary,
            delayMs = durationMs + 300,
            durationMs = durationMs,
            staggerMs = delayMs,
        )
    }
}

/**
 * Multiplication visualization: shows groups of dots
 */
@Composable
private fun MultiplicationDotVisualizer(
    firstNumber: Int,
    secondNumber: Int,
    durationMs: Int,
    delayMs: Int,
    modifier: Modifier = Modifier,
) {
    val maxGroups = 5 // Limit display for readability
    val groupsToShow = firstNumber.coerceAtMost(maxGroups)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(groupsToShow) { groupIndex ->
            DotGroup(
                count = secondNumber.coerceAtMost(4), // Limit dots per group for readability
                color = MaterialTheme.colorScheme.primary,
                delayMs = groupIndex * delayMs,
                durationMs = durationMs,
                staggerMs = delayMs / 2,
            )

            // Show "×" between groups except after last
            if (groupIndex < groupsToShow - 1) {
                AnimatedText(
                    text = "×",
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
    modifier: Modifier = Modifier,
) {
    val groupsToShow = secondNumber.coerceAtMost(4) // Limit groups for readability

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
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
        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                .size(12.dp)
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

    androidx.compose.material3.Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.graphicsLayer { this.alpha = alpha.value },
    )
}
