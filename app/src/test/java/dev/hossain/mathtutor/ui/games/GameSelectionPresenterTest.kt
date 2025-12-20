package dev.hossain.mathtutor.ui.games

import dev.hossain.mathtutor.domain.model.Game
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        assertEquals(50, game.unlockRequirement)

        // Under threshold - not unlocked
        assertFalse(game.isUnlocked(totalProblemsSolved = 0))
        assertFalse(game.isUnlocked(totalProblemsSolved = 49))

        // At or above threshold - unlocked
        assertTrue(game.isUnlocked(totalProblemsSolved = 50))
        assertTrue(game.isUnlocked(totalProblemsSolved = 100))
    }

    @Test
    fun `Memory Match unlocks at 100 problems solved`() {
        // Memory Match requires 100 problems
        val game = Game.MEMORY_MATCH
        assertEquals(100, game.unlockRequirement)

        // Under threshold - not unlocked
        assertFalse(game.isUnlocked(totalProblemsSolved = 0))
        assertFalse(game.isUnlocked(totalProblemsSolved = 99))

        // At or above threshold - unlocked
        assertTrue(game.isUnlocked(totalProblemsSolved = 100))
        assertTrue(game.isUnlocked(totalProblemsSolved = 150))
    }

    @Test
    fun `Number Sequence unlocks at 200 problems solved`() {
        // Number Sequence requires 200 problems
        val game = Game.NUMBER_SEQUENCE
        assertEquals(200, game.unlockRequirement)

        // Under threshold - not unlocked
        assertFalse(game.isUnlocked(totalProblemsSolved = 0))
        assertFalse(game.isUnlocked(totalProblemsSolved = 199))

        // At or above threshold - unlocked
        assertTrue(game.isUnlocked(totalProblemsSolved = 200))
        assertTrue(game.isUnlocked(totalProblemsSolved = 500))
    }

    // ==================== Progress Calculation Tests ====================

    @Test
    fun `unlock progress calculated correctly`() {
        val game = Game.MATH_RACE // 50 problems required

        assertEquals(0f, game.unlockProgress(0), 0.01f)
        assertEquals(0.5f, game.unlockProgress(25), 0.01f)
        assertEquals(1f, game.unlockProgress(50), 0.01f)
        assertEquals(1f, game.unlockProgress(100), 0.01f) // Capped at 1.0
    }

    @Test
    fun `problems until unlock calculated correctly`() {
        val game = Game.MATH_RACE // 50 problems required

        assertEquals(50, game.problemsUntilUnlock(0))
        assertEquals(25, game.problemsUntilUnlock(25))
        assertEquals(0, game.problemsUntilUnlock(50))
        assertEquals(0, game.problemsUntilUnlock(100)) // Already unlocked
    }

    @Test
    fun `all games are present in game list`() {
        // All Game enum values should be in the list
        val allGames = Game.entries

        assertEquals(3, allGames.size)
        assertTrue(allGames.contains(Game.MATH_RACE))
        assertTrue(allGames.contains(Game.MEMORY_MATCH))
        assertTrue(allGames.contains(Game.NUMBER_SEQUENCE))
    }

    // ==================== Game Properties Tests ====================

    @Test
    fun `game display names are correct`() {
        assertEquals("Math Race", Game.MATH_RACE.displayName)
        assertEquals("Memory Match", Game.MEMORY_MATCH.displayName)
        assertEquals("Number Sequence", Game.NUMBER_SEQUENCE.displayName)
    }

    @Test
    fun `game icons are defined`() {
        assertTrue(Game.MATH_RACE.icon.isNotEmpty())
        assertTrue(Game.MEMORY_MATCH.icon.isNotEmpty())
        assertTrue(Game.NUMBER_SEQUENCE.icon.isNotEmpty())
    }

    @Test
    fun `game descriptions are defined`() {
        assertTrue(Game.MATH_RACE.description.isNotEmpty())
        assertTrue(Game.MEMORY_MATCH.description.isNotEmpty())
        assertTrue(Game.NUMBER_SEQUENCE.description.isNotEmpty())
    }

    // ==================== State Tests ====================

    @Test
    fun `state shows correct unlock status for each game at 75 problems`() {
        val totalProblems = 75

        // At 75 problems:
        // - Math Race (50) = unlocked
        // - Memory Match (100) = locked
        // - Number Sequence (200) = locked
        assertTrue(Game.MATH_RACE.isUnlocked(totalProblems))
        assertFalse(Game.MEMORY_MATCH.isUnlocked(totalProblems))
        assertFalse(Game.NUMBER_SEQUENCE.isUnlocked(totalProblems))
    }

    @Test
    fun `state shows all games unlocked at 200 problems`() {
        val totalProblems = 200

        // At 200 problems, all games should be unlocked
        assertTrue(Game.MATH_RACE.isUnlocked(totalProblems))
        assertTrue(Game.MEMORY_MATCH.isUnlocked(totalProblems))
        assertTrue(Game.NUMBER_SEQUENCE.isUnlocked(totalProblems))
    }

    @Test
    fun `no games unlocked at zero problems`() {
        val totalProblems = 0

        // At 0 problems, no games should be unlocked
        assertFalse(Game.MATH_RACE.isUnlocked(totalProblems))
        assertFalse(Game.MEMORY_MATCH.isUnlocked(totalProblems))
        assertFalse(Game.NUMBER_SEQUENCE.isUnlocked(totalProblems))
    }

    @Test
    fun `unlock requirements are in ascending order`() {
        // Verify games unlock in order of difficulty/progression
        assertTrue(Game.MATH_RACE.unlockRequirement < Game.MEMORY_MATCH.unlockRequirement)
        assertTrue(Game.MEMORY_MATCH.unlockRequirement < Game.NUMBER_SEQUENCE.unlockRequirement)
    }
}
