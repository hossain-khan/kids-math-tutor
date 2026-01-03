package dev.hossain.mathtutor.ui.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.ui.component.FeatureTopAppBar
import dev.hossain.mathtutor.ui.component.TopBarFeature
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MAX_CONTENT_WIDTH_STANDARD
import dev.zacsweers.metro.AppScope

private val MIN_CARD_WIDTH: Dp = 280.dp

/**
 * Game selection screen with adaptive layout.
 *
 * Adaptive Layout:
 * - Compact (<600dp): Single column layout
 * - Medium (600-840dp): 2 game cards per row
 * - Expanded (>840dp): 3+ game cards per row
 * - Content centered with max width on larger screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(GameSelectionScreen::class, AppScope::class)
@Composable
fun GameSelectionUi(
    state: GameSelectionScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            FeatureTopAppBar(
                title = { Text("Games") },
                feature = TopBarFeature.GAMES,
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        // Center content on wide screens
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .widthIn(max = MAX_CONTENT_WIDTH_STANDARD)
                        .fillMaxSize()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header with mascot
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp),
                    ) {
                        // Math Pup juggling number blocks
                        Image(
                            painter = painterResource(id = R.drawable.pup_tutor_sticker_juggling_number_blocks),
                            contentDescription = "Math Pup juggling numbers",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(100.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Play fun math games!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Solve more problems to unlock new games",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Adaptive grid of game cards
                items(state.gameInfoList, key = { it.game.name }) { gameInfo ->
                    GameCard(
                        gameInfo = gameInfo,
                        totalProblemsSolved = state.totalProblemsSolved,
                        onPlayClicked = {
                            // If game is locked, it's a trial play
                            state.eventSink(
                                GameSelectionScreen.Event.PlayGame(
                                    game = gameInfo.game,
                                    isTrial = !gameInfo.isUnlocked,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCard(
    gameInfo: GameSelectionScreen.GameInfo,
    totalProblemsSolved: Int,
    onPlayClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val game = gameInfo.game
    val isUnlocked = gameInfo.isUnlocked

    // Animate the card alpha based on unlock state
    val cardAlpha by animateColorAsState(
        targetValue =
            if (isUnlocked) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        label = "cardColor",
    )

    ElevatedCard(
        modifier =
            modifier
                .fillMaxWidth()
                .alpha(if (isUnlocked) 1f else 0.6f),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            // Game icon and title row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Game icon
                Text(
                    text = game.icon,
                    style = MaterialTheme.typography.displaySmall,
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Title and description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = game.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Lock icon for locked games
                if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isUnlocked) {
                // Show stats for unlocked games
                UnlockedGameContent(
                    gameInfo = gameInfo,
                    onPlayClicked = onPlayClicked,
                )
            } else {
                // Show unlock progress for locked games
                LockedGameContent(
                    game = game,
                    totalProblemsSolved = totalProblemsSolved,
                    trialAttempts = gameInfo.trialAttempts,
                    onTryGameClicked = onPlayClicked,
                )
            }
        }
    }
}

@Composable
private fun UnlockedGameContent(
    gameInfo: GameSelectionScreen.GameInfo,
    onPlayClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Stats row - show if there are any meaningful stats
        if (gameInfo.personalBest > 0 || gameInfo.totalPlays > 0) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Personal best
                if (gameInfo.personalBest > 0) {
                    StatItem(
                        icon = "🏆",
                        label = "Best",
                        value = "${gameInfo.personalBest}",
                    )
                }

                // Total plays
                if (gameInfo.totalPlays > 0) {
                    StatItem(
                        icon = "🎮",
                        label = "Plays",
                        value = "${gameInfo.totalPlays}",
                    )
                }
            }
        }

        // Play button
        Button(
            onClick = onPlayClicked,
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PLAY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun LockedGameContent(
    game: Game,
    totalProblemsSolved: Int,
    trialAttempts: Int,
    onTryGameClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxTrials = 3
    val trialsRemaining = maxTrials - trialAttempts
    val hasTrialsLeft = trialsRemaining > 0

    Column(modifier = modifier) {
        // Unlock progress
        val progress = game.unlockProgress(totalProblemsSolved)
        val problemsNeeded = game.problemsUntilUnlock(totalProblemsSolved)

        Text(
            text = "Solve $problemsNeeded more problems to unlock!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        // Progress text
        Text(
            text = "$totalProblemsSolved / ${game.unlockRequirement} problems",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            textAlign = TextAlign.End,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Trial attempts info
        if (hasTrialsLeft) {
            Text(
                text = "🎁 Try this game $trialsRemaining ${if (trialsRemaining == 1) "time" else "times"} before unlocking!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        } else {
            Text(
                text = "✨ Thanks for trying! Practice more to unlock unlimited gameplay!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        // Try Game or Locked button
        if (hasTrialsLeft) {
            Button(
                onClick = onTryGameClicked,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                    ),
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TRY GAME",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            Button(
                onClick = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LOCKED",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true, name = "Compact Phone", widthDp = 411, heightDp = 891)
@Composable
private fun GameSelectionUiPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        GameSelectionUi(
            state =
                GameSelectionScreen.State(
                    gameInfoList =
                        listOf(
                            GameSelectionScreen.GameInfo(
                                game = Game.MATH_RACE,
                                isUnlocked = true,
                                personalBest = 15,
                                totalPlays = 5,
                                trialAttempts = 0,
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.MEMORY_MATCH,
                                isUnlocked = false,
                                personalBest = 0,
                                totalPlays = 0,
                                trialAttempts = 1,
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.NUMBER_SEQUENCE,
                                isUnlocked = false,
                                personalBest = 0,
                                totalPlays = 0,
                                trialAttempts = 3,
                            ),
                        ),
                    totalProblemsSolved = 75,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true, name = "Medium Tablet", widthDp = 700, heightDp = 500)
@Composable
private fun GameSelectionUiMediumPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        GameSelectionUi(
            state =
                GameSelectionScreen.State(
                    gameInfoList =
                        listOf(
                            GameSelectionScreen.GameInfo(
                                game = Game.MATH_RACE,
                                isUnlocked = true,
                                personalBest = 15,
                                totalPlays = 5,
                                trialAttempts = 0,
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.MEMORY_MATCH,
                                isUnlocked = false,
                                personalBest = 0,
                                totalPlays = 0,
                                trialAttempts = 1,
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.NUMBER_SEQUENCE,
                                isUnlocked = false,
                                personalBest = 0,
                                totalPlays = 0,
                                trialAttempts = 3,
                            ),
                        ),
                    totalProblemsSolved = 75,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true, name = "Expanded Tablet", widthDp = 1100, heightDp = 600)
@Composable
private fun GameSelectionUiExpandedPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme {
        GameSelectionUi(
            state =
                GameSelectionScreen.State(
                    gameInfoList =
                        listOf(
                            GameSelectionScreen.GameInfo(
                                game = Game.MATH_RACE,
                                isUnlocked = true,
                                personalBest = 15,
                                totalPlays = 5,
                                trialAttempts = 0,
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.MEMORY_MATCH,
                                isUnlocked = false,
                                personalBest = 0,
                                totalPlays = 0,
                                trialAttempts = 1,
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.NUMBER_SEQUENCE,
                                isUnlocked = false,
                                personalBest = 0,
                                totalPlays = 0,
                                trialAttempts = 3,
                            ),
                        ),
                    totalProblemsSolved = 75,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true, name = "Dark Theme")
@Composable
private fun GameSelectionUiDarkPreview() {
    dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme(darkTheme = true) {
        GameSelectionUi(
            state =
                GameSelectionScreen.State(
                    gameInfoList =
                        listOf(
                            GameSelectionScreen.GameInfo(
                                game = Game.MATH_RACE,
                                isUnlocked = true,
                                personalBest = 20,
                                totalPlays = 8,
                                trialAttempts = 0,
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.MEMORY_MATCH,
                                isUnlocked = true,
                                personalBest = 12,
                                totalPlays = 3,
                                trialAttempts = 0,
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.NUMBER_SEQUENCE,
                                isUnlocked = false,
                                personalBest = 0,
                                totalPlays = 0,
                                trialAttempts = 2,
                            ),
                        ),
                    totalProblemsSolved = 150,
                    eventSink = {},
                ),
        )
    }
}
