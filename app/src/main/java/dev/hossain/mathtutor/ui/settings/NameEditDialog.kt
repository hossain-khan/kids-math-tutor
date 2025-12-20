package dev.hossain.mathtutor.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Dialog for editing the user's name.
 *
 * @param currentName The current name value (can be null)
 * @param onDismiss Called when the dialog is dismissed (Cancel button)
 * @param onSave Called when the user saves the new name
 */
@Composable
fun NameEditDialog(
    currentName: String?,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var nameText by remember { mutableStateOf(currentName ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Name",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter your name (optional):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Your name",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(nameText.takeIf { it.isNotBlank() })
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
private fun NameEditDialogPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        NameEditDialog(
            currentName = "Alex",
            onDismiss = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NameEditDialogEmptyPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        NameEditDialog(
            currentName = null,
            onDismiss = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NameEditDialogDarkPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme(darkTheme = true) {
        NameEditDialog(
            currentName = "Jordan",
            onDismiss = {},
            onSave = {},
        )
    }
}
