package dev.hossain.mathtutor.ui.devportal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.COMPACT_WIDTH_BREAKPOINT
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.EXPANDED_WIDTH_BREAKPOINT
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MAX_CONTENT_WIDTH_EXTRA_LARGE
import dev.zacsweers.metro.AppScope

private val MIN_COLOR_CARD_WIDTH: Dp = 150.dp

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
        // Center content on wide screens
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .widthIn(max = MAX_CONTENT_WIDTH_EXTRA_LARGE)
                        .fillMaxSize()
                        .padding(16.dp),
            ) {
                item {
                    Text(
                        text = "All colors used throughout the app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Render each group (colors or widgets)
                items(state.groups) { group ->
                    when (group) {
                        is ColorPaletteViewerScreen.ColorGroup -> ColorGroupCard(group = group, state = state)
                        is ColorPaletteViewerScreen.WidgetDemoGroup -> WidgetDemoGroupCard(group = group)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Card displaying a group of related colors with adaptive grid layout.
 *
 * Adaptive Layout:
 * - Uses GridCells.Adaptive with minimum card width of 150dp
 * - Grid automatically adjusts number of columns based on available width
 * - Adaptive spacing: 8dp (compact) → 12dp (medium) → 16dp (expanded)
 * - Compact (<600dp): Typically 2 colors per row
 * - Medium (600-840dp): Typically 3-4 colors per row
 * - Expanded (>840dp): Typically 5-6 colors per row
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

            // Adaptive grid layout for color swatches using FlowRow
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val screenWidth = maxWidth

                // Determine adaptive spacing based on screen width
                val gridSpacing =
                    when {
                        screenWidth < COMPACT_WIDTH_BREAKPOINT -> 8.dp
                        screenWidth < EXPANDED_WIDTH_BREAKPOINT -> 12.dp
                        else -> 16.dp
                    }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                    verticalArrangement = Arrangement.spacedBy(gridSpacing),
                ) {
                    group.colors.forEach { colorEntry ->
                        Box(
                            modifier = Modifier.widthIn(min = MIN_COLOR_CARD_WIDTH, max = MIN_COLOR_CARD_WIDTH),
                        ) {
                            ColorSwatchItem(colorEntry = colorEntry, state = state)
                        }
                    }
                }
            }
        }
    }
}

/**
 * A single color swatch card with details (name, hex, RGB, usage).
 *
 * Optimized for grid layout with a compact card design.
 */
@Composable
private fun ColorSwatchItem(
    colorEntry: ColorPaletteViewerScreen.ColorEntry,
    state: ColorPaletteViewerScreen.State,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { state.eventSink(ColorPaletteViewerScreen.Event.CopyColorToClipboard) },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Color swatch
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            color = colorEntry.color,
                            shape = RoundedCornerShape(8.dp),
                        ).border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                if (colorEntry.isDarkModeVariant) {
                    Text(
                        text = "🌒",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Text(
                        text = "🌔",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            // Color details
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = colorEntry.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

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
}

/**
 * Card displaying Material 3 widget demonstrations.
 */
@Composable
private fun WidgetDemoGroupCard(group: ColorPaletteViewerScreen.WidgetDemoGroup) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Widget demonstrations
            WidgetDemosContent()
        }
    }
}

/**
 * Content showcasing different Material 3 widgets with the current theme.
 */
@Composable
private fun WidgetDemosContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Buttons section
        Text(
            text = "Buttons",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            androidx.compose.material3.Button(
                onClick = {},
                modifier = Modifier.weight(1f),
            ) {
                Text("Primary")
            }
            androidx.compose.material3.OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f),
            ) {
                Text("Outlined")
            }
            androidx.compose.material3.FilledTonalButton(
                onClick = {},
                modifier = Modifier.weight(1f),
            ) {
                Text("Tonal")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cards section
        Text(
            text = "Cards",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Card with Primary Container",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "Content in primary container color",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Card with Secondary Container",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "Content in secondary container color",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Switches section
        Text(
            text = "Switches & Chips",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                    ).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Switch(checked = true, onCheckedChange = {})
            androidx.compose.material3.Switch(checked = false, onCheckedChange = {})
            androidx.compose.material3.AssistChip(
                onClick = {},
                label = { Text("Chip") },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Semantic colors demo
        Text(
            text = "Semantic Colors",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Success (using Tertiary as success indicator)
            Card(
                modifier = Modifier.weight(1f),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                    )
                    Text(
                        text = "Success",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                    )
                }
            }

            // Warning (using Secondary)
            Card(
                modifier = Modifier.weight(1f),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                    Text(
                        text = "Warning",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                }
            }

            // Error
            Card(
                modifier = Modifier.weight(1f),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onError,
                    )
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError,
                    )
                }
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

