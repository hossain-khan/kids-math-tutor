package dev.hossain.mathtutor.ui.badges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.ui.component.BadgeDetailDialog
import dev.hossain.mathtutor.ui.component.BadgeGrid
import dev.zacsweers.metro.AppScope

/**
 * UI for [BadgesScreen].
 *
 * Displays all badges organized by category with Material 3 design.
 * Shows progress summary, badge grid by category, and badge detail dialog.
 */
@CircuitInject(BadgesScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesUi(
    state: BadgesScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Your Badges")
                },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(BadgesScreen.Event.BackPressed) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            // Progress Summary
            item {
                ProgressSummarySection(
                    unlockedCount = state.progressSummary.unlockedCount,
                    totalCount = state.progressSummary.totalCount,
                )
            }

            // Badge Categories
            BadgeCategory.entries.forEach { category ->
                val badges = state.badgesByCategory[category] ?: emptyList()
                if (badges.isNotEmpty()) {
                    item(key = category) {
                        BadgeCategorySection(
                            category = category,
                            badges = badges,
                            onBadgeClick = { badge ->
                                state.eventSink(BadgesScreen.Event.BadgeClicked(badge))
                            },
                        )
                    }
                }
            }
        }

        // Badge Detail Dialog
        state.selectedBadge?.let { badge ->
            BadgeDetailDialog(
                badge = badge,
                onDismiss = { state.eventSink(BadgesScreen.Event.CloseDialog) },
            )
        }
    }
}

/**
 * Progress summary section showing unlocked badge count.
 */
@Composable
private fun ProgressSummarySection(
    unlockedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text(
            text = "🏆 $unlockedCount of $totalCount Badges Unlocked",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Badge category section with header and badge grid.
 */
@Composable
private fun BadgeCategorySection(
    category: BadgeCategory,
    badges: List<dev.hossain.mathtutor.domain.model.Badge>,
    onBadgeClick: (dev.hossain.mathtutor.domain.model.Badge) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Category Header
        Text(
            text = formatCategoryName(category),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Badge Grid
        BadgeGrid(
            badges = badges,
            onBadgeClick = onBadgeClick,
        )
    }
}

/**
 * Formats badge category enum to display name.
 */
private fun formatCategoryName(category: BadgeCategory): String =
    when (category) {
        BadgeCategory.GETTING_STARTED -> "Getting Started"
        BadgeCategory.VOLUME -> "Volume"
        BadgeCategory.OPERATION_MASTERY -> "Operation Mastery"
        BadgeCategory.SPEED_ACCURACY -> "Speed & Accuracy"
        BadgeCategory.STREAK -> "Streak"
    }
