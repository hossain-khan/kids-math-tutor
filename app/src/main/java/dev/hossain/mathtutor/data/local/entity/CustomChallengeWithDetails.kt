package dev.hossain.mathtutor.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room relation class that represents a custom challenge with all its related data.
 * Uses [@Embedded] and [@Relation] to fetch the challenge along with its problems and practice sessions.
 *
 * @property challenge The main custom challenge entity
 * @property problems List of problems associated with this challenge
 * @property sessions List of practice sessions for this challenge
 */
data class CustomChallengeWithDetails(
    @Embedded
    val challenge: CustomChallengeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "challengeId",
    )
    val problems: List<ChallengeProblemsEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "challengeId",
    )
    val sessions: List<ChallengePracticeSessionEntity>,
)
