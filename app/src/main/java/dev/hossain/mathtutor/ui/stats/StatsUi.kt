package dev.hossain.mathtutor.ui.stats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.ui.component.FeatureTopAppBar
import dev.hossain.mathtutor.ui.component.TopBarFeature
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.hossain.mathtutor.ui.theme.watermarkFontFamily
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.EXTENDED_WIDTH_BREAKPOINT
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MAX_CONTENT_WIDTH_MEDIUM
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MEDIUM_WIDTH_BREAKPOINT
import dev.hossain.mathtutor.util.TimeFormatter
import dev.zacsweers.metro.AppScope
import java.time.Instant

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
            FeatureTopAppBar(
                title = {
                    Text(
                        if (!state.userName.isNullOrBlank()) {
                            "${state.userName}'s Progress"
                        } else {
                            "My Progress"
                        },
                    )
                },
                feature = TopBarFeature.STATS,
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(StatsScreen.Event.BackPressed) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
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
            val isExpandedScreen = maxWidth >= EXTENDED_WIDTH_BREAKPOINT

            // Center content on wide screens
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                if (state.overallStats.sessionCount == 0) {
                    // Empty state
                    EmptyStatsView(
                        onStartPractice = { state.eventSink(StatsScreen.Event.BackPressed) },
                        modifier = Modifier.widthIn(max = MAX_CONTENT_WIDTH_MEDIUM),
                    )
                } else {
                    // Stats content
                    LazyColumn(
                        modifier =
                            Modifier
                                .widthIn(max = MAX_CONTENT_WIDTH_MEDIUM)
                                .fillMaxSize()
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Hero Image Section
                        item {
                            HeroImageSection()
                        }

                        // Overall Progress Section
                        item {
                            Text(
                                text = "Overall Progress",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        item {
                            OverallProgressCards(
                                stats = state.overallStats,
                                onAccuracyClick = { state.eventSink(StatsScreen.Event.AccuracyClicked) },
                                isExpandedScreen = isExpandedScreen,
                            )
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

                            // Adaptive grid layout for operation stats
                            item {
                                val itemsCount = state.operationStats.size
                                val estimatedHeight =
                                    when {
                                        isExpandedScreen -> (itemsCount / 3 + if (itemsCount % 3 > 0) 1 else 0) * 100
                                        isWideScreen -> (itemsCount / 2 + if (itemsCount % 2 > 0) 1 else 0) * 100
                                        else -> itemsCount * 100
                                    }

                                LazyVerticalGrid(
                                    columns =
                                        GridCells.Adaptive(minSize = 280.dp),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(estimatedHeight.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(state.operationStats.entries.toList()) { (operation, stats) ->
                                        OperationStatsCard(
                                            operation = operation,
                                            stats = stats,
                                        )
                                    }
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
 * Adaptive layout:
 * - Compact/Medium: 2 columns (Total Problems, Accuracy)
 * - Expanded: 3 columns (Total Problems, Sessions, Accuracy)
 */
@Composable
private fun OverallProgressCards(
    stats: SessionStats,
    onAccuracyClick: () -> Unit,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isExpandedScreen) {
        // 3-column layout for expanded screens
        Row(
            modifier = modifier.fillMaxWidth().height(IntrinsicSize.Max),
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
                            .fillMaxHeight()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
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

            // Sessions Count Card
            Card(
                modifier = Modifier.weight(1f),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Sessions",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stats.sessionCount.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }

            // Overall Accuracy Card
            Card(
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable { onAccuracyClick() },
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
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
    } else {
        // 2-column layout for compact/medium screens
        Row(
            modifier = modifier.fillMaxWidth().height(IntrinsicSize.Max),
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
                            .fillMaxHeight()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
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
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable { onAccuracyClick() },
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
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
 * Displays a single recent session item with enhanced visual hierarchy and interactivity.
 *
 * Features:
 * - Duration display with formatted time
 * - Semantic accuracy coloring (green/amber/red based on performance)
 * - Optional grade level badge
 * - Subtle elevation changes on interaction
 * - Better spacing and visual hierarchy
 * - Day-of-week watermark in the background (FRI, SAT, SUN, etc. or FRIDAY, SATURDAY, SUNDAY on larger screens)
 * - Adaptive day name: abbreviated on phone, full name on tablet/landscape
 */
@Composable
private fun RecentSessionItem(
    session: PracticeSessionEntity,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null) { },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                pressedElevation = if (isHovered) 4.dp else 2.dp,
            ),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // Detect available width and choose between abbreviated and full day name
            val dayOfWeek =
                remember {
                    if (maxWidth >= 600.dp) {
                        getDayOfWeekFull(session.timestamp) // MONDAY, TUESDAY, etc.
                    } else {
                        getDayOfWeek(session.timestamp) // MON, TUE, etc.
                    }
                }

            // Day watermark background - using playful Barrio font
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.displayLarge.copy(fontFamily = watermarkFontFamily),
                color =
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(16.dp),
                textAlign = TextAlign.Center,
            )

            // Main content
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Header: Timestamp and Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = TimeFormatter.formatRelativeTime(session.timestamp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    // Duration badge
                    Text(
                        text = "${formatDuration(session.durationSeconds)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Main content row: Operation/Score and Accuracy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Operation and Score details
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = session.operation.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            // Grade level badge if available
                            if (session.gradeLevel != null) {
                                GradeLevelBadge(gradeLevel = session.gradeLevel)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${session.correctAnswers}/${session.totalProblems} correct",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Accuracy badge with semantic coloring
                    AccuracyBadge(accuracy = session.accuracy)
                }
            }
        }
    }
}

/**
 * Extracts the day of week from a timestamp.
 * Returns: MON, TUE, WED, THU, FRI, SAT, SUN
 */
private fun getDayOfWeek(instant: Instant): String {
    val dayOfWeek =
        java.time.LocalDateTime
            .ofInstant(instant, java.time.ZoneId.systemDefault())
            .dayOfWeek
    return dayOfWeek.toString().take(3)
}

/**
 * Extracts the full day of week name from a timestamp.
 * Returns: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
 */
private fun getDayOfWeekFull(instant: Instant): String {
    val dayOfWeek =
        java.time.LocalDateTime
            .ofInstant(instant, java.time.ZoneId.systemDefault())
            .dayOfWeek
    return dayOfWeek.toString()
}

/**
 * Formats duration in seconds to a human-readable string.
 * Examples: "2m 30s", "45s", "1h 5m"
 */
private fun formatDuration(seconds: Long): String =
    when {
        seconds < 60 -> {
            "${seconds}s"
        }

        seconds < 3600 -> {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            if (remainingSeconds > 0) "${minutes}m ${remainingSeconds}s" else "${minutes}m"
        }

        else -> {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
        }
    }

/**
 * Displays accuracy with semantic color coding based on performance.
 * - Green (80%+): Excellent
 * - Amber (60-79%): Good
 * - Red (<60%): Needs improvement
 */
@Composable
private fun AccuracyBadge(accuracy: Float) {
    val (backgroundColor, textColor) =
        when {
            accuracy >= 80f -> {
                MaterialTheme.colorScheme.primaryContainer to
                    MaterialTheme.colorScheme.onPrimaryContainer
            }

            accuracy >= 60f -> {
                MaterialTheme.colorScheme.tertiaryContainer to
                    MaterialTheme.colorScheme.onTertiaryContainer
            }

            else -> {
                MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
            }
        }

    Card(
        modifier = Modifier.padding(0.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
    ) {
        Text(
            text = "${accuracy.toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

/**
 * Displays grade level as a small badge.
 * Maps: 0=K, 1=1st, 2=2nd
 */
@Composable
private fun GradeLevelBadge(gradeLevel: Int) {
    val gradeLabel =
        when (gradeLevel) {
            0 -> "K"
            1 -> "1st"
            2 -> "2nd"
            else -> "$gradeLevel"
        }

    Card(
        modifier = Modifier.padding(0.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Text(
            text = gradeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
        )
    }
}

/**
 * Hero image section showing completion achievements.
 */
@Composable
private fun HeroImageSection(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(240.dp),
    ) {
        // Hero Image with gradient overlays for blending
        Image(
            painter = painterResource(id = R.drawable.hero_complete_challenges),
            contentDescription = "Completion achievements",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )

        // Top gradient overlay
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

        // Bottom gradient overlay
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
                    userName = "Alex",
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
                                gradeLevel = 0,
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
                                gradeLevel = 1,
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
                    userName = null,
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
                    userName = "Jamie",
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
                                gradeLevel = 1,
                            ),
                            PracticeSessionEntity(
                                id = 2,
                                operation = MathOperation.SUBTRACTION,
                                totalProblems = 10,
                                correctAnswers = 5,
                                incorrectAnswers = 5,
                                accuracy = 50f,
                                durationSeconds = 180,
                                timestamp = Instant.now().minusSeconds(3600),
                                gradeLevel = 0,
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
                    userName = "Sam",
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
                                gradeLevel = 2,
                            ),
                            PracticeSessionEntity(
                                id = 2,
                                operation = MathOperation.SUBTRACTION,
                                totalProblems = 10,
                                correctAnswers = 7,
                                incorrectAnswers = 3,
                                accuracy = 70f,
                                durationSeconds = 95,
                                timestamp = Instant.now().minusSeconds(1800),
                                gradeLevel = 1,
                            ),
                        ),
                    eventSink = {},
                ),
        )
    }
}

// Adaptive layout previews for comprehensive testing

@Preview(
    showBackground = true,
    widthDp = 411,
    heightDp = 891,
    name = "Compact - Phone Portrait",
)
@Composable
private fun StatsUiCompactPreview() {
    KidsMathTutorAppTheme {
        StatsUi(
            state =
                StatsScreen.State(
                    userName = "Alex",
                    overallStats =
                        SessionStats(
                            totalProblems = 120,
                            correctCount = 108,
                            accuracy = 90f,
                            sessionCount = 12,
                        ),
                    operationStats =
                        mapOf(
                            MathOperation.ADDITION to
                                SessionStats(
                                    totalProblems = 60,
                                    correctCount = 54,
                                    accuracy = 90f,
                                    sessionCount = 6,
                                ),
                            MathOperation.SUBTRACTION to
                                SessionStats(
                                    totalProblems = 60,
                                    correctCount = 54,
                                    accuracy = 90f,
                                    sessionCount = 6,
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
                                gradeLevel = 1,
                            ),
                            PracticeSessionEntity(
                                id = 2,
                                operation = MathOperation.SUBTRACTION,
                                totalProblems = 10,
                                correctAnswers = 8,
                                incorrectAnswers = 2,
                                accuracy = 80f,
                                durationSeconds = 150,
                                timestamp = Instant.now().minusSeconds(3600),
                                gradeLevel = 0,
                            ),
                        ),
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 700,
    heightDp = 500,
    name = "Medium - Small Tablet",
)
@Composable
private fun StatsUiMediumPreview() {
    KidsMathTutorAppTheme {
        StatsUi(
            state =
                StatsScreen.State(
                    userName = "Jordan",
                    overallStats =
                        SessionStats(
                            totalProblems = 180,
                            correctCount = 162,
                            accuracy = 90f,
                            sessionCount = 18,
                        ),
                    operationStats =
                        mapOf(
                            MathOperation.ADDITION to
                                SessionStats(
                                    totalProblems = 90,
                                    correctCount = 81,
                                    accuracy = 90f,
                                    sessionCount = 9,
                                ),
                            MathOperation.SUBTRACTION to
                                SessionStats(
                                    totalProblems = 90,
                                    correctCount = 81,
                                    accuracy = 90f,
                                    sessionCount = 9,
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
                                gradeLevel = 2,
                            ),
                            PracticeSessionEntity(
                                id = 2,
                                operation = MathOperation.SUBTRACTION,
                                totalProblems = 10,
                                correctAnswers = 7,
                                incorrectAnswers = 3,
                                accuracy = 70f,
                                durationSeconds = 95,
                                timestamp = Instant.now().minusSeconds(1800),
                                gradeLevel = 1,
                            ),
                        ),
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 1100,
    heightDp = 600,
    name = "Expanded - Large Tablet",
)
@Composable
private fun StatsUiExpandedPreview() {
    KidsMathTutorAppTheme {
        StatsUi(
            state =
                StatsScreen.State(
                    userName = "Taylor",
                    overallStats =
                        SessionStats(
                            totalProblems = 250,
                            correctCount = 225,
                            accuracy = 90f,
                            sessionCount = 25,
                        ),
                    operationStats =
                        mapOf(
                            MathOperation.ADDITION to
                                SessionStats(
                                    totalProblems = 100,
                                    correctCount = 90,
                                    accuracy = 90f,
                                    sessionCount = 10,
                                ),
                            MathOperation.SUBTRACTION to
                                SessionStats(
                                    totalProblems = 80,
                                    correctCount = 72,
                                    accuracy = 90f,
                                    sessionCount = 8,
                                ),
                            MathOperation.MULTIPLICATION to
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
                                gradeLevel = 2,
                            ),
                            PracticeSessionEntity(
                                id = 2,
                                operation = MathOperation.SUBTRACTION,
                                totalProblems = 10,
                                correctAnswers = 7,
                                incorrectAnswers = 3,
                                accuracy = 70f,
                                durationSeconds = 95,
                                timestamp = Instant.now().minusSeconds(1800),
                                gradeLevel = 1,
                            ),
                            PracticeSessionEntity(
                                id = 3,
                                operation = MathOperation.MULTIPLICATION,
                                totalProblems = 10,
                                correctAnswers = 8,
                                incorrectAnswers = 2,
                                accuracy = 80f,
                                durationSeconds = 110,
                                timestamp = Instant.now().minusSeconds(3600),
                                gradeLevel = 2,
                            ),
                        ),
                    eventSink = {},
                ),
        )
    }
}

// Preview functions for OperationStatsCard

@Preview(showBackground = true, name = "Addition - High Accuracy")
@Composable
private fun OperationStatsCardAdditionHighPreview() {
    KidsMathTutorAppTheme {
        OperationStatsCard(
            operation = MathOperation.ADDITION,
            stats =
                SessionStats(
                    totalProblems = 50,
                    correctCount = 45,
                    accuracy = 90f,
                    sessionCount = 5,
                ),
        )
    }
}

@Preview(showBackground = true, name = "Subtraction - Medium Accuracy")
@Composable
private fun OperationStatsCardSubtractionMediumPreview() {
    KidsMathTutorAppTheme {
        OperationStatsCard(
            operation = MathOperation.SUBTRACTION,
            stats =
                SessionStats(
                    totalProblems = 35,
                    correctCount = 21,
                    accuracy = 60f,
                    sessionCount = 3,
                ),
        )
    }
}

@Preview(showBackground = true, name = "Multiplication - Perfect")
@Composable
private fun OperationStatsCardMultiplicationPerfectPreview() {
    KidsMathTutorAppTheme {
        OperationStatsCard(
            operation = MathOperation.MULTIPLICATION,
            stats =
                SessionStats(
                    totalProblems = 20,
                    correctCount = 20,
                    accuracy = 100f,
                    sessionCount = 2,
                ),
        )
    }
}

@Preview(showBackground = true, name = "Division - Low Accuracy")
@Composable
private fun OperationStatsCardDivisionLowPreview() {
    KidsMathTutorAppTheme {
        OperationStatsCard(
            operation = MathOperation.DIVISION,
            stats =
                SessionStats(
                    totalProblems = 25,
                    correctCount = 10,
                    accuracy = 40f,
                    sessionCount = 1,
                ),
        )
    }
}

@Preview(showBackground = true, widthDp = 700, name = "Tablet - Multiple Cards")
@Composable
private fun OperationStatsCardTabletPreview() {
    KidsMathTutorAppTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OperationStatsCard(
                operation = MathOperation.ADDITION,
                stats =
                    SessionStats(
                        totalProblems = 50,
                        correctCount = 45,
                        accuracy = 90f,
                        sessionCount = 5,
                    ),
            )
            OperationStatsCard(
                operation = MathOperation.SUBTRACTION,
                stats =
                    SessionStats(
                        totalProblems = 35,
                        correctCount = 28,
                        accuracy = 80f,
                        sessionCount = 3,
                    ),
            )
        }
    }
}

@Preview(showBackground = true, name = "Dark Theme - High Accuracy")
@Composable
private fun OperationStatsCardDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        OperationStatsCard(
            operation = MathOperation.ADDITION,
            stats =
                SessionStats(
                    totalProblems = 50,
                    correctCount = 45,
                    accuracy = 90f,
                    sessionCount = 5,
                ),
        )
    }
}

// Preview functions for RecentSessionItem

@Preview(showBackground = true, name = "Excellent Performance (90%)")
@Composable
private fun RecentSessionItemExcellentPreview() {
    KidsMathTutorAppTheme {
        RecentSessionItem(
            session =
                PracticeSessionEntity(
                    id = 1,
                    operation = MathOperation.ADDITION,
                    totalProblems = 10,
                    correctAnswers = 9,
                    incorrectAnswers = 1,
                    accuracy = 90f,
                    durationSeconds = 120,
                    timestamp = Instant.now(),
                    gradeLevel = 1,
                ),
        )
    }
}

@Preview(showBackground = true, name = "Good Performance (75%)")
@Composable
private fun RecentSessionItemGoodPreview() {
    KidsMathTutorAppTheme {
        RecentSessionItem(
            session =
                PracticeSessionEntity(
                    id = 2,
                    operation = MathOperation.SUBTRACTION,
                    totalProblems = 12,
                    correctAnswers = 9,
                    incorrectAnswers = 3,
                    accuracy = 75f,
                    durationSeconds = 145,
                    timestamp = Instant.now().minusSeconds(3600),
                    gradeLevel = 0,
                ),
        )
    }
}

@Preview(showBackground = true, name = "Needs Improvement (50%)")
@Composable
private fun RecentSessionItemNeedsImprovementPreview() {
    KidsMathTutorAppTheme {
        RecentSessionItem(
            session =
                PracticeSessionEntity(
                    id = 3,
                    operation = MathOperation.MULTIPLICATION,
                    totalProblems = 10,
                    correctAnswers = 5,
                    incorrectAnswers = 5,
                    accuracy = 50f,
                    durationSeconds = 180,
                    timestamp = Instant.now().minusSeconds(86400),
                    gradeLevel = 2,
                ),
        )
    }
}

@Preview(showBackground = true, name = "Very Short Duration (30s)")
@Composable
private fun RecentSessionItemShortDurationPreview() {
    KidsMathTutorAppTheme {
        RecentSessionItem(
            session =
                PracticeSessionEntity(
                    id = 4,
                    operation = MathOperation.DIVISION,
                    totalProblems = 5,
                    correctAnswers = 4,
                    incorrectAnswers = 1,
                    accuracy = 80f,
                    durationSeconds = 30,
                    timestamp = Instant.now().minusSeconds(600),
                    gradeLevel = null,
                ),
        )
    }
}

@Preview(showBackground = true, name = "Long Duration (1h 30m)")
@Composable
private fun RecentSessionItemLongDurationPreview() {
    KidsMathTutorAppTheme {
        RecentSessionItem(
            session =
                PracticeSessionEntity(
                    id = 5,
                    operation = MathOperation.ADDITION,
                    totalProblems = 50,
                    correctAnswers = 48,
                    incorrectAnswers = 2,
                    accuracy = 96f,
                    durationSeconds = 5400,
                    timestamp = Instant.now().minusSeconds(172800),
                    gradeLevel = 2,
                ),
        )
    }
}

@Preview(showBackground = true, name = "No Grade Level")
@Composable
private fun RecentSessionItemNoGradeLevelPreview() {
    KidsMathTutorAppTheme {
        RecentSessionItem(
            session =
                PracticeSessionEntity(
                    id = 6,
                    operation = MathOperation.SUBTRACTION,
                    totalProblems = 15,
                    correctAnswers = 12,
                    incorrectAnswers = 3,
                    accuracy = 80f,
                    durationSeconds = 210,
                    timestamp = Instant.now().minusSeconds(7200),
                    gradeLevel = null,
                ),
        )
    }
}

@Preview(showBackground = true, widthDp = 700, name = "Tablet - Multiple Sessions")
@Composable
private fun RecentSessionItemTabletPreview() {
    KidsMathTutorAppTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RecentSessionItem(
                session =
                    PracticeSessionEntity(
                        id = 1,
                        operation = MathOperation.ADDITION,
                        totalProblems = 10,
                        correctAnswers = 9,
                        incorrectAnswers = 1,
                        accuracy = 90f,
                        durationSeconds = 120,
                        timestamp = Instant.now(),
                        gradeLevel = 1,
                    ),
            )
            RecentSessionItem(
                session =
                    PracticeSessionEntity(
                        id = 2,
                        operation = MathOperation.SUBTRACTION,
                        totalProblems = 12,
                        correctAnswers = 6,
                        incorrectAnswers = 6,
                        accuracy = 50f,
                        durationSeconds = 165,
                        timestamp = Instant.now().minusSeconds(3600),
                        gradeLevel = 0,
                    ),
            )
            RecentSessionItem(
                session =
                    PracticeSessionEntity(
                        id = 3,
                        operation = MathOperation.MULTIPLICATION,
                        totalProblems = 8,
                        correctAnswers = 6,
                        incorrectAnswers = 2,
                        accuracy = 75f,
                        durationSeconds = 95,
                        timestamp = Instant.now().minusSeconds(7200),
                        gradeLevel = 2,
                    ),
            )
        }
    }
}

@Preview(showBackground = true, name = "Dark Theme - Excellent Performance")
@Composable
private fun RecentSessionItemDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        RecentSessionItem(
            session =
                PracticeSessionEntity(
                    id = 1,
                    operation = MathOperation.ADDITION,
                    totalProblems = 10,
                    correctAnswers = 9,
                    incorrectAnswers = 1,
                    accuracy = 90f,
                    durationSeconds = 120,
                    timestamp = Instant.now(),
                    gradeLevel = 1,
                ),
        )
    }
}

@Preview(showBackground = true, name = "Dark Theme - Low Performance")
@Composable
private fun RecentSessionItemDarkLowPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        RecentSessionItem(
            session =
                PracticeSessionEntity(
                    id = 2,
                    operation = MathOperation.DIVISION,
                    totalProblems = 10,
                    correctAnswers = 4,
                    incorrectAnswers = 6,
                    accuracy = 40f,
                    durationSeconds = 200,
                    timestamp = Instant.now().minusSeconds(86400),
                    gradeLevel = 0,
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
private fun StatsUiPhoneLandscapePreview() {
    KidsMathTutorAppTheme {
        StatsUi(
            state =
                StatsScreen.State(
                    overallStats =
                        SessionStats(
                            totalProblems = 100,
                            correctCount = 85,
                            accuracy = 85f,
                            sessionCount = 10,
                        ),
                    streakData =
                        DailyStreak(
                            currentStreak = 5,
                            longestStreak = 8,
                            lastPracticeDate = LocalDate.now(),
                            totalDaysPracticed = 15,
                        ),
                    operationStats =
                        listOf(
                            SessionStats(
                                totalProblems = 50,
                                correctCount = 45,
                                accuracy = 90f,
                                sessionCount = 5,
                            ),
                            SessionStats(
                                totalProblems = 50,
                                correctCount = 40,
                                accuracy = 80f,
                                sessionCount = 5,
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
                                gradeLevel = 1,
                            ),
                        ),
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
private fun StatsUiTabletPortraitPreview() {
    KidsMathTutorAppTheme {
        StatsUi(
            state =
                StatsScreen.State(
                    overallStats =
                        SessionStats(
                            totalProblems = 200,
                            correctCount = 180,
                            accuracy = 90f,
                            sessionCount = 20,
                        ),
                    streakData =
                        DailyStreak(
                            currentStreak = 10,
                            longestStreak = 15,
                            lastPracticeDate = LocalDate.now(),
                            totalDaysPracticed = 25,
                        ),
                    operationStats =
                        listOf(
                            SessionStats(
                                totalProblems = 100,
                                correctCount = 90,
                                accuracy = 90f,
                                sessionCount = 10,
                            ),
                            SessionStats(
                                totalProblems = 100,
                                correctCount = 90,
                                accuracy = 90f,
                                sessionCount = 10,
                            ),
                        ),
                    recentSessions =
                        listOf(
                            PracticeSessionEntity(
                                id = 1,
                                operation = MathOperation.ADDITION,
                                totalProblems = 10,
                                correctAnswers = 10,
                                incorrectAnswers = 0,
                                accuracy = 100f,
                                durationSeconds = 90,
                                timestamp = Instant.now(),
                                gradeLevel = 2,
                            ),
                            PracticeSessionEntity(
                                id = 2,
                                operation = MathOperation.SUBTRACTION,
                                totalProblems = 10,
                                correctAnswers = 8,
                                incorrectAnswers = 2,
                                accuracy = 80f,
                                durationSeconds = 150,
                                timestamp = Instant.now().minusSeconds(3600),
                                gradeLevel = 2,
                            ),
                        ),
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
private fun StatsUiTabletLandscapePreview() {
    KidsMathTutorAppTheme {
        StatsUi(
            state =
                StatsScreen.State(
                    overallStats =
                        SessionStats(
                            totalProblems = 150,
                            correctCount = 120,
                            accuracy = 80f,
                            sessionCount = 15,
                        ),
                    streakData =
                        DailyStreak(
                            currentStreak = 3,
                            longestStreak = 10,
                            lastPracticeDate = LocalDate.now(),
                            totalDaysPracticed = 20,
                        ),
                    operationStats =
                        listOf(
                            SessionStats(
                                totalProblems = 75,
                                correctCount = 60,
                                accuracy = 80f,
                                sessionCount = 8,
                            ),
                            SessionStats(
                                totalProblems = 75,
                                correctCount = 60,
                                accuracy = 80f,
                                sessionCount = 7,
                            ),
                        ),
                    recentSessions =
                        listOf(
                            PracticeSessionEntity(
                                id = 1,
                                operation = MathOperation.ADDITION,
                                totalProblems = 10,
                                correctAnswers = 8,
                                incorrectAnswers = 2,
                                accuracy = 80f,
                                durationSeconds = 120,
                                timestamp = Instant.now(),
                                gradeLevel = 1,
                            ),
                        ),
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
private fun StatsUiFoldablePortraitPreview() {
    KidsMathTutorAppTheme {
        StatsUi(
            state =
                StatsScreen.State(
                    overallStats = SessionStats.EMPTY,
                    streakData = null,
                    operationStats = emptyList(),
                    recentSessions = emptyList(),
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
private fun StatsUiFoldableLandscapePreview() {
    KidsMathTutorAppTheme {
        StatsUi(
            state =
                StatsScreen.State(
                    overallStats =
                        SessionStats(
                            totalProblems = 50,
                            correctCount = 40,
                            accuracy = 80f,
                            sessionCount = 5,
                        ),
                    streakData =
                        DailyStreak(
                            currentStreak = 2,
                            longestStreak = 5,
                            lastPracticeDate = LocalDate.now(),
                            totalDaysPracticed = 8,
                        ),
                    operationStats =
                        listOf(
                            SessionStats(
                                totalProblems = 25,
                                correctCount = 20,
                                accuracy = 80f,
                                sessionCount = 3,
                            ),
                            SessionStats(
                                totalProblems = 25,
                                correctCount = 20,
                                accuracy = 80f,
                                sessionCount = 2,
                            ),
                        ),
                    recentSessions =
                        listOf(
                            PracticeSessionEntity(
                                id = 1,
                                operation = MathOperation.SUBTRACTION,
                                totalProblems = 5,
                                correctAnswers = 4,
                                incorrectAnswers = 1,
                                accuracy = 80f,
                                durationSeconds = 90,
                                timestamp = Instant.now(),
                                gradeLevel = 0,
                            ),
                        ),
                    eventSink = {},
                ),
        )
    }
}
