package dev.hossain.mathtutor.ui.games

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GameStats
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.domain.repository.GameRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.ui.mathrace.MathRaceScreen
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
    ) : Presenter<GameSelectionScreen.State> {
        @CircuitInject(GameSelectionScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): GameSelectionPresenter
        }

        @Composable
        override fun present(): GameSelectionScreen.State {
            // Collect overall stats to get total problems solved for unlock logic
            val sessionStats by sessionRepository.getOverallStats().collectAsState(
                initial = SessionStats.EMPTY,
            )
            val totalProblemsSolved = sessionStats.totalProblems

            Timber.d(
                "GameSelection: Total problems solved = $totalProblemsSolved",
            )

            // Collect personal bests and game stats for each game
            val mathRacePersonalBest by gameRepository
                .getPersonalBest(Game.MATH_RACE)
                .collectAsState(initial = 0)
            val mathRaceStats by gameRepository
                .getGameStats(Game.MATH_RACE)
                .collectAsState(initial = GameStats.empty(Game.MATH_RACE))

            val memoryMatchPersonalBest by gameRepository
                .getPersonalBest(Game.MEMORY_MATCH)
                .collectAsState(initial = 0)
            val memoryMatchStats by gameRepository
                .getGameStats(Game.MEMORY_MATCH)
                .collectAsState(initial = GameStats.empty(Game.MEMORY_MATCH))

            val numberSequencePersonalBest by gameRepository
                .getPersonalBest(Game.NUMBER_SEQUENCE)
                .collectAsState(initial = 0)
            val numberSequenceStats by gameRepository
                .getGameStats(Game.NUMBER_SEQUENCE)
                .collectAsState(initial = GameStats.empty(Game.NUMBER_SEQUENCE))

            // Build game info list
            val gameInfoList =
                listOf(
                    GameSelectionScreen.GameInfo(
                        game = Game.MATH_RACE,
                        isUnlocked = Game.MATH_RACE.isUnlocked(totalProblemsSolved),
                        personalBest = mathRacePersonalBest,
                        totalPlays = mathRaceStats.totalGamesPlayed,
                    ),
                    GameSelectionScreen.GameInfo(
                        game = Game.MEMORY_MATCH,
                        isUnlocked = Game.MEMORY_MATCH.isUnlocked(totalProblemsSolved),
                        personalBest = memoryMatchPersonalBest,
                        totalPlays = memoryMatchStats.totalGamesPlayed,
                    ),
                    GameSelectionScreen.GameInfo(
                        game = Game.NUMBER_SEQUENCE,
                        isUnlocked = Game.NUMBER_SEQUENCE.isUnlocked(totalProblemsSolved),
                        personalBest = numberSequencePersonalBest,
                        totalPlays = numberSequenceStats.totalGamesPlayed,
                    ),
                )

            Timber.d(
                "GameSelection: Games - " +
                    "MathRace(unlocked=${gameInfoList[0].isUnlocked}, " +
                    "best=${gameInfoList[0].personalBest}, plays=${gameInfoList[0].totalPlays}), " +
                    "MemoryMatch(unlocked=${gameInfoList[1].isUnlocked}, " +
                    "best=${gameInfoList[1].personalBest}, plays=${gameInfoList[1].totalPlays}), " +
                    "NumberSequence(unlocked=${gameInfoList[2].isUnlocked}, " +
                    "best=${gameInfoList[2].personalBest}, plays=${gameInfoList[2].totalPlays})",
            )

            return GameSelectionScreen.State(
                gameInfoList = gameInfoList,
                totalProblemsSolved = totalProblemsSolved,
            ) { event ->
                when (event) {
                    is GameSelectionScreen.Event.PlayGame -> {
                        when (event.game) {
                            Game.MATH_RACE -> {
                                Timber.d("GameSelection: Navigating to MathRaceScreen")
                                navigator.goTo(MathRaceScreen)
                            }

                            Game.MEMORY_MATCH -> {
                                // TODO: Navigate to MemoryMatchScreen when implemented
                                Timber.d("GameSelection: Memory Match not yet implemented")
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
