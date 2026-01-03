package dev.hossain.mathtutor.ui.memorymatch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.ui.component.BadgeIcon
import dev.hossain.mathtutor.ui.mathrace.CountdownScreen
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MAX_CONTENT_WIDTH_SMALL
import dev.zacsweers.metro.AppScope

/**
 * Main UI for [MemoryMatchScreen].
 *
 * Routes to the appropriate screen based on the current game state:
 * - NotStarted → Start screen with game description and start button
 * - Countdown → Animated 3-2-1-GO countdown
 * - Playing → Game screen with 4×4 card grid
 * - Finished → Results screen with stats
 */
@CircuitInject(MemoryMatchScreen::class, AppScope::class)
@Composable
fun MemoryMatchUi(
    state: MemoryMatchScreen.State,
    modifier: Modifier = Modifier,
) {
    when (val gameState = state.gameState) {
        is MemoryMatchScreen.GameState.NotStarted -> {
            MemoryMatchStartScreen(
                personalBestTime = state.personalBestTime,
                userName = state.userName,
                onStartGame = { state.eventSink(MemoryMatchScreen.Event.StartGame) },
                onNavigateHome = { state.eventSink(MemoryMatchScreen.Event.NavigateHome) },
                modifier = modifier,
            )
        }

        is MemoryMatchScreen.GameState.Countdown -> {
            CountdownScreen(
                countdownValue = gameState.countdownValue,
                modifier = modifier,
            )
        }

        is MemoryMatchScreen.GameState.Playing -> {
            MemoryMatchGameScreen(
                cards = state.cards,
                moves = state.moves,
                timeElapsed = state.timeElapsed,
                matchesFound = state.matchesFound,
                totalPairs = state.totalPairs,
                onCardFlipped = { cardId -> state.eventSink(MemoryMatchScreen.Event.CardFlipped(cardId)) },
                modifier = modifier,
            )
        }

        is MemoryMatchScreen.GameState.Finished -> {
            MemoryMatchResultsScreen(
                moves = gameState.moves,
                timeElapsed = gameState.timeElapsed,
                isNewRecord = gameState.isNewRecord,
                accuracy = gameState.accuracy,
                personalBestTime = state.personalBestTime,
                userName = state.userName,
                unlockedBadges = gameState.unlockedBadges,
                onPlayAgain = { state.eventSink(MemoryMatchScreen.Event.PlayAgain) },
                onNavigateHome = { state.eventSink(MemoryMatchScreen.Event.NavigateHome) },
                modifier = modifier,
            )
        }
    }
}

/**
 * Start screen for Memory Match game.
 * Shows game instructions and personal best time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryMatchStartScreen(
    personalBestTime: Int,
    userName: String?,
    onStartGame: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Match") },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            // Center content on tablets
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier =
                        Modifier
                            .widthIn(max = MAX_CONTENT_WIDTH_SMALL)
                            .fillMaxSize()
                            .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Game icon
                    Text(
                        text = "🧩",
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    // Welcome message
                    if (!userName.isNullOrBlank()) {
                        Text(
                            text = "Ready, $userName?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }

                    Text(
                        text = "Memory Match",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp),
                    )

                    // Game instructions
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                        ) {
                            Text(
                                text = "How to Play:",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )

                            InstructionItem("🃏 Flip two cards to find pairs")
                            InstructionItem("🧮 Match math problems with answers")
                            InstructionItem("🎯 Find all 8 pairs to win")
                            InstructionItem("⚡ Complete as fast as you can!")
                        }
                    }

                    // Personal best
                    if (personalBestTime > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .padding(bottom = 32.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Personal Best",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                                Text(
                                    text = formatTime(personalBestTime),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }
                    }

                    // Start button
                    Button(
                        onClick = onStartGame,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                    ) {
                        Text(
                            text = "Start Game",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper composable for instruction items.
 */
