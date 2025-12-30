package dev.hossain.mathtutor.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsParam
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.analytics.UserProperty
import dev.hossain.mathtutor.audio.AudioService
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.domain.repository.BadgeRepository
import dev.hossain.mathtutor.domain.repository.GoalRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.domain.repository.StreakRepository
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.ui.badges.BadgesScreen
import dev.hossain.mathtutor.ui.games.GameSelectionScreen
import dev.hossain.mathtutor.ui.goals.progress.GoalProgressScreen
import dev.hossain.mathtutor.ui.operationselector.OperationSelectorScreen
import dev.hossain.mathtutor.ui.settings.SettingsScreen
import dev.hossain.mathtutor.ui.stats.StatsScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
        private val userPreferencesRepository: UserPreferencesRepository,
        private val goalRepository: GoalRepository,
        private val audioService: AudioService,
        private val analyticsService: AnalyticsService,
    ) : Presenter<HomeScreen.State> {
        @CircuitInject(HomeScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): HomePresenter
        }

        @Composable
        override fun present(): HomeScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Home",
                    screenClass = HomeScreen::class.java.name,
                )
            }

            // Update aggregate user properties on home screen load
            // Sequential .first() calls are fine here - this only runs once per presenter instance
            LaunchedEffect(Unit) {
                try {
                    // Collect all analytics data sequentially
                    val stats = sessionRepository.getOverallStats().first()
                    val streak = streakRepository.getStreak().first()
                    val badges = badgeRepository.getUnlockedBadges().first()

                    // Update user properties
                    analyticsService.setUserProperty(
                        UserProperty.TOTAL_PROBLEMS_SOLVED,
                        stats.totalProblems.toString(),
                    )
                    streak?.let {
                        analyticsService.setUserProperty(
                            UserProperty.CURRENT_STREAK,
                            it.currentStreak.toString(),
                        )
                    }
                    analyticsService.setUserProperty(
                        UserProperty.TOTAL_BADGES_UNLOCKED,
                        badges.size.toString(),
                    )

                    Timber.d("Analytics: Aggregate user properties updated")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to update analytics user properties")
                }
            }

            val scope = rememberCoroutineScope()

            // Collect background music state from UserPreferences
            val isMusicPlaying by userPreferencesRepository.isBackgroundMusicEnabled.collectAsState(initial = false)

            // Collect user profile
            val userProfile by userProfileRepository.getProfile().collectAsState(initial = null)

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

            // Collect active goal if one exists
            val activeGoal by goalRepository.getActiveGoal().collectAsState(initial = null)

            // Track whether session resumption dialog has been shown in current session
            var hasShownSessionResumptionDialog by remember { mutableStateOf(false) }

            // Show resumption dialog on first appearance if goal is active and hasn't been shown yet
            val showSessionResumptionDialog = activeGoal != null && !hasShownSessionResumptionDialog

            LaunchedEffect(activeGoal?.goal?.id) {
                val currentGoal = activeGoal
                if (currentGoal != null && !hasShownSessionResumptionDialog) {
                    hasShownSessionResumptionDialog = true
                    Timber.d("HomeScreen: Showing session resumption dialog for goal: ${currentGoal.goal.title}")
                }
            }

            // Log state changes in LaunchedEffect to avoid recomposition spam
            LaunchedEffect(userProfile?.name, userProfile?.gradeLevel) {
                Timber.d(
                    "HomeScreen: User profile - name=${userProfile?.name}, grade=${userProfile?.gradeLevel}",
                )
            }

            LaunchedEffect(streakData?.currentStreak, streakData?.lastPracticeDate) {
                Timber.d(
                    "HomeScreen: Streak data - currentStreak=${streakData?.currentStreak}, lastPracticeDate=${streakData?.lastPracticeDate}",
                )
            }

            LaunchedEffect(overallStats.sessionCount, overallStats.totalProblems) {
                Timber.d(
                    "HomeScreen: Overall stats - sessionCount=${overallStats.sessionCount}, totalProblems=${overallStats.totalProblems}, accuracy=${overallStats.accuracy}",
                )
            }

            LaunchedEffect(recentBadges.size) {
                Timber.d("HomeScreen: Recent badges count=${recentBadges.size}")
            }

            return HomeScreen.State(
                userName = userProfile?.name,
                gradeLevel = userProfile?.gradeLevel,
                streakData = streakData,
                overallStats = overallStats,
                recentBadges = recentBadges,
                activeGoal = activeGoal,
                isMusicPlaying = isMusicPlaying,
                showSessionResumptionDialog = showSessionResumptionDialog,
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
                        val newMusicState = !isMusicPlaying
                        analyticsService.logEvent(
                            eventName = AnalyticsEvent.AUDIO_TOGGLED,
                            parameters =
                                mapOf(
                                    AnalyticsParam.SETTING_NAME to "background_music",
                                    AnalyticsParam.SETTING_VALUE to newMusicState.toString(),
                                ),
                        )
                        audioService.setMusicEnabled(newMusicState)
                        if (newMusicState) {
                            audioService.startBackgroundMusic()
                            Timber.d("HomeScreen: Started background music")
                        } else {
                            audioService.stopBackgroundMusic()
                            Timber.d("HomeScreen: Stopped background music")
                        }
                        // Persist the state to UserPreferences
                        scope.launch {
                            userPreferencesRepository.setBackgroundMusicEnabled(newMusicState)
                        }
                    }

                    is HomeScreen.Event.ViewGamesClicked -> {
                        Timber.d("HomeScreen: Navigating to GameSelectionScreen")
                        navigator.goTo(GameSelectionScreen)
                    }

                    is HomeScreen.Event.ViewGoalProgressClicked -> {
                        Timber.d("HomeScreen: Navigating to GoalProgressScreen")
                        navigator.goTo(GoalProgressScreen)
                    }

                    is HomeScreen.Event.SessionResumptionDismissed -> {
                        Timber.d("HomeScreen: Session resumption dialog dismissed")
                        // Dialog is already dismissed, no further action needed
                    }

                    is HomeScreen.Event.ContinueGoalClicked -> {
                        Timber.d("HomeScreen: User continuing with active goal from resumption dialog")
                        navigator.goTo(GoalProgressScreen)
                    }
                }
            }
        }
    }
