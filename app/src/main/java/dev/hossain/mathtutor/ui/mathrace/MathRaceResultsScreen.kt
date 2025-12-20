package dev.hossain.mathtutor.ui.mathrace

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
import androidx.compose.material.icons.filled.Star
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
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import kotlinx.coroutines.delay

/**
 * Results screen shown after a Math Race game ends.
 *
 * Displays final score, new record indicator (if applicable),
 * game statistics, unlocked badges, and navigation buttons.
 *
 * @param finalScore Final score achieved
 * @param totalAttempts Total problems attempted
 * @param isNewRecord Whether this is a new personal best
 * @param accuracy Percentage of correct answers (0-100)
 * @param averageTimePerProblem Average seconds per problem
 * @param personalBest Player's personal best (previous or new)
 * @param userName Player's name
 * @param unlockedBadges List of badges unlocked during this game
 * @param onPlayAgain Callback to start a new game
 * @param onNavigateHome Callback to return to home
 * @param modifier Optional modifier
 */
@Composable
fun MathRaceResultsScreen(
    finalScore: Int,
    totalAttempts: Int,
    isNewRecord: Boolean,
    accuracy: Float,
    averageTimePerProblem: Float,
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
                    previousBest = if (isNewRecord) personalBest - finalScore + finalScore else personalBest,
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
                    averageTime = averageTimePerProblem,
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
    previousBest: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Score label
        Text(
            text = "Score",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Score value with animation
        val infiniteTransition = rememberInfiniteTransition(label = "score_pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = if (isNewRecord) 1.05f else 1.0f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis = 800,
                            easing = FastOutSlowInEasing,
                        ),
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
                        contentDescription = "Final score: $score"
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
                animation =
                    tween(
                        durationMillis = 600,
                        easing = FastOutSlowInEasing,
                    ),
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() },
            )

            // Correct answers
            StatRow(
                icon = Icons.Filled.CheckCircle,
                label = "Correct",
                value = "$correctAnswers / $totalAttempts (${accuracy.toInt()}%)",
                contentDescription = "$correctAnswers correct out of $totalAttempts attempts, ${accuracy.toInt()} percent accuracy",
            )

            // Average time
            StatRow(
                icon = Icons.Filled.Timer,
                label = "Avg. Time",
                value = String.format("%.1fs", averageTime),
                contentDescription = "Average time per problem: ${String.format("%.1f", averageTime)} seconds",
            )
        }
    }
}

/**
 * A row displaying a statistic with icon, label, and value.
 */
@Composable
private fun StatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    this.contentDescription = contentDescription
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Card displaying badges unlocked during this game session.
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
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics { heading() },
            ) {
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Badge${if (badges.size > 1) "s" else ""} Unlocked!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }

            badges.forEach { badge ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                contentDescription = "${badge.name}: ${badge.description}"
                            },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = badge.icon,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = badge.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = badge.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Play Again and Home buttons.
 */
@Composable
private fun ActionButtons(
    onPlayAgain: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Play Again button (primary)
        Button(
            onClick = onPlayAgain,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        this.contentDescription = "Play again"
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
            )
        }

        // Home button (secondary)
        OutlinedButton(
            onClick = onNavigateHome,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        this.contentDescription = "Go to home"
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
private fun MathRaceResultsScreenPreview() {
    KidsMathTutorAppTheme {
        MathRaceResultsScreen(
            finalScore = 21,
            totalAttempts = 23,
            isNewRecord = true,
            accuracy = 91.3f,
            averageTimePerProblem = 2.6f,
            personalBest = 21,
            userName = "Alex",
            onPlayAgain = {},
            onNavigateHome = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MathRaceResultsScreenNoRecordPreview() {
    KidsMathTutorAppTheme {
        MathRaceResultsScreen(
            finalScore = 15,
            totalAttempts = 18,
            isNewRecord = false,
            accuracy = 83.3f,
            averageTimePerProblem = 3.2f,
            personalBest = 21,
            userName = null,
            onPlayAgain = {},
            onNavigateHome = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MathRaceResultsScreenDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        MathRaceResultsScreen(
            finalScore = 25,
            totalAttempts = 27,
            isNewRecord = true,
            accuracy = 92.6f,
            averageTimePerProblem = 2.2f,
            personalBest = 25,
            userName = "Alex",
            onPlayAgain = {},
            onNavigateHome = {},
        )
    }
}
