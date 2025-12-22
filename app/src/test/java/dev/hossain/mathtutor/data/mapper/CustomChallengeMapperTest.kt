package dev.hossain.mathtutor.data.mapper

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.data.local.entity.ChallengePracticeSessionEntity
import dev.hossain.mathtutor.data.local.entity.ChallengeProblemsEntity
import dev.hossain.mathtutor.data.local.entity.CustomChallengeEntity
import dev.hossain.mathtutor.data.local.entity.CustomChallengeWithDetails
import dev.hossain.mathtutor.domain.model.ChallengePracticeSession
import dev.hossain.mathtutor.domain.model.ChallengeType
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import org.junit.Test
import java.time.Instant

class CustomChallengeMapperTest {
    private val testChallengeId = "test-challenge-id"
    private val testInstant = Instant.ofEpochMilli(1234567890000)

    @Test
    fun `toEntity converts CustomChallenge to CustomChallengeEntity`() {
        val challenge =
            CustomChallenge(
                id = testChallengeId,
                title = "Test Challenge",
                subtitle = "Test Subtitle",
                type = ChallengeType.EXPLICIT,
                problems = emptyList(),
                createdAt = testInstant,
                isArchived = false,
                practiceHistory = emptyList(),
            )

        val entity = CustomChallengeMapper.toEntity(challenge)

        assertThat(entity.id).isEqualTo(testChallengeId)
        assertThat(entity.title).isEqualTo("Test Challenge")
        assertThat(entity.subtitle).isEqualTo("Test Subtitle")
        assertThat(entity.type).isEqualTo(ChallengeType.EXPLICIT)
        assertThat(entity.createdAt).isEqualTo(testInstant)
        assertThat(entity.isArchived).isFalse()
    }

    @Test
    fun `toEntity handles null subtitle`() {
        val challenge =
            CustomChallenge(
                id = testChallengeId,
                title = "Test Challenge",
                subtitle = null,
                type = ChallengeType.GENERATED,
                problems = emptyList(),
                createdAt = testInstant,
                isArchived = true,
            )

        val entity = CustomChallengeMapper.toEntity(challenge)

        assertThat(entity.subtitle).isNull()
        assertThat(entity.isArchived).isTrue()
    }

    @Test
    fun `problemsToEntities converts MathProblems to ChallengeProblemsEntities`() {
        val problems =
            listOf(
                MathProblem(
                    id = "problem-1",
                    num1 = 5,
                    num2 = 3,
                    operation = MathOperation.ADDITION,
                    correctAnswer = 8,
                ),
                MathProblem(
                    id = "problem-2",
                    num1 = 10,
                    num2 = 2,
                    operation = MathOperation.DIVISION,
                    correctAnswer = 5,
                ),
            )

        val entities = CustomChallengeMapper.problemsToEntities(problems, testChallengeId)

        assertThat(entities).hasSize(2)
        assertThat(entities[0].id).isEqualTo("problem-1")
        assertThat(entities[0].challengeId).isEqualTo(testChallengeId)
        assertThat(entities[0].operand1).isEqualTo(5)
        assertThat(entities[0].operand2).isEqualTo(3)
        assertThat(entities[0].operation).isEqualTo(MathOperation.ADDITION)
        assertThat(entities[0].answer).isEqualTo(8)
        assertThat(entities[0].orderIndex).isEqualTo(0)

        assertThat(entities[1].id).isEqualTo("problem-2")
        assertThat(entities[1].orderIndex).isEqualTo(1)
    }

    @Test
    fun `problemsToEntities maintains order with orderIndex`() {
        val problems =
            listOf(
                MathProblem(id = "p1", num1 = 1, num2 = 1, operation = MathOperation.ADDITION, correctAnswer = 2),
                MathProblem(id = "p2", num1 = 2, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 4),
                MathProblem(id = "p3", num1 = 3, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 6),
            )

        val entities = CustomChallengeMapper.problemsToEntities(problems, testChallengeId)

        assertThat(entities[0].orderIndex).isEqualTo(0)
        assertThat(entities[1].orderIndex).isEqualTo(1)
        assertThat(entities[2].orderIndex).isEqualTo(2)
    }

