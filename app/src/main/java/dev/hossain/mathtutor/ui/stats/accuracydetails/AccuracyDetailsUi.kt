package dev.hossain.mathtutor.ui.stats.accuracydetails

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import dev.hossain.mathtutor.domain.model.DailyAccuracy
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Max width for content centering on larger screens
private val MAX_CONTENT_WIDTH: Dp = 700.dp

/**
 * UI for [AccuracyDetailsScreen].
 *
 * Displays a list of daily accuracy data with Material 3 design.
 * Shows date, sessions count, problems attempted, and accuracy for each day.
 */
@CircuitInject(AccuracyDetailsScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccuracyDetailsUi(
    state: AccuracyDetailsScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Accuracy") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(AccuracyDetailsScreen.Event.BackPressed) }) {
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
        // Center content on wide screens
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
        ) {
            when {
                state.isLoading -> {
                    LoadingState()
                }

                state.dailyAccuracyList.isEmpty() -> {
                    EmptyState()
                }

                else -> {
                    LazyColumn(
                        modifier =
                            Modifier
                                .widthIn(max = MAX_CONTENT_WIDTH)
                                .fillMaxSize()
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.dailyAccuracyList, key = { it.date.toString() }) { dailyData ->
                            DailyAccuracyCard(dailyData = dailyData)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays loading indicator while data is being loaded.
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Empty state view when no accuracy data exists.
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Math Pup mascot - ready for practice
        Image(
            painter = painterResource(id = R.drawable.pup_tutor_sticker_outdoot_map_and_bagpack),
            contentDescription = "Math Pup ready for practice",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(150.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No practice data yet!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start practicing to see your daily accuracy here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Displays a single day's accuracy data card.
 */
@Composable
private fun DailyAccuracyCard(
    dailyData: DailyAccuracy,
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
            // Date header
            Text(
                text = formatDate(dailyData.date),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    // Sessions and problems info
                    Text(
                        text = "${dailyData.sessionCount} ${if (dailyData.sessionCount == 1) "session" else "sessions"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${dailyData.correctAnswers}/${dailyData.totalProblems} correct",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Accuracy and stars
                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = "${dailyData.accuracy.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StarRating(rating = dailyData.getStarRating())
                }
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
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    },
            )
        }
    }
}

/**
 * Formats a LocalDate to a human-readable string.
 * Shows "Today", "Yesterday", or the date string.
 */
private fun formatDate(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> {
            "Today"
        }

        today.minusDays(1) -> {
            "Yesterday"
        }

        else -> {
            val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
            date.format(formatter)
        }
    }
}

// Preview composables
@Preview(showBackground = true)
@Composable
private fun AccuracyDetailsUiPreview() {
    KidsMathTutorAppTheme {
        AccuracyDetailsUi(
            state =
                AccuracyDetailsScreen.State(
                    dailyAccuracyList =
                        listOf(
                            DailyAccuracy(
                                date = LocalDate.now(),
                                sessionCount = 3,
                                totalProblems = 30,
                                correctAnswers = 27,
                                accuracy = 90f,
                            ),
                            DailyAccuracy(
                                date = LocalDate.now().minusDays(1),
                                sessionCount = 2,
                                totalProblems = 20,
                                correctAnswers = 18,
                                accuracy = 90f,
                            ),
                            DailyAccuracy(
                                date = LocalDate.now().minusDays(2),
                                sessionCount = 1,
                                totalProblems = 10,
                                correctAnswers = 8,
                                accuracy = 80f,
                            ),
                        ),
                    isLoading = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    KidsMathTutorAppTheme {
        AccuracyDetailsUi(
            state =
                AccuracyDetailsScreen.State(
                    dailyAccuracyList = emptyList(),
                    isLoading = false,
                    eventSink = {},
                ),
        )
    }
}

// Adaptive layout previews
@Preview(
    name = "Compact (411dp × 891dp)",
    showBackground = true,
    widthDp = 411,
    heightDp = 891,
)
@Composable
private fun AccuracyDetailsUiCompactPreview() {
    KidsMathTutorAppTheme {
        AccuracyDetailsUi(
            state =
                AccuracyDetailsScreen.State(
                    dailyAccuracyList =
                        listOf(
                            DailyAccuracy(
                                date = LocalDate.now(),
                                sessionCount = 3,
                                totalProblems = 30,
                                correctAnswers = 27,
                                accuracy = 90f,
                            ),
                            DailyAccuracy(
                                date = LocalDate.now().minusDays(1),
                                sessionCount = 2,
                                totalProblems = 20,
                                correctAnswers = 18,
                                accuracy = 90f,
                            ),
                        ),
                    isLoading = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    name = "Medium (700dp × 500dp)",
    showBackground = true,
    widthDp = 700,
    heightDp = 500,
)
@Composable
private fun AccuracyDetailsUiMediumPreview() {
    KidsMathTutorAppTheme {
        AccuracyDetailsUi(
            state =
                AccuracyDetailsScreen.State(
                    dailyAccuracyList =
                        listOf(
                            DailyAccuracy(
                                date = LocalDate.now(),
                                sessionCount = 3,
                                totalProblems = 30,
                                correctAnswers = 27,
                                accuracy = 90f,
                            ),
                            DailyAccuracy(
                                date = LocalDate.now().minusDays(1),
                                sessionCount = 2,
                                totalProblems = 20,
                                correctAnswers = 18,
                                accuracy = 90f,
                            ),
                        ),
                    isLoading = false,
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    name = "Expanded (1100dp × 600dp)",
    showBackground = true,
    widthDp = 1100,
    heightDp = 600,
)
@Composable
private fun AccuracyDetailsUiExpandedPreview() {
    KidsMathTutorAppTheme {
        AccuracyDetailsUi(
            state =
                AccuracyDetailsScreen.State(
                    dailyAccuracyList =
                        listOf(
                            DailyAccuracy(
                                date = LocalDate.now(),
                                sessionCount = 3,
                                totalProblems = 30,
                                correctAnswers = 27,
                                accuracy = 90f,
                            ),
                            DailyAccuracy(
                                date = LocalDate.now().minusDays(1),
                                sessionCount = 2,
                                totalProblems = 20,
                                correctAnswers = 18,
                                accuracy = 90f,
                            ),
                            DailyAccuracy(
                                date = LocalDate.now().minusDays(2),
                                sessionCount = 1,
                                totalProblems = 10,
                                correctAnswers = 8,
                                accuracy = 80f,
                            ),
                        ),
                    isLoading = false,
                    eventSink = {},
                ),
        )
    }
}
