package dev.hossain.mathtutor.ui.devportal

import androidx.compose.ui.graphics.Color
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

/**
 * Screen for visualizing all colors used throughout the app.
 *
 * Organizes colors by UI component categories:
 * - Navigation Colors: Bottom navigation bar colors
 * - TopAppBar Colors: Feature-based top app bar colors (light & dark modes)
 * - Theme Colors: Material 3 theme colors (semantic colors)
 *
 * This is a debug-only screen accessible from the Developer Portal.
 */
@Parcelize
data object ColorPaletteViewerScreen : Screen {
    /**
     * Represents a single color entry with metadata.
     */
    data class ColorEntry(
        val name: String,
        val color: Color,
        val hexCode: String = color.toHexString(),
        val usage: String = "",
        val isDarkModeVariant: Boolean = false,
    )

    /**
     * A group of related colors.
     */
    data class ColorGroup(
        val title: String,
        val description: String,
        val colors: List<ColorEntry>,
    )

    /**
     * State for the color palette viewer.
     */
    data class State(
        val colorGroups: List<ColorGroup> = emptyList(),
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    /**
     * Events for the color palette viewer.
     */
    sealed interface Event : CircuitUiEvent {
        data object BackClicked : Event

        data object CopyColorToClipboard : Event
    }
}

/**
 * Extension function to convert Color to hex string.
 */
private fun Color.toHexString(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    val alpha = (this.alpha * 255).toInt()

    return if (alpha == 255) {
        "#%02X%02X%02X".format(red, green, blue)
    } else {
        "#%02X%02X%02X%02X".format(red, green, blue, alpha)
    }
}
