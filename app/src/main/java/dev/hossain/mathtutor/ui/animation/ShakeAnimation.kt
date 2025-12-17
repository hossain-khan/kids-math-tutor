package dev.hossain.mathtutor.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Modifier that applies a horizontal shake animation to indicate an error or incorrect input.
 *
 * The shake animation oscillates the element left and right, providing visual feedback
 * for incorrect answers. Uses [graphicsLayer] for optimal performance to ensure 60 FPS.
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

        LaunchedEffect(shouldShake) {
            if (shouldShake) {
                // Shake animation: oscillate left and right
                // Duration: 490ms total (7 keyframes * 70ms each)
                // Amplitude: 10dp horizontal movement
                val shakeKeyframes = listOf(10f, -10f, 8f, -8f, 5f, -5f, 0f)

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
