package dev.hossain.mathtutor.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.ui.games.GameSelectionScreen
import dev.hossain.mathtutor.ui.home.HomeScreen
import dev.hossain.mathtutor.ui.parentchallenges.ParentChallengesScreen
import dev.hossain.mathtutor.ui.settings.SettingsScreen

/**
 * Color pair for selected navigation items: background color and content color.
 */
data class NavigationItemColors(
    val containerColor: Color,
    val contentColor: Color,
)

// Vibrant, kid-friendly background colors
private object VibrantNavigationColors {
    // Home: Vibrant Green
    val homeBackground = Color(0xFF2E7D32)

    // Games: Vibrant Red
    val gamesBackground = Color(0xFFE53935)

    // Parents: Vibrant Blue
    val parentsBackground = Color(0xFF1976D2)

    // Settings: Vibrant Purple
    val settingsBackground = Color(0xFF7B1FA2)
}

/**
 * Represents a top-level navigation destination in the app.
 *
 * These are the main sections accessible from the bottom navigation bar,
 * navigation rail, or navigation drawer.
 */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val contentDescription: String,
    val screen: Screen,
) {
    /**
     * Home screen - main hub with practice access.
     */
    HOME(
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = "Home",
        contentDescription = "Home screen",
        screen = HomeScreen,
    ),

    /**
     * Games screen - mini-games selection.
     */
    GAMES(
        selectedIcon = Icons.Filled.SportsEsports,
        unselectedIcon = Icons.Outlined.SportsEsports,
        label = "Games",
        contentDescription = "Games selection",
        screen = GameSelectionScreen,
    ),

    /**
     * Parents screen - custom challenges creation.
     */
    PARENTS(
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People,
        label = "Parents",
        contentDescription = "Custom challenges",
        screen = ParentChallengesScreen,
    ),

    /**
     * Settings screen - app settings.
     */
    SETTINGS(
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        label = "Settings",
        contentDescription = "Settings",
        screen = SettingsScreen,
    ),
}

/**
 * Gets vibrant, high-contrast colors for a destination when selected.
 * Each destination has its own unique vibrant background color with white text for maximum contrast.
 * Colors are: Home=Green, Games=Red, Parents=Blue, Settings=Purple
 * White text works well on these dark vibrant backgrounds in both light and dark modes.
 */
@Composable
fun TopLevelDestination.getNavigationItemColors(): NavigationItemColors =
    when (this) {
        TopLevelDestination.HOME -> {
            NavigationItemColors(
                containerColor = VibrantNavigationColors.homeBackground,
                contentColor = Color.White,
            )
        }

        TopLevelDestination.GAMES -> {
            NavigationItemColors(
                containerColor = VibrantNavigationColors.gamesBackground,
                contentColor = Color.White,
            )
        }

        TopLevelDestination.PARENTS -> {
            NavigationItemColors(
                containerColor = VibrantNavigationColors.parentsBackground,
                contentColor = Color.White,
            )
        }

        TopLevelDestination.SETTINGS -> {
            NavigationItemColors(
                containerColor = VibrantNavigationColors.settingsBackground,
                contentColor = Color.White,
            )
        }
    }

/**
 * Gets the [TopLevelDestination] for a given [Screen], if any.
 *
 * @return The matching destination, or null if the screen is not a top-level destination.
 */
fun Screen.toTopLevelDestination(): TopLevelDestination? = TopLevelDestination.entries.find { it.screen::class == this::class }

/**
 * Checks if a [Screen] is a top-level destination.
 */
fun Screen.isTopLevelDestination(): Boolean = toTopLevelDestination() != null
