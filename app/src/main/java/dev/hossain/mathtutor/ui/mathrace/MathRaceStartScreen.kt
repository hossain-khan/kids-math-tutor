package dev.hossain.mathtutor.ui.mathrace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MAX_CONTENT_WIDTH_SMALL

/**
 * Start screen for Math Race game.
 *
 * Shows game title, description, personal best (if any), and a start button.
 *
 * @param personalBest Player's current high score (0 if none)
 * @param userName Player's name for personalized greeting
 * @param onStartGame Callback when start button is pressed
 * @param onNavigateHome Callback to return to home/game selection
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathRaceStartScreen(
    personalBest: Int,
    userName: String?,
    onStartGame: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Math Race") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateHome,
                        modifier =
                            Modifier.semantics {
                                contentDescription = "Go back to home"
                                role = Role.Button
                            },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                    // Game title with emoji
                    Text(
                        text = "Math Race ⏱️",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier.semantics {
                                heading()
                                contentDescription = "Math Race"
                            },
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Game description
                    Text(
                        text = "Solve as many problems as you can in 60 seconds!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Personal best display (only show if > 0)
                    if (personalBest > 0) {
                        PersonalBestDisplay(
                            personalBest = personalBest,
                            modifier = Modifier.padding(16.dp),
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Start button
                    Button(
                        onClick = onStartGame,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .semantics {
                                    contentDescription = "Start game"
                                    role = Role.Button
                                },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    ) {
                        Text(
                            text = "START GAME",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

/**
 * Displays the player's personal best score with a trophy icon.
 */
@Composable
private fun PersonalBestDisplay(
    personalBest: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your Best: $personalBest",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.tertiary,
            modifier =
                Modifier.semantics {
                    contentDescription = "Your personal best is $personalBest"
                },
        )
    }
}

@Preview(showBackground = true, name = "Compact Phone", widthDp = 411, heightDp = 891)
@Composable
private fun MathRaceStartScreenPreview() {
    KidsMathTutorAppTheme {
        MathRaceStartScreen(
            personalBest = 18,
            userName = "Alex",
            onStartGame = {},
            onNavigateHome = {},
        )
    }
}

@Preview(showBackground = true, name = "Medium Tablet", widthDp = 700, heightDp = 500)
@Composable
private fun MathRaceStartScreenMediumPreview() {
    KidsMathTutorAppTheme {
        MathRaceStartScreen(
            personalBest = 18,
            userName = "Alex",
            onStartGame = {},
            onNavigateHome = {},
        )
    }
}

@Preview(showBackground = true, name = "Expanded Tablet", widthDp = 1100, heightDp = 600)
@Composable
private fun MathRaceStartScreenExpandedPreview() {
    KidsMathTutorAppTheme {
        MathRaceStartScreen(
            personalBest = 18,
            userName = "Alex",
            onStartGame = {},
            onNavigateHome = {},
        )
    }
}

@Preview(showBackground = true, name = "No Personal Best")
@Composable
private fun MathRaceStartScreenNoBestPreview() {
    KidsMathTutorAppTheme {
        MathRaceStartScreen(
            personalBest = 0,
            userName = null,
            onStartGame = {},
            onNavigateHome = {},
        )
    }
}

@Preview(showBackground = true, name = "Dark Theme")
@Composable
private fun MathRaceStartScreenDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        MathRaceStartScreen(
            personalBest = 18,
            userName = "Alex",
            onStartGame = {},
            onNavigateHome = {},
        )
    }
}