@Composable
private fun InstructionItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Game screen with 4×4 card grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryMatchGameScreen(
    cards: List<MemoryMatchScreen.Card>,
    moves: Int,
    timeElapsed: Int,
    matchesFound: Int,
    totalPairs: Int,
    onCardFlipped: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Timer
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatTime(timeElapsed),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        // Matches progress
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$matchesFound/$totalPairs",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        modifier = modifier,
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            // Center content on tablets
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier =
                        Modifier
                            .widthIn(max = MAX_CONTENT_WIDTH_SMALL)
                            .fillMaxSize()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Moves counter
                    Text(
                        text = "Moves: $moves",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    // 4×4 card grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp),
                    ) {
                        items(cards, key = { it.id }) { card ->
                            FlippableCard(
                                card = card,
                                onClick = { onCardFlipped(card.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated flippable card component.
 * Shows front (hidden) or back (content) based on flip state.
 */
@Composable
fun FlippableCard(
    card: MemoryMatchScreen.Card,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardRotation",
    )

    val isBackVisible = rotation > 90f

    Card(
        modifier =
            modifier
                .size(80.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }.clickable(enabled = !card.isFlipped && !card.isMatched) { onClick() },
        colors =
            if (card.isMatched) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                )
            } else if (card.isFlipped) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            } else {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                )
            },
        border =
            BorderStroke(
                width = 2.dp,
                color =
                    if (card.isMatched) {
                        MaterialTheme.colorScheme.tertiary
                    } else if (card.isFlipped) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    },
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Flip text on back side
                        rotationY = if (isBackVisible) 180f else 0f
                    },
        ) {
            if (isBackVisible) {
                // Show card content
                Text(
                    text = card.content,
                    style = MaterialTheme.typography.titleMedium,
                    color =
                        if (card.isMatched) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(4.dp),
                )
            } else {
                // Show card back (question mark)
                Text(
                    text = "?",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                )
            }
        }
    }
}

/**
 * Results screen showing game statistics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryMatchResultsScreen(
    moves: Int,
    timeElapsed: Int,
    isNewRecord: Boolean,
    accuracy: Float,
    personalBestTime: Int,
    userName: String?,
    unlockedBadges: List<Badge>,
    onPlayAgain: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Over!") },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Home")
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
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            // Center content on tablets
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier =
                        Modifier
                            .widthIn(max = MAX_CONTENT_WIDTH_SMALL)
                            .fillMaxSize()
                            .systemBarsPadding()
                            .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Celebration emoji
                    Text(
                        text = if (isNewRecord) "🏆" else "🎉",
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    // Congratulations message
                    if (!userName.isNullOrBlank()) {
                        Text(
                            text = "Great job, $userName!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }

                    if (isNewRecord) {
                        Text(
                            text = "New Record!",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(bottom = 24.dp),
                        )
                    }

                    // Stats card
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Your Time",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )

                            Text(
                                text = formatTime(timeElapsed),
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp),
                            )

                            // Previous best
                            if (personalBestTime > 0 && personalBestTime != timeElapsed) {
                                Text(
                                    text = "Previous: ${formatTime(personalBestTime)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 16.dp),
                                )
                            }

                            // Move count
                            StatRow("Moves", moves.toString())
                            StatRow("Accuracy", String.format("%.1f%%", accuracy))
                        }
                    }

                    // Unlocked badges
                    AnimatedVisibility(
                        visible = unlockedBadges.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                    ) {
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                ),
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "🌟 Badge${if (unlockedBadges.size > 1) "s" else ""} Unlocked!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(bottom = 12.dp),
                                )

                                unlockedBadges.forEach { badge ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                    ) {
                                        BadgeIcon(
                                            badgeIcon = badge.icon,
                                            contentDescription = badge.name,
                                            size = 32.dp,
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = badge.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons
                    Button(
                        onClick = onPlayAgain,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(bottom = 12.dp),
                    ) {
                        Text(
                            text = "Play Again",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    FilledTonalButton(
                        onClick = onNavigateHome,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                    ) {
                        Text(
                            text = "Home",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper composable for stat rows.
 */
@Composable
private fun StatRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Formats seconds into MM:SS format.
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}

