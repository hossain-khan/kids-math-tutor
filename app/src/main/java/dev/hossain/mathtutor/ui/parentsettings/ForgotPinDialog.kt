package dev.hossain.mathtutor.ui.parentsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import kotlin.random.Random

/**
 * Generates a Grade 10+ level math challenge for PIN recovery.
 *
 * Returns a pair of (problem text, correct answer).
 * Problems are designed to be solvable by parents with a calculator/Google
 * but too complex for K-2 children.
 *
 * ## Example Problems
 * - "What is 127 × 43?" = 5461
 * - "What is 2048 ÷ 16?" = 128
 * - "What is 15² (15 squared)?" = 225
 * - "What is 18% of 250?" = 45
 */
fun generateMathChallenge(): Pair<String, Int> {
    val challengeType = Random.nextInt(4)

    return when (challengeType) {
        0 -> {
            // Multiplication with large numbers
            val num1 = Random.nextInt(100, 200)
            val num2 = Random.nextInt(20, 50)
            val answer = num1 * num2
            "What is $num1 × $num2?" to answer
        }

        1 -> {
            // Division with large numbers (always divisible)
            val quotient = Random.nextInt(50, 200)
            val divisor = listOf(8, 16, 24, 32).random()
            val dividend = quotient * divisor
            val answer = quotient
            "What is $dividend ÷ $divisor?" to answer
        }

        2 -> {
            // Squaring numbers
            val base = Random.nextInt(12, 25)
            val answer = base * base
            "What is $base² ($base squared)?" to answer
        }

        else -> {
            // Percentage calculation
            val percentage = listOf(12, 15, 18, 20, 25, 30).random()
            val number = Random.nextInt(150, 500)
            val answer = (number * percentage) / 100
            "What is $percentage% of $number?" to answer
        }
    }
}

/**
 * Dialog for recovering forgotten PIN through a math challenge.
 *
 * Presents a Grade 10+ level math problem that parents can solve with a calculator
 * but is too difficult for K-2 children. Successfully solving the challenge
 * will clear the PIN, allowing parents to set a new one.
 *
 * ## Rationale
 * This recovery mechanism ensures:
 * - Parents can recover access without developer intervention
 * - Children cannot easily bypass PIN protection
 * - No email/phone verification needed (privacy-friendly)
 *
 * @param onSuccess Called when user successfully solves the math challenge
 * @param onDismiss Called when user taps Cancel or dismisses the dialog
 */
@Composable
fun ForgotPinDialog(
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (problemText, correctAnswer) = remember { generateMathChallenge() }
    var userAnswer by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var attemptCount by remember { mutableStateOf(0) }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Forgot PIN Recovery",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text =
                        "To recover your PIN, solve this math problem. " +
                            "You can use a calculator or Google to help.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = problemText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = {
                        // Allow digits and optional negative sign
                        if (it.isEmpty() || it == "-" || it.all { char -> char.isDigit() } ||
                            (it.startsWith("-") && it.drop(1).all { char -> char.isDigit() })
                        ) {
                            userAnswer = it
                            showError = false
                        }
                    },
                    label = { Text("Your Answer") },
                    placeholder = { Text("Enter the answer") },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            },
                        ),
                    singleLine = true,
                    isError = showError,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (showError) {
                    Text(
                        text =
                            if (attemptCount >= 3) {
                                "Incorrect. Please try again or contact support if you need help."
                            } else {
                                "Incorrect answer. Please try again."
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Text(
                    text = "⚠️ Successfully solving this will clear your current PIN",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val answer = userAnswer.toIntOrNull()
                    if (answer == correctAnswer) {
                        onSuccess()
                    } else {
                        showError = true
                        attemptCount++
                    }
                },
                enabled = userAnswer.isNotEmpty(),
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

// Preview composables
@Preview(showBackground = true)
@Composable
private fun ForgotPinDialogPreview() {
    KidsMathTutorAppTheme {
        ForgotPinDialog(
            onSuccess = {},
            onDismiss = {},
        )
    }
}
