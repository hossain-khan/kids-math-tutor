package dev.hossain.mathtutor.ui.goals.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.model.goals.ComponentProgress
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalProgressUi(
    state: GoalProgressScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Goal Progress") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(GoalProgressScreen.Event.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text("Loading goal progress...")
            }
        } else if (state.error != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        "Error: ${state.error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { state.eventSink(GoalProgressScreen.Event.DismissError) }) {
                        Text("Dismiss")
                    }
                }
            }
        } else if (state.activeGoal != null) {
            GoalProgressContent(
                activeGoal = state.activeGoal,
                currentComponentIndex = state.currentComponentIndex,
                overallProgress = state.overallProgress,
                eventSink = state.eventSink,
                modifier = Modifier.padding(paddingValues),
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text("No active goal")
            }
        }
    }
}

@Composable
private fun GoalProgressContent(
    activeGoal: ActiveGoal,
    currentComponentIndex: Int,
    overallProgress: Float,
    eventSink: (GoalProgressScreen.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Goal title
        item {
            Text(
                activeGoal.goal.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Overall progress
        item {
            OverallProgressCard(
                progress = overallProgress,
                completedSessions = activeGoal.componentProgress.sumOf { it.completedSessions },
                totalSessions = activeGoal.goal.components.sumOf { it.sessionCount },
            )
        }

        // Components list
        itemsIndexed(activeGoal.goal.components) { index, component ->
            val progress = activeGoal.componentProgress.getOrNull(index)
            ComponentCard(
                component = component,
                progress = progress,
                isCurrentComponent = index == currentComponentIndex,
                isCompleted = progress?.isComplete == true,
                onStartClick = {
                    eventSink(GoalProgressScreen.Event.StartComponent(index))
                },
            )
        }

        // Start next session button
        item {
            val nextComponentIndex = activeGoal.componentProgress.indexOfFirst { !it.isComplete }
            if (nextComponentIndex >= 0) {
                Button(
                    onClick = {
                        eventSink(GoalProgressScreen.Event.StartComponent(nextComponentIndex))
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Start Next Session")
                }
            } else {
                Text(
                    "🎉 All sessions complete!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OverallProgressCard(
    progress: Float,
    completedSessions: Int,
    totalSessions: Int,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "Progress animation")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Overall Progress",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "$completedSessions/$totalSessions Sessions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    "${(animatedProgress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun ComponentCard(
    component: dev.hossain.mathtutor.domain.model.goals.GoalComponent,
    progress: ComponentProgress?,
    isCurrentComponent: Boolean,
    isCompleted: Boolean,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        when {
            isCompleted ->
                MaterialTheme.colorScheme.surfaceVariant
            isCurrentComponent ->
                MaterialTheme.colorScheme.secondaryContainer
            else ->
                MaterialTheme.colorScheme.surface
        }

    Card(
        modifier =
            modifier
                .fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        component.getDescription(),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    if (isCurrentComponent && !isCompleted) {
                        Text(
                            "Current Component",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }

                if (isCompleted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (progress != null && !isCompleted) {
                LinearProgressIndicator(
                    progress =
                        progress.completedSessions.toFloat() /
                            maxOf(1, component.sessionCount),
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    "${progress.completedSessions}/${component.sessionCount} Sessions",
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            if (!isCompleted) {
                Button(
                    onClick = onStartClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Start Session")
                }
            }
        }
    }
}

@Composable
private fun PaddingValues(
    horizontal: androidx.compose.ui.unit.Dp,
): androidx.compose.foundation.layout.PaddingValues {
    return androidx.compose.foundation.layout.PaddingValues(horizontal = horizontal)
}
