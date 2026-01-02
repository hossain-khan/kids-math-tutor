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
import androidx.compose.material.icons.outlined.Tune
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

            // Hint System Toggle Card
            HintSystemCard(
                isHintSystemEnabled = state.isHintSystemEnabled,
                onToggleHintSystem = { enabled ->
                    state.eventSink(ParentSettingsScreen.Event.HintSystemToggled(enabled))
                },
            )

            // Adaptive Difficulty Card
            AdaptiveDifficultyCard(
                isAdaptiveDifficultyEnabled = state.adaptiveDifficultyEnabled,
                onToggleAdaptiveDifficulty = { enabled ->
                    state.eventSink(ParentSettingsScreen.Event.AdaptiveDifficultyToggled(enabled))
                },
            )

            // TODO: Add dialogs for PIN setup, verification, reset, forgot PIN, and grade limit
        }

        // PIN Setup Dialog
        if (state.showPinSetup) {
            PinSetupDialog(
                onConfirm = { pin, confirmPin ->
                    state.eventSink(
                        ParentSettingsScreen.Event.PinSetupCompleted(
                            pin = pin,
                            confirmPin = confirmPin,
                        ),
                    )
                },
                onDismiss = {
                    state.eventSink(ParentSettingsScreen.Event.PinSetupCancelled)
                },
            )
        }

        // PIN Verification Dialog
        if (state.showPinVerification) {
            PinVerificationDialog(
                onConfirm = { pin ->
                    state.eventSink(ParentSettingsScreen.Event.PinSubmitted(pin))
                },
                onDismiss = {
                    state.eventSink(ParentSettingsScreen.Event.PinVerificationCancelled)
                },
            )
        }

        // PIN Reset Dialog
        if (state.showPinReset) {
            PinResetDialog(
                onConfirm = { newPin, confirmNewPin ->
                    state.eventSink(
                        ParentSettingsScreen.Event.PinResetCompleted(
                            newPin = newPin,
                            confirmNewPin = confirmNewPin,
                        ),
                    )
                },
                onDismiss = {
                    state.eventSink(ParentSettingsScreen.Event.PinResetCancelled)
                },
            )
        }

        // Forgot PIN Dialog
        if (state.showForgotPin) {
            ForgotPinDialog(
                onSuccess = {
                    // When math challenge is solved correctly, presenter will clear PIN
                    state.eventSink(
                        ParentSettingsScreen.Event.ForgotPinChallengeCompleted(
                            answer = "success",
                            correctAnswer = 0,
                        ),
                    )
                },
                onDismiss = {
                    state.eventSink(ParentSettingsScreen.Event.ForgotPinChallengeCancelled)
                },
            )
        }

        // Grade Limit Dialog
        if (state.showGradeLimit) {
            GradeLimitDialog(
                currentMaxGrade = state.maxGradeLevel,
                onConfirm = { gradeLevel ->
                    state.eventSink(
                        ParentSettingsScreen.Event.GradeLimitChanged(gradeLevel),
                    )
                },
                onDismiss = {
                    state.eventSink(ParentSettingsScreen.Event.GradeLimitCancelled)
                },
            )
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

/**
 * Card for controlling hint system availability.
 *
 * Allows parents to enable or disable the hint system for the entire app.
 * When disabled, children won't see hint buttons even after wrong attempts.
 */
@Composable
private fun HintSystemCard(
    isHintSystemEnabled: Boolean,
    onToggleHintSystem: (Boolean) -> Unit,
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
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column {
                        Text(
                            text = "Hint System",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text =
                                if (isHintSystemEnabled) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Text(
                text =
                    "When enabled, children see helpful hints after making mistakes. " +
                        "When disabled, hints won't be available.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onToggleHintSystem(false) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Disable")
                }
                Button(
                    onClick = { onToggleHintSystem(true) },
                    modifier = Modifier.weight(1f),
                    colors =
                        if (isHintSystemEnabled) {
                            androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        },
                ) {
                    Text("Enable")
                }
            }
        }
    }
}

