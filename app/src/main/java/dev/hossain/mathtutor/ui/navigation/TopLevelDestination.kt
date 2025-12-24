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
import dev.hossain.mathtutor.ui.theme.customGreenDark
import dev.hossain.mathtutor.ui.theme.customPurpleDark
import dev.hossain.mathtutor.ui.theme.customRaspberryDark
import dev.hossain.mathtutor.ui.theme.customTealDark

/**
 * Color pair for selected navigation items: background color and content color.
 */
data class NavigationItemColors(
    val containerColor: Color,
    val contentColor: Color,
)

// Material Theme Builder custom colors for navigation
private object NavigationColors {
    // Home: Custom Green
    val homeBackground = customGreenDark

    // Games: Custom Raspberry
    val gamesBackground = customRaspberryDark

    // Parents: Custom Teal
    val parentsBackground = customTealDark

    // Settings: Custom Purple
    val settingsBackground = customPurpleDark
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
 * Gets Material Theme Builder custom colors for a destination when selected.
 * Each destination has its own unique custom background color with white text for maximum contrast.
 * Colors are: Home=Green, Games=Raspberry, Parents=Teal, Settings=Purple
 * White text works well on these dark custom backgrounds in both light and dark modes.
 */
@Composable
fun TopLevelDestination.getNavigationItemColors(): NavigationItemColors =
    when (this) {
        TopLevelDestination.HOME -> {
            NavigationItemColors(
                containerColor = NavigationColors.homeBackground,
                contentColor = Color.White,
            )
        }

        TopLevelDestination.GAMES -> {
            NavigationItemColors(
                containerColor = NavigationColors.gamesBackground,
                contentColor = Color.White,
            )
        }

        TopLevelDestination.PARENTS -> {
            NavigationItemColors(
                containerColor = NavigationColors.parentsBackground,
                contentColor = Color.White,
            )
        }

        TopLevelDestination.SETTINGS -> {
            NavigationItemColors(
                containerColor = NavigationColors.settingsBackground,
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
