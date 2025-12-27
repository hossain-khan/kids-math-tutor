package dev.hossain.mathtutor.ui.importchallenge

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PreviewData
import dev.zacsweers.metro.AppScope
import timber.log.Timber
import kotlin.time.Duration

/**
 * UI for [ImportChallengeScreen].
 *
 * Provides a form for pasting JSON challenge specifications,
 * displays validation errors, and shows a preview of the challenge.
 */
@CircuitInject(ImportChallengeScreen::class, AppScope::class)
@Composable
fun ImportChallengeUi(
    state: ImportChallengeScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            ImportTopBar(
                onNavigateBack = { state.eventSink(ImportChallengeScreen.Event.NavigateBack) },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Share detection banner
            if (state.detectedJsonFromShare && state.jsonInput.isNotBlank()) {
                item {
                    ShareDetectionBanner(
                        detectedFromShare = true,
                        hasValidationErrors = state.validationState is ValidationState.Invalid,
                    )
                }
            }

            // Parent information section
            item {
                ParentInfoSection()
            }

            // Validation messages (shown above input field)
            if (state.validationState is ValidationState.Invalid) {
                item {
                    ValidationErrorsSection(errors = state.validationState.fieldErrors)
                }
            }

            if (state.validationState is ValidationState.Valid && state.previewData != null) {
                item {
                    ValidationSuccessSection()
                }
            }

            item {
                JsonInputSection(
                    jsonInput = state.jsonInput,
                    onJsonChanged = { state.eventSink(ImportChallengeScreen.Event.JsonInputChanged(it)) },
                    onValidate = { state.eventSink(ImportChallengeScreen.Event.ValidateAndPreview) },
                    onClear = { state.eventSink(ImportChallengeScreen.Event.ClearInput) },
                )
            }

            if (state.previewData != null) {
                item {
                    PreviewSection(
                        previewData = state.previewData,
                        onSave = { state.eventSink(ImportChallengeScreen.Event.SaveChallenge) },
                        isLoading = state.isLoading,
                    )
                }
            }
        }
    }
}

/**
 * Top app bar for the import challenge screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Import Challenge") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
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
}

/**
 * Section for JSON input with validation.
 */
@Composable
private fun JsonInputSection(
    jsonInput: String,
    onJsonChanged: (String) -> Unit,
    onValidate: () -> Unit,
    onClear: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Paste JSON Challenge Specification",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = jsonInput,
                onValueChange = onJsonChanged,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 300.dp),
                placeholder = {
                    Text(
                        text = "Paste your JSON here...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onClear) {
                    Text("Clear")
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onValidate()
                    },
                    enabled = jsonInput.isNotBlank(),
                ) {
                    Text("Validate & Preview")
                }
            }
        }
    }
}

/**
 * Display validation errors.
 */
