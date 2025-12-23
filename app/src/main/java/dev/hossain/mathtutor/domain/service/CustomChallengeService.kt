package dev.hossain.mathtutor.domain.service

import dev.hossain.mathtutor.domain.model.ChallengeImportSpec
import dev.hossain.mathtutor.domain.model.ChallengePracticeSession
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.PreviewData
import kotlinx.coroutines.flow.Flow

/**
 * Service interface for custom challenge operations.
 *
 * This service handles the business logic for converting import specifications
 * into practice-ready custom challenges, including problem generation, validation,
 * and preview generation.
 *
 * Challenge JSON can be created using the Math Pup Worksheet Creator web app:
 * `https://math-worksheet.gohk.xyz/`
 *
 * @see ChallengeImportSpec for the import specification format
 * @see CustomChallenge for the resulting challenge model
 */
interface CustomChallengeService {
    /**
     * Creates a custom challenge from an import specification.
     *
     * This method handles both generated (rule-based) and explicit (manually specified)
     * challenges. It performs validation, problem generation, and filtering.
     *
     * @param spec The challenge import specification
     * @return Result containing the created CustomChallenge or an error
     */
    suspend fun createChallengeFromSpec(spec: ChallengeImportSpec): Result<CustomChallenge>

    /**
     * Generates a preview of a challenge without persisting it.
     *
     * This is useful for showing users what problems will be included
     * before they commit to creating the challenge.
     *
     * @param spec The challenge import specification
     * @return Preview data including sample problems and metadata
     */
    suspend fun generatePreview(spec: ChallengeImportSpec): PreviewData

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
     * Records a practice session for a challenge.
     *
     * @param challengeId The unique identifier of the challenge
     * @param session The practice session to record
     */
    suspend fun recordPracticeSession(
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
