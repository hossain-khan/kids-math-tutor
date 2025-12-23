package dev.hossain.mathtutor.ui.numbersequence

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import kotlinx.coroutines.delay

/**
 * Countdown screen displaying 3-2-1-GO! animation.
 *
 * Shows large animated numbers with scale effect that pulses on each count.
 * Uses displayLarge typography for maximum visibility.
 *
 * @param countdownValue Current countdown number (3, 2, 1, or 0 for GO!)
 * @param modifier Optional modifier
 */
@Composable
fun CountdownScreen(
    countdownValue: Int,
    modifier: Modifier = Modifier,
) {
    // Determine display text
    val displayText = if (countdownValue > 0) countdownValue.toString() else "GO!"

    // Scale animation state - starts big and pulses
    var targetScale by remember(countdownValue) { mutableFloatStateOf(0.5f) }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec =
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing,
            ),
        label = "countdown_scale",
    )

    // Trigger animation when countdown value changes
    LaunchedEffect(countdownValue) {
        targetScale = 1.2f
        delay(150)
        targetScale = 1.0f
    }

    // Text color - green for GO!, primary for numbers
    val textColor =
        if (countdownValue == 0) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.primary
        }

    // Accessibility description
    val contentDesc =
        if (countdownValue > 0) {
            "$countdownValue"
        } else {
            "Go! Game starting"
        }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = displayText,
                style =
                    MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = textColor,
                modifier =
                    Modifier
                        .scale(scale)
                        .semantics {
                            contentDescription = contentDesc
                            liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Assertive
                        },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CountdownScreenPreview3() {
    KidsMathTutorAppTheme {
        CountdownScreen(countdownValue = 3)
    }
}

@Preview(showBackground = true)
@Composable
private fun CountdownScreenPreview1() {
    KidsMathTutorAppTheme {
        CountdownScreen(countdownValue = 1)
    }
}

@Preview(showBackground = true)
@Composable
private fun CountdownScreenPreviewGo() {
    KidsMathTutorAppTheme {
        CountdownScreen(countdownValue = 0)
    }
}
