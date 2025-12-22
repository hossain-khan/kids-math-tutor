package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChallengeTypeTest {
    @Test
    fun `enum has GENERATED type`() {
        val type = ChallengeType.GENERATED
        assertThat(type).isEqualTo(ChallengeType.GENERATED)
    }

    @Test
    fun `enum has EXPLICIT type`() {
        val type = ChallengeType.EXPLICIT
        assertThat(type).isEqualTo(ChallengeType.EXPLICIT)
    }

    @Test
    fun `enum values are distinct`() {
        assertThat(ChallengeType.GENERATED).isNotEqualTo(ChallengeType.EXPLICIT)
    }

    @Test
    fun `enum contains exactly two values`() {
        val values = ChallengeType.entries
        assertThat(values).hasSize(2)
        assertThat(values).containsExactly(ChallengeType.GENERATED, ChallengeType.EXPLICIT)
    }
}
