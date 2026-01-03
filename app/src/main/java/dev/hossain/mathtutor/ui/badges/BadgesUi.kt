package dev.hossain.mathtutor.ui.badges

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeIcon
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.repository.BadgeProgress
import dev.hossain.mathtutor.ui.component.BadgeDetailDialog
import dev.hossain.mathtutor.ui.component.BadgeGrid
import dev.hossain.mathtutor.ui.component.FeatureTopAppBar
import dev.hossain.mathtutor.ui.component.TopBarFeature
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MAX_CONTENT_WIDTH_MEDIUM
import dev.zacsweers.metro.AppScope
import java.time.Instant

/**
 * UI for [BadgesScreen].
 *
 * Displays all badges organized by category with Material 3 design.
 * Shows progress summary, badge grid by category, and badge detail dialog.
 *
 * Adaptive Layout:
 * - Compact: Full width badge grids
 * - Medium/Expanded: Centered content with max width
 */
@CircuitInject(BadgesScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesUi(
    state: BadgesScreen.State,
    modifier: Modifier = Modifier,
) {
    /*
     * IMPORTANT: Explicit BackHandler to prevent ANR on system back button press.
     *
     * Without this BackHandler, pressing the system back button causes a 5+ second freeze
     * with high CPU usage on the main thread, triggering an ANR (Application Not Responding).
     * The BackHandler ensures immediate navigation response by handling the back event directly
     * and triggering navigation without blocking the UI thread.
     *
     * See: Similar fix in OperationSelectorUi and GameSelectionUi for the same ANR issue.
     */
    BackHandler {
        state.eventSink(BadgesScreen.Event.BackPressed)
    }

    Scaffold(
        topBar = {
            FeatureTopAppBar(
                title = {
                    Text("Your Badges")
                },
                feature = TopBarFeature.BADGES,
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
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Center content on wide screens
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                LazyColumn(
                    modifier =
                        Modifier
                            .widthIn(max = MAX_CONTENT_WIDTH_MEDIUM)
                            .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    // Hero Image with fade effect
                    item {
                        HeroImageSection()
                    }

                    // Progress Summary
                    item {
                        ProgressSummarySection(
                            unlockedCount = state.progressSummary.unlockedCount,
                            totalCount = state.progressSummary.totalCount,
                            modifier = Modifier.padding(horizontal = 16.dp),
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
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }
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
 * Hero image section with fade effect at edges for seamless blending.
 */
@Composable
private fun HeroImageSection(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        val gradientColor = MaterialTheme.colorScheme.surface
        Image(
            painter = painterResource(id = R.drawable.hero_image_your_badges),
            contentDescription = "Your Badges Hero",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        // Apply vertical gradient fade at bottom edge
                        drawRect(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color.Transparent,
                                            Color.Transparent,
                                            gradientColor.copy(alpha = 0.3f),
                                            gradientColor.copy(alpha = 0.7f),
                                            gradientColor,
                                        ),
                                    startY = 0f,
                                    endY = size.height,
                                ),
                        )
                    },
        )
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
        BadgeCategory.GAMES -> "Games"
    }

@Preview(showBackground = true, name = "Compact - Phone")
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
                                        icon = BadgeIcon.FIRST_STEPS,
                                        category = BadgeCategory.GETTING_STARTED,
                                        requirement = BadgeRequirement.ProblemCount(1),
                                        unlockedAt = Instant.now(),
                                    ),
                                ),
                            BadgeCategory.VOLUME to
                                listOf(
                                    Badge(
                                        id = "math_rookie",
                                        name = "Math Rookie",
                                        description = "Solved 25 problems",
                                        icon = BadgeIcon.MATH_ROOKIE,
                                        category = BadgeCategory.VOLUME,
                                        requirement = BadgeRequirement.ProblemCount(25),
                                        unlockedAt = Instant.now(),
                                    ),
                                    Badge(
                                        id = "math_champion",
                                        name = "Math Champion",
                                        description = "Solved 100 problems",
                                        icon = BadgeIcon.MATH_CHAMPION,
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
                                        icon = BadgeIcon.STREAK_STARTER,
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

@Preview(showBackground = true, widthDp = 700, heightDp = 900, name = "Medium - Tablet")
@Composable
private fun BadgesUiMediumPreview() {
    KidsMathTutorAppTheme {
        BadgesUi(
            state =
                BadgesScreen.State(
                    progressSummary =
                        BadgeProgress(
                            unlockedCount = 5,
                            totalCount = 12,
                        ),
                    badgesByCategory =
                        mapOf(
                            BadgeCategory.GETTING_STARTED to
                                listOf(
                                    Badge(
                                        id = "first_steps",
                                        name = "First Steps",
                                        description = "Solved first problem",
                                        icon = BadgeIcon.FIRST_STEPS,
                                        category = BadgeCategory.GETTING_STARTED,
                                        requirement = BadgeRequirement.ProblemCount(1),
                                        unlockedAt = Instant.now(),
                                    ),
                                ),
                            BadgeCategory.VOLUME to
                                listOf(
                                    Badge(
                                        id = "math_rookie",
                                        name = "Math Rookie",
                                        description = "Solved 25 problems",
                                        icon = BadgeIcon.MATH_ROOKIE,
                                        category = BadgeCategory.VOLUME,
                                        requirement = BadgeRequirement.ProblemCount(25),
                                        unlockedAt = Instant.now(),
                                    ),
                                    Badge(
                                        id = "math_champion",
                                        name = "Math Champion",
                                        description = "Solved 100 problems",
                                        icon = BadgeIcon.MATH_CHAMPION,
                                        category = BadgeCategory.VOLUME,
                                        requirement = BadgeRequirement.ProblemCount(100),
                                        unlockedAt = null,
                                    ),
                                    Badge(
                                        id = "math_master",
                                        name = "Math Master",
                                        description = "Solved 500 problems",
                                        icon = BadgeIcon.MATH_LEGEND,
                                        category = BadgeCategory.VOLUME,
                                        requirement = BadgeRequirement.ProblemCount(500),
                                        unlockedAt = null,
                                    ),
                                ),
                            BadgeCategory.STREAK to
                                listOf(
                                    Badge(
                                        id = "streak_starter",
                                        name = "Streak Starter",
                                        description = "Practice 3 days in a row",
                                        icon = BadgeIcon.STREAK_STARTER,
                                        category = BadgeCategory.STREAK,
                                        requirement = BadgeRequirement.DailyStreak(3),
                                        unlockedAt = Instant.now(),
                                    ),
                                ),
                            BadgeCategory.GAMES to
                                listOf(
                                    Badge(
                                        id = "speed_demon",
                                        name = "Speed Demon",
                                        description = "Score 20+ in Math Race",
                                        icon = BadgeIcon.SPEED_DEMON,
                                        category = BadgeCategory.GAMES,
                                        requirement = BadgeRequirement.MathRaceScore(20),
                                        unlockedAt = Instant.now(),
                                    ),
                                    Badge(
                                        id = "perfect_race",
                                        name = "Perfect Race",
                                        description = "Complete Math Race with no mistakes",
                                        icon = BadgeIcon.PERFECT_RACE,
                                        category = BadgeCategory.GAMES,
                                        requirement = BadgeRequirement.PerfectGameAccuracy,
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

@Preview(showBackground = true, widthDp = 1100, heightDp = 900, name = "Expanded - Large Tablet")
@Composable
private fun BadgesUiExpandedPreview() {
    KidsMathTutorAppTheme {
        BadgesUi(
            state =
                BadgesScreen.State(
                    progressSummary =
                        BadgeProgress(
                            unlockedCount = 8,
                            totalCount = 18,
                        ),
                    badgesByCategory =
                        mapOf(
                            BadgeCategory.GETTING_STARTED to
                                listOf(
                                    Badge(
                                        id = "first_steps",
                                        name = "First Steps",
                                        description = "Solved first problem",
                                        icon = BadgeIcon.FIRST_STEPS,
                                        category = BadgeCategory.GETTING_STARTED,
                                        requirement = BadgeRequirement.ProblemCount(1),
                                        unlockedAt = Instant.now(),
                                    ),
                                ),
                            BadgeCategory.VOLUME to
                                listOf(
                                    Badge(
                                        id = "math_rookie",
                                        name = "Math Rookie",
                                        description = "Solved 25 problems",
                                        icon = BadgeIcon.MATH_ROOKIE,
                                        category = BadgeCategory.VOLUME,
                                        requirement = BadgeRequirement.ProblemCount(25),
                                        unlockedAt = Instant.now(),
                                    ),
                                    Badge(
                                        id = "math_champion",
                                        name = "Math Champion",
                                        description = "Solved 100 problems",
                                        icon = BadgeIcon.MATH_CHAMPION,
                                        category = BadgeCategory.VOLUME,
                                        requirement = BadgeRequirement.ProblemCount(100),
                                        unlockedAt = Instant.now(),
                                    ),
                                    Badge(
                                        id = "math_master",
                                        name = "Math Master",
                                        description = "Solved 500 problems",
                                        icon = BadgeIcon.MATH_LEGEND,
                                        category = BadgeCategory.VOLUME,
                                        requirement = BadgeRequirement.ProblemCount(500),
                                        unlockedAt = null,
                                    ),
                                ),
                            BadgeCategory.OPERATION_MASTERY to
                                listOf(
                                    Badge(
                                        id = "addition_expert",
                                        name = "Addition Expert",
                                        description = "Master addition operations",
                                        icon = BadgeIcon.ADDITION_EXPERT,
                                        category = BadgeCategory.OPERATION_MASTERY,
                                        requirement =
                                            BadgeRequirement.OperationCount(
                                                dev.hossain.mathtutor.domain.model.MathOperation.ADDITION,
                                                50,
                                            ),
                                        unlockedAt = Instant.now(),
                                    ),
                                    Badge(
                                        id = "subtraction_star",
                                        name = "Subtraction Star",
                                        description = "Master subtraction operations",
                                        icon = BadgeIcon.SUBTRACTION_STAR,
                                        category = BadgeCategory.OPERATION_MASTERY,
                                        requirement =
                                            BadgeRequirement.OperationCount(
                                                dev.hossain.mathtutor.domain.model.MathOperation.SUBTRACTION,
                                                50,
                                            ),
                                        unlockedAt = null,
                                    ),
                                    Badge(
                                        id = "mix_master",
                                        name = "Mix Master",
                                        description = "Complete mixed operation sessions",
                                        icon = BadgeIcon.MIX_MASTER,
                                        category = BadgeCategory.OPERATION_MASTERY,
                                        requirement = BadgeRequirement.MixedSessions(10),
                                        unlockedAt = Instant.now(),
                                    ),
                                ),
                            BadgeCategory.STREAK to
                                listOf(
                                    Badge(
                                        id = "streak_starter",
                                        name = "Streak Starter",
                                        description = "Practice 3 days in a row",
                                        icon = BadgeIcon.STREAK_STARTER,
                                        category = BadgeCategory.STREAK,
                                        requirement = BadgeRequirement.DailyStreak(3),
                                        unlockedAt = Instant.now(),
                                    ),
                                    Badge(
                                        id = "week_warrior",
                                        name = "Week Warrior",
                                        description = "Practice 7 days in a row",
                                        icon = BadgeIcon.DEDICATION_AWARD,
                                        category = BadgeCategory.STREAK,
                                        requirement = BadgeRequirement.DailyStreak(7),
                                        unlockedAt = Instant.now(),
                                    ),
                                ),
                            BadgeCategory.GAMES to
                                listOf(
                                    Badge(
                                        id = "speed_demon",
                                        name = "Speed Demon",
                                        description = "Score 20+ in Math Race",
                                        icon = BadgeIcon.SPEED_DEMON,
                                        category = BadgeCategory.GAMES,
                                        requirement = BadgeRequirement.MathRaceScore(20),
                                        unlockedAt = Instant.now(),
                                    ),
                                    Badge(
                                        id = "perfect_race",
                                        name = "Perfect Race",
                                        description = "Complete Math Race with no mistakes",
                                        icon = BadgeIcon.PERFECT_RACE,
                                        category = BadgeCategory.GAMES,
                                        requirement = BadgeRequirement.PerfectGameAccuracy,
                                        unlockedAt = null,
                                    ),
                                    Badge(
                                        id = "quick_thinker",
                                        name = "Quick Thinker",
                                        description = "Solve problem in under 3 seconds",
                                        icon = BadgeIcon.QUICK_THINKER,
                                        category = BadgeCategory.SPEED_ACCURACY,
                                        requirement = BadgeRequirement.ProblemSpeed(3),
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

@Preview(showBackground = true, name = "Dark Theme")
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
                                        icon = BadgeIcon.FIRST_STEPS,
                                        category = BadgeCategory.GETTING_STARTED,
                                        requirement = BadgeRequirement.ProblemCount(1),
                                        unlockedAt = Instant.now(),
                                    ),
                                ),
                            BadgeCategory.VOLUME to
                                listOf(
                                    Badge(
                                        id = "math_rookie",
                                        name = "Math Rookie",
                                        description = "Solved 25 problems",
                                        icon = BadgeIcon.MATH_ROOKIE,
                                        category = BadgeCategory.VOLUME,
                                        requirement = BadgeRequirement.ProblemCount(25),
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