/**
 * Card for controlling adaptive difficulty.
 *
 * Allows parents to enable or disable adaptive difficulty, which adjusts problem
 * difficulty based on the child's performance.
 */
@Composable
private fun AdaptiveDifficultyCard(
    isAdaptiveDifficultyEnabled: Boolean,
    onToggleAdaptiveDifficulty: (Boolean) -> Unit,
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
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column {
                        Text(
                            text = \"Adaptive Difficulty\",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text =
                                if (isAdaptiveDifficultyEnabled) \"Enabled\" else \"Disabled\",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Text(
                text =
                    \"When enabled, problem difficulty adjusts automatically based on performance. \" +
                        \"When disabled, problems remain at the selected grade level.\",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onToggleAdaptiveDifficulty(false) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(\"Disable\")
                }
                Button(
                    onClick = { onToggleAdaptiveDifficulty(true) },
                    modifier = Modifier.weight(1f),
                    colors =
                        if (isAdaptiveDifficultyEnabled) {
                            androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        },
                ) {
                    Text(\"Enable\")
                }
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
                    isHintSystemEnabled = true,
                    adaptiveDifficultyEnabled = true,
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
                    isHintSystemEnabled = true,
                    adaptiveDifficultyEnabled = true,
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
                    isHintSystemEnabled = true,
                    adaptiveDifficultyEnabled = false,
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

@Preview(showBackground = true)
@Composable
private fun ParentSettingsUiWithPinSetupDialogPreview() {
    KidsMathTutorAppTheme {
        ParentSettingsUi(
            state =
                ParentSettingsScreen.State(
                    hasPinSet = false,
                    maxGradeLevel = null,
                    isHintSystemEnabled = true,
                    adaptiveDifficultyEnabled = true,
                    showPinSetup = true,
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
private fun ParentSettingsUiWithGradeLimitDialogPreview() {
    KidsMathTutorAppTheme {
        ParentSettingsUi(
            state =
                ParentSettingsScreen.State(
                    hasPinSet = true,
                    maxGradeLevel = null,
                    isHintSystemEnabled = true,
                    adaptiveDifficultyEnabled = true,
                    showPinSetup = false,
                    showPinVerification = false,
                    showPinReset = false,
                    showForgotPin = false,
                    showGradeLimit = true,
                    showResetForgotOptions = false,
                    pinVerificationMode = ParentSettingsScreen.PinVerificationMode.NONE,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParentSettingsUiWithPinVerificationDialogPreview() {
    KidsMathTutorAppTheme {
        ParentSettingsUi(
            state =
                ParentSettingsScreen.State(
                    hasPinSet = true,
                    maxGradeLevel = GradeLevel.GRADE_2,
                    isHintSystemEnabled = true,
                    adaptiveDifficultyEnabled = false,
                    showPinSetup = false,
                    showPinVerification = true,
                    showPinReset = false,
                    showForgotPin = false,
                    showGradeLimit = false,
                    showResetForgotOptions = false,
                    pinVerificationMode = ParentSettingsScreen.PinVerificationMode.RESET_PIN,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParentSettingsUiWithForgotPinDialogPreview() {
    KidsMathTutorAppTheme {
        ParentSettingsUi(
            state =
                ParentSettingsScreen.State(
                    hasPinSet = true,
                    maxGradeLevel = GradeLevel.GRADE_1,
                    isHintSystemEnabled = true,
                    adaptiveDifficultyEnabled = true,
                    showPinSetup = false,
                    showPinVerification = false,
                    showPinReset = false,
                    showForgotPin = true,
                    showGradeLimit = false,
                    showResetForgotOptions = true,
                    pinVerificationMode = ParentSettingsScreen.PinVerificationMode.NONE,
                    eventSink = {},
                ),
        )
    }
}
