package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GameTest {
    @Test
    fun `Game has correct values`() {
        assertThat(Game.entries.size).isEqualTo(3)
    }

    @Test
    fun `Game values are correctly named`() {
        assertThat(Game.valueOf("MATH_RACE")).isEqualTo(Game.MATH_RACE)
        assertThat(Game.valueOf("MEMORY_MATCH")).isEqualTo(Game.MEMORY_MATCH)
        assertThat(Game.valueOf("NUMBER_SEQUENCE")).isEqualTo(Game.NUMBER_SEQUENCE)
    }

    @Test
    fun `Game displayNames are set correctly`() {
        assertThat(Game.MATH_RACE.displayName).isEqualTo("Math Race")
        assertThat(Game.MEMORY_MATCH.displayName).isEqualTo("Memory Match")
        assertThat(Game.NUMBER_SEQUENCE.displayName).isEqualTo("Number Sequence")
    }

    @Test
    fun `Game icons are set correctly`() {
        assertThat(Game.MATH_RACE.icon).isEqualTo("⏱️")
        assertThat(Game.MEMORY_MATCH.icon).isEqualTo("🧩")
        assertThat(Game.NUMBER_SEQUENCE.icon).isEqualTo("🎲")
    }

    @Test
    fun `Game unlock requirements are correct`() {
        assertThat(Game.MATH_RACE.unlockRequirement).isEqualTo(50)
        assertThat(Game.MEMORY_MATCH.unlockRequirement).isEqualTo(100)
        assertThat(Game.NUMBER_SEQUENCE.unlockRequirement).isEqualTo(200)
    }

    @Test
    fun `Game durations are correct`() {
        assertThat(Game.MATH_RACE.durationSeconds).isEqualTo(60)
        assertThat(Game.MEMORY_MATCH.durationSeconds).isEqualTo(120)
        assertThat(Game.NUMBER_SEQUENCE.durationSeconds).isEqualTo(90)
    }

    // isUnlocked tests
    @Test
    fun `isUnlocked returns false when problems solved is below requirement`() {
        assertThat(Game.MATH_RACE.isUnlocked(0)).isFalse()
        assertThat(Game.MATH_RACE.isUnlocked(49)).isFalse()
        assertThat(Game.MEMORY_MATCH.isUnlocked(99)).isFalse()
        assertThat(Game.NUMBER_SEQUENCE.isUnlocked(199)).isFalse()
    }

    @Test
    fun `isUnlocked returns true when problems solved equals requirement`() {
        assertThat(Game.MATH_RACE.isUnlocked(50)).isTrue()
        assertThat(Game.MEMORY_MATCH.isUnlocked(100)).isTrue()
        assertThat(Game.NUMBER_SEQUENCE.isUnlocked(200)).isTrue()
    }

    @Test
    fun `isUnlocked returns true when problems solved exceeds requirement`() {
        assertThat(Game.MATH_RACE.isUnlocked(100)).isTrue()
        assertThat(Game.MEMORY_MATCH.isUnlocked(200)).isTrue()
        assertThat(Game.NUMBER_SEQUENCE.isUnlocked(500)).isTrue()
    }

    // unlockProgress tests
    @Test
    fun `unlockProgress returns 0 when no problems solved`() {
        assertThat(Game.MATH_RACE.unlockProgress(0)).isEqualTo(0f)
    }

    @Test
    fun `unlockProgress returns correct fraction`() {
        assertThat(Game.MATH_RACE.unlockProgress(25)).isEqualTo(0.5f)
        assertThat(Game.MEMORY_MATCH.unlockProgress(50)).isEqualTo(0.5f)
        assertThat(Game.NUMBER_SEQUENCE.unlockProgress(100)).isEqualTo(0.5f)
    }

    @Test
    fun `unlockProgress caps at 1 when requirement met`() {
        assertThat(Game.MATH_RACE.unlockProgress(50)).isEqualTo(1f)
        assertThat(Game.MATH_RACE.unlockProgress(100)).isEqualTo(1f)
    }

    // problemsUntilUnlock tests
    @Test
    fun `problemsUntilUnlock returns requirement when no problems solved`() {
        assertThat(Game.MATH_RACE.problemsUntilUnlock(0)).isEqualTo(50)
        assertThat(Game.MEMORY_MATCH.problemsUntilUnlock(0)).isEqualTo(100)
        assertThat(Game.NUMBER_SEQUENCE.problemsUntilUnlock(0)).isEqualTo(200)
    }

    @Test
    fun `problemsUntilUnlock returns remaining problems`() {
        assertThat(Game.MATH_RACE.problemsUntilUnlock(25)).isEqualTo(25)
        assertThat(Game.MEMORY_MATCH.problemsUntilUnlock(50)).isEqualTo(50)
        assertThat(Game.NUMBER_SEQUENCE.problemsUntilUnlock(100)).isEqualTo(100)
    }

    @Test
    fun `problemsUntilUnlock returns 0 when requirement met`() {
        assertThat(Game.MATH_RACE.problemsUntilUnlock(50)).isEqualTo(0)
        assertThat(Game.MATH_RACE.problemsUntilUnlock(100)).isEqualTo(0)
    }
}
