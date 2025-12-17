package dev.hossain.mathtutor.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Success animation composable that displays a celebration effect when a correct answer is given.
 *
 * Features:
 * - Confetti particle animation with physics-based movement
 * - Scale animation with spring physics for the success message
 * - Automatic show/hide based on visibility state
 * - Uses MaterialTheme colors for theme consistency
 * - Optimized for 60 FPS performance
 *
 * @param isVisible Whether the success animation should be displayed
 * @param modifier Optional modifier for the animation container
 * @param content Optional composable content to display (defaults to "✓ Correct!" text)
 */
@Composable
fun SuccessAnimation(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {
        Text(
            text = "✓ Correct!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    },
) {
    // Spring animation for scale effect
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "successScale",
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // Confetti background effect
            ConfettiEffect(isVisible = isVisible)

            // Success message with scale animation
            Box(
                modifier =
                    Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
            ) {
                content()
            }
        }
    }
}

/**
 * Confetti effect composable that displays animated particles.
 *
 * Uses Canvas for efficient particle rendering with theme colors.
 */
@Composable
private fun ConfettiEffect(isVisible: Boolean) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    // Generate confetti particles when visible
    val particles =
        remember(isVisible) {
            if (isVisible) {
                List(30) { index ->
                    ConfettiParticle(
                        color =
                            when (index % 3) {
                                0 -> primaryColor
                                1 -> secondaryColor
                                else -> tertiaryColor
                            },
                        startAngle = Random.nextFloat() * 360f,
                        speed = Random.nextFloat() * 2f + 1f,
                        size = Random.nextFloat() * 8f + 4f,
                    )
                }
            } else {
                emptyList()
            }
        }

    // Animation progress from 0f to 1f
    var animationProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animationProgress = 0f
            val startTime = System.currentTimeMillis()
            val duration = 1000L // 1 second animation

            while (animationProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                animationProgress = (elapsed.toFloat() / duration).coerceAtMost(1f)
                delay(16L) // ~60 FPS
            }
        }
    }

    if (isVisible && particles.isNotEmpty()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                drawConfettiParticle(
                    particle = particle,
                    progress = animationProgress,
                    centerX = size.width / 2,
                    centerY = size.height / 2,
                )
            }
        }
    }
}

/**
 * Data class representing a single confetti particle.
 */
private data class ConfettiParticle(
    val color: Color,
    val startAngle: Float,
    val speed: Float,
    val size: Float,
)

/**
 * Draws a single confetti particle on the canvas.
 */
private fun DrawScope.drawConfettiParticle(
    particle: ConfettiParticle,
    progress: Float,
    centerX: Float,
    centerY: Float,
) {
    // Calculate particle position based on progress
    val distance = progress * 150f * particle.speed
    val angle = Math.toRadians(particle.startAngle.toDouble())

    val x = centerX + (cos(angle) * distance).toFloat()
    val y = centerY + (sin(angle) * distance).toFloat() + (progress * progress * 100f) // Gravity effect

    // Fade out as particles move away
    val alpha = (1f - progress).coerceAtLeast(0f)

    drawCircle(
        color = particle.color.copy(alpha = alpha),
        radius = particle.size,
        center = Offset(x, y),
    )
}
