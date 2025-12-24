package dev.hossain.mathtutor.ui.games

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GameStats
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.domain.repository.GameRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.ui.mathrace.MathRaceScreen
import dev.hossain.mathtutor.ui.memorymatch.MemoryMatchScreen
import dev.hossain.mathtutor.ui.numbersequence.NumberSequenceScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Presenter for [GameSelectionScreen].
 *
 * Manages the state and business logic for the game selection hub.
 * Collects data from GameRepository and SessionRepository to determine
 * game unlock status and display statistics.
 */
@AssistedInject
class GameSelectionPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val gameRepository: GameRepository,
        private val sessionRepository: SessionRepository,
        private val analyticsService: AnalyticsService,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : Presenter<GameSelectionScreen.State> {
        @CircuitInject(GameSelectionScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): GameSelectionPresenter
        }

        @Composable
        override fun present(): GameSelectionScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Game Selection",
                    screenClass = GameSelectionScreen::class.java.name,
                )
            }

            // Use produceRetainedState to batch all game data collection
            // This reduces overhead by:
            // 1. Combining 7 separate flow collections into a single producer (sessionStats + 3 games × 2 flows)
            // 2. Building gameInfoList only once per data update
            // 3. Retaining state across configuration changes without reprocessing
            data class GameSelectionData(
                val gameInfoList: List<GameSelectionScreen.GameInfo>,
                val totalProblemsSolved: Int,
            )

            val gameData by produceRetainedState(
                initialValue =
                    GameSelectionData(
                        gameInfoList = emptyList(),
                        totalProblemsSolved = 0,
                    ),
            ) {
                // Combine all flows including trial attempts
                // First combine session stats with Math Race data
                combine(
                    sessionRepository.getOverallStats(),
                    gameRepository.getPersonalBest(Game.MATH_RACE),
                    gameRepository.getGameStats(Game.MATH_RACE),
                    gameRepository.getPersonalBest(Game.MEMORY_MATCH),
                    gameRepository.getGameStats(Game.MEMORY_MATCH),
                ) { sessionStats, mathRaceBest, mathRaceStats, memoryMatchBest, memoryMatchStats ->
                    Pair(
                        sessionStats,
                        Triple(
                            Pair(mathRaceBest, mathRaceStats),
                            Pair(memoryMatchBest, memoryMatchStats),
                            null, // Placeholder for number sequence data
                        ),
                    )
                }.combine(
                    combine(
                        gameRepository.getPersonalBest(Game.NUMBER_SEQUENCE),
                        gameRepository.getGameStats(Game.NUMBER_SEQUENCE),
                    ) { best, stats -> Pair(best, stats) },
                ) { (sessionStats, gameData), numberSequenceData ->
                    Pair(
                        sessionStats,
                        Triple(gameData.first, gameData.second, numberSequenceData),
                    )
                }.combine(
                    combine(
                        userPreferencesRepository.getGameTrialAttempts(Game.MATH_RACE),
                        userPreferencesRepository.getGameTrialAttempts(Game.MEMORY_MATCH),
                        userPreferencesRepository.getGameTrialAttempts(Game.NUMBER_SEQUENCE),
                    ) { mathRaceTrial, memoryMatchTrial, numberSequenceTrial ->
                        Triple(mathRaceTrial, memoryMatchTrial, numberSequenceTrial)
                    },
                ) { (sessionStats, gameData), trialAttempts ->
                    val totalProblems = sessionStats.totalProblems
                    val (mathRaceData, memoryMatchData, numberSequenceData) = gameData
                    val (mathRaceBest, mathRaceStats) = mathRaceData
                    val (memoryMatchBest, memoryMatchStats) = memoryMatchData
                    val (numberSequenceBest, numberSequenceStats) = numberSequenceData
                    val (mathRaceTrial, memoryMatchTrial, numberSequenceTrial) = trialAttempts

                    // Build game info list
                    val games =
                        listOf(
                            GameSelectionScreen.GameInfo(
                                game = Game.MATH_RACE,
                                isUnlocked = Game.MATH_RACE.isUnlocked(totalProblems),
                                personalBest = mathRaceBest,
                                totalPlays = mathRaceStats.totalGamesPlayed,
                                trialAttempts = mathRaceTrial,
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.MEMORY_MATCH,
                                isUnlocked = Game.MEMORY_MATCH.isUnlocked(totalProblems),
                                personalBest = memoryMatchBest,
                                totalPlays = memoryMatchStats.totalGamesPlayed,
                                trialAttempts = memoryMatchTrial,
                            ),
                            GameSelectionScreen.GameInfo(
                                game = Game.NUMBER_SEQUENCE,
                                isUnlocked = Game.NUMBER_SEQUENCE.isUnlocked(totalProblems),
                                personalBest = numberSequenceBest,
                                totalPlays = numberSequenceStats.totalGamesPlayed,
                                trialAttempts = numberSequenceTrial,
                            ),
                        )

                    Timber.d(
                        "GameSelectionPresenter: Loaded data - totalProblems=$totalProblems, " +
                            "MathRace(unlocked=${games[0].isUnlocked}, " +
                            "best=${games[0].personalBest}, plays=${games[0].totalPlays}, trials=${games[0].trialAttempts}), " +
                            "MemoryMatch(unlocked=${games[1].isUnlocked}, " +
                            "best=${games[1].personalBest}, plays=${games[1].totalPlays}, trials=${games[1].trialAttempts}), " +
                            "NumberSequence(unlocked=${games[2].isUnlocked}, " +
                            "best=${games[2].personalBest}, plays=${games[2].totalPlays}, trials=${games[2].trialAttempts})",
                    )

                    GameSelectionData(
                        gameInfoList = games,
                        totalProblemsSolved = totalProblems,
                    )
                }.collect { data ->
                    value = data
                }
            }

            val coroutineScope = rememberCoroutineScope()

            return GameSelectionScreen.State(
                gameInfoList = gameData.gameInfoList,
                totalProblemsSolved = gameData.totalProblemsSolved,
            ) { event ->
                when (event) {
                    is GameSelectionScreen.Event.PlayGame -> {
                        // If it's a trial play, increment the trial counter
                        if (event.isTrial) {
                            coroutineScope.launch {
                                val newCount = userPreferencesRepository.incrementGameTrialAttempts(event.game)
                                Timber.d("GameSelection: Trial play for ${event.game.name}, new count: $newCount")
                            }
                        }

                        when (event.game) {
                            Game.MATH_RACE -> {
                                Timber.d("GameSelection: Navigating to MathRaceScreen")
                                navigator.goTo(MathRaceScreen)
                            }

                            Game.MEMORY_MATCH -> {
                                Timber.d("GameSelection: Navigating to MemoryMatchScreen")
                                navigator.goTo(MemoryMatchScreen)
                            }

                            Game.NUMBER_SEQUENCE -> {
                                Timber.d("GameSelection: Navigating to NumberSequenceScreen")
                                navigator.goTo(NumberSequenceScreen)
                            }
                        }
                    }

                    is GameSelectionScreen.Event.NavigateBack -> {
                        Timber.d("GameSelection: Navigating back")
                        navigator.pop()
                    }
                }
            }
        }
    }
