package dev.hossain.mathtutor.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A single step in a step-by-step solution breakdown.
 *
 * Used by [StepByStepBreakdown] to display individual steps in the **Tier 3 work breakdown**
 * of the progressive hint system. Each step animates in with a staggered reveal to guide
 * children through the solution process one step at a time.
 *
 * **Visual Design**:
 * - Step number badge with primary color background
 * - Large emoji for visual engagement and quick recognition
 * - Clear description text in body style
 * - Rounded card with primaryContainer background
 * - Smooth fade-in + expand animation
 *
 * **Animation Timing**:
 * - Default stagger: 200ms between steps (set by [StepByStepBreakdown])
 * - Uses `fadeIn()` + `expandVertically()` for smooth reveal
 * - Helps children process each step before seeing the next
 *
 * **Usage Context**:
 * Part of the "📚 How to solve" feature shown after 2 wrong attempts when child
 * requests help and needs explicit step-by-step guidance.
 *
 * @param stepNumber The step number (1-indexed) displayed in the badge
 * @param emoji Emoji representing the step (🔢, ➕, 🧮, etc.)
 * @param description Text description of what to do in this step
 * @param delayMillis Delay in milliseconds before this step animates in (for staggered reveal)
 * @param modifier Optional modifier for additional styling
 *
 * @see dev.hossain.mathtutor.ui.component.StepByStepBreakdown for parent component
 * @see dev.hossain.mathtutor.domain.work.WorkProvider for step generation logic
 */
@Composable
fun WorkStep(
    stepNumber: Int,
    emoji: String,
    description: String,
    delayMillis: Int = 0,
    modifier: Modifier = Modifier,
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (delayMillis > 0) {
            delay(delayMillis.toLong())
        }
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        modifier = modifier,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                    ).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Step number badge
            Column(
                modifier =
                    Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(50),
                        ).width(40.dp)
                        .height(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
            }

            // Emoji and description
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "Step $stepNumber",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
