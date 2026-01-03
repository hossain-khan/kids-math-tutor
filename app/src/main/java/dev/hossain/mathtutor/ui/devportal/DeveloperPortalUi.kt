package dev.hossain.mathtutor.ui.devportal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MAX_CONTENT_WIDTH_LARGE
import dev.zacsweers.metro.AppScope
import timber.log.Timber

@CircuitInject(DeveloperPortalScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperPortalUi(
    state: DeveloperPortalScreen.State,
    modifier: Modifier = Modifier,
) {
    // Local state for section expansion
    var expandedSections by remember {
        mutableStateOf(
            mapOf(
                "dataOps" to true,
                "navigation" to true,
                "profile" to true,
                "badges" to false,
                "challenges" to false,
                "streak" to false,
                "seed" to false,
                "sounds" to false,
                "diagnostics" to false,
            ),
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Portal") },
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        // Center content on wide screens
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = MAX_CONTENT_WIDTH_LARGE)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
            ) {
                // Session Statistics Card (always visible at top)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Session Statistics", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Total Sessions: ${state.totalSessionCount}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Data Operations
                if (state.showDataOpsSection) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Button(
                                onClick = {
                                    expandedSections =
                                        expandedSections.toMutableMap().apply { put("dataOps", !(this["dataOps"] ?: true)) }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = "${if (expandedSections["dataOps"] == true) "▼" else "▶"} Data Operations")
                            }

                            if (expandedSections["dataOps"] == true) {
                                Spacer(modifier = Modifier.height(12.dp))
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Navigation - Jump directly to games for testing
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Quick Navigation", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Jump directly to games for testing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { state.eventSink(DeveloperPortalScreen.Event.NavigateToMathRace) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("🏎️ Math Race")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { state.eventSink(DeveloperPortalScreen.Event.NavigateToMemoryMatch) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("🧠 Memory Match")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { state.eventSink(DeveloperPortalScreen.Event.NavigateToNumberSequence) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("🔢 Number Sequence")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { state.eventSink(DeveloperPortalScreen.Event.ViewColorPalette) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("🎨 Color Palette Viewer")
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

                            // Session count display
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Sessions Created: ${state.totalSessionCount}", style = MaterialTheme.typography.bodyMedium)

                            // Force unlock per-badge controls
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    expandedSections =
                                        expandedSections.toMutableMap().apply { put("badges", !(this["badges"] ?: false)) }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = "${if (expandedSections["badges"] == true) "▼" else "▶"} Badge Controls")
                            }

                            if (expandedSections["badges"] == true) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "Force Unlock Badges", style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Unlock All button
                                if (state.badges.isNotEmpty()) {
                                    Button(
                                        onClick = { state.eventSink(DeveloperPortalScreen.Event.UnlockAllBadges) },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !state.forceUnlockInProgress,
                                    ) {
                                        Text("🔓 Unlock All Badges")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                if (state.badges.isEmpty()) {
                                    Text(text = "No badges available")
                                } else {
                                    state.badges.chunked(3).forEach { badgeRow ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            badgeRow.forEach { badge ->
                                                Column(
                                                    modifier =
                                                        Modifier
                                                            .weight(1f)
                                                            .padding(8.dp),
                                                ) {
                                                    Text(
                                                        text = badge.name,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        maxLines = 2,
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Button(
                                                        onClick = {
                                                            state.eventSink(
                                                                DeveloperPortalScreen.Event.ForceUnlockBadge(badge.id),
                                                            )
                                                        },
                                                        enabled = !badge.isUnlocked(),
                                                        modifier = Modifier.fillMaxWidth(),
                                                    ) {
                                                        Text(
                                                            text = if (badge.isUnlocked()) "Unlocked" else "Unlock",
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                    }
                                                }
                                            }
                                            // Add spacer if only 1 item in row
                                            if (badgeRow.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Import Sample Challenges
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                expandedSections =
                                    expandedSections.toMutableMap().apply { put("challenges", !(this["challenges"] ?: false)) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = "${if (expandedSections["challenges"] == true) "▼" else "▶"} Import Sample Challenges")
                        }

                        if (expandedSections["challenges"] == true) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Import 6 sample challenges with various problem types",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { state.eventSink(DeveloperPortalScreen.Event.ImportSampleChallengesClicked) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.importChallengesInProgress,
                            ) {
                                Text("📚 Import Challenges")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { state.eventSink(DeveloperPortalScreen.Event.DeleteAllChallengesClicked) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.deleteChallengesInProgress,
                            ) {
                                Text("🗑️ Delete All Challenges")
                            }

                            // Show result message if present
                            state.importChallengesResultMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = msg)
                            }
                            state.deleteChallengesResultMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = msg)
                            }
                            if (state.importChallengesInProgress || state.deleteChallengesInProgress) {
                                Spacer(modifier = Modifier.height(8.dp))
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Streak Management
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                expandedSections =
                                    expandedSections.toMutableMap().apply { put("streak", !(this["streak"] ?: false)) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = "${if (expandedSections["streak"] == true) "▼" else "▶"} Streak Management")
                        }

                        if (expandedSections["streak"] == true) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "View and force-set daily streak values",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Display current streak status
                            if (state.currentStreakData != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        ),
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Current Streak Status", style = MaterialTheme.typography.bodyLarge)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Current Streak: ${state.currentStreakData.currentStreak} 🔥")
                                        Text("Longest Streak: ${state.currentStreakData.longestStreak}")
                                        Text("Total Days Practiced: ${state.currentStreakData.totalDaysPracticed}")
                                        if (state.currentStreakData.lastPracticeDate != null) {
                                            Text("Last Practice: ${state.currentStreakData.lastPracticeDate}")
                                        } else {
                                            Text("Last Practice: Never")
                                        }
                                    }
                                }
                            } else {
                                Text("No streak data available", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Force Set Streak Values", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Current streak input
                            var currentStreakInput by remember {
                                mutableStateOf(state.currentStreakData?.currentStreak?.toString() ?: "0")
                            }
                            Text("Current Streak:", style = MaterialTheme.typography.labelSmall)
                            TextField(
                                value = currentStreakInput,
                                onValueChange = { currentStreakInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Longest streak input
                            var longestStreakInput by remember {
                                mutableStateOf(state.currentStreakData?.longestStreak?.toString() ?: "0")
                            }
                            Text("Longest Streak:", style = MaterialTheme.typography.labelSmall)
                            TextField(
                                value = longestStreakInput,
                                onValueChange = { longestStreakInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Total days input
                            var totalDaysInput by remember {
                                mutableStateOf(state.currentStreakData?.totalDaysPracticed?.toString() ?: "0")
                            }
                            Text("Total Days Practiced:", style = MaterialTheme.typography.labelSmall)
                            TextField(
                                value = totalDaysInput,
                                onValueChange = { totalDaysInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Last practice date input (optional)
                            var lastPracticeDateInput by remember {
                                mutableStateOf(state.currentStreakData?.lastPracticeDate?.toString() ?: "")
                            }
                            Text("Last Practice Date (YYYY-MM-DD):", style = MaterialTheme.typography.labelSmall)
                            TextField(
                                value = lastPracticeDateInput,
                                onValueChange = { lastPracticeDateInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Leave blank for no date") },
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    try {
                                        val current = currentStreakInput.toIntOrNull() ?: 0
                                        val longest = longestStreakInput.toIntOrNull() ?: 0
                                        val totalDays = totalDaysInput.toIntOrNull() ?: 0
                                        val lastDate =
                                            if (lastPracticeDateInput.isBlank()) {
                                                null
                                            } else {
                                                java.time.LocalDate.parse(lastPracticeDateInput)
                                            }

                                        state.eventSink(
                                            DeveloperPortalScreen.Event.ForceSetStreak(
                                                currentStreak = current,
                                                longestStreak = longest,
                                                lastPracticeDate = lastDate,
                                                totalDaysPracticed = totalDays,
                                            ),
                                        )
                                    } catch (e: Exception) {
                                        Timber.e(e, "Invalid streak input")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.setStreakInProgress,
                            ) {
                                Text("💾 Save Streak")
                            }

                            // Show result message if present
                            state.setStreakResultMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = msg)
                            }
                            if (state.setStreakInProgress) {
                                Spacer(modifier = Modifier.height(8.dp))
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                expandedSections =
                                    expandedSections.toMutableMap().apply { put("sounds", !(this["sounds"] ?: false)) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = "${if (expandedSections["sounds"] == true) "▼" else "▶"} Sounds & Haptics")
                        }

                        if (expandedSections["sounds"] == true) {
                            Spacer(modifier = Modifier.height(12.dp))
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
                                Icon(
                                    imageVector = if (state.isBackgroundMusicPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (state.isBackgroundMusicPlaying) "Pause" else "Play",
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isBackgroundMusicPlaying) "Stop Background Music" else "Start Background Music")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Additional sound test buttons
                            Button(
                                onClick = { state.eventSink(DeveloperPortalScreen.Event.PlayPerfectScore) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Play Perfect Score Sound & Haptic")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { state.eventSink(DeveloperPortalScreen.Event.PlayStreakContinue) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Play Streak Continue Sound")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { state.eventSink(DeveloperPortalScreen.Event.PlayWarning) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Play Warning Sound")
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

                            Spacer(modifier = Modifier.height(12.dp))

                            // Sound load status
                            Text(text = "Sounds: ${if (state.soundsLoaded) "Loaded" else "Loading..."}")
                            Spacer(modifier = Modifier.height(6.dp))
                            if (state.soundSampleIds.isNotEmpty()) {
                                Text(text = "Sample IDs:", style = MaterialTheme.typography.bodySmall)
                                state.soundSampleIds.entries.forEach { entry ->
                                    Text(text = " - ${entry.key}: ${entry.value}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
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
