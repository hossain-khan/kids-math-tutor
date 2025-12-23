package dev.hossain.mathtutor.ui.numbersequence

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import kotlinx.coroutines.delay
import dev.hossain.mathtutor.domain.model.BadgeIcon as BadgeIconModel
import dev.hossain.mathtutor.ui.component.BadgeIcon as BadgeIconUi

/**
 * Results screen shown after a Number Sequence game ends.
 *
 * Displays final score, new record indicator (if applicable),
 * game statistics, unlocked badges, and navigation buttons.
 *
 * @param finalScore Final score achieved
 * @param totalAttempts Total sequences attempted
 * @param isNewRecord Whether this is a new personal best
 * @param accuracy Percentage of correct answers (0-100)
 * @param averageTimePerSequence Average seconds per sequence
 * @param personalBest Player's personal best (previous or new)
 * @param userName Player's name
 * @param unlockedBadges List of badges unlocked during this game
 * @param onPlayAgain Callback to start a new game
 * @param onNavigateHome Callback to return to home
 * @param modifier Optional modifier
 */
@Composable
fun NumberSequenceResultsScreen(
    finalScore: Int,
    totalAttempts: Int,
    isNewRecord: Boolean,
    accuracy: Float,
    averageTimePerSequence: Float,
    personalBest: Int,
    userName: String?,
    unlockedBadges: List<Badge> = emptyList(),
    onPlayAgain: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    var showNewRecord by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var showBadges by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    // Stagger animations
    LaunchedEffect(Unit) {
        showContent = true
        delay(300)
        showNewRecord = true
        delay(200)
        showStats = true
        delay(200)
        if (unlockedBadges.isNotEmpty()) {
            showBadges = true
            delay(200)
        }
        showButtons = true
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Game Over title
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { -it },
            ) {
                Text(
                    text = "Game Over! 🎉",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier.semantics {
                            heading()
                            contentDescription = "Game Over"
                        },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Final score display
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { -it / 2 },
            ) {
                ScoreSection(
                    score = finalScore,
                    isNewRecord = isNewRecord && showNewRecord,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // New record indicator
            if (isNewRecord) {
                AnimatedVisibility(
                    visible = showNewRecord,
                    enter = fadeIn() + slideInVertically { it },
                ) {
                    NewRecordBadge()
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Stats card
            AnimatedVisibility(
                visible = showStats,
                enter = fadeIn() + slideInVertically { it / 2 },
            ) {
                StatsCard(
                    correctAnswers = finalScore,
                    totalAttempts = totalAttempts,
                    accuracy = accuracy,
                    averageTime = averageTimePerSequence,
                )
            }

            // Badge unlock section
            if (unlockedBadges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedVisibility(
                    visible = showBadges,
                    enter = fadeIn() + slideInVertically { it / 2 },
                ) {
                    UnlockedBadgesCard(badges = unlockedBadges)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            AnimatedVisibility(
                visible = showButtons,
                enter = fadeIn(),
            ) {
                ActionButtons(
                    onPlayAgain = onPlayAgain,
                    onNavigateHome = onNavigateHome,
                )
            }
        }
    }
}

/**
 * Displays the final score prominently.
 */
@Composable
private fun ScoreSection(
    score: Int,
    isNewRecord: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Sequences Solved",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        val infiniteTransition = rememberInfiniteTransition(label = "score_pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = if (isNewRecord) 1.05f else 1.0f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "score_scale",
        )

        Text(
            text = score.toString(),
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
            color =
                if (isNewRecord) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                },
            modifier =
                Modifier
                    .scale(scale)
                    .semantics {
                        contentDescription = "Final score: $score sequences"
                    },
        )
    }
}

/**
 * Animated new record badge with trophy icon.
 */
@Composable
private fun NewRecordBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "trophy_bounce")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "trophy_scale",
    )

    Card(
        modifier =
            modifier
                .scale(scale)
                .semantics {
                    contentDescription = "New Record! Congratulations!"
                },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "🏆 New Record!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Card displaying game statistics.
 */
@Composable
private fun StatsCard(
    correctAnswers: Int,
    totalAttempts: Int,
    accuracy: Float,
    averageTime: Float,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Correct answers
            StatRow(
                icon = Icons.Filled.CheckCircle,
                label = "Correct",
                value = "$correctAnswers / $totalAttempts",
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Accuracy
            StatRow(
                icon = Icons.Filled.CheckCircle,
                label = "Accuracy",
                value = "${accuracy.toInt()}%",
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Average time
            StatRow(
                icon = Icons.Filled.Timer,
                label = "Avg. Time",
                value = "%.1fs".format(averageTime),
            )
        }
    }
}

/**
 * Single row in the stats card.
 */
@Composable
private fun StatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Card showing unlocked badges.
 */
@Composable
private fun UnlockedBadgesCard(
    badges: List<Badge>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "🎖️ Badges Unlocked!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            badges.forEach { badge ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BadgeIconUi(
                        badgeIcon = badge.icon,
                        contentDescription = badge.name,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = badge.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = badge.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                    }
                }
                if (badge != badges.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * Action buttons for play again and go home.
 */
@Composable
private fun ActionButtons(
    onPlayAgain: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Play Again button
        Button(
            onClick = onPlayAgain,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        contentDescription = "Play again"
                        role = Role.Button
                    },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Play Again",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Home button
        OutlinedButton(
            onClick = onNavigateHome,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        contentDescription = "Go to home"
                        role = Role.Button
                    },
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Home",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NumberSequenceResultsScreenPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceResultsScreen(
            finalScore = 12,
            totalAttempts = 15,
            isNewRecord = true,
            accuracy = 80f,
            averageTimePerSequence = 6.5f,
            personalBest = 12,
            userName = "Alex",
            onPlayAgain = {},
            onNavigateHome = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NumberSequenceResultsScreenWithBadgesPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceResultsScreen(
            finalScore = 15,
            totalAttempts = 18,
            isNewRecord = true,
            accuracy = 83.3f,
            averageTimePerSequence = 5.0f,
            personalBest = 15,
            userName = "Alex",
            unlockedBadges =
                listOf(
                    Badge(
                        id = "sequence_solver",
                        name = "Sequence Solver",
                        description = "Complete your first Number Sequence game",
                        icon = BadgeIconModel.GAME_MASTER,
                        category = BadgeCategory.GAMES,
                        requirement = BadgeRequirement.GameCount(1),
                    ),
                ),
            onPlayAgain = {},
            onNavigateHome = {},
        )
    }
}
