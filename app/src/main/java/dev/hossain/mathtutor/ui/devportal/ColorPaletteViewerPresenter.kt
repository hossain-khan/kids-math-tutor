package dev.hossain.mathtutor.ui.devportal

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.ui.component.TopBarFeatureColors
import dev.hossain.mathtutor.ui.theme.DarkBackground
import dev.hossain.mathtutor.ui.theme.DarkError
import dev.hossain.mathtutor.ui.theme.DarkOnBackground
import dev.hossain.mathtutor.ui.theme.DarkOnError
import dev.hossain.mathtutor.ui.theme.DarkOnPrimary
import dev.hossain.mathtutor.ui.theme.DarkOnSecondary
import dev.hossain.mathtutor.ui.theme.DarkOnSurface
import dev.hossain.mathtutor.ui.theme.DarkOnTertiary
import dev.hossain.mathtutor.ui.theme.DarkPrimary
import dev.hossain.mathtutor.ui.theme.DarkSecondary
import dev.hossain.mathtutor.ui.theme.DarkSurface
import dev.hossain.mathtutor.ui.theme.DarkTertiary
import dev.hossain.mathtutor.ui.theme.LightBackground
import dev.hossain.mathtutor.ui.theme.LightError
import dev.hossain.mathtutor.ui.theme.LightOnBackground
import dev.hossain.mathtutor.ui.theme.LightOnError
import dev.hossain.mathtutor.ui.theme.LightOnPrimary
import dev.hossain.mathtutor.ui.theme.LightOnSecondary
import dev.hossain.mathtutor.ui.theme.LightOnSurface
import dev.hossain.mathtutor.ui.theme.LightOnTertiary
import dev.hossain.mathtutor.ui.theme.LightPrimary
import dev.hossain.mathtutor.ui.theme.LightSecondary
import dev.hossain.mathtutor.ui.theme.LightSurface
import dev.hossain.mathtutor.ui.theme.LightTertiary
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

            val colorGroups = buildColorGroups()

            return ColorPaletteViewerScreen.State(
                colorGroups = colorGroups,
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
         * Builds color groups organized by UI component category.
         */
        private fun buildColorGroups(): List<ColorPaletteViewerScreen.ColorGroup> =
            listOf(
                buildNavigationColorsGroup(),
                buildTopAppBarColorsGroup(),
                buildThemeColorsGroup(),
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
                            color = LightPrimary,
                            usage = "Primary interactive elements in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Primary - Light",
                            color = LightOnPrimary,
                            usage = "Content on primary color in light mode",
                        ),
                        // Light Mode - Secondary
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Secondary - Light",
                            color = LightSecondary,
                            usage = "Secondary interactive elements in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Secondary - Light",
                            color = LightOnSecondary,
                            usage = "Content on secondary color in light mode",
                        ),
                        // Light Mode - Tertiary
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Tertiary - Light",
                            color = LightTertiary,
                            usage = "Tertiary interactive elements in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Tertiary - Light",
                            color = LightOnTertiary,
                            usage = "Content on tertiary color in light mode",
                        ),
                        // Light Mode - Error
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Error - Light",
                            color = LightError,
                            usage = "Error and warning states in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Error - Light",
                            color = LightOnError,
                            usage = "Content on error color in light mode",
                        ),
                        // Light Mode - Surface
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Surface - Light",
                            color = LightSurface,
                            usage = "Surface backgrounds in light mode",
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Surface - Light",
                            color = LightOnSurface,
                            usage = "Text and content on surface in light mode",
                        ),
                        // Dark Mode - Primary
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Primary - Dark",
                            color = DarkPrimary,
                            usage = "Primary interactive elements in dark mode",
                            isDarkModeVariant = true,
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Primary - Dark",
                            color = DarkOnPrimary,
                            usage = "Content on primary color in dark mode",
                            isDarkModeVariant = true,
                        ),
                        // Dark Mode - Secondary
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Secondary - Dark",
                            color = DarkSecondary,
                            usage = "Secondary interactive elements in dark mode",
                            isDarkModeVariant = true,
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Secondary - Dark",
                            color = DarkOnSecondary,
                            usage = "Content on secondary color in dark mode",
                            isDarkModeVariant = true,
                        ),
                        // Dark Mode - Tertiary
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Tertiary - Dark",
                            color = DarkTertiary,
                            usage = "Tertiary interactive elements in dark mode",
                            isDarkModeVariant = true,
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Tertiary - Dark",
                            color = DarkOnTertiary,
                            usage = "Content on tertiary color in dark mode",
                            isDarkModeVariant = true,
                        ),
                        // Dark Mode - Error
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Error - Dark",
                            color = DarkError,
                            usage = "Error and warning states in dark mode",
                            isDarkModeVariant = true,
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Error - Dark",
                            color = DarkOnError,
                            usage = "Content on error color in dark mode",
                            isDarkModeVariant = true,
                        ),
                        // Dark Mode - Surface
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "Surface - Dark",
                            color = DarkSurface,
                            usage = "Surface backgrounds in dark mode",
                            isDarkModeVariant = true,
                        ),
                        ColorPaletteViewerScreen.ColorEntry(
                            name = "On Surface - Dark",
                            color = DarkOnSurface,
                            usage = "Text and content on surface in dark mode",
                            isDarkModeVariant = true,
                        ),
                    ),
            )
    }
