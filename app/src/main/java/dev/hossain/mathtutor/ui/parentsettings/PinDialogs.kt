package dev.hossain.mathtutor.ui.parentsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme

/**
 * Dialog for setting up a new parent PIN.
 *
 * Prompts the user to enter a 4-digit PIN twice for confirmation.
 * Shows error messages if PINs don't match or are invalid format.
 *
 * @param onConfirm Called with PIN and confirmation PIN when user taps Save
 * @param onDismiss Called when user taps Cancel or dismisses the dialog
 */
@Composable
fun PinSetupDialog(
    onConfirm: (pin: String, confirmPin: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Setup Parent PIN",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Create a 4-digit PIN to protect parent settings from being changed by children.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pin = it
                            errorMessage = null
                            if (it.length == 4) {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        }
                    },
                    label = { Text("Enter PIN") },
                    placeholder = { Text("Use a PIN kids can't guess :-)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                            },
                        ),
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            confirmPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Confirm PIN") },
                    placeholder = { Text("Use the same secure PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            },
                        ),
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        pin.length != 4 -> {
                            errorMessage = "PIN must be exactly 4 digits"
                        }

                        confirmPin.length != 4 -> {
                            errorMessage = "Please confirm your PIN"
                        }

                        pin != confirmPin -> {
                            errorMessage = "PINs do not match"
                        }

                        else -> {
                            onConfirm(pin, confirmPin)
                        }
                    }
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

/**
 * Dialog for verifying parent PIN before allowing access to protected features.
 *
 * @param onConfirm Called with the entered PIN when user taps Submit
 * @param onDismiss Called when user taps Cancel or dismisses the dialog
 */
@Composable
fun PinVerificationDialog(
    onConfirm: (pin: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pin by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Enter Parent PIN",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "This setting is protected. Enter your parent PIN to continue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pin = it
                        }
                    },
                    label = { Text("PIN") },
                    placeholder = { Text("Provide the save PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (pin.length == 4) {
                                    onConfirm(pin)
                                }
                            },
                        ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pin) },
                enabled = pin.length == 4,
            ) {
                Text(
                    text = "Submit",
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

/**
 * Dialog for resetting parent PIN after verifying old PIN.
 *
 * @param onConfirm Called with new PIN and confirmation when user taps Save
 * @param onDismiss Called when user taps Cancel or dismisses the dialog
 */
@Composable
fun PinResetDialog(
    onConfirm: (newPin: String, confirmNewPin: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reset Parent PIN",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Enter your new 4-digit PIN.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            newPin = it
                            errorMessage = null
                            if (it.length == 4) {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        }
                    },
                    label = { Text("New PIN") },
                    placeholder = { Text("Enter a new secure PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                            },
                        ),
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = confirmNewPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            confirmNewPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Confirm New PIN") },
                    placeholder = { Text("Re-enter the new PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            },
                        ),
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        newPin.length != 4 -> {
                            errorMessage = "PIN must be exactly 4 digits"
                        }

                        confirmNewPin.length != 4 -> {
                            errorMessage = "Please confirm your new PIN"
                        }

                        newPin != confirmNewPin -> {
                            errorMessage = "PINs do not match"
                        }

                        else -> {
                            onConfirm(newPin, confirmNewPin)
                        }
                    }
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
private fun PinSetupDialogPreview() {
    KidsMathTutorAppTheme {
        PinSetupDialog(
            onConfirm = { _, _ -> },
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PinVerificationDialogPreview() {
    KidsMathTutorAppTheme {
        PinVerificationDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PinResetDialogPreview() {
    KidsMathTutorAppTheme {
        PinResetDialog(
            onConfirm = { _, _ -> },
            onDismiss = {},
        )
    }
}
