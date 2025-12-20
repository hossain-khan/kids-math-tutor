package dev.hossain.mathtutor.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import timber.log.Timber

/**
 * A read-only text field component for displaying the user's answer input.
 *
 * Displays the current answer with centered text and a placeholder "?" when empty.
 * Uses Material 3 OutlinedTextField with large, child-friendly text.
 *
 * @param answer The current answer text to display (empty string if no input yet)
 * @param modifier Optional modifier for the text field
 */
@Composable
fun AnswerField(
    answer: String,
    modifier: Modifier = Modifier,
) {
    // Helper function to create content description for screen readers
    fun createAnswerDescription(input: String): String =
        if (input.isEmpty()) {
            "empty"
        } else {
            // Announce each digit separately for clarity
            input.toCharArray().joinToString(" ")
        }

    // Create content description for screen readers
    // Note: The field label "Your Answer" is already announced by TalkBack,
    // so we only need to announce the state or digits to avoid redundancy
    val answerDescription = createAnswerDescription(answer)

    // Log only when answer changes (not on every recomposition)
    LaunchedEffect(answer) {
        val answerDescriptionForLog = createAnswerDescription(answer)
        Timber.d("[AnswerField] Answer updated: '$answer', TalkBack will announce: '$answerDescriptionForLog'")
    }

    OutlinedTextField(
        value = answer,
        onValueChange = {}, // Read-only, no direct text input
        modifier =
            modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = answerDescription
                },
        readOnly = true,
        label = {
            Text("Your Answer")
        },
        textStyle =
            MaterialTheme.typography.displayLarge.copy(
                textAlign = TextAlign.Center,
            ),
        placeholder = {
            Text(
                text = "?",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        singleLine = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun AnswerFieldEmptyPreview() {
    KidsMathTutorAppTheme {
        AnswerField(
            answer = "",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnswerFieldWithAnswerPreview() {
    KidsMathTutorAppTheme {
        AnswerField(
            answer = "42",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnswerFieldDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        AnswerField(
            answer = "123",
            modifier = Modifier.padding(16.dp),
        )
    }
}
