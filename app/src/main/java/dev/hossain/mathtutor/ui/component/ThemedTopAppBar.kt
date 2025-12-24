package dev.hossain.mathtutor.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

/**
 * Enum for top-level feature colors in the app.
 *
 * Each feature area has a designated vibrant color for consistent visual branding:
 * - PRACTICE: Bright Blue for math practice operations
 * - STATS: Vibrant Orange for performance tracking
 * - BADGES: Cheerful Green for achievement recognition
 * - SETTINGS: Inverse Primary for configuration
 * - GAMES: Bright Blue for game selection (same as practice)
 *
 * @see ThemedTopAppBar
 */
enum class TopBarFeature {
    PRACTICE,
    STATS,
    BADGES,
    SETTINGS,
    GAMES,
}

/**
 * Gets the color scheme for a top-level feature.
 *
 * @param feature The feature to get colors for
 * @return Pair of (containerColor, contentColor)
 */
@Composable
fun TopBarFeature.getColors(): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> =
    when (this) {
        TopBarFeature.PRACTICE, TopBarFeature.GAMES -> {
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        }

        TopBarFeature.STATS -> {
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        }

        TopBarFeature.BADGES -> {
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        }

        TopBarFeature.SETTINGS -> {
            MaterialTheme.colorScheme.inversePrimary to MaterialTheme.colorScheme.onSurface
        }
    }

/**
 * A styled TopAppBar with vibrant color for a top-level feature area.
 *
 * Provides consistent branding across the app with designated colors for each feature:
 * - Practice screens (math practice, results) → primaryContainer (Blue)
 * - Stats screens (stats, accuracy details) → secondaryContainer (Orange)
 * - Badges screen → tertiaryContainer (Green)
 * - Settings screens (settings, audio/haptic) → inversePrimary
 * - Game screens (game selection, games) → primaryContainer (Blue)
 *
 * Usage:
 * ```kotlin
 * Scaffold(
 *     topBar = {
 *         FeatureTopAppBar(
 *             title = "Math Practice",
 *             feature = TopBarFeature.PRACTICE,
 *             navigationIcon = { ... },
 *             actions = { ... }
 *         )
 *     }
 * )
 * ```
 *
 * @param title The title text to display in the app bar
 * @param feature The feature category determining the color scheme
 * @param navigationIcon Optional navigation icon (e.g., back button)
 * @param actions Optional action icons on the right side
 * @param modifier Optional modifier for the TopAppBar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureTopAppBar(
    title: @Composable () -> Unit,
    feature: TopBarFeature,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = feature.getColors()

    TopAppBar(
        title = title,
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                titleContentColor = contentColor,
                navigationIconContentColor = contentColor,
                actionIconContentColor = contentColor,
            ),
        navigationIcon = navigationIcon ?: {},
        actions = actions ?: {},
        modifier = modifier.shadow(elevation = 4.dp),
    )
}
