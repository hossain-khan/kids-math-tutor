package dev.hossain.mathtutor.ui.accessibility

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * An accessible text component that automatically adjusts font size based on system settings.
 *
 * This composable respects the system's font scale settings and provides better support
 * for users who need larger text. It also supports custom content descriptions for screen readers.
 *
 * @param text The text to display
 * @param modifier Optional modifier for the text
 * @param style The text style to use (will be scaled based on font scale)
 * @param color The color of the text
 * @param textAlign Optional text alignment
 * @param overflow How to handle text overflow
 * @param maxLines Maximum number of lines for the text
 * @param contentDescription Optional content description for screen readers (if null, uses text)
 */
@Composable
fun AccessibleText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    contentDescription: String? = null,
) {
    // Get the current font scale from configuration
    val fontScale = LocalConfiguration.current.fontScale

    // Apply font scale to the text style
    val scaledStyle =
        style.copy(
            fontSize = style.fontSize * fontScale,
        )

    Text(
        text = text,
        modifier =
            modifier.semantics {
                // Use custom content description if provided, otherwise use the text
                this.contentDescription = contentDescription ?: text
            },
        style = scaledStyle,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
    )
}
