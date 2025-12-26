package dev.hossain.mathtutor.ui.onboarding

import androidx.compose.ui.graphics.Color

/**
 * Color configuration for onboarding pages.
 * Each page has distinct colors for light and dark modes.
 */
data class OnboardingPageColors(
    // Light mode
    val lightBackgroundColor: Color,
    val lightTextColor: Color,
    val lightButtonColor: Color,
    // Dark mode
    val darkBackgroundColor: Color,
    val darkTextColor: Color,
    val darkButtonColor: Color,
)

/**
 * Onboarding color configurations for each page.
 * Colors are chosen based on the dominant colors shown in each page's onboarding image.
 * Update these values when the onboarding images are changed or color adjustments are needed.
 */
val onboardingPageColorsConfig =
    listOf(
        // Page 1: Welcome
        // Based on: R.drawable.onboarding_1_app_name_welcome
        OnboardingPageColors(
            lightBackgroundColor = Color(0xFFF5E1D0), // Light warm beige
            lightTextColor = Color(0xFF6B4423), // Dark brown
            lightButtonColor = Color(0xFF7A4E2E), // Warm brown
            darkBackgroundColor = Color(0xFF4A2C1A), // Dark brown
            darkTextColor = Color(0xFFF5E1D0), // Light beige
            darkButtonColor = Color(0xFFD4A574), // Light brown
        ),
        // Page 2: Creative Learning (Red theme)
        // Based on: R.drawable.onboarding_2_creative_math_red_theme
        OnboardingPageColors(
            lightBackgroundColor = Color(0xFFFFE5E0), // Light red/pink
            lightTextColor = Color(0xFFC41C3B), // Deep red
            lightButtonColor = Color(0xFFBE2A37), // Bright red
            darkBackgroundColor = Color(0xFF8B1A2E), // Dark red
            darkTextColor = Color(0xFFFFCDD2), // Light pink
            darkButtonColor = Color(0xFFFD7886), // Light red
        ),
        // Page 3: Discover Numbers (Green theme)
        // Based on: R.drawable.onboarding_3_explore_numbers_green_theme
        OnboardingPageColors(
            lightBackgroundColor = Color(0xFFE3F2E0), // Light green
            lightTextColor = Color(0xFF2E7D32), // Dark green
            lightButtonColor = Color(0xFF2A752D), // Medium green
            darkBackgroundColor = Color(0xFF1B5E20), // Dark green
            darkTextColor = Color(0xFFC8E6C9), // Light green
            darkButtonColor = Color(0xFF66BB6A), // Light green
        ),
        // Page 4: Master Math Skills (Blue theme)
        // Based on: R.drawable.onboarding_4_master_math_blue_theme
        OnboardingPageColors(
            lightBackgroundColor = Color(0xFFE3F2FD), // Light blue
            lightTextColor = Color(0xFF1565C0), // Dark blue
            lightButtonColor = Color(0xFF1D6FB0), // Bright blue
            darkBackgroundColor = Color(0xFF0D47A1), // Dark blue
            darkTextColor = Color(0xFFBBDEFB), // Light blue
            darkButtonColor = Color(0xFF64B5F6), // Light blue
        ),
    )
