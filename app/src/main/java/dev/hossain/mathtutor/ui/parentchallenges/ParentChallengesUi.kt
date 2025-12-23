package dev.hossain.mathtutor.ui.parentchallenges

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.ChallengeType
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import timber.log.Timber
import java.time.Instant

/**
 * UI for [ParentChallengesScreen].
 *
 * Displays all custom challenges in a list with management actions.
 * Shows challenge statistics, practice history, and provides actions
 * for archiving and deleting challenges.
 */
@CircuitInject(ParentChallengesScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentChallengesUi(
    state: ParentChallengesScreen.State,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Timber.d("ParentChallengesUi: Composing with importSuccessMessage=${state.importSuccessMessage}")

    // Show snackbar when import succeeds
    LaunchedEffect(state.importSuccessMessage) {
        Timber.d("ParentChallengesUi: LaunchedEffect triggered, message=${state.importSuccessMessage}")
        state.importSuccessMessage?.let { message ->
            Timber.d("ParentChallengesUi: ⭐ Showing snackbar with message: $message")
            snackbarHostState.showSnackbar(message)
            Timber.d("ParentChallengesUi: Snackbar shown, dismissing")
            state.eventSink(ParentChallengesScreen.Event.DismissImportSuccess)
        }
    }

    BackHandler {
        state.eventSink(ParentChallengesScreen.Event.NavigateBack)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Challenges") },
                navigationIcon = {
                    IconButton(
                        onClick = { state.eventSink(ParentChallengesScreen.Event.NavigateBack) },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { state.eventSink(ParentChallengesScreen.Event.ImportNewChallenge) },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Import Challenge",
                )
            }
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Toggle archived section
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
            ) {
                FilterChip(
                    selected = state.showArchived,
                    onClick = {
                        state.eventSink(
                            ParentChallengesScreen.Event.ToggleArchived(!state.showArchived),
                        )
                    },
                    label = { Text(if (state.showArchived) "Show Active Only" else "Show Archived") },
                )
            }

            // Content based on state
            when {
                state.isLoading -> {
                    LoadingState()
                }

                state.challenges.isEmpty() -> {
                    EmptyState(
                        onImportClick = {
                            state.eventSink(ParentChallengesScreen.Event.ImportNewChallenge)
                        },
                    )
                }

                else -> {
                    ChallengesList(
                        challenges = state.challenges,
                        onChallengeClick = {
                            state.eventSink(ParentChallengesScreen.Event.ChallengeSelected(it))
                        },
                        onArchiveClick = {
                            state.eventSink(ParentChallengesScreen.Event.ArchiveChallenge(it.id))
                        },
                        onDeleteClick = {
                            state.eventSink(
                                ParentChallengesScreen.Event.DeleteChallengeRequested(it),
                            )
                        },
                    )
                }
            }
        }

        // Delete confirmation dialog
        if (state.showDeleteConfirmation && state.challengeToDelete != null) {
            DeleteConfirmationDialog(
                challenge = state.challengeToDelete,
                onConfirm = {
                    state.eventSink(
                        ParentChallengesScreen.Event.ConfirmDelete(state.challengeToDelete.id),
                    )
                },
                onDismiss = {
                    state.eventSink(ParentChallengesScreen.Event.CancelDelete)
                },
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Quiz,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Custom Challenges Yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Create personalized math problems for your child by importing JSON specifications.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onImportClick) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import Challenge")
        }
    }
}