// Preview composables - Start Screen state
@Preview(
    showBackground = true,
    device = "spec:width=891dp,height=411dp,dpi=420,isRound=false,orientation=landscape",
    name = "Phone Landscape - Start",
)
@Composable
private fun MemoryMatchUiPhoneLandscapeStartPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState = MemoryMatchScreen.GameState.NotStarted,
                    cards = emptyList(),
                    moves = 0,
                    timeElapsed = 0,
                    matchesFound = 0,
                    totalPairs = 8,
                    personalBestTime = 45,
                    userName = "Alex",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240,isRound=false,orientation=portrait",
    name = "Tablet Portrait - Start",
)
@Composable
private fun MemoryMatchUiTabletPortraitStartPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState = MemoryMatchScreen.GameState.NotStarted,
                    cards = emptyList(),
                    moves = 0,
                    timeElapsed = 0,
                    matchesFound = 0,
                    totalPairs = 8,
                    personalBestTime = 38,
                    userName = "Jordan",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,isRound=false,orientation=landscape",
    name = "Tablet Landscape - Start",
)
@Composable
private fun MemoryMatchUiTabletLandscapeStartPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState = MemoryMatchScreen.GameState.NotStarted,
                    cards = emptyList(),
                    moves = 0,
                    timeElapsed = 0,
                    matchesFound = 0,
                    totalPairs = 8,
                    personalBestTime = 32,
                    userName = "Sam",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=673dp,height=841dp,dpi=373,isRound=false,orientation=portrait",
    name = "Foldable Portrait (Pixel Fold Unfolded) - Start",
)
@Composable
private fun MemoryMatchUiFoldablePortraitStartPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState = MemoryMatchScreen.GameState.NotStarted,
                    cards = emptyList(),
                    moves = 0,
                    timeElapsed = 0,
                    matchesFound = 0,
                    totalPairs = 8,
                    personalBestTime = 40,
                    userName = "Chris",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=841dp,height=673dp,dpi=373,isRound=false,orientation=landscape",
    name = "Foldable Landscape (Pixel Fold Unfolded) - Start",
)
@Composable
private fun MemoryMatchUiFoldableLandscapeStartPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState = MemoryMatchScreen.GameState.NotStarted,
                    cards = emptyList(),
                    moves = 0,
                    timeElapsed = 0,
                    matchesFound = 0,
                    totalPairs = 8,
                    personalBestTime = 35,
                    userName = "Taylor",
                    eventSink = {},
                ),
        )
    }
}

// Helper function to create sample cards for previews
private fun createSampleCards(): List<MemoryMatchScreen.Card> =
    listOf(
        MemoryMatchScreen.Card(id = 0, content = "3+5", pairId = 0),
        MemoryMatchScreen.Card(id = 1, content = "8", pairId = 0, isFlipped = true),
        MemoryMatchScreen.Card(id = 2, content = "4+2", pairId = 1),
        MemoryMatchScreen.Card(id = 3, content = "6", pairId = 1, isMatched = true, isFlipped = true),
        MemoryMatchScreen.Card(id = 4, content = "7-3", pairId = 2),
        MemoryMatchScreen.Card(id = 5, content = "4", pairId = 2, isMatched = true, isFlipped = true),
        MemoryMatchScreen.Card(id = 6, content = "9-5", pairId = 3),
        MemoryMatchScreen.Card(id = 7, content = "4", pairId = 3),
        MemoryMatchScreen.Card(id = 8, content = "2+6", pairId = 4),
        MemoryMatchScreen.Card(id = 9, content = "8", pairId = 4),
        MemoryMatchScreen.Card(id = 10, content = "5+4", pairId = 5),
        MemoryMatchScreen.Card(id = 11, content = "9", pairId = 5),
        MemoryMatchScreen.Card(id = 12, content = "6-2", pairId = 6),
        MemoryMatchScreen.Card(id = 13, content = "4", pairId = 6),
        MemoryMatchScreen.Card(id = 14, content = "3+4", pairId = 7),
        MemoryMatchScreen.Card(id = 15, content = "7", pairId = 7),
    )

// Preview composables - Playing state
@Preview(
    showBackground = true,
    device = "spec:width=891dp,height=411dp,dpi=420,isRound=false,orientation=landscape",
    name = "Phone Landscape - Playing",
)
@Composable
private fun MemoryMatchUiPhoneLandscapePlayingPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState = MemoryMatchScreen.GameState.Playing,
                    cards = createSampleCards(),
                    moves = 12,
                    timeElapsed = 28,
                    matchesFound = 2,
                    totalPairs = 8,
                    personalBestTime = 45,
                    userName = "Alex",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240,isRound=false,orientation=portrait",
    name = "Tablet Portrait - Playing",
)
@Composable
private fun MemoryMatchUiTabletPortraitPlayingPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState = MemoryMatchScreen.GameState.Playing,
                    cards = createSampleCards(),
                    moves = 8,
                    timeElapsed = 18,
                    matchesFound = 3,
                    totalPairs = 8,
                    personalBestTime = 38,
                    userName = "Jordan",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,isRound=false,orientation=landscape",
    name = "Tablet Landscape - Playing",
)
@Composable
private fun MemoryMatchUiTabletLandscapePlayingPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState = MemoryMatchScreen.GameState.Playing,
                    cards = createSampleCards(),
                    moves = 15,
                    timeElapsed = 35,
                    matchesFound = 5,
                    totalPairs = 8,
                    personalBestTime = 32,
                    userName = "Sam",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=673dp,height=841dp,dpi=373,isRound=false,orientation=portrait",
    name = "Foldable Portrait (Pixel Fold Unfolded) - Playing",
)
@Composable
private fun MemoryMatchUiFoldablePortraitPlayingPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState = MemoryMatchScreen.GameState.Playing,
                    cards = createSampleCards(),
                    moves = 10,
                    timeElapsed = 22,
                    matchesFound = 4,
                    totalPairs = 8,
                    personalBestTime = 40,
                    userName = "Chris",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=841dp,height=673dp,dpi=373,isRound=false,orientation=landscape",
    name = "Foldable Landscape (Pixel Fold Unfolded) - Playing",
)
@Composable
private fun MemoryMatchUiFoldableLandscapePlayingPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState = MemoryMatchScreen.GameState.Playing,
                    cards = createSampleCards(),
                    moves = 18,
                    timeElapsed = 42,
                    matchesFound = 6,
                    totalPairs = 8,
                    personalBestTime = 35,
                    userName = "Taylor",
                    eventSink = {},
                ),
        )
    }
}

