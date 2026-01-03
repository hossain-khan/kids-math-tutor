package dev.hossain.mathtutor.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeIcon
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import java.time.Instant

// Width breakpoints for adaptive badge layouts
private val COMPACT_BREAKPOINT: Dp = 600.dp
private val EXPANDED_BREAKPOINT: Dp = 840.dp

/**
 * A grid component for displaying a list of badges with adaptive column layout.
 *
 * Displays badges in a responsive grid layout with Material 3 design:
 * - Compact (<600dp): 3 badges per row
 * - Medium (600-840dp): 4 badges per row
 * - Expanded (>840dp): 6 badges per row
 *
 * Unlocked badges show full color with a checkmark, while locked badges are dimmed
 * (40% alpha) with a lock icon. Badge icon sizes scale appropriately for larger screens
 * while maintaining 48dp minimum touch targets.
 *
 * @param badges List of badges to display
 * @param onBadgeClick Callback when a badge is clicked
 * @param modifier Optional modifier for the grid
 */
@Composable
fun BadgeGrid(
    badges: List<Badge>,
    onBadgeClick: (Badge) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val screenWidth = maxWidth

        // Calculate adaptive column count and badge size based on screen width
        val columnCount =
            when {
                screenWidth < COMPACT_BREAKPOINT -> 3

                // Compact: phones
                screenWidth < EXPANDED_BREAKPOINT -> 4

                // Medium: small tablets
                else -> 6 // Expanded: large tablets
            }

        // Scale badge icon size based on screen width
        val badgeIconSize =
            when {
                screenWidth < COMPACT_BREAKPOINT -> 56.dp
                screenWidth < EXPANDED_BREAKPOINT -> 80.dp
                else -> 96.dp
            }

        // Adaptive spacing based on screen width
        val gridSpacing =
            when {
                screenWidth < COMPACT_BREAKPOINT -> 12.dp
                screenWidth < EXPANDED_BREAKPOINT -> 16.dp
                else -> 20.dp
            }

        // Calculate height needed for the grid
        // Each badge card maintains 1:1 aspect ratio, plus spacing
        // Add extra padding for card elevation shadows (4dp top + 4dp bottom = 8dp total)
        val elevationPadding = 8.dp
        val badgeCardSize = (screenWidth - (gridSpacing * (columnCount - 1))) / columnCount
        val rows = (badges.size + columnCount - 1) / columnCount // Ceiling division
        val gridHeight = (badgeCardSize * rows) + (gridSpacing * (rows - 1).coerceAtLeast(0)) + elevationPadding

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .size(width = screenWidth, height = gridHeight),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
            verticalArrangement = Arrangement.spacedBy(gridSpacing),
            contentPadding = PaddingValues(vertical = 4.dp),
            userScrollEnabled = false, // Disable scrolling since parent LazyColumn handles it
        ) {
            items(badges, key = { it.id }) { badge ->
                BadgeCard(
                    badge = badge,
                    onClick = { onBadgeClick(badge) },
                    badgeIconSize = badgeIconSize,
                    screenWidth = screenWidth,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891, name = "Compact - Phone (3 badges/row)")
@Composable
private fun BadgeGridCompactPreview() {
    KidsMathTutorAppTheme {
        BadgeGrid(
            badges =
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
                        id = "math_rookie",
                        name = "Math Rookie",
                        description = "Solved 25 problems",
                        icon = BadgeIcon.MATH_ROOKIE,
                        category = BadgeCategory.VOLUME,
                        requirement = BadgeRequirement.ProblemCount(25),
                        unlockedAt = null,
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
                        id = "speed_demon",
                        name = "Speed Demon",
                        description = "Score 20+ in Math Race",
                        icon = BadgeIcon.SPEED_DEMON,
                        category = BadgeCategory.GAMES,
                        requirement = BadgeRequirement.MathRaceScore(20),
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
            onBadgeClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 700, heightDp = 500, name = "Medium - Tablet (4 badges/row)")
@Composable
private fun BadgeGridMediumPreview() {
    KidsMathTutorAppTheme {
        BadgeGrid(
            badges =
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
                        id = "math_rookie",
                        name = "Math Rookie",
                        description = "Solved 25 problems",
                        icon = BadgeIcon.MATH_ROOKIE,
                        category = BadgeCategory.VOLUME,
                        requirement = BadgeRequirement.ProblemCount(25),
                        unlockedAt = null,
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
                        id = "speed_demon",
                        name = "Speed Demon",
                        description = "Score 20+ in Math Race",
                        icon = BadgeIcon.SPEED_DEMON,
                        category = BadgeCategory.GAMES,
                        requirement = BadgeRequirement.MathRaceScore(20),
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
                    Badge(
                        id = "math_master",
                        name = "Math Master",
                        description = "Solved 500 problems",
                        icon = BadgeIcon.MATH_LEGEND,
                        category = BadgeCategory.VOLUME,
                        requirement = BadgeRequirement.ProblemCount(500),
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
            onBadgeClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 1100, heightDp = 600, name = "Expanded - Large Tablet (6 badges/row)")
@Composable
private fun BadgeGridExpandedPreview() {
    KidsMathTutorAppTheme {
        BadgeGrid(
            badges =
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
                        id = "math_rookie",
                        name = "Math Rookie",
                        description = "Solved 25 problems",
                        icon = BadgeIcon.MATH_ROOKIE,
                        category = BadgeCategory.VOLUME,
                        requirement = BadgeRequirement.ProblemCount(25),
                        unlockedAt = null,
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
                        id = "speed_demon",
                        name = "Speed Demon",
                        description = "Score 20+ in Math Race",
                        icon = BadgeIcon.SPEED_DEMON,
                        category = BadgeCategory.GAMES,
                        requirement = BadgeRequirement.MathRaceScore(20),
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
                    Badge(
                        id = "math_master",
                        name = "Math Master",
                        description = "Solved 500 problems",
                        icon = BadgeIcon.MATH_LEGEND,
                        category = BadgeCategory.VOLUME,
                        requirement = BadgeRequirement.ProblemCount(500),
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
                    Badge(
                        id = "perfect_race",
                        name = "Perfect Race",
                        description = "Complete Math Race with no mistakes",
                        icon = BadgeIcon.PERFECT_RACE,
                        category = BadgeCategory.GAMES,
                        requirement = BadgeRequirement.PerfectGameAccuracy,
                        unlockedAt = Instant.now(),
                    ),
                    Badge(
                        id = "addition_expert",
                        name = "Addition Expert",
                        description = "Master addition operations",
                        icon = BadgeIcon.ADDITION_EXPERT,
                        category = BadgeCategory.OPERATION_MASTERY,
                        requirement = BadgeRequirement.OperationCount(dev.hossain.mathtutor.domain.model.MathOperation.ADDITION, 50),
                        unlockedAt = null,
                    ),
                    Badge(
                        id = "subtraction_star",
                        name = "Subtraction Star",
                        description = "Master subtraction operations",
                        icon = BadgeIcon.SUBTRACTION_STAR,
                        category = BadgeCategory.OPERATION_MASTERY,
                        requirement = BadgeRequirement.OperationCount(dev.hossain.mathtutor.domain.model.MathOperation.SUBTRACTION, 50),
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
            onBadgeClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 700, heightDp = 500, name = "Medium - Tablet Dark (4 badges/row)")
@Composable
private fun BadgeGridMediumDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        BadgeGrid(
            badges =
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
                        id = "math_rookie",
                        name = "Math Rookie",
                        description = "Solved 25 problems",
                        icon = BadgeIcon.MATH_ROOKIE,
                        category = BadgeCategory.VOLUME,
                        requirement = BadgeRequirement.ProblemCount(25),
                        unlockedAt = null,
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
                        id = "speed_demon",
                        name = "Speed Demon",
                        description = "Score 20+ in Math Race",
                        icon = BadgeIcon.SPEED_DEMON,
                        category = BadgeCategory.GAMES,
                        requirement = BadgeRequirement.MathRaceScore(20),
                        unlockedAt = Instant.now(),
                    ),
                ),
            onBadgeClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

/**
 * Individual badge card component with adaptive sizing.
 *
 * @param badge Badge to display
 * @param onClick Callback when badge is clicked
 * @param badgeIconSize Size for the badge icon
 * @param screenWidth Current screen width for responsive sizing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BadgeCard(
    badge: Badge,
    onClick: () -> Unit,
    badgeIconSize: Dp,
    screenWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val isUnlocked = badge.isUnlocked()

    // Adaptive padding and spacing based on screen width
    val cardPadding =
        when {
            screenWidth < COMPACT_BREAKPOINT -> 8.dp
            screenWidth < EXPANDED_BREAKPOINT -> 16.dp
            else -> 20.dp
        }

    val contentSpacing =
        when {
            screenWidth < COMPACT_BREAKPOINT -> 4.dp
            screenWidth < EXPANDED_BREAKPOINT -> 8.dp
            else -> 12.dp
        }

    // Adaptive text style based on screen width
    val textStyle =
        when {
            screenWidth < COMPACT_BREAKPOINT -> MaterialTheme.typography.labelSmall
            screenWidth < EXPANDED_BREAKPOINT -> MaterialTheme.typography.labelMedium
            else -> MaterialTheme.typography.labelLarge
        }

    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f), // Maintain square aspect ratio
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isUnlocked) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(cardPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Badge Icon with status indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        BadgeIcon(
                            badgeIcon = badge.icon,
                            contentDescription = badge.name,
                            size = badgeIconSize,
                            colorFilter =
                                if (isUnlocked) {
                                    null
                                } else {
                                    ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                },
                        )

                        // Status icon (checkmark or lock) - scaled relative to badge icon
                        val statusIconSize = (badgeIconSize.value * 0.3f).dp
                        Icon(
                            imageVector =
                                if (isUnlocked) {
                                    Icons.Filled.CheckCircleOutline
                                } else {
                                    Icons.Filled.Lock
                                },
                            contentDescription =
                                if (isUnlocked) {
                                    "Unlocked"
                                } else {
                                    "Locked"
                                },
                            modifier = Modifier.size(statusIconSize),
                            tint =
                                if (isUnlocked) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }

                // Badge Name
                Text(
                    text = badge.name,
                    style = textStyle,
                    color =
                        if (isUnlocked) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = contentSpacing),
                )
            }
        }
    }
}
