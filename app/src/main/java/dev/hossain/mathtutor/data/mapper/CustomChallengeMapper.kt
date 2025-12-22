package dev.hossain.mathtutor.data.mapper

import dev.hossain.mathtutor.data.local.entity.ChallengePracticeSessionEntity
import dev.hossain.mathtutor.data.local.entity.ChallengeProblemsEntity
import dev.hossain.mathtutor.data.local.entity.CustomChallengeEntity
import dev.hossain.mathtutor.data.local.entity.CustomChallengeWithDetails
import dev.hossain.mathtutor.domain.model.ChallengePracticeSession
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.MathProblem

/**
 * Mapper object for converting between CustomChallenge domain models and database entities.
 * Handles bidirectional conversion for custom challenges, problems, and practice sessions.
 */
object CustomChallengeMapper {
    /**
     * Converts a CustomChallenge domain model to a CustomChallengeEntity.
     *
     * @param challenge The domain model to convert
     * @return The corresponding database entity
     */
    fun toEntity(challenge: CustomChallenge): CustomChallengeEntity =
        CustomChallengeEntity(
            id = challenge.id,
            title = challenge.title,
            subtitle = challenge.subtitle,
            type = challenge.type,
            createdAt = challenge.createdAt,
            isArchived = challenge.isArchived,
        )

    /**
     * Converts a list of MathProblems to a list of ChallengeProblemsEntities.
     *
     * @param problems The list of domain model problems
     * @param challengeId The ID of the parent challenge
     * @return List of database entities
     */
    fun problemsToEntities(
        problems: List<MathProblem>,
        challengeId: String,
    ): List<ChallengeProblemsEntity> =
        problems.mapIndexed { index, problem ->
            ChallengeProblemsEntity(
                id = problem.id,
                challengeId = challengeId,
                operand1 = problem.num1,
                operand2 = problem.num2,
                operation = problem.operation,
                answer = problem.correctAnswer,
                orderIndex = index,
            )
        }

    /**
     * Converts a ChallengePracticeSession domain model to a ChallengePracticeSessionEntity.
     *
     * @param session The domain model to convert
     * @param challengeId The ID of the parent challenge
     * @return The corresponding database entity
     */
    fun sessionToEntity(
        session: ChallengePracticeSession,
        challengeId: String,
    ): ChallengePracticeSessionEntity =
        ChallengePracticeSessionEntity(
            sessionId = session.sessionId,
            challengeId = challengeId,
            startTime = session.startTime,
            endTime = session.endTime,
            problemsAttempted = session.problemsAttempted,
            correctAnswers = session.correctAnswers,
            totalTimeMs = session.totalTimeMs,
        )

    /**
     * Converts a CustomChallengeWithDetails entity to a CustomChallenge domain model.
     *
     * @param entity The database entity with all related data
     * @return The corresponding domain model
     */
    fun toDomain(entity: CustomChallengeWithDetails): CustomChallenge {
        val problems =
            entity.problems
                .sortedBy { it.orderIndex }
                .map { problemEntity ->
                    MathProblem(
                        id = problemEntity.id,
                        num1 = problemEntity.operand1,
                        num2 = problemEntity.operand2,
                        operation = problemEntity.operation,
                        correctAnswer = problemEntity.answer,
                    )
                }

        val sessions =
            entity.sessions.map { sessionEntity ->
                ChallengePracticeSession(
                    sessionId = sessionEntity.sessionId,
                    startTime = sessionEntity.startTime,
                    endTime = sessionEntity.endTime,
                    problemsAttempted = sessionEntity.problemsAttempted,
                    correctAnswers = sessionEntity.correctAnswers,
                    totalTimeMs = sessionEntity.totalTimeMs,
                )
            }

        return CustomChallenge(
            id = entity.challenge.id,
            title = entity.challenge.title,
            subtitle = entity.challenge.subtitle,
            type = entity.challenge.type,
            problems = problems,
            createdAt = entity.challenge.createdAt,
            isArchived = entity.challenge.isArchived,
            practiceHistory = sessions,
        )
    }

    /**
     * Converts a list of CustomChallengeWithDetails entities to a list of CustomChallenge domain models.
     *
     * @param entities The list of database entities
     * @return List of domain models
     */
    fun toDomainList(entities: List<CustomChallengeWithDetails>): List<CustomChallenge> = entities.map(::toDomain)
}