// Preview composables - Results state
@Preview(
    showBackground = true,
    device = "spec:width=891dp,height=411dp,dpi=420,isRound=false,orientation=landscape",
    name = "Phone Landscape - Results",
)
@Composable
private fun MemoryMatchUiPhoneLandscapeResultsPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState =
                        MemoryMatchScreen.GameState.Finished(
                            moves = 20,
                            timeElapsed = 42,
                            isNewRecord = true,
                            accuracy = 80f,
                        ),
                    cards = emptyList(),
                    moves = 20,
                    timeElapsed = 42,
                    matchesFound = 8,
                    totalPairs = 8,
                    personalBestTime = 42,
                    userName = "Alex",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240,isRound=false,orientation=portrait",
    name = "Tablet Portrait - Results",
)
@Composable
private fun MemoryMatchUiTabletPortraitResultsPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState =
                        MemoryMatchScreen.GameState.Finished(
                            moves = 16,
                            timeElapsed = 35,
                            isNewRecord = true,
                            accuracy = 87.5f,
                        ),
                    cards = emptyList(),
                    moves = 16,
                    timeElapsed = 35,
                    matchesFound = 8,
                    totalPairs = 8,
                    personalBestTime = 35,
                    userName = "Jordan",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,isRound=false,orientation=landscape",
    name = "Tablet Landscape - Results",
)
@Composable
private fun MemoryMatchUiTabletLandscapeResultsPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState =
                        MemoryMatchScreen.GameState.Finished(
                            moves = 24,
                            timeElapsed = 55,
                            isNewRecord = false,
                            accuracy = 66.7f,
                        ),
                    cards = emptyList(),
                    moves = 24,
                    timeElapsed = 55,
                    matchesFound = 8,
                    totalPairs = 8,
                    personalBestTime = 32,
                    userName = "Sam",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=673dp,height=841dp,dpi=373,isRound=false,orientation=portrait",
    name = "Foldable Portrait (Pixel Fold Unfolded) - Results",
)
@Composable
private fun MemoryMatchUiFoldablePortraitResultsPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState =
                        MemoryMatchScreen.GameState.Finished(
                            moves = 18,
                            timeElapsed = 38,
                            isNewRecord = true,
                            accuracy = 83.3f,
                        ),
                    cards = emptyList(),
                    moves = 18,
                    timeElapsed = 38,
                    matchesFound = 8,
                    totalPairs = 8,
                    personalBestTime = 38,
                    userName = "Chris",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=841dp,height=673dp,dpi=373,isRound=false,orientation=landscape",
    name = "Foldable Landscape (Pixel Fold Unfolded) - Results",
)
@Composable
private fun MemoryMatchUiFoldableLandscapeResultsPreview() {
    KidsMathTutorAppTheme {
        MemoryMatchUi(
            state =
                MemoryMatchScreen.State(
                    gameState =
                        MemoryMatchScreen.GameState.Finished(
                            moves = 14,
                            timeElapsed = 30,
                            isNewRecord = true,
                            accuracy = 92.9f,
                        ),
                    cards = emptyList(),
                    moves = 14,
                    timeElapsed = 30,
                    matchesFound = 8,
                    totalPairs = 8,
                    personalBestTime = 30,
                    userName = "Taylor",
                    eventSink = {},
                ),
        )
    }
}
