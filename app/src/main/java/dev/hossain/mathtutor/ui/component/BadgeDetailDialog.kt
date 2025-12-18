package dev.hossain.mathtutor.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.hossain.mathtutor.util.TimeFormatter
import java.time.Instant

/**
 * Dialog component for displaying badge details.
 *
 * Shows the badge icon with animation, name, description, and unlock status.
 * If unlocked, displays the unlock date. If locked, shows the requirement.
 *
 * @param badge The badge to display details for
 * @param onDismiss Callback when the dialog is dismissed
 * @param modifier Optional modifier for the dialog
 */
@Composable
fun BadgeDetailDialog(
    badge: Badge,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Animated badge icon with scale/bounce
                val scale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                    label = "badge_icon_scale",
                )

                Text(
                    text = badge.icon,
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.scale(scale),
                )

                // Badge unlock status header
                if (badge.isUnlocked()) {
                    Text(
                        text = "Badge Unlocked!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                // Badge Name
                Text(
                    text = badge.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                // Badge Description
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                // Requirement or Unlock Date
                if (badge.isUnlocked()) {
                    badge.unlockedAt?.let { unlockedAt ->
                        Text(
                            text = "Unlocked on ${TimeFormatter.formatDate(unlockedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Text(
                        text = "Requirement: ${formatRequirement(badge)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                // Dismiss Button
                Button(onClick = onDismiss) {
                    Text(if (badge.isUnlocked()) "Awesome!" else "Close")
                }
            }
        }
    }
}

/**
 * Formats the badge requirement for display.
 */
private fun formatRequirement(badge: Badge): String =
    when (val req = badge.requirement) {
        is dev.hossain.mathtutor.domain.model.BadgeRequirement.ProblemCount -> {
            "Solve ${req.count} problems"
        }

        is dev.hossain.mathtutor.domain.model.BadgeRequirement.OperationCount -> {
            "Solve ${req.count} ${req.operation.name.lowercase()} problems"
        }

        is dev.hossain.mathtutor.domain.model.BadgeRequirement.ConsecutiveCorrect -> {
            "Get ${req.count} correct in a row"
        }

        is dev.hossain.mathtutor.domain.model.BadgeRequirement.SessionAccuracy -> {
            "Get ${req.percentage.toInt()}% accuracy in ${req.sessionCount} session(s)"
        }

        is dev.hossain.mathtutor.domain.model.BadgeRequirement.DailyStreak -> {
            "Practice ${req.days} days in a row"
        }

        is dev.hossain.mathtutor.domain.model.BadgeRequirement.ProblemSpeed -> {
            "Solve a problem in under ${req.maxSeconds} seconds"
        }

        is dev.hossain.mathtutor.domain.model.BadgeRequirement.MixedSessions -> {
            "Complete ${req.count} mixed mode sessions"
        }
    }

@Preview(showBackground = true)
@Composable
private fun BadgeDetailDialogUnlockedPreview() {
    KidsMathTutorAppTheme {
        BadgeDetailDialog(
            badge =
                Badge(
                    id = "first_steps",
                    name = "First Steps",
                    description = "You solved your first math problem! Great job!",
                    icon = "🎯",
                    category = BadgeCategory.GETTING_STARTED,
                    requirement = BadgeRequirement.ProblemCount(1),
                    unlockedAt = Instant.now(),
                ),
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgeDetailDialogLockedPreview() {
    KidsMathTutorAppTheme {
        BadgeDetailDialog(
            badge =
                Badge(
                    id = "math_master",
                    name = "Math Master",
                    description = "You're a true math champion! Keep up the great work!",
                    icon = "🏆",
                    category = BadgeCategory.VOLUME,
                    requirement = BadgeRequirement.ProblemCount(100),
                    unlockedAt = null,
                ),
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgeDetailDialogDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        BadgeDetailDialog(
            badge =
                Badge(
                    id = "speed_demon",
                    name = "Speed Demon",
                    description = "Lightning fast! You solved a problem in record time!",
                    icon = "⚡",
                    category = BadgeCategory.SPEED_ACCURACY,
                    requirement = BadgeRequirement.ProblemSpeed(5),
                    unlockedAt = Instant.now(),
                ),
            onDismiss = {},
        )
    }
}
