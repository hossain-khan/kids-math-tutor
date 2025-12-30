package dev.hossain.mathtutor.ui.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal

/**
 * A dialog that blocks game access when a goal is active.
 *
 * Displays the active goal's title, progress, and provides navigation options
 * to either view the goal progress or return to home.
 *
 * @param activeGoal The active goal being worked on
 * @param onViewGoalProgressClicked Callback when user clicks "View Goal Progress"
 * @param onBackToHomeClicked Callback when user clicks "Back to Home"
 * @param modifier Modifier for the dialog
 */
@Composable
fun GameBlockerDialog(
    activeGoal: ActiveGoal,
    onViewGoalProgressClicked: () -> Unit,
    onBackToHomeClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalSessions = activeGoal.goal.components.sumOf { it.sessionCount }
    val completedSessions = activeGoal.componentProgress.sumOf { it.completedSessions }
    val progress = if (totalSessions > 0) completedSessions.toFloat() / totalSessions else 0f

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onBackToHomeClicked,
        title = {
            Text(
                text = "Complete Your Goal First",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Goal title
                Text(
                    text = "🎯 ${activeGoal.goal.title}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )

                // Progress indicator
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LinearProgressIndicator(
                        { progress },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    // Session count
                    Text(
                        text = "$completedSessions / $totalSessions sessions completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }

                // Blocking message
                Text(
                    text = "Focus on completing your current goal before playing other games.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onViewGoalProgressClicked,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                Text("View Goal Progress →")
            }
        },
        dismissButton = {
            TextButton(onClick = onBackToHomeClicked) {
                Text("Back to Home")
            }
        },
    )
}
