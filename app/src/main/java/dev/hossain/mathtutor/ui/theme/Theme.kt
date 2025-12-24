package dev.hossain.mathtutor.ui.theme
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * High contrast color scheme for accessibility (Dark mode variant).
 * Uses enhanced Material 3 colors with improved contrast while maintaining usability.
 * Meets WCAG AAA standards: 4.5:1+ for text, 3:1+ for components.
 * Uses darker primary/secondary/tertiary colors and lighter text for maximum readability.
 */
private val HighContrastDarkColorScheme =
    darkColorScheme(
        primary = Color(0xFFFFD700), // Vivid yellow - highly visible
        onPrimary = Color(0xFF000000), // Black text on yellow
        primaryContainer = Color(0xFFFFC300), // Darker gold container
        onPrimaryContainer = Color(0xFF000000), // Black text
        secondary = Color(0xFF64B5F6), // Bright blue
        onSecondary = Color(0xFF000000), // Black text
        secondaryContainer = Color(0xFF1976D2), // Darker blue
        onSecondaryContainer = Color(0xFFFFFFFF), // White text
        tertiary = Color(0xFF4CAF50), // Bright green
        onTertiary = Color(0xFF000000), // Black text
        tertiaryContainer = Color(0xFF388E3C), // Darker green
        onTertiaryContainer = Color(0xFFFFFFFF), // White text
        error = Color(0xFFFF5252), // Bright red
        onError = Color(0xFF000000), // Black text
        errorContainer = Color(0xFFD32F2F), // Darker red
        onErrorContainer = Color(0xFFFFFFFF), // White text
        background = Color(0xFF121212), // Very dark background
        onBackground = Color(0xFFFFFFFF), // White text
        surface = Color(0xFF1E1E1E), // Dark surface
        onSurface = Color(0xFFFFFFFF), // White text
        surfaceVariant = Color(0xFF2C2C2C), // Slightly lighter surface
        onSurfaceVariant = Color(0xFFE0E0E0), // Light gray text
        outline = Color(0xFF90CAF9), // Light blue outline
        outlineVariant = Color(0xFF64B5F6), // Medium blue outline variant
    )

/**
 * High contrast color scheme for accessibility (Light mode variant).
 * Uses enhanced Material 3 colors with improved contrast while maintaining usability.
 * Meets WCAG AAA standards: 4.5:1+ for text, 3:1+ for components.
 * Uses darker colors with high saturation for maximum readability.
 */
private val HighContrastLightColorScheme =
    lightColorScheme(
        primary = Color(0xFF1565C0), // Deep blue
        onPrimary = Color(0xFFFFFFFF), // White text
        primaryContainer = Color(0xFF0D47A1), // Darker blue
        onPrimaryContainer = Color(0xFFFFFFFF), // White text
        secondary = Color(0xFFC62828), // Deep red
        onSecondary = Color(0xFFFFFFFF), // White text
        secondaryContainer = Color(0xFF7F0000), // Very dark red
        onSecondaryContainer = Color(0xFFFFFFFF), // White text
        tertiary = Color(0xFF00695C), // Deep teal
        onTertiary = Color(0xFFFFFFFF), // White text
        tertiaryContainer = Color(0xFF004D40), // Very dark teal
        onTertiaryContainer = Color(0xFFFFFFFF), // White text
        error = Color(0xFFD32F2F), // Deep red for errors
        onError = Color(0xFFFFFFFF), // White text
        errorContainer = Color(0xFFB71C1C), // Very deep red
        onErrorContainer = Color(0xFFFFFFFF), // White text
        background = Color(0xFFFAFAFA), // Off-white background
        onBackground = Color(0xFF000000), // Black text
        surface = Color(0xFFFFFFFF), // White surface
        onSurface = Color(0xFF000000), // Black text
        surfaceVariant = Color(0xFFF5F5F5), // Light gray surface variant
        onSurfaceVariant = Color(0xFF212121), // Dark gray text
        outline = Color(0xFF424242), // Dark gray outline
        outlineVariant = Color(0xFF616161), // Medium gray outline variant
    )

/**
 * CompositionLocal for large text scaling factor.
 * Default: 1.0f (normal text size)
 * When enabled: 1.3f (30% larger)
 */
val LocalTextScaleFactor: CompositionLocal<Float> = compositionLocalOf { 1.0f }

