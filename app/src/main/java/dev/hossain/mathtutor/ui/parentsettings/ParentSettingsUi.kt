package dev.hossain.mathtutor.ui.parentsettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope

/**
 * UI for [ParentSettingsScreen].
 *
 * Displays parent-specific settings with Material 3 design:
 * - PIN setup and management (with animated Reset/Forgot options)
 * - Grade limit configuration
 *
 * ## Design Rationale
 * - Uses cards to group related settings visually
 * - Lock icons indicate protected features
 * - Animated visibility for advanced options reduces clutter
 * - Clear warnings help parents understand the impact of their choices
 */
@CircuitInject(ParentSettingsScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentSettingsUi(
    state: ParentSettingsScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Settings") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(ParentSettingsScreen.Event.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header description
            Text(
                text = "Manage parental controls and child access restrictions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // PIN Setup Card
            PinSetupCard(
                hasPinSet = state.hasPinSet,
                showResetForgotOptions = state.showResetForgotOptions,
                onSetupPinClick = { state.eventSink(ParentSettingsScreen.Event.SetupPinClicked) },
                onToggleResetForgotOptions = {
                    state.eventSink(ParentSettingsScreen.Event.ToggleResetForgotOptions)
                },
                onResetPinClick = { state.eventSink(ParentSettingsScreen.Event.ResetPinClicked) },
                onForgotPinClick = { state.eventSink(ParentSettingsScreen.Event.ForgotPinClicked) },
            )

            // Grade Limit Card
            GradeLimitCard(
                maxGradeLevel = state.maxGradeLevel,
                isPinProtected = state.hasPinSet,
                onChangeGradeLimitClick = {
                    state.eventSink(ParentSettingsScreen.Event.ChangeGradeLimitClicked)
                },
            )

            // TODO: Add dialogs for PIN setup, verification, reset, forgot PIN, and grade limit
        }
    }
}

/**
 * Card for PIN setup and management.
 *
 * Shows different UI based on whether PIN is set:
 * - If no PIN: Shows "Setup PIN" button
 * - If PIN is set: Shows lock icon with info button to reveal Reset/Forgot options
 */
@Composable
private fun PinSetupCard(
    hasPinSet: Boolean,
    showResetForgotOptions: Boolean,
    onSetupPinClick: () -> Unit,
    onToggleResetForgotOptions: () -> Unit,
    onResetPinClick: () -> Unit,
    onForgotPinClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = if (hasPinSet) Icons.Default.Lock else Icons.Outlined.LockOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column {
                        Text(
                            text = "Parent PIN",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text =
                                if (hasPinSet) {
                                    "PIN is set"
                                } else {
                                    "Not configured"
                                },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (hasPinSet) {
                    IconButton(onClick = onToggleResetForgotOptions) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "PIN options",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Text(
                text = "Set a 4-digit PIN to lock sensitive settings and prevent children from making unauthorized changes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!hasPinSet) {
                Button(
                    onClick = onSetupPinClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Setup PIN")
                }
            }

            // Animated Reset and Forgot PIN options
            AnimatedVisibility(
                visible = hasPinSet && showResetForgotOptions,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = onResetPinClick,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Reset PIN")
                        }
                        OutlinedButton(
                            onClick = onForgotPinClick,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Forgot PIN")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card for grade limit configuration.
 *
 * Allows parents to set maximum grade level children can select.
 * Shows lock icon if PIN protection is enabled.
 */
@Composable
private fun GradeLimitCard(
    maxGradeLevel: GradeLevel?,
    isPinProtected: Boolean,
    onChangeGradeLimitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column {
                        Text(
                            text = "Grade Limit",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text =
                                maxGradeLevel?.displayName
                                    ?: "No limit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (isPinProtected) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "PIN protected",
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            Text(
                text =
                    "Set the maximum grade level your child can select. " +
                        "This prevents them from accessing problems that are too difficult and frustrating.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = onChangeGradeLimitClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (maxGradeLevel != null) "Change Grade Limit" else "Set Grade Limit")
            }
        }
    }
}

// Preview composables
@Preview(showBackground = true)
@Composable
private fun ParentSettingsUiPreview() {
    KidsMathTutorAppTheme {
        ParentSettingsUi(
            state =
                ParentSettingsScreen.State(
                    hasPinSet = false,
                    maxGradeLevel = null,
                    showPinSetup = false,
                    showPinVerification = false,
                    showPinReset = false,
                    showForgotPin = false,
                    showGradeLimit = false,
                    showResetForgotOptions = false,
                    pinVerificationMode = ParentSettingsScreen.PinVerificationMode.NONE,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParentSettingsUiWithPinSetPreview() {
    KidsMathTutorAppTheme {
        ParentSettingsUi(
            state =
                ParentSettingsScreen.State(
                    hasPinSet = true,
                    maxGradeLevel = GradeLevel.GRADE_2,
                    showPinSetup = false,
                    showPinVerification = false,
                    showPinReset = false,
                    showForgotPin = false,
                    showGradeLimit = false,
                    showResetForgotOptions = false,
                    pinVerificationMode = ParentSettingsScreen.PinVerificationMode.NONE,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParentSettingsUiWithOptionsExpandedPreview() {
    KidsMathTutorAppTheme {
        ParentSettingsUi(
            state =
                ParentSettingsScreen.State(
                    hasPinSet = true,
                    maxGradeLevel = GradeLevel.GRADE_1,
                    showPinSetup = false,
                    showPinVerification = false,
                    showPinReset = false,
                    showForgotPin = false,
                    showGradeLimit = false,
                    showResetForgotOptions = true,
                    pinVerificationMode = ParentSettingsScreen.PinVerificationMode.NONE,
                    eventSink = {},
                ),
        )
    }
}
