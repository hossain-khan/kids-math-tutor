package dev.hossain.mathtutor.ui.devportal

import androidx.compose.ui.graphics.Color
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import dev.zacsweers.metro.AppScope
import kotlinx.parcelize.Parcelize

/**
 * Screen for visualizing all colors used throughout the app.
 *
 * Organizes colors by UI component categories:
 * - Navigation Colors: Bottom navigation bar colors
 * - TopAppBar Colors: Feature-based top app bar colors (light & dark modes)
 * - Theme Colors: Material 3 theme colors (semantic colors)
 * - Widget Demos: Live Material 3 components showcasing theme in action
 *
 * This is a debug-only screen accessible from the Developer Portal.
 */
@Parcelize
@CircuitSerializable(AppScope::class)
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
     * Sealed class for group content - either colors or composable widgets
     */
    sealed interface GroupContent

    /**
     * A group of related colors.
     */
    data class ColorGroup(
        val title: String,
        val description: String,
        val colors: List<ColorEntry>,
    ) : GroupContent

    /**
     * A group showcasing Material 3 widgets and components in the app.
     */
    data class WidgetDemoGroup(
        val title: String,
        val description: String,
    ) : GroupContent

    /**
     * State for the color palette viewer.
     */
    data class State(
        val groups: List<GroupContent> = emptyList(),
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