// Helper function to create sample color groups for previews
private fun createSampleColorGroups(): List<ColorPaletteViewerScreen.GroupContent> =
    listOf(
        ColorPaletteViewerScreen.ColorGroup(
            title = "Primary Colors",
            description = "Main brand and accent colors",
            colors =
                listOf(
                    ColorPaletteViewerScreen.ColorEntry(
                        name = "Primary",
                        color = Color(0xFF6750A4),
                        hexCode = "#6750A4",
                        usage = "Buttons, highlights",
                    ),
                    ColorPaletteViewerScreen.ColorEntry(
                        name = "On Primary",
                        color = Color(0xFFFFFFFF),
                        hexCode = "#FFFFFF",
                        usage = "Text on primary",
                    ),
                    ColorPaletteViewerScreen.ColorEntry(
                        name = "Primary Container",
                        color = Color(0xFFEADDFF),
                        hexCode = "#EADDFF",
                        usage = "Card backgrounds",
                    ),
                ),
        ),
        ColorPaletteViewerScreen.ColorGroup(
            title = "Surface Colors",
            description = "Background and surface colors",
            colors =
                listOf(
                    ColorPaletteViewerScreen.ColorEntry(
                        name = "Surface",
                        color = Color(0xFFFFFBFE),
                        hexCode = "#FFFBFE",
                        usage = "Main background",
                    ),
                    ColorPaletteViewerScreen.ColorEntry(
                        name = "On Surface",
                        color = Color(0xFF1C1B1F),
                        hexCode = "#1C1B1F",
                        usage = "Body text",
                    ),
                ),
        ),
        ColorPaletteViewerScreen.WidgetDemoGroup(
            title = "Widget Demos",
            description = "Material 3 component showcase",
        ),
    )

// Preview composables
@Preview(
    showBackground = true,
    device = "spec:width=891dp,height=411dp,dpi=420,isRound=false,orientation=landscape",
    name = "Phone Landscape",
)
@Composable
private fun ColorPaletteViewerUiPhoneLandscapePreview() {
    KidsMathTutorAppTheme {
        ColorPaletteViewerUi(
            state =
                ColorPaletteViewerScreen.State(
                    groups = createSampleColorGroups(),
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240,isRound=false,orientation=portrait",
    name = "Tablet Portrait",
)
@Composable
private fun ColorPaletteViewerUiTabletPortraitPreview() {
    KidsMathTutorAppTheme {
        ColorPaletteViewerUi(
            state =
                ColorPaletteViewerScreen.State(
                    groups = createSampleColorGroups(),
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,isRound=false,orientation=landscape",
    name = "Tablet Landscape",
)
@Composable
private fun ColorPaletteViewerUiTabletLandscapePreview() {
    KidsMathTutorAppTheme {
        ColorPaletteViewerUi(
            state =
                ColorPaletteViewerScreen.State(
                    groups = createSampleColorGroups(),
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=673dp,height=841dp,dpi=373,isRound=false,orientation=portrait",
    name = "Foldable Portrait (Pixel Fold Unfolded)",
)
@Composable
private fun ColorPaletteViewerUiFoldablePortraitPreview() {
    KidsMathTutorAppTheme {
        ColorPaletteViewerUi(
            state =
                ColorPaletteViewerScreen.State(
                    groups = createSampleColorGroups(),
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=841dp,height=673dp,dpi=373,isRound=false,orientation=landscape",
    name = "Foldable Landscape (Pixel Fold Unfolded)",
)
@Composable
private fun ColorPaletteViewerUiFoldableLandscapePreview() {
    KidsMathTutorAppTheme {
        ColorPaletteViewerUi(
            state =
                ColorPaletteViewerScreen.State(
                    groups = createSampleColorGroups(),
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    name = "Dark Theme",
)
@Composable
private fun ColorPaletteViewerUiDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        ColorPaletteViewerUi(
            state =
                ColorPaletteViewerScreen.State(
                    groups = createSampleColorGroups(),
                    eventSink = {},
                ),
        )
    }
}
