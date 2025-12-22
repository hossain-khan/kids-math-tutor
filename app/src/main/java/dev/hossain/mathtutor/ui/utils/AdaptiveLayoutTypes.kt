package dev.hossain.mathtutor.ui.utils

/**
 * Different types of navigation supported by the app depending on device size and state.
 *
 * Based on the Reply sample from Google's compose-samples.
 */
enum class NavigationType {
    /**
     * Bottom navigation bar - used for compact (phone) devices.
     * Shows up to 5 destinations at the bottom of the screen.
     */
    BOTTOM_NAVIGATION,

    /**
     * Navigation rail - used for medium (tablet) devices.
     * Shows destinations on the side with icons and optional labels.
     */
    NAVIGATION_RAIL,

    /**
     * Permanent navigation drawer - used for expanded (large tablet/desktop) devices.
     * Always visible sidebar with full labels.
     */
    PERMANENT_NAVIGATION_DRAWER,
}

/**
 * Different types of content layouts based on device size.
 */
enum class ContentType {
    /**
     * Single pane content - used for compact devices.
     * Shows one screen at a time.
     */
    SINGLE_PANE,

    /**
     * Dual pane content - used for expanded devices.
     * Can show list + detail side by side.
     */
    DUAL_PANE,
}

/**
 * Different positions for navigation content inside Navigation Rail/Drawer.
 */
enum class NavigationContentPosition {
    /**
     * Content positioned at the top.
     */
    TOP,

    /**
     * Content positioned at the center.
     */
    CENTER,
}
