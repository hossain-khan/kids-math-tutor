package dev.hossain.mathtutor.ui.numbersequence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme

/**
 * Start screen for Number Sequence game.
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
fun NumberSequenceStartScreen(
    personalBest: Int,
    userName: String?,
    onStartGame: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Number Sequence") },
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
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Game title with emoji
            Text(
                text = "Number Sequence 🎲",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier.semantics {
                        heading()
                        contentDescription = "Number Sequence"
                    },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Game description
            Text(
                text = "Find the missing number in the sequence!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary description
            Text(
                text = "You have 90 seconds to solve as many patterns as you can.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Preview(showBackground = true)
@Composable
private fun NumberSequenceStartScreenPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceStartScreen(
            personalBest = 12,
            userName = "Alex",
            onStartGame = {},
            onNavigateHome = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NumberSequenceStartScreenNoBestPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceStartScreen(
            personalBest = 0,
            userName = "Alex",
            onStartGame = {},
            onNavigateHome = {},
        )
    }
}
