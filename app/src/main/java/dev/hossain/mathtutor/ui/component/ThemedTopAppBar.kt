package dev.hossain.mathtutor.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
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
 * Light and dark mode versions of the vibrant navigation colors.
 *
 * Light mode: Lighter, soothing versions for excellent readability on white backgrounds
 * Dark mode: Darker, more saturated versions that are easier on eyes in dark mode
 *
 * All colors maintain high contrast with white text for excellent accessibility.
 */
private object TopBarFeatureColors {
    // RED: Matches Games nav
    val redAccentLight = Color(0xFFEF5350) // Light mode
    val redAccentDark = Color(0xFFE53935) // Dark mode - more saturated

    // BLUE: Matches Parents nav
    val blueAccentLight = Color(0xFF42A5F5) // Light mode
    val blueAccentDark = Color(0xFF1565C0) // Dark mode - darker for dark theme

    // GREEN: Matches Home nav
    val greenAccentLight = Color(0xFF66BB6A) // Light mode
    val greenAccentDark = Color(0xFF2E7D32) // Dark mode - matches nav color

    // PURPLE: Matches Settings nav
    val purpleAccentLight = Color(0xFFAB47BC) // Light mode
    val purpleAccentDark = Color(0xFF7B1FA2) // Dark mode - matches nav color
}

/**
 * Gets the color scheme for a top-level feature.
 * Uses lighter, soothing versions for light mode and darker versions for dark mode
 * to optimize readability and eye comfort across themes.
 *
 * @param feature The feature to get colors for
 * @return Pair of (containerColor, contentColor)
 */
@Composable
fun TopBarFeature.getColors(): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    val isDarkMode = isSystemInDarkTheme()

    return when (this) {
        TopBarFeature.PRACTICE, TopBarFeature.GAMES -> {
            val color = if (isDarkMode) TopBarFeatureColors.redAccentDark else TopBarFeatureColors.redAccentLight
            color to Color.White
        }

        TopBarFeature.STATS -> {
            val color = if (isDarkMode) TopBarFeatureColors.blueAccentDark else TopBarFeatureColors.blueAccentLight
            color to Color.White
        }

        TopBarFeature.BADGES -> {
            val color = if (isDarkMode) TopBarFeatureColors.greenAccentDark else TopBarFeatureColors.greenAccentLight
            color to Color.White
        }

        TopBarFeature.SETTINGS -> {
            val color = if (isDarkMode) TopBarFeatureColors.purpleAccentDark else TopBarFeatureColors.purpleAccentLight
            color to Color.White
        }
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