    @Test
    fun `sessionToEntity converts ChallengePracticeSession to entity`() {
        val endTime = Instant.ofEpochMilli(1234567900000)
        val session =
            ChallengePracticeSession(
                sessionId = "session-1",
                startTime = testInstant,
                endTime = endTime,
                problemsAttempted = 10,
                correctAnswers = 8,
                totalTimeMs = 120000,
            )

        val entity = CustomChallengeMapper.sessionToEntity(session, testChallengeId)

        assertThat(entity.sessionId).isEqualTo("session-1")
        assertThat(entity.challengeId).isEqualTo(testChallengeId)
        assertThat(entity.startTime).isEqualTo(testInstant)
        assertThat(entity.endTime).isEqualTo(endTime)
        assertThat(entity.problemsAttempted).isEqualTo(10)
        assertThat(entity.correctAnswers).isEqualTo(8)
        assertThat(entity.totalTimeMs).isEqualTo(120000)
    }

    @Test
    fun `sessionToEntity handles null endTime`() {
        val session =
            ChallengePracticeSession(
                sessionId = "session-1",
                startTime = testInstant,
                endTime = null,
                problemsAttempted = 5,
                correctAnswers = 3,
                totalTimeMs = 60000,
            )

        val entity = CustomChallengeMapper.sessionToEntity(session, testChallengeId)

        assertThat(entity.endTime).isNull()
    }

    @Test
    fun `toDomain converts CustomChallengeWithDetails to CustomChallenge`() {
        val challengeEntity =
            CustomChallengeEntity(
                id = testChallengeId,
                title = "Test Challenge",
                subtitle = "Test Subtitle",
                type = ChallengeType.EXPLICIT,
                createdAt = testInstant,
                isArchived = false,
            )

        val problemEntities =
            listOf(
                ChallengeProblemsEntity(
                    id = "p1",
                    challengeId = testChallengeId,
                    operand1 = 5,
                    operand2 = 3,
                    operation = MathOperation.ADDITION,
                    answer = 8,
                    orderIndex = 0,
                ),
                ChallengeProblemsEntity(
                    id = "p2",
                    challengeId = testChallengeId,
                    operand1 = 10,
                    operand2 = 2,
                    operation = MathOperation.SUBTRACTION,
                    answer = 8,
                    orderIndex = 1,
                ),
            )

        val sessionEntities =
            listOf(
                ChallengePracticeSessionEntity(
                    sessionId = "s1",
                    challengeId = testChallengeId,
                    startTime = testInstant,
                    endTime = Instant.ofEpochMilli(1234567900000),
                    problemsAttempted = 10,
                    correctAnswers = 8,
                    totalTimeMs = 120000,
                ),
            )

        val entityWithDetails =
            CustomChallengeWithDetails(
                challenge = challengeEntity,
                problems = problemEntities,
                sessions = sessionEntities,
            )

        val domain = CustomChallengeMapper.toDomain(entityWithDetails)

        assertThat(domain.id).isEqualTo(testChallengeId)
        assertThat(domain.title).isEqualTo("Test Challenge")
        assertThat(domain.subtitle).isEqualTo("Test Subtitle")
        assertThat(domain.type).isEqualTo(ChallengeType.EXPLICIT)
        assertThat(domain.createdAt).isEqualTo(testInstant)
        assertThat(domain.isArchived).isFalse()
        assertThat(domain.problems).hasSize(2)
        assertThat(domain.practiceHistory).hasSize(1)
    }

