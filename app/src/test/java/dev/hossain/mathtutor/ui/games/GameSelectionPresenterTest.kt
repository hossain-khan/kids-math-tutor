package dev.hossain.mathtutor.ui.games

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.Game
import org.junit.Test

/**
 * Unit tests for game selection unlock logic and Game enum functionality.
 *
 * Tests unlock logic based on total problems solved.
 */
class GameSelectionPresenterTest {
    // ==================== Unlock Logic Tests ====================

    @Test
    fun `Math Race unlocks at 50 problems solved`() {
        // Math Race requires 50 problems
        val game = Game.MATH_RACE
        assertThat(game.unlockRequirement).isEqualTo(50)

        // Under threshold - not unlocked
        assertThat(game.isUnlocked(totalProblemsSolved = 0)).isFalse()
        assertThat(game.isUnlocked(totalProblemsSolved = 49)).isFalse()

        // At or above threshold - unlocked
        assertThat(game.isUnlocked(totalProblemsSolved = 50)).isTrue()
        assertThat(game.isUnlocked(totalProblemsSolved = 100)).isTrue()
    }

    @Test
    fun `Memory Match unlocks at 100 problems solved`() {
        // Memory Match requires 100 problems
        val game = Game.MEMORY_MATCH
        assertThat(game.unlockRequirement).isEqualTo(100)

        // Under threshold - not unlocked
        assertThat(game.isUnlocked(totalProblemsSolved = 0)).isFalse()
        assertThat(game.isUnlocked(totalProblemsSolved = 99)).isFalse()

        // At or above threshold - unlocked
        assertThat(game.isUnlocked(totalProblemsSolved = 100)).isTrue()
        assertThat(game.isUnlocked(totalProblemsSolved = 150)).isTrue()
    }

    @Test
    fun `Number Sequence unlocks at 200 problems solved`() {
        // Number Sequence requires 200 problems
        val game = Game.NUMBER_SEQUENCE
        assertThat(game.unlockRequirement).isEqualTo(200)

        // Under threshold - not unlocked
        assertThat(game.isUnlocked(totalProblemsSolved = 0)).isFalse()
        assertThat(game.isUnlocked(totalProblemsSolved = 199)).isFalse()

        // At or above threshold - unlocked
        assertThat(game.isUnlocked(totalProblemsSolved = 200)).isTrue()
        assertThat(game.isUnlocked(totalProblemsSolved = 500)).isTrue()
    }

    // ==================== Progress Calculation Tests ====================

    @Test
    fun `unlock progress calculated correctly`() {
        val game = Game.MATH_RACE // 50 problems required

        assertThat(game.unlockProgress(0)).isWithin(0.01f).of(0f)
        assertThat(game.unlockProgress(25)).isWithin(0.01f).of(0.5f)
        assertThat(game.unlockProgress(50)).isWithin(0.01f).of(1f)
        assertThat(game.unlockProgress(100)).isWithin(0.01f).of(1f) // Capped at 1.0
    }

    @Test
    fun `problems until unlock calculated correctly`() {
        val game = Game.MATH_RACE // 50 problems required

        assertThat(game.problemsUntilUnlock(0)).isEqualTo(50)
        assertThat(game.problemsUntilUnlock(25)).isEqualTo(25)
        assertThat(game.problemsUntilUnlock(50)).isEqualTo(0)
        assertThat(game.problemsUntilUnlock(100)).isEqualTo(0) // Already unlocked
    }

    @Test
    fun `all games are present in game list`() {
        // All Game enum values should be in the list
        val allGames = Game.entries

        assertThat(allGames.size).isEqualTo(3)
        assertThat(allGames.contains(Game.MATH_RACE)).isTrue()
        assertThat(allGames.contains(Game.MEMORY_MATCH)).isTrue()
        assertThat(allGames.contains(Game.NUMBER_SEQUENCE)).isTrue()
    }

    // ==================== Game Properties Tests ====================

    @Test
    fun `game display names are correct`() {
        assertThat(Game.MATH_RACE.displayName).isEqualTo("Math Race")
        assertThat(Game.MEMORY_MATCH.displayName).isEqualTo("Memory Match")
        assertThat(Game.NUMBER_SEQUENCE.displayName).isEqualTo("Number Sequence")
    }

    @Test
    fun `game icons are defined`() {
        assertThat(Game.MATH_RACE.icon.isNotEmpty()).isTrue()
        assertThat(Game.MEMORY_MATCH.icon.isNotEmpty()).isTrue()
        assertThat(Game.NUMBER_SEQUENCE.icon.isNotEmpty()).isTrue()
    }

    @Test
    fun `game descriptions are defined`() {
        assertThat(Game.MATH_RACE.description.isNotEmpty()).isTrue()
        assertThat(Game.MEMORY_MATCH.description.isNotEmpty()).isTrue()
        assertThat(Game.NUMBER_SEQUENCE.description.isNotEmpty()).isTrue()
    }

    // ==================== State Tests ====================

