package dev.hossain.mathtutor.ui.importchallenge

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PreviewData
import dev.zacsweers.metro.AppScope
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
            item {
                JsonInputSection(
                    jsonInput = state.jsonInput,
                    validationState = state.validationState,
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
    validationState: ValidationState,
    onJsonChanged: (String) -> Unit,
    onValidate: () -> Unit,
    onClear: () -> Unit,
) {
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
                isError = validationState is ValidationState.Invalid,
                textStyle = MaterialTheme.typography.bodySmall,
            )

            if (validationState is ValidationState.Invalid) {
                Spacer(modifier = Modifier.height(8.dp))
                ValidationErrorsDisplay(errors = validationState.fieldErrors)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onClear) {
                    Text("Clear")
                }

                Button(
                    onClick = onValidate,
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
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = previewData.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )

            if (previewData.subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = previewData.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Challenge statistics
            ChallengeStatsRow(previewData = previewData)

            Spacer(modifier = Modifier.height(16.dp))

            // Sample problems preview
            Text(
                text = "Sample Problems:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
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
