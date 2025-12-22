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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
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
import dev.zacsweers.metro.AppScope

// Width breakpoints for adaptive layouts
private val MAX_CONTENT_WIDTH: Dp = 700.dp

/**
 * Game selection screen with adaptive layout.
 *
 * Adaptive Layout:
 * - Compact: Full width game cards
 * - Medium/Expanded: Centered content with max width
 */
@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(GameSelectionScreen::class, AppScope::class)
@Composable
fun GameSelectionUi(
    state: GameSelectionScreen.State,
    modifier: Modifier = Modifier,
) {
    /*
     * IMPORTANT: Explicit BackHandler to prevent ANR on system back button press.
     *
     * Without this BackHandler, pressing the system back button causes a 5+ second freeze
     * with 97-110% CPU usage on the main thread, triggering an ANR (Application Not Responding).
     * The BackHandler ensures immediate navigation response by handling the back event directly
     * and triggering navigation without blocking the UI thread.
     *
     * See: PR #143 for details on the ANR issue and fix.
     */
    BackHandler {
        state.eventSink(GameSelectionScreen.Event.NavigateBack)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Games") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(GameSelectionScreen.Event.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                modifier = Modifier.shadow(elevation = 4.dp),
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
                        .widthIn(max = MAX_CONTENT_WIDTH)
                        .fillMaxSize()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    // Header with mascot
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp),
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

                items(state.gameInfoList, key = { it.game.name }) { gameInfo ->
                    GameCard(
                        gameInfo = gameInfo,
                        totalProblemsSolved = state.totalProblemsSolved,
                        onPlayClicked = { state.eventSink(GameSelectionScreen.Event.PlayGame(gameInfo.game)) },
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
    modifier: Modifier = Modifier,
) {
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

        // Locked button (disabled)
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

@Preview(showBackground = true)
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
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.MEMORY_MATCH,
                                isUnlocked = false,
                                personalBest = 0,
                                totalPlays = 0,
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.NUMBER_SEQUENCE,
                                isUnlocked = false,
                                personalBest = 0,
                                totalPlays = 0,
                            ),
                        ),
                    totalProblemsSolved = 75,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
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
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.MEMORY_MATCH,
                                isUnlocked = true,
                                personalBest = 12,
                                totalPlays = 3,
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.NUMBER_SEQUENCE,
                                isUnlocked = false,
                                personalBest = 0,
                                totalPlays = 0,
                            ),
                        ),
                    totalProblemsSolved = 150,
                    eventSink = {},
                ),
        )
    }
}
