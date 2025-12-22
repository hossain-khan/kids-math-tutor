package dev.hossain.mathtutor.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.ui.utils.NavigationType

// Width breakpoints for responsive layouts (in dp)
private const val MEDIUM_WIDTH_BREAKPOINT = 600
private const val EXPANDED_WIDTH_BREAKPOINT = 840

/**
 * Adaptive navigation wrapper that switches between different navigation components
 * based on the window size class.
 *
 * - Compact (< 600dp): Bottom navigation bar
 * - Medium (600-840dp): Navigation rail
 * - Expanded (> 840dp): Permanent navigation drawer
 *
 * @param currentScreen The currently displayed screen to highlight the correct destination.
 * @param onDestinationSelected Callback when a navigation destination is selected.
 * @param content The main content of the screen.
 */
@Composable
fun AdaptiveNavigationWrapper(
    currentScreen: Screen?,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val windowSizeClass = adaptiveInfo.windowSizeClass

    // Use breakpoint checks instead of deprecated WindowWidthSizeClass
    val navigationType =
        when {
            windowSizeClass.isWidthAtLeastBreakpoint(EXPANDED_WIDTH_BREAKPOINT) -> {
                NavigationType.PERMANENT_NAVIGATION_DRAWER
            }

            windowSizeClass.isWidthAtLeastBreakpoint(MEDIUM_WIDTH_BREAKPOINT) -> {
                NavigationType.NAVIGATION_RAIL
            }

            else -> {
                NavigationType.BOTTOM_NAVIGATION
            }
        }

    val currentDestination = currentScreen?.toTopLevelDestination()

    when (navigationType) {
        NavigationType.BOTTOM_NAVIGATION -> {
            AppBottomNavigation(
                currentDestination = currentDestination,
                onDestinationSelected = onDestinationSelected,
                modifier = modifier,
                content = content,
            )
        }

        NavigationType.NAVIGATION_RAIL -> {
            AppNavigationRail(
                currentDestination = currentDestination,
                onDestinationSelected = onDestinationSelected,
                modifier = modifier,
                content = content,
            )
        }

        NavigationType.PERMANENT_NAVIGATION_DRAWER -> {
            AppPermanentNavigationDrawer(
                currentDestination = currentDestination,
                onDestinationSelected = onDestinationSelected,
                modifier = modifier,
                content = content,
            )
        }
    }
}

/**
 * Bottom navigation bar for compact (phone) devices.
 */
@Composable
private fun AppBottomNavigation(
    currentDestination: TopLevelDestination?,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    val selected = currentDestination == destination
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onDestinationSelected(destination) },
                        icon = {
                            Icon(
                                imageVector =
                                    if (selected) {
                                        destination.selectedIcon
                                    } else {
                                        destination.unselectedIcon
                                    },
                                contentDescription = destination.contentDescription,
                            )
                        },
                        label = {
                            Text(text = destination.label)
                        },
                    )
                }
            }
        },
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            content()
        }
    }
}

/**
 * Navigation rail for medium (tablet) devices.
 */
@Composable
private fun AppNavigationRail(
    currentDestination: TopLevelDestination?,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(modifier = modifier.fillMaxSize()) {
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            TopLevelDestination.entries.forEach { destination ->
                val selected = currentDestination == destination
                NavigationRailItem(
                    selected = selected,
                    onClick = { onDestinationSelected(destination) },
                    icon = {
                        Icon(
                            imageVector =
                                if (selected) {
                                    destination.selectedIcon
                                } else {
                                    destination.unselectedIcon
                                },
                            contentDescription = destination.contentDescription,
                        )
                    },
                    label = {
                        Text(text = destination.label)
                    },
                )
            }
        }
        // Main content
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            content()
        }
    }
}

/**
 * Permanent navigation drawer for expanded (large tablet/desktop) devices.
 */
@Composable
private fun AppPermanentNavigationDrawer(
    currentDestination: TopLevelDestination?,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier.width(240.dp),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Math Pup Tutor",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                TopLevelDestination.entries.forEach { destination ->
                    val selected = currentDestination == destination
                    NavigationDrawerItem(
                        selected = selected,
                        onClick = { onDestinationSelected(destination) },
                        icon = {
                            Icon(
                                imageVector =
                                    if (selected) {
                                        destination.selectedIcon
                                    } else {
                                        destination.unselectedIcon
                                    },
                                contentDescription = destination.contentDescription,
                            )
                        },
                        label = {
                            Text(text = destination.label)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
            }
        },
        modifier = modifier,
    ) {
        content()
    }
}
