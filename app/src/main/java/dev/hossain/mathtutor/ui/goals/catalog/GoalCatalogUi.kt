package dev.hossain.mathtutor.ui.goals.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.di.AppScope
import me.tatarka.inject.annotations.Inject

@Inject
class GoalCatalogUiFactory : (CircuitContext) -> Unit by { }

@Composable
fun GoalCatalogUi(
    state: GoalCatalogScreen.State,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Goal Management",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { state.eventSink(GoalCatalogScreen.Event.CreateNewGoal) },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create New Goal")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                state.goals.isEmpty() -> {
                    EmptyGoalsState(
                        modifier = Modifier.align(Alignment.Center),
                        onCreateGoal = {
                            state.eventSink(GoalCatalogScreen.Event.CreateNewGoal)
                        },
                    )
                }

                else -> {
                    GoalsListView(
                        goals = state.goals,
                        activeGoalId = state.activeGoalId,
                        onActivate = { goalId ->
                            state.eventSink(
                                GoalCatalogScreen.Event.ActivateGoal(goalId),
                            )
                        },
                        onDelete = { goalId ->
                            state.eventSink(GoalCatalogScreen.Event.DeleteGoal(goalId))
                        },
                        onViewHistory = { goalId ->
                            state.eventSink(
                                GoalCatalogScreen.Event.ViewHistory(goalId),
                            )
                        },
                        onArchive = { goalId ->
                            state.eventSink(
                                GoalCatalogScreen.Event.ArchiveGoal(goalId),
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Error snackbar
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        Text(
                            text = "Dismiss",
                            modifier = Modifier.padding(8.dp),
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ) {
                    Text(state.error.orEmpty())
                }
            }
        }
    }
}

@Composable
private fun GoalsListView(
    goals: List<Goal>,
    activeGoalId: String?,
    onActivate: (String) -> Unit,
    onDelete: (String) -> Unit,
    onViewHistory: (String) -> Unit,
    onArchive: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(goals, key = { it.id }) { goal ->
            GoalItem(
                goal = goal,
                isActive = goal.id == activeGoalId,
                onActivate = { onActivate(goal.id) },
                onDelete = { onDelete(goal.id) },
                onViewHistory = { onViewHistory(goal.id) },
                onArchive = { onArchive(goal.id) },
            )
        }
    }
}

@Composable
private fun GoalItem(
    goal: Goal,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    onViewHistory: () -> Unit,
    onArchive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (goal.description != null) {
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Text(
                text = "${goal.components.size} components, ${goal.getTotalSessions()} sessions",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            if (isActive) {
                Text(
                    text = "Active Goal ✓",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            // Action buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            ) {
                androidx.compose.material3.Button(
                    onClick = onActivate,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Assign to Child")
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = onViewHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text("View History")
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun EmptyGoalsState(
    onCreateGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "No Goals Yet",
            style = MaterialTheme.typography.headlineSmall,
        )

        Text(
            text = "Create a goal to get started with guided math practice.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        androidx.compose.material3.Button(
            onClick = onCreateGoal,
        ) {
            Text("Create First Goal")
        }
    }
}
