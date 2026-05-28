package dev.hossain.mathtutor.ui.importchallenge

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.highlight.ui.ExperimentalHighlightApi
import dev.hossain.highlight.ui.HighlightThemeProvider
import dev.hossain.highlight.ui.SyntaxHighlightedTextEditor
import dev.hossain.highlight.ui.rememberTomorrowNightTheme
import dev.hossain.highlight.ui.rememberTomorrowTheme
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PreviewData
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MAX_CONTENT_WIDTH_STANDARD
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
        val lazyListState = rememberLazyListState()

        // Auto-scroll to preview when validation is successful
        LaunchedEffect(state.validationState, state.previewData) {
            if (state.validationState is ValidationState.Valid && state.previewData != null) {
                // Scroll to preview with slower animation by adding a small delay
                // This gives a more relaxed feel to the automatic scroll
                // Items: 0-detectedJsonBanner (conditional), 1-guide, 2-validation success (conditional), 3-json input, 4-preview
                val previewIndex = 4
                kotlinx.coroutines.delay(400)
                lazyListState.animateScrollToItem(previewIndex)
            }
        }

        // Center content on wide screens with max width constraint
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                state = lazyListState,
                modifier =
                    Modifier
                        .widthIn(max = MAX_CONTENT_WIDTH_STANDARD)
                        .fillMaxSize(),
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
                    ParentInfoSection(
                        isExpanded = state.isGuideExpanded,
                        onToggleExpand = { state.eventSink(ImportChallengeScreen.Event.ToggleGuideExpanded) },
                    )
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
    var editorValue by remember { mutableStateOf(TextFieldValue(jsonInput)) }
    val sampleJson =
        """
        {
          "type": "generated",
          "title": "Addition Practice",
          "subtitle": "Numbers 1-10",
          "operation": "addition",
          "problemCount": 10,
          "numberRange": {"min": 1, "max": 10}
        }
        """.trimIndent()

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

            @OptIn(ExperimentalHighlightApi::class)
            HighlightThemeProvider(
                lightHighlightTheme = rememberTomorrowTheme(),
                darkHighlightTheme = rememberTomorrowNightTheme(),
            ) {
                SyntaxHighlightedTextEditor(
                    value = editorValue,
                    onValueChange = { editorValue = it },
                    language = "json",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .heightIn(min = 200.dp, max = 300.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(12.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sample JSON suggestion
            if (jsonInput.isBlank()) {
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Example JSON Format:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = sampleJson,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { onJsonChanged(sampleJson) },
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text("Use This Example", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Action buttons
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
 * Display validation errors as a section above the input field with helpful guidance.
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
        Column(modifier = Modifier.padding(16.dp)) {
            // Error header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Validation Failed",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detailed error messages
            errors.forEach { (field, error) ->
                if (field == "general" || field == "duplicate") {
                    // General or duplicate errors show as full-width messages
                    Text(
                        text = "⚠ $error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                } else {
                    // Field-specific errors show with field name
                    Text(
                        text = "• ${fieldDisplayName(field)}: $error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Helpful hint
            Text(
                text = "💡 Tip: Check the JSON format and ensure all required fields are present.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
            )
        }
    }
}

/**
 * Convert field name to display-friendly format.
 */
private fun fieldDisplayName(field: String): String =
    when (field) {
        "title" -> "Challenge Title"
        "operation" -> "Operation Type"
        "problemCount" -> "Number of Problems"
        "numberRange" -> "Number Range"
        "type" -> "Challenge Type"
        "problems" -> "Problems"
        "subtitle" -> "Subtitle (Optional)"
        else -> field.replaceFirstChar { it.uppercase() }
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
 * Information section for parents with quick-start guide and link to the worksheet creator.
 * Can be collapsed and expanded with state persistence.
 */
@Composable
private fun ParentInfoSection(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            // Header with expand/collapse button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .weight(1f)
                            .clickable(enabled = true, onClick = onToggleExpand),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quick Start Guide",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                // Expand/Collapse button
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector =
                            if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse guide" else "Expand guide",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            // Animated content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Step-by-step instructions
                    QuickStartStep(
                        stepNumber = 1,
                        title = "Create Challenge",
                        description = "Use our worksheet creator to design custom math challenges",
                        onButtonClick = {
                            openWorksheetCreator(context, worksheetUrl)
                            // Collapse the guide after opening the creator
                            onToggleExpand()
                        },
                        buttonLabel = "Open Creator",
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickStartStep(
                        stepNumber = 2,
                        title = "Copy JSON",
                        description = "The creator generates a JSON specification you can copy",
                        onButtonClick = null,
                        buttonLabel = null,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickStartStep(
                        stepNumber = 3,
                        title = "Paste & Save",
                        description = "Paste the JSON below, preview it, and save to your library",
                        onButtonClick = null,
                        buttonLabel = null,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

/**
 * Individual step in the quick-start guide.
 */
@Composable
private fun QuickStartStep(
    stepNumber: Int,
    title: String,
    description: String,
    onButtonClick: (() -> Unit)?,
    buttonLabel: String?,
    textColor: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        // Step number badge
        Card(
            modifier =
                Modifier
                    .size(32.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = textColor.copy(alpha = 0.3f),
                ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = textColor,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.9f),
            )

            if (onButtonClick != null && buttonLabel != null) {
                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onButtonClick,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(buttonLabel, style = MaterialTheme.typography.labelLarge)
                }
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
                            MaterialTheme.colorScheme.tertiaryContainer
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

// ==================== Previews ====================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ImportChallengeUiPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput = "",
                    validationState = ValidationState.Idle,
                    previewData = null,
                    isLoading = false,
                    detectedJsonFromShare = false,
                    isGuideExpanded = true,
                    eventSink = {},
                ),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ImportChallengeUiWithJsonPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput =
                        """
                        {
                          "type": "generated",
                          "title": "Addition Practice",
                          "subtitle": "Numbers 1-20",
                          "operation": "addition",
                          "problemCount": 10,
                          "numberRange": {"min": 1, "max": 20}
                        }
                        """.trimIndent(),
                    validationState = ValidationState.Idle,
                    previewData = null,
                    isLoading = false,
                    detectedJsonFromShare = false,
                    isGuideExpanded = true,
                    eventSink = {},
                ),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ImportChallengeUiWithValidationSuccessPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput =
                        """
                        {
                          "type": "generated",
                          "title": "Subtraction Challenge",
                          "operation": "subtraction",
                          "problemCount": 5,
                          "numberRange": {"min": 0, "max": 10}
                        }
                        """.trimIndent(),
                    validationState = ValidationState.Valid,
                    previewData =
                        PreviewData(
                            title = "Subtraction Challenge",
                            subtitle = null,
                            problemCount = 5,
                            operationsSummary = mapOf(MathOperation.SUBTRACTION to 5),
                            sampleProblems =
                                listOf(
                                    MathProblem(
                                        id = "1",
                                        num1 = 10,
                                        num2 = 3,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 7,
                                    ),
                                    MathProblem(
                                        id = "2",
                                        num1 = 7,
                                        num2 = 2,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 5,
                                    ),
                                    MathProblem(
                                        id = "3",
                                        num1 = 9,
                                        num2 = 4,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 5,
                                    ),
                                ),
                            estimatedDuration = Duration.parse("PT5M"),
                        ),
                    isLoading = false,
                    detectedJsonFromShare = false,
                    isGuideExpanded = true,
                    eventSink = {},
                ),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ImportChallengeUiWithValidationErrorsPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput =
                        """
                        {
                          "type": "generated",
                          "title": "",
                          "operation": "addition",
                          "problemCount": 0,
                          "numberRange": {"min": 10, "max": 5}
                        }
                        """.trimIndent(),
                    validationState =
                        ValidationState.Invalid(
                            mapOf(
                                "title" to "Title is required",
                                "problemCount" to "Problem count must be between 1 and 50",
                                "numberRange" to "Minimum must be less than maximum",
                            ),
                        ),
                    previewData = null,
                    isLoading = false,
                    detectedJsonFromShare = false,
                    isGuideExpanded = true,
                    eventSink = {},
                ),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ImportChallengeUiLoadingPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput =
                        """
                        {
                          "type": "explicit",
                          "title": "Mixed Practice",
                          "problems": [
                            {"operand1": 5, "operand2": 3, "operation": "addition"},
                            {"operand1": 8, "operand2": 2, "operation": "subtraction"}
                          ]
                        }
                        """.trimIndent(),
                    validationState = ValidationState.Valid,
                    previewData =
                        PreviewData(
                            title = "Mixed Practice",
                            subtitle = null,
                            problemCount = 2,
                            operationsSummary =
                                mapOf(
                                    MathOperation.ADDITION to 1,
                                    MathOperation.SUBTRACTION to 1,
                                ),
                            sampleProblems =
                                listOf(
                                    MathProblem(
                                        id = "1",
                                        num1 = 5,
                                        num2 = 3,
                                        operation = MathOperation.ADDITION,
                                        correctAnswer = 8,
                                    ),
                                    MathProblem(
                                        id = "2",
                                        num1 = 8,
                                        num2 = 2,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 6,
                                    ),
                                ),
                            estimatedDuration = Duration.parse("PT2M"),
                        ),
                    isLoading = true,
                    detectedJsonFromShare = false,
                    isGuideExpanded = true,
                    eventSink = {},
                ),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ImportChallengeUiWithShareDetectionPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput =
                        """
                        {
                          "type": "generated",
                          "title": "Addition Practice",
                          "subtitle": "From shared link",
                          "operation": "addition",
                          "problemCount": 10,
                          "numberRange": {"min": 1, "max": 20}
                        }
                        """.trimIndent(),
                    validationState = ValidationState.Idle,
                    previewData = null,
                    isLoading = false,
                    detectedJsonFromShare = true,
                    isGuideExpanded = true,
                    eventSink = {},
                ),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ImportChallengeUiWithShareDetectionAndErrorsPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput =
                        """
                        {
                          "type": "generated",
                          "title": "",
                          "operation": "addition",
                          "problemCount": 0
                        }
                        """.trimIndent(),
                    validationState =
                        ValidationState.Invalid(
                            mapOf(
                                "title" to "Title is required",
                                "problemCount" to "Problem count must be between 1 and 50",
                                "numberRange" to "Number range is required",
                            ),
                        ),
                    previewData = null,
                    isLoading = false,
                    detectedJsonFromShare = true,
                    isGuideExpanded = true,
                    eventSink = {},
                ),
        )
    }
}

// Responsive previews for adaptive layout testing

@androidx.compose.ui.tooling.preview.Preview(
    name = "Compact - Import Challenge",
    showBackground = true,
    device = "spec:width=411dp,height=891dp",
)
@Composable
private fun ImportChallengeUiCompactPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput = "",
                    validationState = ValidationState.Idle,
                    previewData = null,
                    isLoading = false,
                    detectedJsonFromShare = false,
                    isGuideExpanded = false,
                    eventSink = {},
                ),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Medium - Import Challenge",
    showBackground = true,
    device = "spec:width=700dp,height=500dp",
)
@Composable
private fun ImportChallengeUiMediumPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput =
                        """
                        {
                          "type": "generated",
                          "title": "Addition Practice",
                          "operation": "addition",
                          "problemCount": 10,
                          "numberRange": {"min": 1, "max": 20}
                        }
                        """.trimIndent(),
                    validationState = ValidationState.Idle,
                    previewData = null,
                    isLoading = false,
                    detectedJsonFromShare = false,
                    isGuideExpanded = false,
                    eventSink = {},
                ),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Expanded - Import Challenge with Preview",
    showBackground = true,
    device = "spec:width=1100dp,height=600dp",
)
@Composable
private fun ImportChallengeUiExpandedPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput =
                        """
                        {
                          "type": "generated",
                          "title": "Subtraction Challenge",
                          "operation": "subtraction",
                          "problemCount": 5,
                          "numberRange": {"min": 0, "max": 10}
                        }
                        """.trimIndent(),
                    validationState = ValidationState.Valid,
                    previewData =
                        PreviewData(
                            title = "Subtraction Challenge",
                            subtitle = null,
                            problemCount = 5,
                            operationsSummary = mapOf(MathOperation.SUBTRACTION to 5),
                            sampleProblems =
                                listOf(
                                    MathProblem(
                                        id = "1",
                                        num1 = 10,
                                        num2 = 3,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 7,
                                    ),
                                    MathProblem(
                                        id = "2",
                                        num1 = 7,
                                        num2 = 2,
                                        operation = MathOperation.SUBTRACTION,
                                        correctAnswer = 5,
                                    ),
                                ),
                            estimatedDuration = Duration.parse("PT5M"),
                        ),
                    isLoading = false,
                    detectedJsonFromShare = false,
                    isGuideExpanded = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=891dp,height=411dp,dpi=420,isRound=false,orientation=landscape",
    name = "Phone Landscape",
)
@Composable
private fun ImportChallengeUiPhoneLandscapePreview() {
    KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput = "",
                    validationState = ValidationState.Idle,
                    previewData = null,
                    isLoading = false,
                    detectedJsonFromShare = false,
                    isGuideExpanded = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240,isRound=false,orientation=portrait",
    name = "Tablet Portrait",
)
@Composable
private fun ImportChallengeUiTabletPortraitPreview() {
    KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput =
                        "{\"title\":\"Sample Challenge\",\"problems\":" +
                            "[{\"num1\":5,\"num2\":3,\"operation\":\"ADDITION\",\"correctAnswer\":8}]}",
                    validationState = ValidationState.Idle,
                    previewData = null,
                    isLoading = false,
                    detectedJsonFromShare = false,
                    isGuideExpanded = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,isRound=false,orientation=landscape",
    name = "Tablet Landscape",
)
@Composable
private fun ImportChallengeUiTabletLandscapePreview() {
    KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput = "",
                    validationState = ValidationState.Idle,
                    previewData = null,
                    isLoading = false,
                    detectedJsonFromShare = true,
                    isGuideExpanded = true,
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=673dp,height=841dp,dpi=373,isRound=false,orientation=portrait",
    name = "Foldable Portrait (Pixel Fold Unfolded)",
)
@Composable
private fun ImportChallengeUiFoldablePortraitPreview() {
    KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput = "",
                    validationState = ValidationState.Idle,
                    previewData = null,
                    isLoading = true,
                    detectedJsonFromShare = false,
                    isGuideExpanded = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=841dp,height=673dp,dpi=373,isRound=false,orientation=landscape",
    name = "Foldable Landscape (Pixel Fold Unfolded)",
)
@Composable
private fun ImportChallengeUiFoldableLandscapePreview() {
    KidsMathTutorAppTheme {
        ImportChallengeUi(
            state =
                ImportChallengeScreen.State(
                    jsonInput = "{\"title\":\"Test\"}",
                    validationState = ValidationState.Idle,
                    previewData = null,
                    isLoading = false,
                    detectedJsonFromShare = false,
                    isGuideExpanded = false,
                    eventSink = {},
                ),
        )
    }
}
