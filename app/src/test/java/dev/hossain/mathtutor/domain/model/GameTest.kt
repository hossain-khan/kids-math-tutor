package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTest {
    @Test
    fun `Game has correct values`() {
        assertEquals(3, Game.entries.size)
    }

    @Test
    fun `Game values are correctly named`() {
        assertEquals(Game.MATH_RACE, Game.valueOf("MATH_RACE"))
        assertEquals(Game.MEMORY_MATCH, Game.valueOf("MEMORY_MATCH"))
        assertEquals(Game.NUMBER_SEQUENCE, Game.valueOf("NUMBER_SEQUENCE"))
    }

    @Test
    fun `Game displayNames are set correctly`() {
        assertEquals("Math Race", Game.MATH_RACE.displayName)
        assertEquals("Memory Match", Game.MEMORY_MATCH.displayName)
        assertEquals("Number Sequence", Game.NUMBER_SEQUENCE.displayName)
    }

    @Test
    fun `Game icons are set correctly`() {
        assertEquals("⏱️", Game.MATH_RACE.icon)
        assertEquals("🧩", Game.MEMORY_MATCH.icon)
        assertEquals("🎲", Game.NUMBER_SEQUENCE.icon)
    }

    @Test
    fun `Game unlock requirements are correct`() {
        assertEquals(50, Game.MATH_RACE.unlockRequirement)
        assertEquals(100, Game.MEMORY_MATCH.unlockRequirement)
        assertEquals(200, Game.NUMBER_SEQUENCE.unlockRequirement)
    }

    @Test
    fun `Game durations are correct`() {
        assertEquals(60, Game.MATH_RACE.durationSeconds)
        assertEquals(120, Game.MEMORY_MATCH.durationSeconds)
        assertEquals(90, Game.NUMBER_SEQUENCE.durationSeconds)
    }

    // isUnlocked tests
    @Test
    fun `isUnlocked returns false when problems solved is below requirement`() {
        assertFalse(Game.MATH_RACE.isUnlocked(0))
        assertFalse(Game.MATH_RACE.isUnlocked(49))
        assertFalse(Game.MEMORY_MATCH.isUnlocked(99))
        assertFalse(Game.NUMBER_SEQUENCE.isUnlocked(199))
    }

    @Test
    fun `isUnlocked returns true when problems solved equals requirement`() {
        assertTrue(Game.MATH_RACE.isUnlocked(50))
        assertTrue(Game.MEMORY_MATCH.isUnlocked(100))
        assertTrue(Game.NUMBER_SEQUENCE.isUnlocked(200))
    }

    @Test
    fun `isUnlocked returns true when problems solved exceeds requirement`() {
        assertTrue(Game.MATH_RACE.isUnlocked(100))
        assertTrue(Game.MEMORY_MATCH.isUnlocked(200))
        assertTrue(Game.NUMBER_SEQUENCE.isUnlocked(500))
    }

    // unlockProgress tests
    @Test
    fun `unlockProgress returns 0 when no problems solved`() {
        assertEquals(0f, Game.MATH_RACE.unlockProgress(0))
    }

    @Test
    fun `unlockProgress returns correct fraction`() {
        assertEquals(0.5f, Game.MATH_RACE.unlockProgress(25))
        assertEquals(0.5f, Game.MEMORY_MATCH.unlockProgress(50))
        assertEquals(0.5f, Game.NUMBER_SEQUENCE.unlockProgress(100))
    }

    @Test
    fun `unlockProgress caps at 1 when requirement met`() {
        assertEquals(1f, Game.MATH_RACE.unlockProgress(50))
        assertEquals(1f, Game.MATH_RACE.unlockProgress(100))
    }

    // problemsUntilUnlock tests
    @Test
    fun `problemsUntilUnlock returns requirement when no problems solved`() {
        assertEquals(50, Game.MATH_RACE.problemsUntilUnlock(0))
        assertEquals(100, Game.MEMORY_MATCH.problemsUntilUnlock(0))
        assertEquals(200, Game.NUMBER_SEQUENCE.problemsUntilUnlock(0))
    }

    @Test
    fun `problemsUntilUnlock returns remaining problems`() {
        assertEquals(25, Game.MATH_RACE.problemsUntilUnlock(25))
        assertEquals(50, Game.MEMORY_MATCH.problemsUntilUnlock(50))
        assertEquals(100, Game.NUMBER_SEQUENCE.problemsUntilUnlock(100))
    }

    @Test
    fun `problemsUntilUnlock returns 0 when requirement met`() {
        assertEquals(0, Game.MATH_RACE.problemsUntilUnlock(50))
        assertEquals(0, Game.MATH_RACE.problemsUntilUnlock(100))
    }
}
