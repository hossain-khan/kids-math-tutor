package dev.hossain.mathtutor.ui.settings

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

/**
 * Dialog for changing the user's grade level.
 *
 * @param currentGrade The current grade level
 * @param maxGradeLevel Optional maximum grade level set by parents (null = no limit)
 * @param onDismiss Called when the dialog is dismissed (Cancel button)
 * @param onSave Called when the user saves the new grade level
 */
@Composable
fun GradeChangeDialog(
    currentGrade: GradeLevel,
    maxGradeLevel: GradeLevel? = null,
    onDismiss: () -> Unit,
    onSave: (GradeLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedGrade by remember { mutableStateOf(currentGrade) }

    // Filter grades based on max grade level if set
    val availableGrades =
        if (maxGradeLevel != null) {
            GradeLevel.entries.filter { it.ordinal <= maxGradeLevel.ordinal }
        } else {
            GradeLevel.entries
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Change Grade Level",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column {
                Text(
                    text = "Select your grade level:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Radio button group for grade selection
                Column(
                    modifier = Modifier.selectableGroup(),
                ) {
                    availableGrades.forEach { grade ->
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
                    text = "⚠️ Changing grade will affect problem difficulty",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )

                // Show parent lock message if max grade is set
                if (maxGradeLevel != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔒 Maximum grade limited to ${maxGradeLevel.displayName} by parent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(selectedGrade)
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

@Preview(showBackground = true)
@Composable
private fun GradeChangeDialogPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        GradeChangeDialog(
            currentGrade = GradeLevel.GRADE_1,
            maxGradeLevel = null,
            onDismiss = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GradeChangeDialogDarkPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme(darkTheme = true) {
        GradeChangeDialog(
            currentGrade = GradeLevel.GRADE_2,
            maxGradeLevel = null,
            onDismiss = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GradeChangeDialogWithLimitPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        GradeChangeDialog(
            currentGrade = GradeLevel.KINDERGARTEN,
            maxGradeLevel = GradeLevel.GRADE_1,
            onDismiss = {},
            onSave = {},
        )
    }
}
