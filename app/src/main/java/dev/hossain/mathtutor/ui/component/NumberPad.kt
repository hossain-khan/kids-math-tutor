package dev.hossain.mathtutor.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.haptic.HapticService
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import timber.log.Timber

// Width breakpoints for adaptive sizing
private val EXPANDED_WIDTH_BREAKPOINT: Dp = 840.dp

/**
 * A number pad component for kids to input their answers.
 *
 * Displays numbers 0-9 in a 2x5 grid layout with large, child-friendly buttons.
 * First row: 1, 2, 3, 4, 5
 * Second row: 6, 7, 8, 9, 0
 *
 * Adapts button size and spacing based on screen width:
 * - Compact: 64dp buttons with 12dp spacing
 * - Expanded (≥840dp): 80dp buttons with 16dp spacing
 *
 * @param onNumberClick Callback invoked when a number button is clicked, provides the number (0-9)
 * @param modifier Optional modifier for the number pad container
 * @param hapticService Optional haptic service for button click feedback
 */
@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hapticService: HapticService? = null,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // Adaptive sizing based on screen width
        val isExpanded = maxWidth >= EXPANDED_WIDTH_BREAKPOINT
        val buttonSize = if (isExpanded) 80.dp else 64.dp
        val spacing = if (isExpanded) 16.dp else 12.dp
        val textStyle =
            if (isExpanded) {
                MaterialTheme.typography.displaySmall
            } else {
                MaterialTheme.typography.headlineMedium
            }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing),
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
                        buttonSize = buttonSize,
                        textStyle = textStyle,
                        hapticService = hapticService,
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
                        buttonSize = buttonSize,
                        textStyle = textStyle,
                        hapticService = hapticService,
                    )
                }
                NumberButton(
                    number = 0,
                    onClick = { onNumberClick(0) },
                    buttonSize = buttonSize,
                    textStyle = textStyle,
                    hapticService = hapticService,
                )
            }
        }
    }
}

/**
 * A single number button with child-friendly size and styling.
 *
 * @param number The number to display (0-9)
 * @param onClick Callback invoked when the button is clicked
 * @param buttonSize The size of the button (both width and height)
 * @param textStyle The text style for the number
 * @param modifier Optional modifier for the button
 * @param hapticService Optional haptic service for button click feedback
 */
@Composable
private fun NumberButton(
    number: Int,
    onClick: () -> Unit,
    buttonSize: Dp,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    hapticService: HapticService? = null,
) {
    Button(
        onClick = {
            hapticService?.triggerButtonClick()
            Timber.d("[NumberPad] Number button $number clicked - triggered haptic feedback")
            onClick()
        },
        modifier =
            modifier
                .size(buttonSize)
                .semantics {
                    contentDescription = "Number $number"
                    role = Role.Button
                },
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    ) {
        Text(
            text = number.toString(),
            style = textStyle,
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

@Preview(
    showBackground = true,
    widthDp = 800,
    heightDp = 600,
    name = "NumberPad - Medium Tablet",
)
@Composable
private fun NumberPadTabletPreview() {
    KidsMathTutorAppTheme {
        NumberPad(
            onNumberClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 1100,
    heightDp = 600,
    name = "NumberPad - Expanded Tablet Landscape",
)
@Composable
private fun NumberPadExpandedTabletPreview() {
    KidsMathTutorAppTheme {
        NumberPad(
            onNumberClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
