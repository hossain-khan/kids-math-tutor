package dev.hossain.mathtutor.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import java.time.LocalDate

/**
 * A card component displaying the user's current streak information.
 *
 * Shows:
 * - Fire emoji 🔥 for active streak
 * - Large streak count display
 * - Weekly calendar with checkmarks for practice days
 * - Encouraging message based on streak status
 * - Urgent message if streak is at risk
 *
 * @param streakData The current streak data, null if no practice history
 * @param userName Optional user name for personalized messages
 * @param today Current date for calculating streak status
 * @param modifier Optional modifier for the card
 */
@Composable
fun StreakCard(
    streakData: DailyStreak?,
    userName: String? = null,
    today: LocalDate = LocalDate.now(),
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
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header with fire emoji and title
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🔥",
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = " Streak",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            // Large streak count
            if (streakData != null && streakData.isStreakAlive(today)) {
                Text(
                    text = "${streakData.currentStreak}",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (streakData.currentStreak == 1) "day" else "days",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.alpha(0.6f),
                )
                Text(
                    text = "days",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.alpha(0.6f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weekly calendar with checkmarks
            WeeklyCalendar(
                streakData = streakData,
                today = today,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Encouraging message
            val namePrefix = if (userName != null) "$userName, " else ""
            val message =
                when {
                    streakData == null || !streakData.isStreakAlive(today) -> {
                        if (userName != null) {
                            "$userName, start your streak today! 🎯"
                        } else {
                            "Start your streak today! 🎯"
                        }
                    }

                    streakData.currentStreak == 1 -> {
                        "Great start${if (userName != null) ", $userName" else ""}! Come back tomorrow! 🌟"
                    }

                    streakData.lastPracticeDate == today -> {
                        "Amazing${if (userName != null) ", $userName" else ""}! You practiced today! 🎉"
                    }

                    streakData.lastPracticeDate == today.minusDays(1) -> {
                        "⚠️ ${namePrefix}practice today to keep your streak alive!"
                    }

                    else -> {
                        "Keep it up${if (userName != null) ", $userName" else ""}! You're doing great! 💪"
                    }
                }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Displays a weekly calendar view with checkmarks for days with practice.
 *
 * Shows the last 7 days with checkmarks indicating which days the user practiced.
 */
@Composable
private fun WeeklyCalendar(
    streakData: DailyStreak?,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        // Show last 7 days (including today)
        for (daysAgo in 6 downTo 0) {
            val date = today.minusDays(daysAgo.toLong())
            val dayOfWeek = date.dayOfWeek.name.take(1) // First letter (M, T, W, etc.)

            // Check if this day was practiced
            val isPracticed =
                if (streakData?.lastPracticeDate == null || streakData.currentStreak <= 0) {
                    false
                } else {
                    val lastPracticeDate = streakData.lastPracticeDate
                    val streakLength = streakData.currentStreak.toLong()
                    val streakStartDate = lastPracticeDate.minusDays(streakLength - 1)

                    // Mark as practiced if the date falls within the consecutive streak period
                    !date.isBefore(streakStartDate) && !date.isAfter(lastPracticeDate)
                }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = dayOfWeek,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .background(
                                color =
                                    if (isPracticed) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                shape = CircleShape,
                            ).border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isPracticed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Practiced",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StreakCardWithActiveStreakPreview() {
    KidsMathTutorAppTheme {
        val today = LocalDate.now()
        StreakCard(
            streakData =
                DailyStreak(
                    currentStreak = 5,
                    longestStreak = 7,
                    lastPracticeDate = today,
                    totalDaysPracticed = 10,
                ),
            today = today,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StreakCardAtRiskPreview() {
    KidsMathTutorAppTheme {
        val today = LocalDate.now()
        StreakCard(
            streakData =
                DailyStreak(
                    currentStreak = 3,
                    longestStreak = 5,
                    lastPracticeDate = today.minusDays(1),
                    totalDaysPracticed = 8,
                ),
            today = today,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StreakCardNoStreakPreview() {
    KidsMathTutorAppTheme {
        StreakCard(
            streakData = null,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StreakCardDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        val today = LocalDate.now()
        StreakCard(
            streakData =
                DailyStreak(
                    currentStreak = 7,
                    longestStreak = 10,
                    lastPracticeDate = today,
                    totalDaysPracticed = 15,
                ),
            today = today,
            modifier = Modifier.padding(16.dp),
        )
    }
}
