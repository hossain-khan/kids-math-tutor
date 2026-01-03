package dev.hossain.mathtutor.ui.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized constants for adaptive layouts following Material 3 guidelines.
 *
 * These constants define breakpoints and maximum content widths used throughout the app
 * to ensure consistent responsive behavior across different screen sizes.
 *
 * ## Screen Width Breakpoints
 *
 * The app uses three size classes based on Material 3 guidelines:
 * - **Compact** (< 600dp): Phones in portrait mode
 * - **Medium** (600dp - 840dp): Small tablets, large phones in landscape
 * - **Expanded** (> 840dp): Large tablets, desktops
 *
 * Some screens may use different expanded thresholds (e.g., 1100dp) for additional
 * layout variations on very large screens.
 *
 * ## Maximum Content Width
 *
 * To prevent content from stretching too wide on large screens (which hurts readability),
 * screens constrain content to maximum widths. Different screen types use different
 * maximum widths based on their content density:
 *
 * - Narrow content (forms, practice screens): 500-700dp
 * - Standard content (home, settings): 700-840dp
 * - Wide content (debug, developer tools): 900-1000dp
 *
 * ## Usage
 *
 * ```kotlin
 * BoxWithConstraints {
 *     val isWideScreen = maxWidth >= AdaptiveLayoutConstants.MEDIUM_WIDTH_BREAKPOINT
 *     val isExpandedScreen = maxWidth >= AdaptiveLayoutConstants.EXPANDED_WIDTH_BREAKPOINT
 *
 *     Box(
 *         modifier = Modifier.widthIn(max = AdaptiveLayoutConstants.MAX_CONTENT_WIDTH_STANDARD)
 *     ) {
 *         // Content
 *     }
 * }
 * ```
 *
 * @see <a href="https://m3.material.io/foundations/layout/applying-layout/window-size-classes">Material 3 Window Size Classes</a>
 */
object AdaptiveLayoutConstants {
    // ========================================================================================
    // Screen Width Breakpoints
    // ========================================================================================

    /**
     * Breakpoint between compact and medium screen widths.
     *
     * - Below 600dp: Compact (phones)
     * - 600dp and above: Medium (tablets, large phones in landscape)
     *
     * At this breakpoint, the app typically:
     * - Switches from bottom navigation to navigation rail
     * - Increases grid column counts
     * - Adds more spacing between elements
     */
    val MEDIUM_WIDTH_BREAKPOINT: Dp = 600.dp

    /**
     * Breakpoint for medium width (same as [MEDIUM_WIDTH_BREAKPOINT]).
     *
     * Alias provided for clarity when comparing against compact screens.
     * Screens below this width are considered "compact".
     */
    val COMPACT_WIDTH_BREAKPOINT: Dp = 600.dp

    /**
     * Breakpoint between medium and expanded screen widths.
     *
     * - Below 840dp: Medium (tablets)
     * - 840dp and above: Expanded (large tablets, desktops)
     *
     * At this breakpoint, the app typically:
     * - Switches from navigation rail to permanent navigation drawer
     * - May show dual-pane layouts
     * - Further increases grid column counts
     * - Centers content with maximum width constraints
     */
    val EXPANDED_WIDTH_BREAKPOINT: Dp = 840.dp

    /**
     * Extended breakpoint for very large screens (e.g., desktop monitors).
     *
     * Used by screens that need an additional layout tier beyond the standard
     * expanded breakpoint, such as StatsScreen which shows more detailed
     * statistics side-by-side on very wide screens.
     */
    val EXTENDED_WIDTH_BREAKPOINT: Dp = 1100.dp

    // ========================================================================================
    // Integer Breakpoints (for WindowSizeClass.isWidthAtLeastBreakpoint)
    // ========================================================================================

    /**
     * Medium width breakpoint as Int (for use with WindowSizeClass.isWidthAtLeastBreakpoint).
     *
     * @see MEDIUM_WIDTH_BREAKPOINT
     */
    const val MEDIUM_WIDTH_BREAKPOINT_INT: Int = 600

    /**
     * Expanded width breakpoint as Int (for use with WindowSizeClass.isWidthAtLeastBreakpoint).
     *
     * @see EXPANDED_WIDTH_BREAKPOINT
     */
    const val EXPANDED_WIDTH_BREAKPOINT_INT: Int = 840

    // ========================================================================================
    // Maximum Content Widths
    // ========================================================================================

    /**
     * Narrow maximum content width for focused content screens.
     *
     * Used for screens that display focused, single-task content like:
     * - Math practice problems
     * - Simple forms
     *
     * Keeping content narrow improves focus and reduces eye movement.
     */
    val MAX_CONTENT_WIDTH_NARROW: Dp = 500.dp

    /**
     * Small maximum content width for form-like screens.
     *
     * Used for screens with moderate content density like:
     * - Name entry
     * - Operation selection
     * - Accuracy details
     * - Game screens (start, game, results)
     * - Memory match
     */
    val MAX_CONTENT_WIDTH_SMALL: Dp = 700.dp

    /**
     * Standard maximum content width for typical screens.
     *
     * Used for screens with standard content density like:
     * - Grade selection
     * - Game selection
     * - Import challenge
     * - Settings screens (varies by screen width tier)
     */
    val MAX_CONTENT_WIDTH_STANDARD: Dp = 800.dp

    /**
     * Medium maximum content width for content-rich screens.
     *
     * Used for screens that benefit from slightly more width like:
     * - Home screen
     * - Stats screen
     * - Badges screen
     */
    val MAX_CONTENT_WIDTH_MEDIUM: Dp = 840.dp

    /**
     * Large maximum content width for wide content screens.
     *
     * Used for screens with wide content like:
     * - Onboarding carousel
     * - Developer portal
     * - Parent challenges list
     * - Results screen grid
     */
    val MAX_CONTENT_WIDTH_LARGE: Dp = 900.dp

    /**
     * Extra large maximum content width for very wide content.
     *
     * Used for screens that display lots of information like:
     * - Color palette viewer (debug screen)
     */
    val MAX_CONTENT_WIDTH_EXTRA_LARGE: Dp = 1000.dp

    // ========================================================================================
    // Settings-Specific Content Widths
    // ========================================================================================

    /**
     * Maximum content width for settings on compact screens.
     *
     * Settings use adaptive content widths that scale with screen size
     * to maintain good readability across all device sizes.
     */
    val SETTINGS_MAX_WIDTH_COMPACT: Dp = 600.dp

    /**
     * Maximum content width for settings on medium screens.
     */
    val SETTINGS_MAX_WIDTH_MEDIUM: Dp = 700.dp

    /**
     * Maximum content width for settings on expanded screens.
     */
    val SETTINGS_MAX_WIDTH_EXPANDED: Dp = 800.dp

    // ========================================================================================
    // Results Screen Specific Widths
    // ========================================================================================

    /**
     * Maximum width for results summary on compact screens.
     */
    val RESULTS_SUMMARY_WIDTH_COMPACT: Dp = 700.dp

    /**
     * Maximum width for results summary on expanded screens.
     */
    val RESULTS_SUMMARY_WIDTH_EXPANDED: Dp = 800.dp

    /**
     * Maximum width for results grid display.
     */
    val RESULTS_GRID_MAX_WIDTH: Dp = 900.dp
}
