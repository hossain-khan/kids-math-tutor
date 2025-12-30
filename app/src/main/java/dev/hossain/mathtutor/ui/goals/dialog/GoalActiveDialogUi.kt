package dev.hossain.mathtutor.ui.goals.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.ui.goals.dialog.GoalActiveDialogScreen.Event
import dev.hossain.mathtutor.ui.goals.dialog.GoalActiveDialogScreen.State
import dev.zacsweers.metro.AppScope

/**
 * UI for [GoalActiveDialogScreen].
 *
 * Displays a dialog showing the user has an active goal and offering options to:
 * - Continue working on the goal
 * - Dismiss the dialog
 *
 * The dialog shows:
 * - Goal title
 * - Current progress (e.g., "2/3 components completed")
 * - Action buttons
 */
@CircuitInject(GoalActiveDialogScreen::class, AppScope::class)
@Composable
fun GoalActiveDialogUi(
    state: State,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = { state.eventSink(Event.DismissClicked) },
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
    ) {
        Card(
            modifier =
                modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Title
                Text(
                    text = "Active Goal in Progress",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                // Goal info
                GoalLockDialogContent(
                    activeGoal = state.activeGoal,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Primary action: Continue Goal
                    Button(
                        onClick = { state.eventSink(Event.ContinueGoalClicked) },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        Text(
                            text = "Continue Goal",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    // Secondary action: Dismiss
                    OutlinedButton(
                        onClick = { state.eventSink(Event.DismissClicked) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Dismiss",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Content showing goal information in the lock dialog.
 */
@Composable
private fun GoalLockDialogContent(
    activeGoal: ActiveGoal,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Goal title
            Text(
                text = activeGoal.goal.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            // Progress information
            val totalComponents = activeGoal.goal.components.size
            val completedComponents =
                activeGoal.componentProgress.count {
                    it.completedSessions >= it.totalSessions
                }
            val overallProgress =
                if (totalComponents > 0) {
                    completedComponents.toFloat() / totalComponents.toFloat()
                } else {
                    0f
                }

            Text(
                text = "Progress: $completedComponents of $totalComponents components completed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            // Progress bar
            LinearProgressIndicator(
                progress = { overallProgress },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
            )

            // Motivational text
            Text(
                text = "Keep going! You're doing great!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
