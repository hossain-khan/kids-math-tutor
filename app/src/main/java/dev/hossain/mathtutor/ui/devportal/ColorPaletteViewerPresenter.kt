package dev.hossain.mathtutor.ui.devportal

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.ui.component.TopBarFeatureColors
import dev.hossain.mathtutor.ui.theme.backgroundDark
import dev.hossain.mathtutor.ui.theme.backgroundLight
import dev.hossain.mathtutor.ui.theme.errorDark
import dev.hossain.mathtutor.ui.theme.errorLight
import dev.hossain.mathtutor.ui.theme.onBackgroundDark
import dev.hossain.mathtutor.ui.theme.onBackgroundLight
import dev.hossain.mathtutor.ui.theme.onErrorDark
import dev.hossain.mathtutor.ui.theme.onErrorLight
import dev.hossain.mathtutor.ui.theme.onPrimaryDark
import dev.hossain.mathtutor.ui.theme.onPrimaryLight
import dev.hossain.mathtutor.ui.theme.onSecondaryDark
import dev.hossain.mathtutor.ui.theme.onSecondaryLight
import dev.hossain.mathtutor.ui.theme.onSurfaceDark
import dev.hossain.mathtutor.ui.theme.onSurfaceLight
import dev.hossain.mathtutor.ui.theme.onTertiaryDark
import dev.hossain.mathtutor.ui.theme.onTertiaryLight
import dev.hossain.mathtutor.ui.theme.primaryDark
import dev.hossain.mathtutor.ui.theme.primaryLight
import dev.hossain.mathtutor.ui.theme.secondaryDark
import dev.hossain.mathtutor.ui.theme.secondaryLight
import dev.hossain.mathtutor.ui.theme.surfaceDark
import dev.hossain.mathtutor.ui.theme.surfaceLight
import dev.hossain.mathtutor.ui.theme.tertiaryDark
import dev.hossain.mathtutor.ui.theme.tertiaryLight
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import timber.log.Timber

/**
 * Presenter for [ColorPaletteViewerScreen].
 *
 * Manages color palette data and organizes colors by UI component categories.
 * Builds color groups for:
 * - Navigation Colors: Bottom navigation bar colors
 * - TopAppBar Colors: Feature-based colors with light/dark variants
 * - Theme Colors: Material 3 semantic colors
 */
