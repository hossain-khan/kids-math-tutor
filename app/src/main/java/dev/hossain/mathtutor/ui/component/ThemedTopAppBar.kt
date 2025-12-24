package dev.hossain.mathtutor.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Enum for top-level feature colors in the app.
 *
 * Each feature area has a designated vibrant color for consistent visual branding,
 * carefully chosen to match and complement the bottom navigation colors:
 * - PRACTICE: Lighter Red - matches GAMES nav for practice activities
 * - STATS: Lighter Blue - matches PARENTS nav for data/insights
 * - BADGES: Lighter Green - matches HOME nav for achievement recognition
 * - SETTINGS: Lighter Purple - matches SETTINGS nav for configuration
 * - GAMES: Lighter Red - matches GAMES nav for game selection
 *
 * @see TopBarFeatureColors
 */
enum class TopBarFeature {
    PRACTICE,
    STATS,
    BADGES,
    SETTINGS,
    GAMES,
}

/**
 * Lighter, soothing versions of the vibrant navigation colors.
 * These are designed to be easier on the eyes while maintaining visual connection
 * to their corresponding navigation bar items.
 */
private object TopBarFeatureColors {
    // Lighter Red - matches Games nav (0xFFE53935) - for practice activities and games
    val redAccent = Color(0xFFEF5350)

    // Lighter Blue - matches Parents nav (0xFF1976D2) - for stats and insights
    val blueAccent = Color(0xFF42A5F5)

    // Lighter Green - matches Home nav (0xFF2E7D32) - for badges and achievements
    val greenAccent = Color(0xFF66BB6A)

    // Lighter Purple - matches Settings nav (0xFF7B1FA2) - for settings
    val purpleAccent = Color(0xFFAB47BC)
}

/**
 * Gets the color scheme for a top-level feature.
 * Uses lighter, soothing versions of the navigation bar colors to create visual harmony.
 *
 * @param feature The feature to get colors for
 * @return Pair of (containerColor, contentColor)
 */
@Composable
fun TopBarFeature.getColors(): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> =
    when (this) {
        TopBarFeature.PRACTICE, TopBarFeature.GAMES -> {
            TopBarFeatureColors.redAccent to Color.White
        }

        TopBarFeature.STATS -> {
            TopBarFeatureColors.blueAccent to Color.White
        }

        TopBarFeature.BADGES -> {
            TopBarFeatureColors.greenAccent to Color.White
        }

        TopBarFeature.SETTINGS -> {
            TopBarFeatureColors.purpleAccent to Color.White
        }
    }

/**
 * A styled TopAppBar with vibrant color for a top-level feature area.
 *
 * Uses lighter, soothing versions of the navigation bar colors to create visual harmony:
 * - Practice & Games screens → Lighter Red (matches Games nav)
 * - Stats screen → Lighter Blue (matches Parents nav)
 * - Badges screen → Lighter Green (matches Home nav)
 * - Settings screen → Lighter Purple (matches Settings nav)
 *
 * White text on these background colors provides excellent contrast and readability.
 * The lighter color palette is designed to be easy on the eyes while maintaining strong
 * visual connection to the corresponding bottom navigation items.
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
