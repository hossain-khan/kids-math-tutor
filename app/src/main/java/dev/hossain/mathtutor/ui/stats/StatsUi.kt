package dev.hossain.mathtutor.ui.stats

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.hossain.mathtutor.util.TimeFormatter
import dev.zacsweers.metro.AppScope
import java.time.Instant

// Width breakpoints for adaptive layouts
private val MEDIUM_WIDTH_BREAKPOINT: Dp = 600.dp
private val MAX_CONTENT_WIDTH: Dp = 840.dp

/**
 * UI for [StatsScreen].
 *
 * Displays practice statistics including overall progress, per-operation breakdown,
 * and recent session history with Material 3 design.
 *
 * Adaptive Layout:
 * - Compact: Single column, full width
 * - Medium/Expanded: Centered content with max width
 */
@CircuitInject(StatsScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsUi(
    state: StatsScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Stats")
                },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(StatsScreen.Event.BackPressed) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            val isWideScreen = maxWidth >= MEDIUM_WIDTH_BREAKPOINT

            // Center content on wide screens
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                if (state.overallStats.sessionCount == 0) {
                    // Empty state
                    EmptyStatsView(
                        onStartPractice = { state.eventSink(StatsScreen.Event.BackPressed) },
                        modifier = Modifier.widthIn(max = MAX_CONTENT_WIDTH),
                    )
                } else {
                    // Stats content
                    LazyColumn(
                        modifier =
                            Modifier
                                .widthIn(max = MAX_CONTENT_WIDTH)
                                .fillMaxSize()
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Overall Progress Section
                        item {
                            Text(
                                text = "Overall Progress",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        item {
                            OverallProgressCards(stats = state.overallStats)
                        }

                        // By Operation Section
                        if (state.operationStats.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "By Operation",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            // Show operation stats in grid on wide screens
                            if (isWideScreen && state.operationStats.size >= 2) {
                                items(state.operationStats.entries.chunked(2)) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        rowItems.forEach { (operation, stats) ->
                                            OperationStatsCard(
                                                operation = operation,
                                                stats = stats,
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                        // Fill remaining space if odd number of items
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                items(state.operationStats.entries.toList()) { (operation, stats) ->
                                    OperationStatsCard(
                                        operation = operation,
                                        stats = stats,
                                    )
                                }
                            }
                        }

                        // Recent Sessions Section
                        if (state.recentSessions.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Recent Sessions",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            items(state.recentSessions) { session ->
                                RecentSessionItem(session = session)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays overall progress cards showing total problems and accuracy.
 */
@Composable
private fun OverallProgressCards(
    stats: SessionStats,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Total Problems Card
        Card(
            modifier = Modifier.weight(1f),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Total Problems",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stats.totalProblems.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        // Overall Accuracy Card
        Card(
            modifier = Modifier.weight(1f),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Accuracy",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stats.accuracy.toInt()}%",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                StarRating(rating = stats.getStarRating())
            }
        }
    }
}

/**
 * Displays star rating visualization.
 */
@Composable
private fun StarRating(
    rating: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        repeat(5) { index ->
            Icon(
                imageVector =
                    if (index < rating) {
                        Icons.Filled.Star
                    } else {
                        Icons.Outlined.StarOutline
                    },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint =
                    if (index < rating) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f)
                    },
            )
        }
    }
}

/**
 * Displays statistics card for a specific operation.
 */
@Composable
private fun OperationStatsCard(
    operation: MathOperation,
    stats: SessionStats,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Operation Icon
            Icon(
                imageVector =
                    when (operation) {
                        MathOperation.ADDITION -> Icons.Default.Add
                        MathOperation.SUBTRACTION -> Icons.Default.Remove
                        else -> Icons.Default.Add
                    },
                contentDescription = operation.displayName,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Stats
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = operation.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stats.totalProblems} problems • ${stats.accuracy.toInt()}% accuracy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Star Rating
            StarRating(rating = stats.getStarRating())
        }
    }
}

/**
 * Displays a single recent session item.
 */
@Composable
private fun RecentSessionItem(
    session: PracticeSessionEntity,
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            // Timestamp
            Text(
                text = TimeFormatter.formatRelativeTime(session.timestamp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Operation and Score
                Column {
                    Text(
                        text = session.operation.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${session.correctAnswers}/${session.totalProblems} correct",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Accuracy Badge
                Text(
                    text = "${session.accuracy.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Empty state view when no sessions exist.
 */
@Composable
private fun EmptyStatsView(
    onStartPractice: () -> Unit,
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
        // Math Pup mascot with map and backpack - ready for adventure!
        Image(
            painter = painterResource(id = R.drawable.pup_tutor_sticker_outdoot_map_and_bagpack),
            contentDescription = "Math Pup ready for practice",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(150.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No practice sessions yet!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start practicing to see your stats here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
private fun StatsUiPreview() {
    KidsMathTutorAppTheme {
        StatsUi(
            state =
                StatsScreen.State(
                    overallStats =
                        SessionStats(
                            totalProblems = 50,
                            correctCount = 45,
                            accuracy = 90f,
                            sessionCount = 5,
                        ),
                    operationStats =
                        mapOf(
                            MathOperation.ADDITION to
                                SessionStats(
                                    totalProblems = 30,
                                    correctCount = 27,
                                    accuracy = 90f,
                                    sessionCount = 3,
                                ),
                            MathOperation.SUBTRACTION to
                                SessionStats(
                                    totalProblems = 20,
                                    correctCount = 18,
                                    accuracy = 90f,
                                    sessionCount = 2,
                                ),
                        ),
                    recentSessions =
                        listOf(
                            PracticeSessionEntity(
                                id = 1,
                                operation = MathOperation.ADDITION,
                                totalProblems = 10,
                                correctAnswers = 9,
                                incorrectAnswers = 1,
                                accuracy = 90f,
                                durationSeconds = 120,
                                timestamp = Instant.now(),
                            ),
                            PracticeSessionEntity(
                                id = 2,
                                operation = MathOperation.SUBTRACTION,
                                totalProblems = 10,
                                correctAnswers = 8,
                                incorrectAnswers = 2,
                                accuracy = 80f,
                                durationSeconds = 150,
                                timestamp = Instant.now().minusSeconds(86400),
                            ),
                        ),
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatsUiPreview() {
    KidsMathTutorAppTheme {
        StatsUi(
            state =
                StatsScreen.State(
                    overallStats = SessionStats.EMPTY,
                    operationStats = emptyMap(),
                    recentSessions = emptyList(),
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsUiDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        StatsUi(
            state =
                StatsScreen.State(
                    overallStats =
                        SessionStats(
                            totalProblems = 50,
                            correctCount = 45,
                            accuracy = 90f,
                            sessionCount = 5,
                        ),
                    operationStats =
                        mapOf(
                            MathOperation.ADDITION to
                                SessionStats(
                                    totalProblems = 30,
                                    correctCount = 27,
                                    accuracy = 90f,
                                    sessionCount = 3,
                                ),
                        ),
                    recentSessions =
                        listOf(
                            PracticeSessionEntity(
                                id = 1,
                                operation = MathOperation.ADDITION,
                                totalProblems = 10,
                                correctAnswers = 9,
                                incorrectAnswers = 1,
                                accuracy = 90f,
                                durationSeconds = 120,
                                timestamp = Instant.now(),
                            ),
                        ),
                    eventSink = {},
                ),
        )
    }
}

// Tablet previews for adaptive layout testing

@Preview(
    showBackground = true,
    widthDp = 700,
    heightDp = 500,
    name = "Tablet Landscape",
)
@Composable
private fun StatsUiTabletLandscapePreview() {
    KidsMathTutorAppTheme {
        StatsUi(
            state =
                StatsScreen.State(
                    overallStats =
                        SessionStats(
                            totalProblems = 150,
                            correctCount = 135,
                            accuracy = 90f,
                            sessionCount = 15,
                        ),
                    operationStats =
                        mapOf(
                            MathOperation.ADDITION to
                                SessionStats(
                                    totalProblems = 80,
                                    correctCount = 72,
                                    accuracy = 90f,
                                    sessionCount = 8,
                                ),
                            MathOperation.SUBTRACTION to
                                SessionStats(
                                    totalProblems = 70,
                                    correctCount = 63,
                                    accuracy = 90f,
                                    sessionCount = 7,
                                ),
                        ),
                    recentSessions =
                        listOf(
                            PracticeSessionEntity(
                                id = 1,
                                operation = MathOperation.ADDITION,
                                totalProblems = 10,
                                correctAnswers = 9,
                                incorrectAnswers = 1,
                                accuracy = 90f,
                                durationSeconds = 120,
                                timestamp = Instant.now(),
                            ),
                        ),
                    eventSink = {},
                ),
        )
    }
}
