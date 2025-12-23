package dev.hossain.mathtutor.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeIcon
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import java.time.Instant

/**
 * A grid component for displaying a list of badges.
 *
 * Displays badges in a responsive grid layout (3-4 per row) with Material 3 design.
 * Unlocked badges show full color with a checkmark, while locked badges are dimmed
 * (40% alpha) with a lock icon.
 *
 * @param badges List of badges to display
 * @param onBadgeClick Callback when a badge is clicked
 * @param modifier Optional modifier for the grid
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BadgeGrid(
    badges: List<Badge>,
    onBadgeClick: (Badge) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 4,
    ) {
        badges.forEach { badge ->
            BadgeCard(
                badge = badge,
                onClick = { onBadgeClick(badge) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgeGridPreview() {
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
                ),
            onBadgeClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgeGridDarkPreview() {
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
                ),
            onBadgeClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

/**
 * Individual badge card component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BadgeCard(
    badge: Badge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUnlocked = badge.isUnlocked()

    Card(
        onClick = onClick,
        modifier = modifier,
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
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Badge Icon with status indicator
                Box(contentAlignment = Alignment.TopEnd) {
                    BadgeIcon(
                        badgeIcon = badge.icon,
                        contentDescription = badge.name,
                        size = 56.dp,
                        colorFilter =
                            if (isUnlocked) {
                                null
                            } else {
                                ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            },
                    )

                    // Status icon (checkmark or lock)
                    Icon(
                        imageVector =
                            if (isUnlocked) {
                                Icons.Filled.Check
                            } else {
                                Icons.Filled.Lock
                            },
                        contentDescription =
                            if (isUnlocked) {
                                "Unlocked"
                            } else {
                                "Locked"
                            },
                        modifier = Modifier.size(16.dp),
                        tint =
                            if (isUnlocked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }

                // Badge Name
                Text(
                    text = badge.name,
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        if (isUnlocked) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