    @Test
    fun `state shows correct unlock status for each game at 75 problems`() {
        val totalProblems = 75

        // At 75 problems:
        // - Math Race (50) = unlocked
        // - Memory Match (100) = locked
        // - Number Sequence (200) = locked
        assertThat(Game.MATH_RACE.isUnlocked(totalProblems)).isTrue()
        assertThat(Game.MEMORY_MATCH.isUnlocked(totalProblems)).isFalse()
        assertThat(Game.NUMBER_SEQUENCE.isUnlocked(totalProblems)).isFalse()
    }

    @Test
    fun `state shows all games unlocked at 200 problems`() {
        val totalProblems = 200

        // At 200 problems, all games should be unlocked
        assertThat(Game.MATH_RACE.isUnlocked(totalProblems)).isTrue()
        assertThat(Game.MEMORY_MATCH.isUnlocked(totalProblems)).isTrue()
        assertThat(Game.NUMBER_SEQUENCE.isUnlocked(totalProblems)).isTrue()
    }

    @Test
    fun `no games unlocked at zero problems`() {
        val totalProblems = 0

        // At 0 problems, no games should be unlocked
        assertThat(Game.MATH_RACE.isUnlocked(totalProblems)).isFalse()
        assertThat(Game.MEMORY_MATCH.isUnlocked(totalProblems)).isFalse()
        assertThat(Game.NUMBER_SEQUENCE.isUnlocked(totalProblems)).isFalse()
    }

    @Test
    fun `unlock requirements are in ascending order`() {
        // Verify games unlock in order of difficulty/progression
        assertThat(Game.MATH_RACE.unlockRequirement < Game.MEMORY_MATCH.unlockRequirement).isTrue()
        assertThat(Game.MEMORY_MATCH.unlockRequirement < Game.NUMBER_SEQUENCE.unlockRequirement).isTrue()
    }

    // ==================== Trial Feature Tests ====================

    @Test
    fun `GameInfo includes trial attempts for locked games`() {
        // Create GameInfo for a locked game with trial attempts
        val gameInfo =
            GameSelectionScreen.GameInfo(
                game = Game.MATH_RACE,
                isUnlocked = false,
                personalBest = 0,
                totalPlays = 0,
                trialAttempts = 2,
            )

        assertThat(gameInfo.trialAttempts).isEqualTo(2)
        assertThat(gameInfo.isUnlocked).isFalse()
    }

    @Test
    fun `GameInfo defaults to zero trial attempts`() {
        // When creating GameInfo without specifying trialAttempts
        val gameInfo =
            GameSelectionScreen.GameInfo(
                game = Game.MEMORY_MATCH,
                isUnlocked = true,
                personalBest = 15,
                totalPlays = 3,
            )

        assertThat(gameInfo.trialAttempts).isEqualTo(0)
    }

    @Test
    fun `PlayGame event includes isTrial flag`() {
        // Create PlayGame event with trial flag
        val trialEvent = GameSelectionScreen.Event.PlayGame(Game.MATH_RACE, isTrial = true)
        val regularEvent = GameSelectionScreen.Event.PlayGame(Game.MATH_RACE, isTrial = false)

        assertThat(trialEvent.isTrial).isTrue()
        assertThat(regularEvent.isTrial).isFalse()
    }

    @Test
    fun `PlayGame event defaults to non-trial`() {
        // When creating PlayGame event without specifying isTrial
        val event = GameSelectionScreen.Event.PlayGame(Game.NUMBER_SEQUENCE)

        assertThat(event.isTrial).isFalse()
    }

    // ==================== Trial Mode Screen Tests ====================

    @Test
    fun `MathRaceScreen defaults to non-trial mode`() {
        val screen =
            dev.hossain.mathtutor.ui.mathrace
                .MathRaceScreen()

        assertThat(screen.isTrialMode).isFalse()
    }

    @Test
    fun `MathRaceScreen can be created in trial mode`() {
        val screen =
            dev.hossain.mathtutor.ui.mathrace
                .MathRaceScreen(isTrialMode = true)

        assertThat(screen.isTrialMode).isTrue()
    }

    @Test
    fun `MemoryMatchScreen defaults to non-trial mode`() {
        val screen =
            dev.hossain.mathtutor.ui.memorymatch
                .MemoryMatchScreen()

        assertThat(screen.isTrialMode).isFalse()
    }

    @Test
    fun `MemoryMatchScreen can be created in trial mode`() {
        val screen =
            dev.hossain.mathtutor.ui.memorymatch
                .MemoryMatchScreen(isTrialMode = true)

        assertThat(screen.isTrialMode).isTrue()
    }

    @Test
    fun `NumberSequenceScreen defaults to non-trial mode`() {
        val screen =
            dev.hossain.mathtutor.ui.numbersequence
                .NumberSequenceScreen()

        assertThat(screen.isTrialMode).isFalse()
    }

    @Test
    fun `NumberSequenceScreen can be created in trial mode`() {
        val screen =
            dev.hossain.mathtutor.ui.numbersequence
                .NumberSequenceScreen(isTrialMode = true)

        assertThat(screen.isTrialMode).isTrue()
    }
}
