package dev.hossain.mathtutor.ui.games

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GameStats
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.domain.repository.GameRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.ui.mathrace.MathRaceScreen
import dev.hossain.mathtutor.ui.memorymatch.MemoryMatchScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
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
                // Collect overall stats for unlock logic
                sessionRepository.getOverallStats().collect { sessionStats ->
                    val totalProblems = sessionStats.totalProblems

                    // Collect Math Race data
                    gameRepository.getPersonalBest(Game.MATH_RACE).collect { mathRaceBest ->
                        gameRepository.getGameStats(Game.MATH_RACE).collect { mathRaceStats ->
                            // Collect Memory Match data
                            gameRepository.getPersonalBest(Game.MEMORY_MATCH).collect { memoryMatchBest ->
                                gameRepository.getGameStats(Game.MEMORY_MATCH).collect { memoryMatchStats ->
                                    // Collect Number Sequence data
                                    gameRepository.getPersonalBest(Game.NUMBER_SEQUENCE).collect { numberSequenceBest ->
                                        gameRepository.getGameStats(Game.NUMBER_SEQUENCE).collect { numberSequenceStats ->
                                            // Build game info list
                                            val games =
                                                listOf(
                                                    GameSelectionScreen.GameInfo(
                                                        game = Game.MATH_RACE,
                                                        isUnlocked = Game.MATH_RACE.isUnlocked(totalProblems),
                                                        personalBest = mathRaceBest,
                                                        totalPlays = mathRaceStats.totalGamesPlayed,
                                                    ),
                                                    GameSelectionScreen.GameInfo(
                                                        game = Game.MEMORY_MATCH,
                                                        isUnlocked = Game.MEMORY_MATCH.isUnlocked(totalProblems),
                                                        personalBest = memoryMatchBest,
                                                        totalPlays = memoryMatchStats.totalGamesPlayed,
                                                    ),
                                                    GameSelectionScreen.GameInfo(
                                                        game = Game.NUMBER_SEQUENCE,
                                                        isUnlocked = Game.NUMBER_SEQUENCE.isUnlocked(totalProblems),
                                                        personalBest = numberSequenceBest,
                                                        totalPlays = numberSequenceStats.totalGamesPlayed,
                                                    ),
                                                )

                                            Timber.d(
                                                "GameSelectionPresenter: Loaded data - totalProblems=$totalProblems, " +
                                                    "MathRace(unlocked=${games[0].isUnlocked}, " +
                                                    "best=${games[0].personalBest}, plays=${games[0].totalPlays}), " +
                                                    "MemoryMatch(unlocked=${games[1].isUnlocked}, " +
                                                    "best=${games[1].personalBest}, plays=${games[1].totalPlays}), " +
                                                    "NumberSequence(unlocked=${games[2].isUnlocked}, " +
                                                    "best=${games[2].personalBest}, plays=${games[2].totalPlays})",
                                            )

                                            value =
                                                GameSelectionData(
                                                    gameInfoList = games,
                                                    totalProblemsSolved = totalProblems,
                                                )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return GameSelectionScreen.State(
                gameInfoList = gameData.gameInfoList,
                totalProblemsSolved = gameData.totalProblemsSolved,
            ) { event ->
                when (event) {
                    is GameSelectionScreen.Event.PlayGame -> {
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
                                // TODO: Navigate to NumberSequenceScreen when implemented
                                Timber.d("GameSelection: Number Sequence not yet implemented")
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
