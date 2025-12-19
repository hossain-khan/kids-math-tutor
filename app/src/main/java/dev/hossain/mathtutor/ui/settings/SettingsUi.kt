package dev.hossain.mathtutor.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import java.time.Instant

/**
 * UI for [SettingsScreen].
 *
 * Displays user settings and profile information with:
 * - Profile section (name, grade level with edit buttons)
 * - Adaptive difficulty toggle switch
 * - About, Privacy, Help sections
 */
@CircuitInject(SettingsScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUi(
    state: SettingsScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings")
                },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(SettingsScreen.Event.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
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
            // Profile section
            ProfileSection(
                profile = state.profile,
                onEditNameClick = { state.eventSink(SettingsScreen.Event.EditNameClicked) },
                onChangeGradeClick = { state.eventSink(SettingsScreen.Event.ChangeGradeClicked) },
            )

            // Adaptive difficulty section
            AdaptiveDifficultySection(
                enabled = state.profile?.adaptiveDifficultyEnabled ?: true,
                onToggle = { enabled ->
                    state.eventSink(SettingsScreen.Event.ToggleAdaptiveDifficulty(enabled))
                },
            )

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Audio & Haptics link
            SettingsLinkItem(
                text = "Audio & Haptics",
                onClick = { state.eventSink(SettingsScreen.Event.AudioHapticsClicked) },
            )

            // Additional sections
            SettingsLinks()
        }
    }

    // Show dialogs when needed
    if (state.showNameDialog && state.profile != null) {
        NameEditDialog(
            currentName = state.profile.name,
            onDismiss = { state.eventSink(SettingsScreen.Event.CancelNameEdit) },
            onSave = { name -> state.eventSink(SettingsScreen.Event.SaveName(name)) },
        )
    }

    if (state.showGradeDialog && state.profile != null) {
        GradeChangeDialog(
            currentGrade = state.profile.gradeLevel,
            onDismiss = { state.eventSink(SettingsScreen.Event.CancelGradeChange) },
            onSave = { grade -> state.eventSink(SettingsScreen.Event.SaveGrade(grade)) },
        )
    }
}

/**
 * Profile section with name and grade level.
 */
@Composable
private fun ProfileSection(
    profile: UserProfile?,
    onEditNameClick: () -> Unit,
    onChangeGradeClick: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            // Name field
            ProfileField(
                label = "Name",
                value = profile?.name ?: "Not set",
                onEditClick = onEditNameClick,
            )

            // Grade level field
            ProfileField(
                label = "Grade Level",
                value = profile?.gradeLevel?.displayName ?: "Loading...",
                onEditClick = onChangeGradeClick,
                actionLabel = "Change",
            )
        }
    }
}

/**
 * Individual profile field with label, value, and edit button.
 */
@Composable
private fun ProfileField(
    label: String,
    value: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = "Edit",
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        TextButton(onClick = onEditClick) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

/**
 * Adaptive difficulty section with toggle switch.
 */
@Composable
private fun AdaptiveDifficultySection(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
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
                text = "Adaptive Difficulty",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Adjust difficulty based on performance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                )
            }
        }
    }
}

/**
 * Additional settings links (About, Privacy, Help).
 */
@Composable
private fun SettingsLinks(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        SettingsLinkItem(text = "About", onClick = { /* TODO: Implement navigation */ })
        SettingsLinkItem(text = "Privacy", onClick = { /* TODO: Implement navigation */ })
        SettingsLinkItem(text = "Help", onClick = { /* TODO: Implement navigation */ })
    }
}

/**
 * Individual settings link item.
 */
@Composable
private fun SettingsLinkItem(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsUiPreview() {
    KidsMathTutorAppTheme {
        SettingsUi(
            state =
                SettingsScreen.State(
                    profile =
                        UserProfile(
                            name = "Sarah",
                            gradeLevel = GradeLevel.GRADE_1,
                            createdAt = Instant.now(),
                            adaptiveDifficultyEnabled = true,
                        ),
                    showNameDialog = false,
                    showGradeDialog = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsUiNoNamePreview() {
    KidsMathTutorAppTheme {
        SettingsUi(
            state =
                SettingsScreen.State(
                    profile =
                        UserProfile(
                            name = null,
                            gradeLevel = GradeLevel.GRADE_2,
                            createdAt = Instant.now(),
                            adaptiveDifficultyEnabled = false,
                        ),
                    showNameDialog = false,
                    showGradeDialog = false,
                    eventSink = {},
                ),
        )
    }
}
