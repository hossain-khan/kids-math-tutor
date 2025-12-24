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
 * High contrast color scheme for accessibility.
 * Uses maximum contrast colors (black on white / white on black).
 */
private val HighContrastDarkColorScheme =
    darkColorScheme(
        primary = Color.White,
        onPrimary = Color.Black,
        primaryContainer = Color.White,
        onPrimaryContainer = Color.Black,
        secondary = Color.White,
        onSecondary = Color.Black,
        secondaryContainer = Color.White,
        onSecondaryContainer = Color.Black,
        tertiary = Color.White,
        onTertiary = Color.Black,
        tertiaryContainer = Color.White,
        onTertiaryContainer = Color.Black,
        error = Color(0xFFFF0000),
        onError = Color.White,
        errorContainer = Color(0xFFFF0000),
        onErrorContainer = Color.White,
        background = Color.Black,
        onBackground = Color.White,
        surface = Color.Black,
        onSurface = Color.White,
        surfaceVariant = Color.Black,
        onSurfaceVariant = Color.White,
        outline = Color.White,
        outlineVariant = Color.White,
    )

private val HighContrastLightColorScheme =
    lightColorScheme(
        primary = Color.Black,
        onPrimary = Color.White,
        primaryContainer = Color.Black,
        onPrimaryContainer = Color.White,
        secondary = Color.Black,
        onSecondary = Color.White,
        secondaryContainer = Color.Black,
        onSecondaryContainer = Color.White,
        tertiary = Color.Black,
        onTertiary = Color.White,
        tertiaryContainer = Color.Black,
        onTertiaryContainer = Color.White,
        error = Color(0xFFFF0000),
        onError = Color.White,
        errorContainer = Color(0xFFFF0000),
        onErrorContainer = Color.White,
        background = Color.White,
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black,
        surfaceVariant = Color.White,
        onSurfaceVariant = Color.Black,
        outline = Color.Black,
        outlineVariant = Color.Black,
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
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
