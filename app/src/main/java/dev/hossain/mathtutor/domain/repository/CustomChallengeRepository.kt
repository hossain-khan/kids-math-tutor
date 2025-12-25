package dev.hossain.mathtutor.domain.repository

import dev.hossain.mathtutor.domain.model.ChallengePracticeSession
import dev.hossain.mathtutor.domain.model.CustomChallenge
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for custom challenge operations.
 * Provides methods to manage custom challenges created by parents.
 *
 * Parents can create challenge JSON using the Math Pup Worksheet Creator web app:
 * `https://math-worksheet.gohk.xyz/`
 *
 * @see CustomChallenge for the challenge data model
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
     * Unarchives a challenge by marking it as active.
     * Unarchived challenges will appear in the active challenges list.
     *
     * @param id The unique identifier of the challenge to unarchive
     */
    suspend fun unarchiveChallenge(id: String)

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
     * Clears all practice sessions for a specific challenge.
     * This removes the practice history but keeps the challenge itself.
     *
     * @param challengeId The unique identifier of the challenge
     */
    suspend fun clearChallengeSessions(challengeId: String)

    /**
     * Observes all challenges (including archived).
     * Emits a new list whenever the data changes.
     *
     * @return Flow of list of all custom challenges
     */
    fun observeAllChallenges(): Flow<List<CustomChallenge>>

    /**
     * Observes active (non-archived) challenges.
     * Emits a new list whenever the data changes.
     *
     * @return Flow of list of active custom challenges
     */
    fun observeActiveChallenges(): Flow<List<CustomChallenge>>
}