    @Test
    fun `toDomain sorts problems by orderIndex`() {
        val challengeEntity =
            CustomChallengeEntity(
                id = testChallengeId,
                title = "Test",
                subtitle = null,
                type = ChallengeType.EXPLICIT,
                createdAt = testInstant,
                isArchived = false,
            )

        // Insert problems in wrong order
        val problemEntities =
            listOf(
                ChallengeProblemsEntity(
                    id = "p2",
                    challengeId = testChallengeId,
                    operand1 = 20,
                    operand2 = 2,
                    operation = MathOperation.ADDITION,
                    answer = 22,
                    orderIndex = 2,
                ),
                ChallengeProblemsEntity(
                    id = "p0",
                    challengeId = testChallengeId,
                    operand1 = 10,
                    operand2 = 1,
                    operation = MathOperation.ADDITION,
                    answer = 11,
                    orderIndex = 0,
                ),
                ChallengeProblemsEntity(
                    id = "p1",
                    challengeId = testChallengeId,
                    operand1 = 15,
                    operand2 = 3,
                    operation = MathOperation.ADDITION,
                    answer = 18,
                    orderIndex = 1,
                ),
            )

        val entityWithDetails =
            CustomChallengeWithDetails(
                challenge = challengeEntity,
                problems = problemEntities,
                sessions = emptyList(),
            )

        val domain = CustomChallengeMapper.toDomain(entityWithDetails)

        assertThat(domain.problems).hasSize(3)
        assertThat(domain.problems[0].id).isEqualTo("p0")
        assertThat(domain.problems[1].id).isEqualTo("p1")
        assertThat(domain.problems[2].id).isEqualTo("p2")
    }

    @Test
    fun `toDomain converts problem entities correctly`() {
        val challengeEntity =
            CustomChallengeEntity(
                id = testChallengeId,
                title = "Test",
                subtitle = null,
                type = ChallengeType.EXPLICIT,
                createdAt = testInstant,
                isArchived = false,
            )

        val problemEntities =
            listOf(
                ChallengeProblemsEntity(
                    id = "p1",
                    challengeId = testChallengeId,
                    operand1 = 12,
                    operand2 = 4,
                    operation = MathOperation.MULTIPLICATION,
                    answer = 48,
                    orderIndex = 0,
                ),
            )

        val entityWithDetails =
            CustomChallengeWithDetails(
                challenge = challengeEntity,
                problems = problemEntities,
                sessions = emptyList(),
            )

        val domain = CustomChallengeMapper.toDomain(entityWithDetails)

        assertThat(domain.problems[0].id).isEqualTo("p1")
        assertThat(domain.problems[0].num1).isEqualTo(12)
        assertThat(domain.problems[0].num2).isEqualTo(4)
        assertThat(domain.problems[0].operation).isEqualTo(MathOperation.MULTIPLICATION)
        assertThat(domain.problems[0].correctAnswer).isEqualTo(48)
    }

    @Test
    fun `toDomainList converts list of entities to list of domain models`() {
        val entities =
            listOf(
                CustomChallengeWithDetails(
                    challenge =
                        CustomChallengeEntity(
                            id = "c1",
                            title = "Challenge 1",
                            subtitle = null,
                            type = ChallengeType.GENERATED,
                            createdAt = testInstant,
                            isArchived = false,
                        ),
                    problems = emptyList(),
                    sessions = emptyList(),
                ),
                CustomChallengeWithDetails(
                    challenge =
                        CustomChallengeEntity(
                            id = "c2",
                            title = "Challenge 2",
                            subtitle = "Subtitle",
                            type = ChallengeType.EXPLICIT,
                            createdAt = testInstant,
                            isArchived = true,
                        ),
                    problems = emptyList(),
                    sessions = emptyList(),
                ),
            )

        val domainList = CustomChallengeMapper.toDomainList(entities)

        assertThat(domainList).hasSize(2)
        assertThat(domainList[0].id).isEqualTo("c1")
        assertThat(domainList[0].title).isEqualTo("Challenge 1")
        assertThat(domainList[1].id).isEqualTo("c2")
        assertThat(domainList[1].title).isEqualTo("Challenge 2")
    }

    @Test
    fun `toDomain handles empty problems and sessions`() {
        val challengeEntity =
            CustomChallengeEntity(
                id = testChallengeId,
                title = "Test",
                subtitle = null,
                type = ChallengeType.GENERATED,
                createdAt = testInstant,
                isArchived = false,
            )

        val entityWithDetails =
            CustomChallengeWithDetails(
                challenge = challengeEntity,
                problems = emptyList(),
                sessions = emptyList(),
            )

        val domain = CustomChallengeMapper.toDomain(entityWithDetails)

        assertThat(domain.problems).isEmpty()
        assertThat(domain.practiceHistory).isEmpty()
    }
}
