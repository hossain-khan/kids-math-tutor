package dev.hossain.mathtutor.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme

/**
 * A number pad component for kids to input their answers.
 *
 * Displays numbers 0-9 in a 2x5 grid layout with large, child-friendly buttons.
 * First row: 1, 2, 3, 4, 5
 * Second row: 6, 7, 8, 9, 0
 *
 * @param onNumberClick Callback invoked when a number button is clicked, provides the number (0-9)
 * @param modifier Optional modifier for the number pad container
 */
@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // First row: 1, 2, 3, 4, 5
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            for (number in 1..5) {
                NumberButton(
                    number = number,
                    onClick = { onNumberClick(number) },
                )
            }
        }

        // Second row: 6, 7, 8, 9, 0
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            for (number in 6..9) {
                NumberButton(
                    number = number,
                    onClick = { onNumberClick(number) },
                )
            }
            NumberButton(
                number = 0,
                onClick = { onNumberClick(0) },
            )
        }
    }
}

/**
 * A single number button with child-friendly size and styling.
 *
 * @param number The number to display (0-9)
 * @param onClick Callback invoked when the button is clicked
 * @param modifier Optional modifier for the button
 */
@Composable
private fun NumberButton(
    number: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier =
            modifier
                .size(64.dp)
                .semantics {
                    contentDescription = "Number $number"
                },
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NumberPadPreview() {
    KidsMathTutorAppTheme {
        NumberPad(
            onNumberClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NumberPadDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        NumberPad(
            onNumberClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