@Composable
private fun ChallengesList(
    challenges: List<CustomChallenge>,
    onChallengeClick: (CustomChallenge) -> Unit,
    onArchiveClick: (CustomChallenge) -> Unit,
    onDeleteClick: (CustomChallenge) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(challenges, key = { it.id }) { challenge ->
            ChallengeListItem(
                challenge = challenge,
                onClick = onChallengeClick,
                onArchiveClick = onArchiveClick,
                onDeleteClick = onDeleteClick,
            )
        }
        // Add bottom padding for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ChallengeListItem(
    challenge: CustomChallenge,
    onClick: (CustomChallenge) -> Unit,
    onArchiveClick: (CustomChallenge) -> Unit,
    onDeleteClick: (CustomChallenge) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick(challenge) }
                .padding(horizontal = 16.dp, vertical = 4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (challenge.subtitle != null) {
                        Text(
                            text = challenge.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                DropdownMenuButton(
                    challenge = challenge,
                    onArchive = onArchiveClick,
                    onDelete = onDeleteClick,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            ChallengeStatsRow(challenge = challenge)

            if (challenge.practiceHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PracticeHistorySection(challenge = challenge)
            }
        }
    }
}

@Composable
private fun DropdownMenuButton(
    challenge: CustomChallenge,
    onArchive: (CustomChallenge) -> Unit,
    onDelete: (CustomChallenge) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(if (challenge.isArchived) "Unarchive" else "Archive") },
                onClick = {
                    onArchive(challenge)
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Archive,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    onDelete(challenge)
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun ChallengeStatsRow(
    challenge: CustomChallenge,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatsChip(
            icon = Icons.Outlined.Quiz,
            text = "${challenge.problems.size} problems",
        )

        val operationsCounts =
            challenge.problems.groupBy { it.operation }.mapValues { it.value.size }
        operationsCounts.forEach { (operation, count) ->
            StatsChip(
                icon = getOperationIcon(operation),
                text = "$count ${operation.displayName.lowercase()}",
            )
        }

        StatsChip(
            icon = Icons.Outlined.Schedule,
            text = "~${estimateDuration(challenge.problems.size)} min",
        )
    }
}

@Composable
private fun StatsChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PracticeHistorySection(
    challenge: CustomChallenge,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Practice History",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val totalSessions = challenge.practiceHistory.size
        val bestScore =
            challenge.practiceHistory.maxOfOrNull {
                (it.correctAnswers.toFloat() / it.problemsAttempted * 100).toInt()
            } ?: 0

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "$totalSessions sessions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Best: $bestScore%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    challenge: CustomChallenge,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Challenge?") },
        text = {
            Text(
                "Are you sure you want to delete \"${challenge.title}\"? This action cannot be undone.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

/**
 * Returns the icon for a given math operation.
 */
private fun getOperationIcon(operation: MathOperation): androidx.compose.ui.graphics.vector.ImageVector =
    when (operation) {
        MathOperation.ADDITION -> Icons.Default.Add

        MathOperation.SUBTRACTION -> Icons.Outlined.Quiz

        // Using quiz as placeholder
        MathOperation.MULTIPLICATION -> Icons.Outlined.Quiz

        // Using quiz as placeholder
        MathOperation.DIVISION -> Icons.Outlined.Quiz

        // Using quiz as placeholder
        MathOperation.MIXED -> Icons.Outlined.Quiz
    }

/**
 * Estimates completion duration in minutes based on problem count.
 * Assumes ~30 seconds per problem on average.
 */
private fun estimateDuration(problemCount: Int): Int {
    val secondsPerProblem = 30
    val totalSeconds = problemCount * secondsPerProblem
    return (totalSeconds / 60).coerceAtLeast(1)
}

// Preview composables
@Preview(showBackground = true)
@Composable
private fun ParentChallengesUiPreview() {
    KidsMathTutorAppTheme {
        ParentChallengesUi(
            state =
                ParentChallengesScreen.State(
                    challenges =
                        listOf(
                            CustomChallenge(
                                id = "1",
                                title = "Addition Practice",
                                subtitle = "Focus on carrying over",
                                type = ChallengeType.GENERATED,
                                problems =
                                    listOf(
                                        MathProblem(
                                            num1 = 15,
                                            num2 = 27,
                                            operation = MathOperation.ADDITION,
                                            correctAnswer = 42,
                                        ),
                                        MathProblem(
                                            num1 = 43,
                                            num2 = 29,
                                            operation = MathOperation.ADDITION,
                                            correctAnswer = 72,
                                        ),
                                    ),
                                createdAt = Instant.now(),
                            ),
                            CustomChallenge(
                                id = "2",
                                title = "Mixed Operations",
                                subtitle = null,
                                type = ChallengeType.EXPLICIT,
                                problems =
                                    listOf(
                                        MathProblem(
                                            num1 = 10,
                                            num2 = 5,
                                            operation = MathOperation.SUBTRACTION,
                                            correctAnswer = 5,
                                        ),
                                    ),
                                createdAt = Instant.now(),
                            ),
                        ),
                    isLoading = false,
                    showArchived = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    KidsMathTutorAppTheme {
        EmptyState(onImportClick = {})
    }
}
