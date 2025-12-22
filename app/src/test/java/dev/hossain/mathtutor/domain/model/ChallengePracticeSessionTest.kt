package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Instant

class ChallengePracticeSessionTest {
    @Test
    fun `creates session with all properties`() {
        val startTime = Instant.now()
        val endTime = startTime.plusSeconds(120)
        val session =
            ChallengePracticeSession(
                sessionId = "test-id",
                startTime = startTime,
                endTime = endTime,
                problemsAttempted = 10,
                correctAnswers = 8,
                totalTimeMs = 120000L,
            )

        assertThat(session.sessionId).isEqualTo("test-id")
        assertThat(session.startTime).isEqualTo(startTime)
        assertThat(session.endTime).isEqualTo(endTime)
        assertThat(session.problemsAttempted).isEqualTo(10)
        assertThat(session.correctAnswers).isEqualTo(8)
        assertThat(session.totalTimeMs).isEqualTo(120000L)
    }

    @Test
    fun `session has unique ID when not specified`() {
        val startTime = Instant.now()
        val session1 =
            ChallengePracticeSession(
                startTime = startTime,
                problemsAttempted = 10,
                correctAnswers = 8,
                totalTimeMs = 120000L,
            )
        val session2 =
            ChallengePracticeSession(
                startTime = startTime,
                problemsAttempted = 10,
                correctAnswers = 8,
                totalTimeMs = 120000L,
            )

        assertThat(session1.sessionId).isNotEmpty()
        assertThat(session2.sessionId).isNotEmpty()
        assertThat(session1.sessionId).isNotEqualTo(session2.sessionId)
    }

    @Test
    fun `getAccuracy calculates correct percentage`() {
        val session =
            ChallengePracticeSession(
                startTime = Instant.now(),
                problemsAttempted = 10,
                correctAnswers = 8,
                totalTimeMs = 120000L,
            )

        assertThat(session.getAccuracy()).isWithin(0.01f).of(80f)
    }

    @Test
    fun `getAccuracy returns 100 for all correct answers`() {
        val session =
            ChallengePracticeSession(
                startTime = Instant.now(),
                problemsAttempted = 10,
                correctAnswers = 10,
                totalTimeMs = 120000L,
            )

        assertThat(session.getAccuracy()).isWithin(0.01f).of(100f)
    }

    @Test
    fun `getAccuracy returns 0 for no correct answers`() {
        val session =
            ChallengePracticeSession(
                startTime = Instant.now(),
                problemsAttempted = 10,
                correctAnswers = 0,
                totalTimeMs = 120000L,
            )

        assertThat(session.getAccuracy()).isWithin(0.01f).of(0f)
    }

    @Test
    fun `getAccuracy returns 0 when no problems attempted`() {
        val session =
            ChallengePracticeSession(
                startTime = Instant.now(),
                problemsAttempted = 0,
                correctAnswers = 0,
                totalTimeMs = 0L,
            )

        assertThat(session.getAccuracy()).isWithin(0.01f).of(0f)
    }

    @Test
    fun `isComplete returns false when endTime is null`() {
        val session =
            ChallengePracticeSession(
                startTime = Instant.now(),
                endTime = null,
                problemsAttempted = 5,
                correctAnswers = 3,
                totalTimeMs = 60000L,
            )

        assertThat(session.isComplete()).isFalse()
    }

    @Test
    fun `isComplete returns true when endTime is set`() {
        val startTime = Instant.now()
        val session =
            ChallengePracticeSession(
                startTime = startTime,
                endTime = startTime.plusSeconds(120),
                problemsAttempted = 10,
                correctAnswers = 8,
                totalTimeMs = 120000L,
            )

        assertThat(session.isComplete()).isTrue()
    }

    @Test
    fun `session can be created with default endTime null`() {
        val session =
            ChallengePracticeSession(
                startTime = Instant.now(),
                problemsAttempted = 5,
                correctAnswers = 3,
                totalTimeMs = 60000L,
            )

        assertThat(session.endTime).isNull()
        assertThat(session.isComplete()).isFalse()
    }

    @Test
    fun `session handles partial accuracy correctly`() {
        val session =
            ChallengePracticeSession(
                startTime = Instant.now(),
                problemsAttempted = 7,
                correctAnswers = 5,
                totalTimeMs = 90000L,
            )

        // 5/7 ≈ 71.43%
        assertThat(session.getAccuracy()).isWithin(0.01f).of(71.43f)
    }
}
