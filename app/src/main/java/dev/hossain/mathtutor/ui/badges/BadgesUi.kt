package dev.hossain.mathtutor.ui.badges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.repository.BadgeProgress
import dev.hossain.mathtutor.ui.component.BadgeDetailDialog
import dev.hossain.mathtutor.ui.component.BadgeGrid
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import java.time.Instant

/**
 * UI for [BadgesScreen].
 *
 * Displays all badges organized by category with Material 3 design.
 * Shows progress summary, badge grid by category, and badge detail dialog.
 */
@CircuitInject(BadgesScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesUi(
    state: BadgesScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Your Badges")
                },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(BadgesScreen.Event.BackPressed) }) {
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
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            // Progress Summary
            item {
                ProgressSummarySection(
                    unlockedCount = state.progressSummary.unlockedCount,
                    totalCount = state.progressSummary.totalCount,
                )
            }

            // Badge Categories
            BadgeCategory.entries.forEach { category ->
                val badges = state.badgesByCategory[category] ?: emptyList()
                if (badges.isNotEmpty()) {
                    item(key = category) {
                        BadgeCategorySection(
                            category = category,
                            badges = badges,
                            onBadgeClick = { badge ->
                                state.eventSink(BadgesScreen.Event.BadgeClicked(badge))
                            },
                        )
                    }
                }
            }
        }

        // Badge Detail Dialog
        state.selectedBadge?.let { badge ->
            BadgeDetailDialog(
                badge = badge,
                onDismiss = { state.eventSink(BadgesScreen.Event.CloseDialog) },
            )
        }
    }
}

/**
 * Progress summary section showing unlocked badge count.
 */
@Composable
private fun ProgressSummarySection(
    unlockedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text(
            text = "🏆 $unlockedCount of $totalCount Badges Unlocked",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Badge category section with header and badge grid.
 */
@Composable
private fun BadgeCategorySection(
    category: BadgeCategory,
    badges: List<dev.hossain.mathtutor.domain.model.Badge>,
    onBadgeClick: (dev.hossain.mathtutor.domain.model.Badge) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Category Header
        Text(
            text = formatCategoryName(category),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Badge Grid
        BadgeGrid(
            badges = badges,
            onBadgeClick = onBadgeClick,
        )
    }
}

/**
 * Formats badge category enum to display name.
 */
private fun formatCategoryName(category: BadgeCategory): String =
    when (category) {
        BadgeCategory.GETTING_STARTED -> "Getting Started"
        BadgeCategory.VOLUME -> "Volume"
        BadgeCategory.OPERATION_MASTERY -> "Operation Mastery"
        BadgeCategory.SPEED_ACCURACY -> "Speed & Accuracy"
        BadgeCategory.STREAK -> "Streak"
    }

@Preview(showBackground = true)
@Composable
private fun BadgesUiPreview() {
    KidsMathTutorAppTheme {
        BadgesUi(
            state =
                BadgesScreen.State(
                    progressSummary =
                        BadgeProgress(
                            unlockedCount = 3,
                            totalCount = 10,
                        ),
                    badgesByCategory =
                        mapOf(
                            BadgeCategory.GETTING_STARTED to
                                listOf(
                                    Badge(
                                        id = "first_steps",
                                        name = "First Steps",
                                        description = "Solved first problem",
                                        icon = "🎯",
                                        category = BadgeCategory.GETTING_STARTED,
                                        requirement = BadgeRequirement.ProblemCount(1),
                                        unlockedAt = Instant.now(),
                                    ),
                                ),
                            BadgeCategory.VOLUME to
                                listOf(
                                    Badge(
                                        id = "quick_learner",
                                        name = "Quick Learner",
                                        description = "Solved 10 problems",
                                        icon = "🚀",
                                        category = BadgeCategory.VOLUME,
                                        requirement = BadgeRequirement.ProblemCount(10),
                                        unlockedAt = Instant.now(),
                                    ),
                                    Badge(
                                        id = "math_master",
                                        name = "Math Master",
                                        description = "Solved 100 problems",
                                        icon = "🏆",
                                        category = BadgeCategory.VOLUME,
                                        requirement = BadgeRequirement.ProblemCount(100),
                                        unlockedAt = null,
                                    ),
                                ),
                            BadgeCategory.STREAK to
                                listOf(
                                    Badge(
                                        id = "streak_starter",
                                        name = "Streak Starter",
                                        description = "Practice 3 days in a row",
                                        icon = "🔥",
                                        category = BadgeCategory.STREAK,
                                        requirement = BadgeRequirement.DailyStreak(3),
                                        unlockedAt = Instant.now(),
                                    ),
                                ),
                        ),
                    selectedBadge = null,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgesUiDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        BadgesUi(
            state =
                BadgesScreen.State(
                    progressSummary =
                        BadgeProgress(
                            unlockedCount = 2,
                            totalCount = 8,
                        ),
                    badgesByCategory =
                        mapOf(
                            BadgeCategory.GETTING_STARTED to
                                listOf(
                                    Badge(
                                        id = "first_steps",
                                        name = "First Steps",
                                        description = "Solved first problem",
                                        icon = "🎯",
                                        category = BadgeCategory.GETTING_STARTED,
                                        requirement = BadgeRequirement.ProblemCount(1),
                                        unlockedAt = Instant.now(),
                                    ),
                                ),
                            BadgeCategory.VOLUME to
                                listOf(
                                    Badge(
                                        id = "quick_learner",
                                        name = "Quick Learner",
                                        description = "Solved 10 problems",
                                        icon = "🚀",
                                        category = BadgeCategory.VOLUME,
                                        requirement = BadgeRequirement.ProblemCount(10),
                                        unlockedAt = null,
                                    ),
                                ),
                        ),
                    selectedBadge = null,
                    eventSink = {},
                ),
        )
    }
}
