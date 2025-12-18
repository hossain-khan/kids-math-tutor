package dev.hossain.mathtutor.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.domain.repository.BadgeRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.domain.repository.StreakRepository
import dev.hossain.mathtutor.ui.badges.BadgesScreen
import dev.hossain.mathtutor.ui.operationselector.OperationSelectorScreen
import dev.hossain.mathtutor.ui.stats.StatsScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

/**
 * Presenter for [HomeScreen].
 *
 * Manages the state and business logic for the home dashboard.
 * Collects data from multiple repositories (streak, session, badge) and handles navigation.
 */
@AssistedInject
class HomePresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val streakRepository: StreakRepository,
        private val sessionRepository: SessionRepository,
        private val badgeRepository: BadgeRepository,
    ) : Presenter<HomeScreen.State> {
        @CircuitInject(HomeScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): HomePresenter
        }

        @Composable
        override fun present(): HomeScreen.State {
            // Collect streak data
            val streakData by streakRepository.getStreak().collectAsState(initial = null)

            // Collect overall stats
            val overallStats by sessionRepository.getOverallStats().collectAsState(
                initial = SessionStats.EMPTY,
            )

            // Collect 3 most recently unlocked badges
            val recentBadges by badgeRepository.getRecentlyUnlockedBadges(limit = 3).collectAsState(
                initial = emptyList(),
            )

            return HomeScreen.State(
                userName = null, // TODO: Add user name support in future
                streakData = streakData,
                overallStats = overallStats,
                recentBadges = recentBadges,
            ) { event ->
                when (event) {
                    is HomeScreen.Event.StartPracticeClicked -> {
                        navigator.goTo(OperationSelectorScreen)
                    }

                    is HomeScreen.Event.ViewStatsClicked -> {
                        navigator.goTo(StatsScreen)
                    }

                    is HomeScreen.Event.ViewBadgesClicked -> {
                        navigator.goTo(BadgesScreen)
                    }
                }
            }
        }
    }
