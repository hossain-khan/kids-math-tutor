package dev.hossain.mathtutor.ui.parentchallenges

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.domain.model.ChallengeType
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.ui.component.FeatureTopAppBar
import dev.hossain.mathtutor.ui.component.TopBarFeature
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
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

    // Show snackbar when import succeeds
    LaunchedEffect(state.importSuccessMessage) {
        state.importSuccessMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            state.eventSink(ParentChallengesScreen.Event.DismissImportSuccess)
        }
    }

    Scaffold(
        topBar = {
            FeatureTopAppBar(
                title = { Text("Custom Challenges") },
                feature = TopBarFeature.STATS,
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
            // Stats and filter header
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Stats on the left
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text =
                            if (state.showArchived) {
                                "Archived Challenges"
                            } else {
                                "Active Challenges"
                            },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${state.challenges.size} challenge${if (state.challenges.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val totalSessions =
                        state.challenges.sumOf { it.practiceHistory.size }
                    if (totalSessions > 0) {
                        Text(
                            text =
                                "$totalSessions practice session${if (totalSessions != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Toggle button on the right
                FilterChip(
                    selected = state.showArchived,
                    onClick = {
                        state.eventSink(
                            ParentChallengesScreen.Event.ToggleArchived(!state.showArchived),
                        )
                    },
                    label = {
                        Text(
                            if (state.showArchived) "Show Active Only" else "Show Archived",
                        )
                    },
                )
            }

            // Content based on state
            when {
                state.isLoading -> {
                    LoadingState()
                }

                state.challenges.isEmpty() -> {
                    EmptyState(
                        showArchived = state.showArchived,
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
                            state.eventSink(ParentChallengesScreen.Event.ArchiveChallenge(it))
                        },
                        onClearSessionsClick = {
                            state.eventSink(
                                ParentChallengesScreen.Event.ClearSessionsRequested(it),
                            )
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

        // Clear sessions confirmation dialog
        if (state.showClearSessionsConfirmation && state.challengeToClearSessions != null) {
            ClearSessionsConfirmationDialog(
                challenge = state.challengeToClearSessions,
                onConfirm = {
                    state.eventSink(
                        ParentChallengesScreen.Event.ConfirmClearSessions(state.challengeToClearSessions.id),
                    )
                },
                onDismiss = {
                    state.eventSink(ParentChallengesScreen.Event.CancelClearSessions)
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
    showArchived: Boolean,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Hero image based on state
        val heroImageRes =
            if (showArchived) {
                R.drawable.hero_custom_challenge_empty
            } else {
                R.drawable.hero_parent_teaching_kid
            }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
        ) {
            Image(
                painter = painterResource(id = heroImageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )

            // Gradient overlay at top (20%)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.2f)
                        .align(Alignment.TopCenter)
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.surface,
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                        ),
                                ),
                        ),
            )

            // Gradient overlay at bottom (20%)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.2f)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                            MaterialTheme.colorScheme.surface,
                                        ),
                                ),
                        ),
            )
        }

        Text(
            text =
                if (showArchived) {
                    "No Archived Challenges"
                } else {
                    "No Custom Challenges Yet"
                },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Text(
            text =
                if (showArchived) {
                    "Archive completed challenges to keep your list organized."
                } else {
                    "Create personalized math problems for your child by importing JSON specifications."
                },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (!showArchived) {
            Button(onClick = onImportClick) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Challenge")
            }
        }
    }
}

@Composable
private fun ChallengesList(
    challenges: List<CustomChallenge>,
    onChallengeClick: (CustomChallenge) -> Unit,
    onArchiveClick: (CustomChallenge) -> Unit,
    onClearSessionsClick: (CustomChallenge) -> Unit,
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
                onClearSessionsClick = onClearSessionsClick,
                onDeleteClick = onDeleteClick,
                modifier = Modifier.animateItem(),
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
    onClearSessionsClick: (CustomChallenge) -> Unit,
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
                    onClearSessions = onClearSessionsClick,
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
    onClearSessions: (CustomChallenge) -> Unit,
    onDelete: (CustomChallenge) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier =
                Modifier
                    .size(48.dp) // Increased from default 40.dp for bigger touch area
                    .padding(4.dp), // Padding inside the button for the icon
        ) {
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
                text = { Text("Clear Sessions") },
                onClick = {
                    onClearSessions(challenge)
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                    )
                },
                enabled = challenge.practiceHistory.isNotEmpty(),
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
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
 * Confirmation dialog for clearing practice sessions.
 */
@Composable
private fun ClearSessionsConfirmationDialog(
    challenge: CustomChallenge,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear Practice Sessions?") },
        text = {
            Text(
                "Are you sure you want to clear all practice sessions for \"${challenge.title}\"? " +
                    "This will remove the practice history but keep the challenge. This action cannot be undone.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Clear")
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
        EmptyState(showArchived = false, onImportClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateArchivedPreview() {
    KidsMathTutorAppTheme {
        EmptyState(showArchived = true, onImportClick = {})
    }
}
