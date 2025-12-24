package dev.hossain.mathtutor.ui.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import timber.log.Timber
import java.time.Instant

// Width breakpoints for adaptive layouts
private val MAX_CONTENT_WIDTH: Dp = 600.dp

/**
 * UI for [SettingsScreen].
 *
 * Displays user settings and profile information with:
 * - Profile section (name, grade level with edit buttons)
 * - Adaptive difficulty toggle switch
 * - About, Privacy, Help sections
 *
 * Adaptive Layout:
 * - Compact: Full width settings
 * - Medium/Expanded: Centered content with max width
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
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Center content on wide screens
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier =
                        Modifier
                            .widthIn(max = MAX_CONTENT_WIDTH)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    // Hero image
                    Image(
                        painter = painterResource(id = R.drawable.pup_tutor_sticker_teaching_math_and_painting),
                        contentDescription = "Math Pup teaching",
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(1560f / 970f),
                    )

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
                        text = "Audio, Haptics & Accessibility",
                        onClick = { state.eventSink(SettingsScreen.Event.AudioHapticsClicked) },
                    )

                    // Parent Challenges link
                    SettingsLinkItem(
                        text = "Parent Challenges",
                        onClick = { state.eventSink(SettingsScreen.Event.ParentChallengesClicked) },
                    )

                    // Developer Portal (debug-only)
                    if (state.showDeveloperPortal) {
                        SettingsLinkItem(
                            text = "Developer Portal",
                            onClick = { state.eventSink(SettingsScreen.Event.DeveloperPortalClicked) },
                        )
                    }

                    // Divider
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )

                    // Privacy section header
                    Text(
                        text = "Privacy",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )

                    // Analytics toggle
                    AnalyticsToggleRow(
                        checked = state.analyticsEnabled,
                        onCheckedChange = { enabled ->
                            state.eventSink(SettingsScreen.Event.AnalyticsToggled(enabled))
                        },
                    )

                    // Divider
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )

                    // Additional sections
                    SettingsLinks()
                }
            }
        }
    }

    // Show dialogs when needed
    if (state.showNameDialog) {
        NameEditDialog(
            currentName = state.profile?.name,
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
                color = MaterialTheme.colorScheme.onSurface,
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
                value = profile?.gradeLevel?.displayName ?: "Not set",
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
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        TextButton(onClick = onEditClick) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
 * Analytics toggle row with icon, description, and switch.
 */
@Composable
private fun AnalyticsToggleRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Analytics,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text =
                    "Help improve the app by sharing usage data. " +
                        "We collect screen views and feature usage, but never personal information like names or locations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

/**
 * Additional settings links (About, Privacy, Help).
 */
@Composable
private fun SettingsLinks(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val toolbarColor = MaterialTheme.colorScheme.primary.toArgb()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        SettingsLinkItem(text = "About", onClick = { openExternalUrl(context, "https://liquidlabs.ca/android/math-tutor/", toolbarColor) })
        SettingsLinkItem(text = "Terms of Service", onClick = {
            openExternalUrl(context, "https://liquidlabs.ca/android/math-tutor/terms-of-service.html", toolbarColor)
        })
        SettingsLinkItem(text = "Privacy", onClick = {
            openExternalUrl(context, "https://liquidlabs.ca/android/math-tutor/privacy.html", toolbarColor)
        })
    }
}

/**
 * Helper to open an external URL using Chrome Custom Tabs with a themed toolbar color.
 * Falls back to ACTION_VIEW if Custom Tabs cannot be launched.
 */
private fun openExternalUrl(
    context: Context,
    url: String,
    toolbarColor: Int? = null,
) {
    try {
        val uri = url.toUri()
        val builder = CustomTabsIntent.Builder().setShowTitle(true)
        toolbarColor?.let { builder.setToolbarColor(it) }
        val customTabsIntent = builder.build()

        customTabsIntent.launchUrl(context, uri)
    } catch (e: Exception) {
        Timber.e(e, "[Settings] CustomTabs failed, falling back to ACTION_VIEW for URL=%s", url)
        // Fallback to ACTION_VIEW if Custom Tabs fails (safe for previews/tests)
        try {
            val intent =
                android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(intent)
        } catch (ignored: Exception) {
            Timber.e(ignored, "[Settings] Failed to open URL via ACTION_VIEW: %s", url)
        }
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
                    analyticsEnabled = true,
                    showDeveloperPortal = true,
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
                    analyticsEnabled = false,
                    showDeveloperPortal = true,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsUiDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        SettingsUi(
            state =
                SettingsScreen.State(
                    profile =
                        UserProfile(
                            name = "Jordan",
                            gradeLevel = GradeLevel.KINDERGARTEN,
                            createdAt = Instant.now(),
                            adaptiveDifficultyEnabled = true,
                        ),
                    showNameDialog = false,
                    showGradeDialog = false,
                    analyticsEnabled = true,
                    showDeveloperPortal = true,
                    eventSink = {},
                ),
        )
    }
}
