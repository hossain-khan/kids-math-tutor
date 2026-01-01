package dev.hossain.mathtutor.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme

/**
 * A reusable card component for displaying math operation options.
 *
 * Shows the operation name, icon (or text), and example problems in a Material 3 elevated card.
 * Includes an optional hero image on the right side with a fade effect.
 *
 * @param title The name of the operation (e.g., "Addition", "Subtraction")
 * @param icon The icon representing the operation (optional if iconText is provided)
 * @param examples List of example problem strings (e.g., "5 + 3 = ?")
 * @param operation The math operation type (used to determine hero image)
 * @param onClick Callback when the card is clicked
 * @param modifier Optional modifier for the card
 * @param iconText Optional text to display as icon (e.g., "÷" for division)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationCard(
    title: String,
    icon: ImageVector? = null,
    examples: List<String>,
    operation: MathOperation? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconText: String? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation =
            CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp,
            ),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        // Get the theme surface color for dark mode awareness
        val surfaceColor = MaterialTheme.colorScheme.surface

        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Hero image on the right side with fade effect (if available)
            operation?.let { op ->
                val heroImageRes = getHeroImageForOperation(op)
                heroImageRes?.let { imageRes ->
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight,
                        alignment = Alignment.CenterEnd,
                        modifier =
                            Modifier
                                .matchParentSize()
                                .align(Alignment.CenterEnd)
                                .drawWithContent {
                                    drawContent()
                                    // Apply horizontal fade from left (opaque surface color) to right (transparent)
                                    // Since image is right-aligned, start gradient from right edge
                                    // This blends the image with the card background in both light and dark modes
                                    drawRect(
                                        brush =
                                            Brush.horizontalGradient(
                                                colors =
                                                    listOf(
                                                        surfaceColor,
                                                        surfaceColor.copy(alpha = 0.8f),
                                                        surfaceColor.copy(alpha = 0.5f),
                                                        surfaceColor.copy(alpha = 0.2f),
                                                        Color.Transparent,
                                                    ),
                                                startX = size.width * 0.4f,
                                                endX = size.width,
                                            ),
                                    )
                                },
                    )
                }
            }

            // Content on top of hero image
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Icon or Text Icon
                if (iconText != null) {
                    Text(
                        text = iconText,
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp),
                        textAlign = TextAlign.Center,
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Example problems
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    examples.forEach { example ->
                        Text(
                            text = example,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Returns the hero image resource ID for the given operation.
 * Returns null if no hero image is available for the operation.
 */
private fun getHeroImageForOperation(operation: MathOperation): Int? =
    when (operation) {
        MathOperation.ADDITION -> R.drawable.hero_math_operation_addition
        MathOperation.SUBTRACTION -> R.drawable.hero_math_operation_subtraction
        MathOperation.MULTIPLICATION -> R.drawable.hero_math_operation_multiplication
        MathOperation.DIVISION -> R.drawable.hero_math_operation_division
        MathOperation.MIXED -> R.drawable.hero_math_operation_mixed
    }

@Preview(showBackground = true)
@Composable
private fun OperationCardPreview() {
    KidsMathTutorAppTheme {
        OperationCard(
            title = "Addition",
            icon = Icons.Default.Add,
            examples = listOf("1 + 1 = ?", "5 + 3 = ?"),
            operation = MathOperation.ADDITION,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OperationCardDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        OperationCard(
            title = "Subtraction",
            icon = Icons.Default.Add,
            examples = listOf("10 - 5 = ?", "7 - 2 = ?"),
            operation = MathOperation.SUBTRACTION,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
