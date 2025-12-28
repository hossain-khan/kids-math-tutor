package dev.hossain.mathtutor.ui.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.ui.component.FeatureTopAppBar
import dev.hossain.mathtutor.ui.component.StreakCard
import dev.hossain.mathtutor.ui.component.TopBarFeature
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import java.time.Instant
import java.time.LocalDate
import dev.hossain.mathtutor.ui.component.BadgeIcon as BadgeIconImage

// Width breakpoints for adaptive layouts
private val MEDIUM_WIDTH_BREAKPOINT: Dp = 600.dp
private val MAX_CONTENT_WIDTH: Dp = 840.dp

/**
 * UI for [HomeScreen].
 *
 * Displays the home dashboard with:
 * - Welcome message (personalized or generic)
 * - Streak card with calendar
 * - Quick stats card (total problems, accuracy)
 * - Latest badges section (3 badges)
 * - Start Practice button (primary action)
 * - View Full Stats and View All Badges links
 *
 * Adaptive Layout:
 * - Compact: Single column, full width
 * - Medium/Expanded: Centered content with max width, side-by-side action buttons
 */
@CircuitInject(HomeScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUi(
    state: HomeScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            FeatureTopAppBar(
                title = {
                    Text("Math Pup Tutor")
                },
                feature = TopBarFeature.BADGES,
                actions = {
                    IconButton(onClick = { state.eventSink(HomeScreen.Event.ToggleMusicClicked) }) {
                        Icon(
                            imageVector =
                                if (state.isMusicPlaying) {
                                    Icons.Filled.MusicNote
                                } else {
                                    Icons.Filled.MusicOff
                                },
                            contentDescription =
                                if (state.isMusicPlaying) {
                                    "Turn off music"
                                } else {
                                    "Turn on music"
                                },
                        )
                    }
                    IconButton(onClick = { state.eventSink(HomeScreen.Event.ViewSettingsClicked) }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
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

            // Center content with max width on larger screens
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier =
                        Modifier
                            .widthIn(max = MAX_CONTENT_WIDTH)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // Welcome message
                    WelcomeSection(
                        userName = state.userName,
                        gradeLevel = state.gradeLevel,
                        totalProblems = state.overallStats.totalProblems,
                        accuracy = state.overallStats.accuracy,
                    )

                    // Action buttons - side by side on wider screens
                    if (isWideScreen) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // Primary action: Start Practice button
                            Button(
                                onClick = { state.eventSink(HomeScreen.Event.StartPracticeClicked) },
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                    ),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.dog_outline),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Start Practice",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }

                            // Games button
                            Button(
                                onClick = { state.eventSink(HomeScreen.Event.ViewGamesClicked) },
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    ),
                            ) {
                                Text(
                                    text = "🎮 Play Games",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }
                        }
                    } else {
                        // Stacked buttons on compact screens
                        Button(
                            onClick = { state.eventSink(HomeScreen.Event.StartPracticeClicked) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.dog_outline),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Practice",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }

                        Button(
                            onClick = { state.eventSink(HomeScreen.Event.ViewGamesClicked) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                ),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.controller_bold_outline),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Play Games",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }

                    // Cards layout - side by side on wider screens
                    if (isWideScreen && state.overallStats.sessionCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // Streak card
                            Box(modifier = Modifier.weight(1f)) {
                                StreakCard(
                                    streakData = state.streakData,
                                    userName = state.userName,
                                )
                            }

                            // Quick stats card
                            Box(modifier = Modifier.weight(1f)) {
                                QuickStatsCard(stats = state.overallStats)
                            }
                        }
                    } else {
                        // Stacked cards on compact screens
                        StreakCard(
                            streakData = state.streakData,
                            userName = state.userName,
                        )

                        if (state.overallStats.sessionCount > 0) {
                            QuickStatsCard(stats = state.overallStats)
                        }
                    }

                    // Latest badges section
                    if (state.recentBadges.isNotEmpty()) {
                        LatestBadgesSection(
                            badges = state.recentBadges,
                            onViewAllClicked = { state.eventSink(HomeScreen.Event.ViewBadgesClicked) },
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // View Full Stats link
                    if (state.overallStats.sessionCount > 0) {
                        TextButton(
                            onClick = { state.eventSink(HomeScreen.Event.ViewStatsClicked) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "View Full Stats",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Welcome section with personalized or generic greeting.
 * Shows grade level and accuracy if available.
 * Accuracy is only shown if the user has completed at least one practice session.
 */
@Composable
private fun WelcomeSection(
    userName: String?,
    gradeLevel: GradeLevel?,
    totalProblems: Int,
    accuracy: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Personalized greeting with mascot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Math Pup mascot
            Image(
                painter = painterResource(id = R.drawable.pup_tutor_sticker_standing_hand_left),
                contentDescription = "Math Pup waving",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(80.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text =
                        if (userName != null) {
                            "Hi $userName!"
                        } else {
                            "Welcome back!"
                        },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Ready to practice some math? 📚",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Grade level and accuracy (only show accuracy if problems solved)
        if (gradeLevel != null) {
            Text(
                text =
                    if (totalProblems > 0) {
                        "${gradeLevel.displayName} • ${accuracy.toInt()}% accuracy"
                    } else {
                        gradeLevel.displayName
                    },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

/**
 * Quick stats card showing total problems solved and accuracy.
 * If no problems have been solved yet, shows an encouraging message instead.
 */
@Composable
private fun QuickStatsCard(
    stats: SessionStats,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation =
            CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp,
            ),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Quick Stats",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )

            if (stats.totalProblems == 0) {
                // No practice sessions yet
                Text(
                    text = "No practice sessions yet. Start practicing to see your stats! 🚀",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                )
            } else {
                // Show stats when problems have been solved
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    // Total problems
                    StatItem(
                        label = "Problems Solved",
                        value = "${stats.totalProblems}",
                        emoji = "📝",
                    )

                    // Accuracy
                    StatItem(
                        label = "Accuracy",
                        value = "${stats.accuracy.toInt()}%",
                        emoji = "🎯",
                    )
                }
            }
        }
    }
}

/**
 * Individual stat item with emoji, value, and label.
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Latest badges section showing 3 most recently unlocked badges.
 */
@Composable
private fun LatestBadgesSection(
    badges: List<Badge>,
    onViewAllClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation =
            CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp,
            ),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Latest Badges",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                TextButton(onClick = onViewAllClicked) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            // Display badges in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                badges.take(3).forEach { badge ->
                    BadgeItem(badge = badge)
                }
            }
        }
    }
}

/**
 * Individual badge item with icon and name.
 */
@Composable
private fun BadgeItem(
    badge: Badge,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BadgeIconImage(
            badgeIcon = badge.icon,
            contentDescription = badge.name,
            size = 48.dp,
        )
        Text(
            text = badge.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeUiWithDataPreview() {
    KidsMathTutorAppTheme {
        HomeUi(
            state =
                HomeScreen.State(
                    userName = "Alex",
                    gradeLevel = GradeLevel.GRADE_1,
                    streakData =
                        DailyStreak(
                            currentStreak = 5,
                            longestStreak = 7,
                            lastPracticeDate = LocalDate.now(),
                            totalDaysPracticed = 10,
                        ),
                    overallStats =
                        SessionStats(
                            totalProblems = 150,
                            correctCount = 135,
                            accuracy = 90f,
                            sessionCount = 15,
                        ),
                    recentBadges =
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
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeUiNewUserPreview() {
    KidsMathTutorAppTheme {
        HomeUi(
            state =
                HomeScreen.State(
                    userName = null,
                    gradeLevel = null,
                    streakData = null,
                    overallStats = SessionStats.EMPTY,
                    recentBadges = emptyList(),
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeUiDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        HomeUi(
            state =
                HomeScreen.State(
                    userName = null,
                    gradeLevel = GradeLevel.GRADE_2,
                    streakData =
                        DailyStreak(
                            currentStreak = 3,
                            longestStreak = 5,
                            lastPracticeDate = LocalDate.now().minusDays(1),
                            totalDaysPracticed = 8,
                        ),
                    overallStats =
                        SessionStats(
                            totalProblems = 50,
                            correctCount = 42,
                            accuracy = 84f,
                            sessionCount = 5,
                        ),
                    recentBadges = emptyList(),
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
private fun HomeUiTabletLandscapePreview() {
    KidsMathTutorAppTheme {
        HomeUi(
            state =
                HomeScreen.State(
                    userName = "Alex",
                    gradeLevel = GradeLevel.GRADE_1,
                    streakData =
                        DailyStreak(
                            currentStreak = 5,
                            longestStreak = 7,
                            lastPracticeDate = LocalDate.now(),
                            totalDaysPracticed = 10,
                        ),
                    overallStats =
                        SessionStats(
                            totalProblems = 150,
                            correctCount = 135,
                            accuracy = 90f,
                            sessionCount = 15,
                        ),
                    recentBadges =
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
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 500,
    heightDp = 700,
    name = "Tablet Portrait",
)
@Composable
private fun HomeUiTabletPortraitPreview() {
    KidsMathTutorAppTheme {
        HomeUi(
            state =
                HomeScreen.State(
                    userName = "Alex",
                    gradeLevel = GradeLevel.GRADE_1,
                    streakData =
                        DailyStreak(
                            currentStreak = 5,
                            longestStreak = 7,
                            lastPracticeDate = LocalDate.now(),
                            totalDaysPracticed = 10,
                        ),
                    overallStats =
                        SessionStats(
                            totalProblems = 150,
                            correctCount = 135,
                            accuracy = 90f,
                            sessionCount = 15,
                        ),
                    recentBadges = emptyList(),
                    eventSink = {},
                ),
        )
    }
}
