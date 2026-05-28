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
 * Uses Material Theme Builder generated colors optimized for WCAG AAA standards.
 * Meets 4.5:1+ for text, 3:1+ for components with maximum readability.
 */
private val HighContrastDarkColorScheme =
    darkColorScheme(
        primary = primaryDarkHighContrast,
        onPrimary = onPrimaryDarkHighContrast,
        primaryContainer = primaryContainerDarkHighContrast,
        onPrimaryContainer = onPrimaryContainerDarkHighContrast,
        secondary = secondaryDarkHighContrast,
        onSecondary = onSecondaryDarkHighContrast,
        secondaryContainer = secondaryContainerDarkHighContrast,
        onSecondaryContainer = onSecondaryContainerDarkHighContrast,
        tertiary = tertiaryDarkHighContrast,
        onTertiary = onTertiaryDarkHighContrast,
        tertiaryContainer = tertiaryContainerDarkHighContrast,
        onTertiaryContainer = onTertiaryContainerDarkHighContrast,
        error = errorDarkHighContrast,
        onError = onErrorDarkHighContrast,
        errorContainer = errorContainerDarkHighContrast,
        onErrorContainer = onErrorContainerDarkHighContrast,
        background = backgroundDarkHighContrast,
        onBackground = onBackgroundDarkHighContrast,
        surface = surfaceDarkHighContrast,
        onSurface = onSurfaceDarkHighContrast,
        surfaceVariant = surfaceVariantDarkHighContrast,
        onSurfaceVariant = onSurfaceVariantDarkHighContrast,
        outline = outlineDarkHighContrast,
        outlineVariant = outlineVariantDarkHighContrast,
    )

/**
 * High contrast color scheme for accessibility (Light mode variant).
 * Uses Material Theme Builder generated colors optimized for WCAG AAA standards.
 * Meets 4.5:1+ for text, 3:1+ for components with maximum readability.
 */
private val HighContrastLightColorScheme =
    lightColorScheme(
        primary = primaryLightHighContrast,
        onPrimary = onPrimaryLightHighContrast,
        primaryContainer = primaryContainerLightHighContrast,
        onPrimaryContainer = onPrimaryContainerLightHighContrast,
        secondary = secondaryLightHighContrast,
        onSecondary = onSecondaryLightHighContrast,
        secondaryContainer = secondaryContainerLightHighContrast,
        onSecondaryContainer = onSecondaryContainerLightHighContrast,
        tertiary = tertiaryLightHighContrast,
        onTertiary = onTertiaryLightHighContrast,
        tertiaryContainer = tertiaryContainerLightHighContrast,
        onTertiaryContainer = onTertiaryContainerLightHighContrast,
        error = errorLightHighContrast,
        onError = onErrorLightHighContrast,
        errorContainer = errorContainerLightHighContrast,
        onErrorContainer = onErrorContainerLightHighContrast,
        background = backgroundLightHighContrast,
        onBackground = onBackgroundLightHighContrast,
        surface = surfaceLightHighContrast,
        onSurface = onSurfaceLightHighContrast,
        surfaceVariant = surfaceVariantLightHighContrast,
        onSurfaceVariant = onSurfaceVariantLightHighContrast,
        outline = outlineLightHighContrast,
        outlineVariant = outlineVariantLightHighContrast,
    )

/**
 * CompositionLocal for large text scaling factor.
 * Default: 1.0f (normal text size)
 * When enabled: 1.3f (30% larger)
 */
val LocalTextScaleFactor: CompositionLocal<Float> = compositionLocalOf { 1.0f }

private val DarkColorScheme =
    darkColorScheme(
        primary = primaryDark,
        onPrimary = onPrimaryDark,
        primaryContainer = primaryContainerDark,
        onPrimaryContainer = onPrimaryContainerDark,
        secondary = secondaryDark,
        onSecondary = onSecondaryDark,
        secondaryContainer = secondaryContainerDark,
        onSecondaryContainer = onSecondaryContainerDark,
        tertiary = tertiaryDark,
        onTertiary = onTertiaryDark,
        tertiaryContainer = tertiaryContainerDark,
        onTertiaryContainer = onTertiaryContainerDark,
        error = errorDark,
        onError = onErrorDark,
        errorContainer = errorContainerDark,
        onErrorContainer = onErrorContainerDark,
        background = backgroundDark,
        onBackground = onBackgroundDark,
        surface = surfaceDark,
        onSurface = onSurfaceDark,
        surfaceVariant = surfaceVariantDark,
        onSurfaceVariant = onSurfaceVariantDark,
        outline = outlineDark,
        outlineVariant = outlineVariantDark,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = primaryLight,
        onPrimary = onPrimaryLight,
        primaryContainer = primaryContainerLight,
        onPrimaryContainer = onPrimaryContainerLight,
        secondary = secondaryLight,
        onSecondary = onSecondaryLight,
        secondaryContainer = secondaryContainerLight,
        onSecondaryContainer = onSecondaryContainerLight,
        tertiary = tertiaryLight,
        onTertiary = onTertiaryLight,
        tertiaryContainer = tertiaryContainerLight,
        onTertiaryContainer = onTertiaryContainerLight,
        error = errorLight,
        onError = onErrorLight,
        errorContainer = errorContainerLight,
        onErrorContainer = onErrorContainerLight,
        background = backgroundLight,
        onBackground = onBackgroundLight,
        surface = surfaceLight,
        onSurface = onSurfaceLight,
        surfaceVariant = surfaceVariantLight,
        onSurfaceVariant = onSurfaceVariantLight,
        outline = outlineLight,
        outlineVariant = outlineVariantLight,
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
                @Suppress("NewApi")
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
