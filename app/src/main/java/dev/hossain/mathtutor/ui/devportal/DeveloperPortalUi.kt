package dev.hossain.mathtutor.ui.devportal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

@CircuitInject(DeveloperPortalScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperPortalUi(
    state: DeveloperPortalScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Portal") },
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .fillMaxWidth(),
        ) {
            // Data Operations
            if (state.showDataOpsSection) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Data Operations", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { state.eventSink(DeveloperPortalScreen.Event.ResetOnboardingClicked) }) {
                            Text("Reset Onboarding")
                        }

                        // Show result message if present
                        state.resetOnboardingResultMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = msg)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(onClick = { state.eventSink(DeveloperPortalScreen.Event.ClearAppDataClicked) }) {
                            Text("Clear App Data")
                        }

                        // Show result message if present
                        state.clearResultMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = msg)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Profile Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Profile Controls", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Quick controls for testing profile settings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name field
                    Text(text = "Name: ${state.currentProfileName ?: "Not set"}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    var nameText by remember(state.currentProfileName) { mutableStateOf(state.currentProfileName ?: "") }
                    TextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        placeholder = { Text("Enter name") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = {
                        state.eventSink(DeveloperPortalScreen.Event.UpdateProfileName(nameText.ifBlank { null }))
                    }) {
                        Text("Update Name")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grade level selector
                    Text(
                        text = "Grade Level: ${state.currentGradeLevel?.displayName ?: "Not set"}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        dev.hossain.mathtutor.domain.model.GradeLevel.values().forEach { grade ->
                            Button(
                                onClick = { state.eventSink(DeveloperPortalScreen.Event.UpdateGradeLevel(grade)) },
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                            ) {
                                Text(
                                    text =
                                        when (grade) {
                                            dev.hossain.mathtutor.domain.model.GradeLevel.KINDERGARTEN -> "K"
                                            dev.hossain.mathtutor.domain.model.GradeLevel.GRADE_1 -> "G1"
                                            dev.hossain.mathtutor.domain.model.GradeLevel.GRADE_2 -> "G2"
                                        },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Adaptive difficulty toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Adaptive Difficulty",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "Adjusts problem difficulty based on performance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = state.currentAdaptiveDifficulty,
                            onCheckedChange = { enabled ->
                                state.eventSink(DeveloperPortalScreen.Event.UpdateAdaptiveDifficulty(enabled))
                            },
                        )
                    }

                    // Show update result message if present
                    state.profileUpdateResultMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seed / Simulators
            if (state.showSeedSection) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Seed & Simulators", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        var countText by remember { mutableStateOf("10") }
                        var opIndex by remember { mutableStateOf(0) }
                        var gradeIndex by remember { mutableStateOf(1) } // default Grade 1

                        TextField(
                            value = countText,
                            onValueChange = { countText = it.filter { ch -> ch.isDigit() } },
                            placeholder = { Text("Number of sessions (e.g., 10)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Operation: ${dev.hossain.mathtutor.domain.model.MathOperation.values()[opIndex].displayName}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = {
                            opIndex = (opIndex + 1) %
                                dev.hossain.mathtutor.domain.model.MathOperation
                                    .values()
                                    .size
                        }) {
                            Text("Change Operation")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Grade: ${dev.hossain.mathtutor.domain.model.GradeLevel.values()[gradeIndex].displayName}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = {
                            gradeIndex = (gradeIndex + 1) %
                                dev.hossain.mathtutor.domain.model.GradeLevel
                                    .values()
                                    .size
                        }) {
                            Text("Change Grade")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            val count = countText.toIntOrNull() ?: 0
                            val op =
                                dev.hossain.mathtutor.domain.model.MathOperation
                                    .values()[opIndex]
                            val grade =
                                dev.hossain.mathtutor.domain.model.GradeLevel
                                    .values()[gradeIndex]
                            state.eventSink(DeveloperPortalScreen.Event.SeedSessionsRequested(count, op, grade))
                        }) {
                            Text("Seed Sample Sessions")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { state.eventSink(DeveloperPortalScreen.Event.ForceBadgeCheckClicked) }) {
                            Text("Run Badge Checks")
                        }

                        // Force unlock per-badge controls
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Force Unlock Badges", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (state.badges.isEmpty()) {
                            Text(text = "No badges available")
                        } else {
                            state.badges.forEach { badge ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = badge.name)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = { state.eventSink(DeveloperPortalScreen.Event.ForceUnlockBadge(badge.id)) },
                                        enabled = !badge.isUnlocked(),
                                    ) {
                                        Text(if (badge.isUnlocked()) "Unlocked" else "Force Unlock")
                                    }
                                }
                            }
                        }

                        // Show force unlock progress/result
                        state.forceUnlockResultMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = msg)
                        }
                        if (state.forceUnlockInProgress) {
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sounds & Haptics Testing
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Sounds & Haptics", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Test audio and haptic feedback",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Success
                    Button(
                        onClick = { state.eventSink(DeveloperPortalScreen.Event.PlaySuccessSound) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Play Success Sound & Haptic")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Error
                    Button(
                        onClick = { state.eventSink(DeveloperPortalScreen.Event.PlayErrorSound) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Play Error Sound & Haptic")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Level Up
                    Button(
                        onClick = { state.eventSink(DeveloperPortalScreen.Event.PlayLevelUpSound) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Play Level-Up Sound & Haptic")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badge Unlock
                    Button(
                        onClick = { state.eventSink(DeveloperPortalScreen.Event.PlayBadgeUnlockSound) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Play Badge Unlock Sound & Haptic")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Countdown
                    Button(
                        onClick = { state.eventSink(DeveloperPortalScreen.Event.PlayCountdownSound) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Play Countdown Sound")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // GO!
                    Button(
                        onClick = { state.eventSink(DeveloperPortalScreen.Event.PlayGoSound) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Play GO! Sound & Haptic")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Background Music Toggle
                    Button(
                        onClick = { state.eventSink(DeveloperPortalScreen.Event.ToggleBackgroundMusic) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (state.isBackgroundMusicPlaying) "Stop Background Music" else "Start Background Music")
                    }

                    // Show feedback message if present
                    state.soundHapticFeedback?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Diagnostics / Misc
            if (state.showDiagnosticsSection) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Diagnostics", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Analytics Override Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Analytics (override)",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text = "Debug-only: Immediately toggles analytics collection",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = state.isAnalyticsEnabled,
                                onCheckedChange = { state.eventSink(DeveloperPortalScreen.Event.ToggleAnalyticsOverride) },
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Use the 'Sounds & Haptics' section above to test audio and haptic feedback.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Clear confirmation dialog
        if (state.showClearConfirm) {
            var confirmText by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { state.eventSink(DeveloperPortalScreen.Event.CancelClear) },
                title = { Text("Confirm Clear App Data") },
                text = {
                    Column {
                        Text(
                            "This will delete local sessions, game data, and reset preferences.\n\n" +
                                "Type DELETE (all caps) in the field below to confirm.",
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextField(
                            value = confirmText,
                            onValueChange = { confirmText = it },
                            placeholder = { Text("Type DELETE to confirm") },
                        )
                        if (state.clearInProgress) {
                            Spacer(modifier = Modifier.height(12.dp))
                            CircularProgressIndicator()
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            state.eventSink(DeveloperPortalScreen.Event.ConfirmClear(confirmText))
                            confirmText = ""
                        },
                        enabled = confirmText == "DELETE" && !state.clearInProgress,
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { state.eventSink(DeveloperPortalScreen.Event.CancelClear) }) {
                        Text("Cancel")
                    }
                },
            )
        }

        // Reset Onboarding confirmation dialog
        if (state.showResetOnboardingConfirm) {
            AlertDialog(
                onDismissRequest = { state.eventSink(DeveloperPortalScreen.Event.CancelResetOnboarding) },
                title = { Text("Confirm Reset Onboarding") },
                text = {
                    Column {
                        Text(
                            "This will reset the onboarding state so the app shows onboarding " +
                                "screens on the next launch.\n\n" +
                                "This is useful for testing the first-run experience.\n\n" +
                                "Your user profile, progress, and settings will be preserved.",
                        )
                        if (state.resetOnboardingInProgress) {
                            Spacer(modifier = Modifier.height(12.dp))
                            CircularProgressIndicator()
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { state.eventSink(DeveloperPortalScreen.Event.ConfirmResetOnboarding) },
                        enabled = !state.resetOnboardingInProgress,
                    ) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { state.eventSink(DeveloperPortalScreen.Event.CancelResetOnboarding) }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}
