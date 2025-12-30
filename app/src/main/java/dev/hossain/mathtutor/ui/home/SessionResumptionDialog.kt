package dev.hossain.mathtutor.ui.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal

/**
 * Dialog that prompts user to resume their active goal session.
 *
 * Shown on HomeScreen when:
 * - Child has an active goal
 * - Screen first appears
 * - Dialog hasn't been shown in current session
 *
 * Provides two options:
 * - "Continue" → Navigate to GoalProgressScreen
 * - "Continue Later" → Dismiss dialog
 */
@Composable
fun SessionResumptionDialog(
    activeGoal: ActiveGoal,
    onContinueClicked: () -> Unit,
    onDismissClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissClicked,
        title = {
            Text(
                text = "Continue your goal?",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = "You have an active goal: 🎯 ${activeGoal.goal.title}\n\nWould you like to continue working on it?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        },
        confirmButton = {
            Button(
                onClick = onContinueClicked,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissClicked,
                colors =
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
            ) {
                Text("Continue Later")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}
