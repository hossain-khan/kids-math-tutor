package dev.hossain.mathtutor.domain.repository

import dev.hossain.mathtutor.domain.model.ChallengePracticeSession
import dev.hossain.mathtutor.domain.model.CustomChallenge
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for custom challenge operations.
 * Provides methods to manage custom challenges created by parents.
 */
interface CustomChallengeRepository {
    /**
     * Saves a custom challenge with all its problems.
     *
     * @param challenge The challenge to save
     */
    suspend fun saveChallenge(challenge: CustomChallenge)

    /**
     * Retrieves all custom challenges (including archived).
     *
     * @return List of all custom challenges with their problems and practice history
     */
    suspend fun getAllChallenges(): List<CustomChallenge>

    /**
     * Retrieves a specific challenge by its ID.
     *
     * @param id The unique identifier of the challenge
     * @return The challenge if found, null otherwise
     */
    suspend fun getChallengeById(id: String): CustomChallenge?

    /**
     * Archives a challenge by marking it as archived.
     * Archived challenges are hidden from the active list but data is retained.
     *
     * @param id The unique identifier of the challenge to archive
     */
    suspend fun archiveChallenge(id: String)

    /**
     * Permanently deletes a challenge and all its related data.
     *
     * @param id The unique identifier of the challenge to delete
     */
    suspend fun deleteChallenge(id: String)

    /**
     * Adds a practice session to a challenge.
     *
     * @param challengeId The unique identifier of the challenge
     * @param session The practice session to add
     */
    suspend fun addPracticeSession(
        challengeId: String,
        session: ChallengePracticeSession,
    )

    /**
     * Observes active (non-archived) challenges.
     * Emits a new list whenever the data changes.
     *
     * @return Flow of list of active custom challenges
     */
    fun observeActiveChallenges(): Flow<List<CustomChallenge>>
}
