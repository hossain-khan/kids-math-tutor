package dev.hossain.mathtutor.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.audio.AudioService
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.domain.repository.BadgeRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.domain.repository.StreakRepository
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.ui.badges.BadgesScreen
import dev.hossain.mathtutor.ui.games.GameSelectionScreen
import dev.hossain.mathtutor.ui.operationselector.OperationSelectorScreen
import dev.hossain.mathtutor.ui.settings.SettingsScreen
import dev.hossain.mathtutor.ui.stats.StatsScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import timber.log.Timber

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
        private val userProfileRepository: UserProfileRepository,
        private val audioService: AudioService,
    ) : Presenter<HomeScreen.State> {
        @CircuitInject(HomeScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): HomePresenter
        }

        @Composable
        override fun present(): HomeScreen.State {
            // Track music playing state
            var isMusicPlaying by remember { mutableStateOf(false) }

            // Collect user profile
            val userProfile by userProfileRepository.getProfile().collectAsState(initial = null)
            Timber.d(
                "HomeScreen: User profile - name=${userProfile?.name}, grade=${userProfile?.gradeLevel}",
            )

            // Collect streak data
            val streakData by streakRepository.getStreak().collectAsState(initial = null)
            Timber.d(
                "HomeScreen: Streak data - currentStreak=${streakData?.currentStreak}, lastPracticeDate=${streakData?.lastPracticeDate}",
            )

            // Collect overall stats
            val overallStats by sessionRepository.getOverallStats().collectAsState(
                initial = SessionStats.EMPTY,
            )
            Timber.d(
                "HomeScreen: Overall stats - sessionCount=${overallStats.sessionCount}, totalProblems=${overallStats.totalProblems}, accuracy=${overallStats.accuracy}",
            )

            // Collect 3 most recently unlocked badges
            val recentBadges by badgeRepository.getRecentlyUnlockedBadges(limit = 3).collectAsState(
                initial = emptyList(),
            )
            Timber.d("HomeScreen: Recent badges count=${recentBadges.size}")

            return HomeScreen.State(
                userName = userProfile?.name,
                gradeLevel = userProfile?.gradeLevel,
                streakData = streakData,
                overallStats = overallStats,
                recentBadges = recentBadges,
                isMusicPlaying = isMusicPlaying,
            ) { event ->
                when (event) {
                    is HomeScreen.Event.StartPracticeClicked -> {
                        Timber.d("HomeScreen: Navigating to OperationSelectorScreen")
                        navigator.goTo(OperationSelectorScreen)
                    }

                    is HomeScreen.Event.ViewStatsClicked -> {
                        Timber.d("HomeScreen: Navigating to StatsScreen")
                        navigator.goTo(StatsScreen)
                    }

                    is HomeScreen.Event.ViewBadgesClicked -> {
                        Timber.d("HomeScreen: Navigating to BadgesScreen")
                        navigator.goTo(BadgesScreen)
                    }

                    is HomeScreen.Event.ViewSettingsClicked -> {
                        Timber.d("HomeScreen: Navigating to SettingsScreen")
                        navigator.goTo(SettingsScreen)
                    }

                    is HomeScreen.Event.ToggleMusicClicked -> {
                        isMusicPlaying = !isMusicPlaying
                        if (isMusicPlaying) {
                            audioService.setMusicEnabled(true)
                            audioService.startBackgroundMusic()
                            Timber.d("HomeScreen: Started background music")
                        } else {
                            audioService.stopBackgroundMusic()
                            audioService.setMusicEnabled(false)
                            Timber.d("HomeScreen: Stopped background music")
                        }
                    }

                    is HomeScreen.Event.ViewGamesClicked -> {
                        Timber.d("HomeScreen: Navigating to GameSelectionScreen")
                        navigator.goTo(GameSelectionScreen)
                    }
                }
            }
        }
    }
