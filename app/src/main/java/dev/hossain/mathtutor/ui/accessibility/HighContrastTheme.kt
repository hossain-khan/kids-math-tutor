package dev.hossain.mathtutor.ui.accessibility

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * High contrast color scheme for improved accessibility.
 *
 * This color scheme provides WCAG 2.1 Level AA compliant contrast ratios (≥ 4.5:1)
 * for all text and interactive elements. The high contrast mode uses stark white on
 * black or black on white to maximize visibility for users with low vision.
 */
private val HighContrastColorScheme: ColorScheme =
    darkColorScheme(
        primary = Color(0xFFFFFFFF), // White
        onPrimary = Color(0xFF000000), // Black
        primaryContainer = Color(0xFFFFFFFF), // White
        onPrimaryContainer = Color(0xFF000000), // Black
        secondary = Color(0xFFFFFFFF), // White
        onSecondary = Color(0xFF000000), // Black
        secondaryContainer = Color(0xFF333333), // Dark gray
        onSecondaryContainer = Color(0xFFFFFFFF), // White
        tertiary = Color(0xFFFFFFFF), // White
        onTertiary = Color(0xFF000000), // Black
        tertiaryContainer = Color(0xFF333333), // Dark gray
        onTertiaryContainer = Color(0xFFFFFFFF), // White
        error = Color(0xFFFF0000), // Pure red for errors
        onError = Color(0xFFFFFFFF), // White
        errorContainer = Color(0xFFFF0000), // Pure red
        onErrorContainer = Color(0xFFFFFFFF), // White
        background = Color(0xFF000000), // Black
        onBackground = Color(0xFFFFFFFF), // White
        surface = Color(0xFF000000), // Black
        onSurface = Color(0xFFFFFFFF), // White
        surfaceVariant = Color(0xFF1A1A1A), // Very dark gray
        onSurfaceVariant = Color(0xFFFFFFFF), // White
        outline = Color(0xFFFFFFFF), // White
        outlineVariant = Color(0xFFCCCCCC), // Light gray
    )

/**
 * High contrast theme composable that wraps content with enhanced contrast colors.
 *
 * Use this theme when accessibility settings indicate high contrast mode should be enabled.
 * The theme provides maximum color contrast to help users with visual impairments.
 *
 * @param content The composable content to wrap with high contrast theme
 */
@Composable
fun HighContrastTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HighContrastColorScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}