@AssistedInject
class ColorPaletteViewerPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val analyticsService: AnalyticsService,
    ) : Presenter<ColorPaletteViewerScreen.State> {
        @CircuitInject(ColorPaletteViewerScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): ColorPaletteViewerPresenter
        }

        @Composable
        override fun present(): ColorPaletteViewerScreen.State {
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Color Palette Viewer",
                    screenClass = ColorPaletteViewerScreen::class.java.name,
                )
            }

            val groups = buildGroups()

            return ColorPaletteViewerScreen.State(
                groups = groups,
            ) { event ->
                when (event) {
                    is ColorPaletteViewerScreen.Event.BackClicked -> {
                        Timber.d("ColorPaletteViewer: Back clicked")
                        navigator.pop()
                    }

                    is ColorPaletteViewerScreen.Event.CopyColorToClipboard -> {
                        Timber.d("ColorPaletteViewer: Copy color to clipboard")
                        // TODO: Implement clipboard copy functionality
                    }
                }
            }
        }

        /**
         * Builds all groups (colors and widgets) organized by category.
         */
        private fun buildGroups(): List<ColorPaletteViewerScreen.GroupContent> =
            listOf(
                buildNavigationColorsGroup(),
                buildTopAppBarColorsGroup(),
                buildThemeColorsGroup(),
                buildWidgetDemoGroup(),
            )

        /**
         * Builds the Navigation Colors group showing bottom nav bar colors.
         */
        private fun buildNavigationColorsGroup(): ColorPaletteViewerScreen.ColorGroup =
            ColorPaletteViewerScreen.ColorGroup(
                title = "Navigation Colors",
                description = "Bottom navigation bar colors",
                colors =
                    listOf(
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "HOME",
                            color = Color(0xFF2E7D32),
                            usage = "Home navigation button",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "GAMES",
                            color = Color(0xFFE53935),
                            usage = "Games navigation button",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "PARENTS",
                            color = Color(0xFF1976D2),
                            usage = "Parents/Stats navigation button",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "SETTINGS",
                            color = Color(0xFF7B1FA2),
                            usage = "Settings navigation button",
                        ),
                    ),
            )

        /**
         * Builds the TopAppBar Colors group with light and dark variants.
         */
        private fun buildTopAppBarColorsGroup(): ColorPaletteViewerScreen.ColorGroup =
            ColorPaletteViewerScreen.ColorGroup(
                title = "TopAppBar Colors",
                description = "Feature-based top app bar colors with light/dark theme support",
                colors =
                    listOf(
                        // Red - PRACTICE & GAMES
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "PRACTICE & GAMES - Light Mode",
                            color = TopBarFeatureColors.redAccentLight,
                            usage = "Practice and Games screens in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "PRACTICE & GAMES - Dark Mode",
                            color = TopBarFeatureColors.redAccentDark,
                            usage = "Practice and Games screens in dark mode",
                            isDarkModeVariant = true,
                        ),
                        // Blue - STATS
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "STATS - Light Mode",
                            color = TopBarFeatureColors.blueAccentLight,
                            usage = "Stats/Parents screen in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "STATS - Dark Mode",
                            color = TopBarFeatureColors.blueAccentDark,
                            usage = "Stats/Parents screen in dark mode",
                            isDarkModeVariant = true,
                        ),
                        // Green - BADGES
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "BADGES - Light Mode",
                            color = TopBarFeatureColors.greenAccentLight,
                            usage = "Badges/Home screen in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "BADGES - Dark Mode",
                            color = TopBarFeatureColors.greenAccentDark,
                            usage = "Badges/Home screen in dark mode",
                            isDarkModeVariant = true,
                        ),
                        // Purple - SETTINGS
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "SETTINGS - Light Mode",
                            color = TopBarFeatureColors.purpleAccentLight,
                            usage = "Settings screen in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "SETTINGS - Dark Mode",
                            color = TopBarFeatureColors.purpleAccentDark,
                            usage = "Settings screen in dark mode",
                            isDarkModeVariant = true,
                        ),
                    ),
            )

        /**
         * Builds the Theme Colors group with Material 3 semantic colors for light and dark modes.
         */
        private fun buildThemeColorsGroup(): ColorPaletteViewerScreen.ColorGroup =
            ColorPaletteViewerScreen.ColorGroup(
                title = "Theme Colors",
                description = "Material 3 semantic colors for light and dark modes",
                colors =
                    listOf(
                        // Light Mode - Primary
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Primary - Light",
                            color = primaryLight,
                            usage = "Primary interactive elements in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Primary - Light",
                            color = onPrimaryLight,
                            usage = "Content on primary color in light mode",
                        ),
                        // Light Mode - Secondary
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Secondary - Light",
                            color = secondaryLight,
                            usage = "Secondary interactive elements in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Secondary - Light",
                            color = onSecondaryLight,
                            usage = "Content on secondary color in light mode",
                        ),
                        // Light Mode - Tertiary
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Tertiary - Light",
                            color = tertiaryLight,
                            usage = "Tertiary interactive elements in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Tertiary - Light",
                            color = onTertiaryLight,
                            usage = "Content on tertiary color in light mode",
                        ),
                        // Light Mode - Error
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Error - Light",
                            color = errorLight,
                            usage = "Error and warning states in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Error - Light",
                            color = onErrorLight,
                            usage = "Content on error color in light mode",
                        ),
                        // Light Mode - Surface
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Surface - Light",
                            color = surfaceLight,
                            usage = "Surface backgrounds in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Surface - Light",
                            color = onSurfaceLight,
                            usage = "Text and content on surface in light mode",
                        ),
                        // Dark Mode - Primary
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Primary - Dark",
                            color = primaryDark,
                            usage = "Primary interactive elements in dark mode",
                            isDarkModeVariant = true,
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Primary - Dark",
                            color = onPrimaryDark,
                            usage = "Content on primary color in dark mode",
                            isDarkModeVariant = true,
                        ),
                        // Dark Mode - Secondary
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Secondary - Dark",
                            color = secondaryDark,
                            usage = "Secondary interactive elements in dark mode",
                            isDarkModeVariant = true,
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Secondary - Dark",
                            color = onSecondaryDark,
                            usage = "Content on secondary color in dark mode",
                            isDarkModeVariant = true,
                        ),
                        // Dark Mode - Tertiary
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Tertiary - Dark",
                            color = tertiaryDark,
                            usage = "Tertiary interactive elements in dark mode",
                            isDarkModeVariant = true,
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Tertiary - Dark",
                            color = onTertiaryDark,
                            usage = "Content on tertiary color in dark mode",
                            isDarkModeVariant = true,
                        ),
                        // Dark Mode - Error
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Error - Dark",
                            color = errorDark,
                            usage = "Error and warning states in dark mode",
                            isDarkModeVariant = true,
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Error - Dark",
                            color = onErrorDark,
                            usage = "Content on error color in dark mode",
                            isDarkModeVariant = true,
                        ),
                        // Dark Mode - Surface
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Surface - Dark",
                            color = surfaceDark,
                            usage = "Surface backgrounds in dark mode",
                            isDarkModeVariant = true,
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Surface - Dark",
                            color = onSurfaceDark,
                            usage = "Text and content on surface in dark mode",
                            isDarkModeVariant = true,
                        ),
                    ),
            )

        /**
         * Builds the Widget Demo group showcasing Material 3 components in the app.
         */
        private fun buildWidgetDemoGroup(): ColorPaletteViewerScreen.WidgetDemoGroup =
            ColorPaletteViewerScreen.WidgetDemoGroup(
                title = "Widget Demos",
                description = "Live Material 3 components showcasing the theme in action",
            )
    }
