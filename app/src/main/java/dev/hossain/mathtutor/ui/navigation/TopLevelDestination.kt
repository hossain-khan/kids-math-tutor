package dev.hossain.mathtutor.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.ui.games.GameSelectionScreen
import dev.hossain.mathtutor.ui.home.HomeScreen
import dev.hossain.mathtutor.ui.settings.SettingsScreen
import dev.hossain.mathtutor.ui.stats.StatsScreen

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
     * Stats screen - practice statistics.
     */
    STATS(
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        label = "Stats",
        contentDescription = "Statistics",
        screen = StatsScreen,
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
 * Gets the [TopLevelDestination] for a given [Screen], if any.
 *
 * @return The matching destination, or null if the screen is not a top-level destination.
 */
fun Screen.toTopLevelDestination(): TopLevelDestination? = TopLevelDestination.entries.find { it.screen::class == this::class }

/**
 * Checks if a [Screen] is a top-level destination.
 */
fun Screen.isTopLevelDestination(): Boolean = toTopLevelDestination() != null
