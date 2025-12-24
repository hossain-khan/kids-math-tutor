package dev.hossain.mathtutor.ui.devportal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

/**
 * UI for the Color Palette Viewer developer tool.
 *
 * Displays all colors used in the app organized by UI component categories.
 * Each color shows:
 * - Color preview swatch
 * - Color name
 * - Hex code
 * - RGB values
 * - Usage description
 */
@CircuitInject(ColorPaletteViewerScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPaletteViewerUi(
    state: ColorPaletteViewerScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎨 Color Palette Viewer") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(ColorPaletteViewerScreen.Event.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .fillMaxWidth(),
        ) {
            Text(
                text = "All colors used throughout the app",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Render each color group
            state.colorGroups.forEach { group ->
                ColorGroupCard(group = group, state = state)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Card displaying a group of related colors.
 */
@Composable
private fun ColorGroupCard(
    group: ColorPaletteViewerScreen.ColorGroup,
    state: ColorPaletteViewerScreen.State,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = group.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Render each color in the group
            group.colors.forEach { colorEntry ->
                ColorSwatchItem(colorEntry = colorEntry, state = state)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * A single color swatch with details (name, hex, RGB, usage).
 */
@Composable
private fun ColorSwatchItem(
    colorEntry: ColorPaletteViewerScreen.ColorEntry,
    state: ColorPaletteViewerScreen.State,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                ).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Color swatch
        Row(
            modifier =
                Modifier
                    .size(80.dp)
                    .background(
                        color = colorEntry.color,
                        shape = RoundedCornerShape(8.dp),
                    ).border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                    ).clickable { state.eventSink(ColorPaletteViewerScreen.Event.CopyColorToClipboard) },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (colorEntry.isDarkModeVariant) {
                Text(
                    text = "🌙",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        // Color details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = colorEntry.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Hex
                Column {
                    Text(
                        text = "HEX",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = colorEntry.hexCode,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // RGB
                Column {
                    Text(
                        text = "RGB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = colorEntry.color.toRgbString(),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            if (colorEntry.usage.isNotEmpty()) {
                Text(
                    text = "Usage: ${colorEntry.usage}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Extension function to convert Color to RGB string.
 */
private fun Color.toRgbString(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return "rgb($red, $green, $blue)"
}
