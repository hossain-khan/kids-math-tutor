package dev.hossain.mathtutor.ui.parentsettings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme

/**
 * Dialog for setting the maximum grade level children can select.
 *
 * Allows parents to restrict the difficulty of problems accessible to children.
 * This prevents frustration from attempting problems that are too advanced.
 *
 * ## Rationale
 * - Children may be tempted to try higher grade problems
 * - Attempting problems that are too difficult can be frustrating and demotivating
 * - Parents know their child's capability better than the app
 * - Provides a "training wheels" approach to gradually increase difficulty
 *
 * @param currentMaxGrade The currently set maximum grade (null = no limit)
 * @param onConfirm Called with the selected grade level (null = remove limit)
 * @param onDismiss Called when user taps Cancel or dismisses the dialog
 */
@Composable
fun GradeLimitDialog(
    currentMaxGrade: GradeLevel?,
    onConfirm: (GradeLevel?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedGrade by remember { mutableStateOf(currentMaxGrade) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Grade Limit",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column {
                Text(
                    text =
                        "Choose the maximum grade level your child can select. " +
                            "This prevents them from accessing problems that are too difficult.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Maximum Grade:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Radio button group for grade selection
                Column(
                    modifier = Modifier.selectableGroup(),
                ) {
                    // Option for no limit
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (selectedGrade == null),
                                    onClick = { selectedGrade = null },
                                    role = Role.RadioButton,
                                ).padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (selectedGrade == null),
                            onClick = null, // Handled by Row's selectable
                        )
                        Text(
                            text = "No Limit (All Grades)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }

                    // Options for each grade level
                    GradeLevel.entries.forEach { grade ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (grade == selectedGrade),
                                        onClick = { selectedGrade = grade },
                                        role = Role.RadioButton,
                                    ).padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = (grade == selectedGrade),
                                onClick = null, // Handled by Row's selectable
                            )
                            Text(
                                text = grade.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Warning message
                Text(
                    text = "⚠️ Your child won't be able to select grades above this limit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedGrade)
                },
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    )
}

// Preview composables
@Preview(showBackground = true)
@Composable
private fun GradeLimitDialogNoLimitPreview() {
    KidsMathTutorAppTheme {
        GradeLimitDialog(
            currentMaxGrade = null,
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GradeLimitDialogWithLimitPreview() {
    KidsMathTutorAppTheme {
        GradeLimitDialog(
            currentMaxGrade = GradeLevel.GRADE_1,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
