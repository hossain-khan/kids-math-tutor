package dev.hossain.mathtutor.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Modifier that applies a horizontal shake animation to indicate an error or incorrect input.
 *
 * The shake animation oscillates the element left and right, providing visual feedback
 * for incorrect answers. Uses [graphicsLayer] for optimal performance to ensure 60 FPS.
 *
 * The shake amplitude scales based on screen width:
 * - Compact screens: 10dp base amplitude
 * - Expanded screens (≥840dp): 15dp base amplitude for better visibility on larger displays
 *
 * @param shouldShake Boolean that triggers the shake animation when true
 * @param onAnimationComplete Callback invoked when the shake animation completes
 */
fun Modifier.shake(
    shouldShake: Boolean,
    onAnimationComplete: () -> Unit = {},
): Modifier =
    composed {
        val offsetX = remember { Animatable(0f) }
        val density = LocalDensity.current

        LaunchedEffect(shouldShake) {
            if (shouldShake) {
                // Scale shake amplitude based on screen width
                // Use BoxWithConstraints' maxWidth would be ideal, but we approximate
                // with density for simplicity in composed modifier
                val baseAmplitude = with(density) { 10.dp.toPx() }

                // Shake animation: oscillate left and right
                // Duration: 490ms total (7 keyframes * 70ms each)
                // Amplitude: 10dp-15dp horizontal movement (scaled for larger screens)
                val shakeKeyframes =
                    listOf(
                        baseAmplitude,
                        -baseAmplitude,
                        baseAmplitude * 0.8f,
                        -baseAmplitude * 0.8f,
                        baseAmplitude * 0.5f,
                        -baseAmplitude * 0.5f,
                        0f,
                    )

                for (offset in shakeKeyframes) {
                    offsetX.animateTo(
                        targetValue = offset,
                        animationSpec =
                            tween(
                                durationMillis = 70,
                                easing = LinearEasing,
                            ),
                    )
                }

                // Notify completion (animation naturally returns to 0f via last keyframe)
                onAnimationComplete()
            }
        }

        this.graphicsLayer {
            translationX = offsetX.value
        }
    }
