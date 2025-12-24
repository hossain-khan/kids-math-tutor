package dev.hossain.mathtutor.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import kotlin.math.roundToInt

/**
 * UI for [AudioHapticSettingsScreen].
 *
 * Displays audio and haptic feedback settings with:
 * - Sound effects toggle
 * - Volume slider (disabled when both sound and music off)
 * - Background music toggle
 * - Haptic feedback toggle
 * - Accessibility settings (high contrast, large text)
 */
@CircuitInject(AudioHapticSettingsScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioHapticSettingsUi(
    state: AudioHapticSettingsScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Audio & Haptics")
                },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(AudioHapticSettingsScreen.Event.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                        )
                    }
                },
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Device audio suppression warning (only show if device is in silent/vibrate mode)
            if (state.isDeviceAudioSuppressed) {
                DeviceAudioSuppressionWarning()
            }

            // Sound Effects section
            SoundEffectsSection(
                enabled = state.soundEffectsEnabled,
                onToggle = { enabled ->
                    state.eventSink(AudioHapticSettingsScreen.Event.ToggleSoundEffects(enabled))
                },
            )

            // Volume slider
            VolumeSection(
                volume = state.volume,
                enabled = state.soundEffectsEnabled || state.backgroundMusicEnabled,
                onVolumeChange = { volume ->
                    state.eventSink(AudioHapticSettingsScreen.Event.SetVolume(volume))
                },
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Background Music section
            BackgroundMusicSection(
                enabled = state.backgroundMusicEnabled,
                onToggle = { enabled ->
                    state.eventSink(AudioHapticSettingsScreen.Event.ToggleBackgroundMusic(enabled))
                },
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Haptic Feedback section
            HapticFeedbackSection(
                enabled = state.hapticsEnabled,
                onToggle = { enabled ->
                    state.eventSink(AudioHapticSettingsScreen.Event.ToggleHaptics(enabled))
                },
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Accessibility section
            AccessibilitySection(
                highContrastEnabled = state.highContrastEnabled,
                largeTextEnabled = state.largeTextEnabled,
                onToggleHighContrast = { enabled ->
                    state.eventSink(AudioHapticSettingsScreen.Event.ToggleHighContrast(enabled))
                },
                onToggleLargeText = { enabled ->
                    state.eventSink(AudioHapticSettingsScreen.Event.ToggleLargeText(enabled))
                },
            )
        }
    }
}

/**
 * Device audio suppression warning card.
 * Displayed when device is in silent or vibrate mode.
 */
@Composable
private fun DeviceAudioSuppressionWarning(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "🔕 Device Audio Suppressed",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )

            Text(
                text =
                    "Your device is in silent or vibrate mode. Audio from this app will not play. " +
                        "Change your device's ringer mode to normal to hear sounds.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

/**
 * Sound Effects section with toggle switch.
 */
@Composable
private fun SoundEffectsSection(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Sound Effects",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Enable sound effects",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                )
            }
        }
    }
}

/**
 * Volume slider section.
 */
@Composable
private fun VolumeSection(
    volume: Float,
    enabled: Boolean,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Volume",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "${(volume * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                enabled = enabled,
                valueRange = 0f..1f,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription =
                                if (enabled) {
                                    "Volume slider, current volume ${(volume * 100).roundToInt()} percent"
                                } else {
                                    "Volume slider disabled, enable sound effects or music to adjust volume"
                                }
                        },
            )

            if (!enabled) {
                Text(
                    text = "Enable sound effects or music to adjust volume",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                )
            }
        }
    }
}

/**
 * Background Music section with toggle switch.
 */
@Composable
private fun BackgroundMusicSection(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Background Music",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Play background music",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                )
            }
        }
    }
}

/**
 * Haptic Feedback section with toggle switch.
 */
@Composable
private fun HapticFeedbackSection(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Haptic Feedback",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Vibrate on interactions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                )
            }
        }
    }
}

/**
 * Accessibility section with toggle switches.
 */
@Composable
private fun AccessibilitySection(
    highContrastEnabled: Boolean,
    largeTextEnabled: Boolean,
    onToggleHighContrast: (Boolean) -> Unit,
    onToggleLargeText: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Accessibility",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )

            // High Contrast toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "High contrast mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = highContrastEnabled,
                    onCheckedChange = onToggleHighContrast,
                )
            }

            // Large Text toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Large text",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = largeTextEnabled,
                    onCheckedChange = onToggleLargeText,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioHapticSettingsUiPreview() {
    KidsMathTutorAppTheme {
        AudioHapticSettingsUi(
            state =
                AudioHapticSettingsScreen.State(
                    soundEffectsEnabled = true,
                    backgroundMusicEnabled = false,
                    hapticsEnabled = true,
                    volume = 0.7f,
                    highContrastEnabled = false,
                    largeTextEnabled = false,
                    isDeviceAudioSuppressed = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioHapticSettingsUiAllDisabledPreview() {
    KidsMathTutorAppTheme(highContrast = true, largeText = true) {
        AudioHapticSettingsUi(
            state =
                AudioHapticSettingsScreen.State(
                    soundEffectsEnabled = false,
                    backgroundMusicEnabled = false,
                    hapticsEnabled = false,
                    volume = 0.5f,
                    highContrastEnabled = true,
                    largeTextEnabled = true,
                    isDeviceAudioSuppressed = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioHapticSettingsUiDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        AudioHapticSettingsUi(
            state =
                AudioHapticSettingsScreen.State(
                    soundEffectsEnabled = true,
                    backgroundMusicEnabled = true,
                    hapticsEnabled = true,
                    volume = 0.8f,
                    highContrastEnabled = false,
                    largeTextEnabled = false,
                    isDeviceAudioSuppressed = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioHapticSettingsUiWithSuppressionWarningPreview() {
    KidsMathTutorAppTheme {
        AudioHapticSettingsUi(
            state =
                AudioHapticSettingsScreen.State(
                    soundEffectsEnabled = true,
                    backgroundMusicEnabled = true,
                    hapticsEnabled = true,
                    volume = 0.8f,
                    highContrastEnabled = false,
                    largeTextEnabled = false,
                    isDeviceAudioSuppressed = true,
                    eventSink = {},
                ),
        )
    }
}
