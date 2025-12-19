package dev.hossain.mathtutor.ui.accessibility

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import timber.log.Timber

/**
 * An accessible text component that respects system font scale settings.
 *
 * This composable provides better support for users who need larger text by using Compose's
 * built-in font scaling. It also supports custom content descriptions for screen readers.
 *
 * Note: Compose Text automatically respects system font scale settings via LocalConfiguration,
 * so no manual scaling is needed. This component primarily adds semantic support for accessibility.
 *
 * @param text The text to display
 * @param modifier Optional modifier for the text
 * @param style The text style to use (Compose will automatically scale based on system settings)
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
    val finalContentDescription = contentDescription ?: text

    Timber.d("[AccessibleText] Rendering text with content description: $finalContentDescription")

    Text(
        text = text,
        modifier =
            modifier.semantics {
                // Use custom content description if provided, otherwise use the text
                this.contentDescription = finalContentDescription
            },
        style = style,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
    )
}
