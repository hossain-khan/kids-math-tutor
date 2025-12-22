package dev.hossain.mathtutor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.hossain.mathtutor.data.local.entity.ChallengePracticeSessionEntity
import dev.hossain.mathtutor.data.local.entity.ChallengeProblemsEntity
import dev.hossain.mathtutor.data.local.entity.CustomChallengeEntity
import dev.hossain.mathtutor.data.local.entity.CustomChallengeWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for custom challenge operations.
 * Provides methods for creating, reading, updating, and deleting custom challenges and their related data.
 */
@Dao
interface CustomChallengeDao {
    /**
     * Observes all active (non-archived) custom challenges ordered by creation date (newest first).
     * Returns a Flow that emits whenever the data changes.
     *
     * @return Flow of list of challenges with all their details (problems and sessions)
     */
    @Transaction
    @Query("SELECT * FROM custom_challenges WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun observeActiveChallenges(): Flow<List<CustomChallengeWithDetails>>

    /**
     * Gets a specific challenge with all its details (problems and sessions).
     *
     * @param id The unique identifier of the challenge
     * @return CustomChallengeWithDetails if found, null otherwise
     */
    @Transaction
    @Query("SELECT * FROM custom_challenges WHERE id = :id")
    suspend fun getChallengeWithDetails(id: String): CustomChallengeWithDetails?

    /**
     * Gets all challenges (including archived) with all their details.
     *
     * @return List of all challenges with details
     */
    @Transaction
    @Query("SELECT * FROM custom_challenges ORDER BY createdAt DESC")
    suspend fun getAllChallengesWithDetails(): List<CustomChallengeWithDetails>

    /**
     * Inserts or updates a custom challenge.
     * If a challenge with the same ID exists, it will be replaced.
     *
     * @param challenge The challenge to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: CustomChallengeEntity)

    /**
     * Inserts or updates a list of challenge problems.
     * If problems with the same IDs exist, they will be replaced.
     *
     * @param problems The list of problems to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblems(problems: List<ChallengeProblemsEntity>)

    /**
     * Inserts a new practice session for a challenge.
     *
     * @param session The practice session to insert
     */
    @Insert
    suspend fun insertPracticeSession(session: ChallengePracticeSessionEntity)

    /**
     * Archives a challenge by setting its isArchived flag to true.
     *
     * @param id The unique identifier of the challenge to archive
     */
    @Query("UPDATE custom_challenges SET isArchived = 1 WHERE id = :id")
    suspend fun archiveChallenge(id: String)

    /**
     * Deletes a challenge and all its related data (problems and sessions will cascade delete).
     *
     * @param id The unique identifier of the challenge to delete
     */
    @Query("DELETE FROM custom_challenges WHERE id = :id")
    suspend fun deleteChallenge(id: String)
}