@Composable
private fun ValidationErrorsDisplay(errors: Map<String, String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Validation Errors:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )

            Spacer(modifier = Modifier.height(4.dp))

            errors.forEach { (field, error) ->
                Text(
                    text = "• $field: $error",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

/**
 * Display validation errors as a section above the input field.
 */
@Composable
private fun ValidationErrorsSection(errors: Map<String, String>) {
    Card(
        modifier =
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Validation Failed",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )

                Spacer(modifier = Modifier.height(4.dp))

                errors.forEach { (field, error) ->
                    Text(
                        text = if (field == "general" || field == "duplicate") error else "• $field: $error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
    }
}

/**
 * Display validation success message as a section above the input field.
 */
@Composable
private fun ValidationSuccessSection() {
    Card(
        modifier =
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Validation Successful",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Your challenge is valid! Review the preview below and tap \"Save Challenge\" to add it to your library.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

/**
 * Section for previewing the challenge.
 */
@Composable
private fun PreviewSection(
    previewData: PreviewData,
    onSave: () -> Unit,
    isLoading: Boolean,
) {
    Card(
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header section with title
            Text(
                text = "Challenge Preview",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Challenge title and subtitle
            Text(
                text = previewData.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            if (previewData.subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = previewData.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Challenge statistics
            ChallengeStatsRow(previewData = previewData)

            Spacer(modifier = Modifier.height(16.dp))

            // Sample problems preview
            Text(
                text = "Sample Problems:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
            ) {
                previewData.sampleProblems.take(5).forEach { problem ->
                    ProblemPreviewCard(problem = problem)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSave,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Text("Save Challenge")
                }
            }
        }
    }
}

/**
 * Display challenge statistics.
 */
@Composable
private fun ChallengeStatsRow(previewData: PreviewData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Total Problems: ${previewData.problemCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Operations: ${formatOperationsSummary(previewData.operationsSummary)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Estimated Duration: ${formatDuration(previewData.estimatedDuration)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Display a single problem preview.
 */
@Composable
private fun ProblemPreviewCard(problem: MathProblem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${problem.num1} ${problem.operation.symbol} ${problem.num2} = ${problem.correctAnswer}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Format operations summary for display.
 */
private fun formatOperationsSummary(summary: Map<MathOperation, Int>): String =
    summary.entries.joinToString(", ") { (op, count) ->
        "$count ${op.displayName}"
    }

/**
 * Format duration for display.
 */
private fun formatDuration(duration: Duration): String {
    val minutes = duration.inWholeMinutes
    return if (minutes > 0) {
        "$minutes min"
    } else {
        "${duration.inWholeSeconds} sec"
    }
}

/**
 * Information section for parents with instructions and link to the worksheet creator.
 */
@Composable
private fun ParentInfoSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val worksheetUrl = "https://math-worksheet.gohk.xyz/"

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "For Parents",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "This feature allows you to create custom math challenges for your child using JSON.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text =
                    "You can easily generate challenge JSON using our online worksheet creator at " +
                        "math-worksheet.gohk.xyz. Once generated, copy the JSON and paste it below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    openWorksheetCreator(context, worksheetUrl)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Worksheet Creator")
            }
        }
    }
}

/**
 * Share detection banner indicating JSON was detected and extracted from shared content.
 */
@Composable
private fun ShareDetectionBanner(
    detectedFromShare: Boolean,
    hasValidationErrors: Boolean,
    modifier: Modifier = Modifier,
) {
    if (detectedFromShare) {
        Card(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        if (hasValidationErrors) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                ),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector =
                        if (hasValidationErrors) {
                            Icons.Outlined.Warning
                        } else {
                            Icons.Outlined.Share
                        },
                    contentDescription = null,
                    tint =
                        if (hasValidationErrors) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text =
                        if (hasValidationErrors) {
                            "JSON detected from shared content, but validation found errors. Please review."
                        } else {
                            "JSON detected from shared content!"
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (hasValidationErrors) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                )
            }
        }
    }
}

/**
 * Helper to open the worksheet creator URL using Chrome Custom Tabs with fallback to ACTION_VIEW.
 * Includes error handling to prevent crashes if no browser is available.
 *
 * Note: This duplicates the URL opening pattern from SettingsUi. Consider extracting to a shared
 * utility function if this pattern is needed in more places.
 */
private fun openWorksheetCreator(
    context: Context,
    url: String,
) {
    try {
        val uri = url.toUri()
        val builder = CustomTabsIntent.Builder().setShowTitle(true)
        val customTabsIntent = builder.build()

        customTabsIntent.launchUrl(context, uri)
    } catch (e: Exception) {
        Timber.e(e, "[ImportChallenge] CustomTabs failed, falling back to ACTION_VIEW for URL=%s", url)
        // Fallback to ACTION_VIEW if Custom Tabs fails
        try {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(intent)
        } catch (ignored: Exception) {
            Timber.e(ignored, "[ImportChallenge] Failed to open URL via ACTION_VIEW: %s", url)
        }
    }
}