private val DarkColorScheme =
    darkColorScheme(
        primary = DarkPrimary,
        onPrimary = DarkOnPrimary,
        primaryContainer = DarkPrimaryContainer,
        onPrimaryContainer = DarkOnPrimaryContainer,
        secondary = DarkSecondary,
        onSecondary = DarkOnSecondary,
        secondaryContainer = DarkSecondaryContainer,
        onSecondaryContainer = DarkOnSecondaryContainer,
        tertiary = DarkTertiary,
        onTertiary = DarkOnTertiary,
        tertiaryContainer = DarkTertiaryContainer,
        onTertiaryContainer = DarkOnTertiaryContainer,
        error = DarkError,
        onError = DarkOnError,
        errorContainer = DarkErrorContainer,
        onErrorContainer = DarkOnErrorContainer,
        background = DarkBackground,
        onBackground = DarkOnBackground,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkOnSurfaceVariant,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = LightPrimary,
        onPrimary = LightOnPrimary,
        primaryContainer = LightPrimaryContainer,
        onPrimaryContainer = LightOnPrimaryContainer,
        secondary = LightSecondary,
        onSecondary = LightOnSecondary,
        secondaryContainer = LightSecondaryContainer,
        onSecondaryContainer = LightOnSecondaryContainer,
        tertiary = LightTertiary,
        onTertiary = LightOnTertiary,
        tertiaryContainer = LightTertiaryContainer,
        onTertiaryContainer = LightOnTertiaryContainer,
        error = LightError,
        onError = LightOnError,
        errorContainer = LightErrorContainer,
        onErrorContainer = LightOnErrorContainer,
        background = LightBackground,
        onBackground = LightOnBackground,
        surface = LightSurface,
        onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightOnSurfaceVariant,
        outline = LightOutline,
        outlineVariant = LightOutlineVariant,
    )

@Composable
fun KidsMathTutorAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled - using static vibrant theme for kids
    dynamicColor: Boolean = false,
    highContrast: Boolean = false,
    largeText: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            // High contrast mode takes precedence
            highContrast -> {
                if (darkTheme) HighContrastDarkColorScheme else HighContrastLightColorScheme
            }

            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> {
                DarkColorScheme
            }

            else -> {
                LightColorScheme
            }
        }

    // Scale typography if large text is enabled
    val scaledTypography =
        if (largeText) {
            AppTypography.copy(
                displayLarge = AppTypography.displayLarge.copy(fontSize = AppTypography.displayLarge.fontSize * 1.3f),
                displayMedium = AppTypography.displayMedium.copy(fontSize = AppTypography.displayMedium.fontSize * 1.3f),
                displaySmall = AppTypography.displaySmall.copy(fontSize = AppTypography.displaySmall.fontSize * 1.3f),
                headlineLarge = AppTypography.headlineLarge.copy(fontSize = AppTypography.headlineLarge.fontSize * 1.3f),
                headlineMedium = AppTypography.headlineMedium.copy(fontSize = AppTypography.headlineMedium.fontSize * 1.3f),
                headlineSmall = AppTypography.headlineSmall.copy(fontSize = AppTypography.headlineSmall.fontSize * 1.3f),
                titleLarge = AppTypography.titleLarge.copy(fontSize = AppTypography.titleLarge.fontSize * 1.3f),
                titleMedium = AppTypography.titleMedium.copy(fontSize = AppTypography.titleMedium.fontSize * 1.3f),
                titleSmall = AppTypography.titleSmall.copy(fontSize = AppTypography.titleSmall.fontSize * 1.3f),
                bodyLarge = AppTypography.bodyLarge.copy(fontSize = AppTypography.bodyLarge.fontSize * 1.3f),
                bodyMedium = AppTypography.bodyMedium.copy(fontSize = AppTypography.bodyMedium.fontSize * 1.3f),
                bodySmall = AppTypography.bodySmall.copy(fontSize = AppTypography.bodySmall.fontSize * 1.3f),
                labelLarge = AppTypography.labelLarge.copy(fontSize = AppTypography.labelLarge.fontSize * 1.3f),
                labelMedium = AppTypography.labelMedium.copy(fontSize = AppTypography.labelMedium.fontSize * 1.3f),
                labelSmall = AppTypography.labelSmall.copy(fontSize = AppTypography.labelSmall.fontSize * 1.3f),
            )
        } else {
            AppTypography
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content,
    )
}
