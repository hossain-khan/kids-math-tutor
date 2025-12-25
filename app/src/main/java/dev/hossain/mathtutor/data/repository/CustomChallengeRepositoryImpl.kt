package dev.hossain.mathtutor.data.repository

import dev.hossain.mathtutor.data.local.dao.CustomChallengeDao
import dev.hossain.mathtutor.data.mapper.CustomChallengeMapper
import dev.hossain.mathtutor.domain.model.ChallengePracticeSession
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.repository.CustomChallengeRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Implementation of [CustomChallengeRepository] using Room database.
 * Handles all custom challenge data operations with Flow-based reactive streams.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class CustomChallengeRepositoryImpl
    constructor(
        private val dao: CustomChallengeDao,
    ) : CustomChallengeRepository {
        override suspend fun saveChallenge(challenge: CustomChallenge) {
            try {
                Timber.d(
                    "CustomChallengeRepository: Saving challenge - id=${challenge.id}, " +
                        "title=${challenge.title}, problems=${challenge.problems.size}",
                )

                // Insert challenge entity
                val challengeEntity = CustomChallengeMapper.toEntity(challenge)
                dao.insertChallenge(challengeEntity)

                // Insert problems
                val problemEntities = CustomChallengeMapper.problemsToEntities(challenge.problems, challenge.id)
                dao.insertProblems(problemEntities)

                Timber.d("CustomChallengeRepository: Challenge saved successfully")
            } catch (e: Exception) {
                Timber.e(e, "CustomChallengeRepository: Failed to save challenge")
                throw e
            }
        }

        override suspend fun getAllChallenges(): List<CustomChallenge> {
            try {
                Timber.d("CustomChallengeRepository: Fetching all challenges")
                val entities = dao.getAllChallengesWithDetails()
                val challenges = CustomChallengeMapper.toDomainList(entities)
                Timber.d("CustomChallengeRepository: Fetched ${challenges.size} challenges")
                return challenges
            } catch (e: Exception) {
                Timber.e(e, "CustomChallengeRepository: Failed to fetch all challenges")
                throw e
            }
        }

        override suspend fun getChallengeById(id: String): CustomChallenge? {
            try {
                Timber.d("CustomChallengeRepository: Fetching challenge with id=$id")
                val entity = dao.getChallengeWithDetails(id)
                val challenge = entity?.let { CustomChallengeMapper.toDomain(it) }
                Timber.d("CustomChallengeRepository: Challenge ${if (challenge != null) "found" else "not found"}")
                return challenge
            } catch (e: Exception) {
                Timber.e(e, "CustomChallengeRepository: Failed to fetch challenge by id=$id")
                throw e
            }
        }

        override suspend fun archiveChallenge(id: String) {
            try {
                Timber.d("CustomChallengeRepository: Archiving challenge with id=$id")
                dao.archiveChallenge(id)
                Timber.d("CustomChallengeRepository: Challenge archived successfully")
            } catch (e: Exception) {
                Timber.e(e, "CustomChallengeRepository: Failed to archive challenge")
                throw e
            }
        }

        override suspend fun unarchiveChallenge(id: String) {
            try {
                Timber.d("CustomChallengeRepository: Unarchiving challenge with id=$id")
                dao.unarchiveChallenge(id)
                Timber.d("CustomChallengeRepository: Challenge unarchived successfully")
            } catch (e: Exception) {
                Timber.e(e, "CustomChallengeRepository: Failed to unarchive challenge")
                throw e
            }
        }

        override suspend fun deleteChallenge(id: String) {
            try {
                Timber.d("CustomChallengeRepository: Deleting challenge with id=$id")
                dao.deleteChallenge(id)
                Timber.d("CustomChallengeRepository: Challenge deleted successfully")
            } catch (e: Exception) {
                Timber.e(e, "CustomChallengeRepository: Failed to delete challenge")
                throw e
            }
        }

        override suspend fun addPracticeSession(
            challengeId: String,
            session: ChallengePracticeSession,
        ) {
            try {
                Timber.d(
                    "CustomChallengeRepository: Adding practice session - " +
                        "challengeId=$challengeId, sessionId=${session.sessionId}",
                )
                val sessionEntity = CustomChallengeMapper.sessionToEntity(session, challengeId)
                dao.insertPracticeSession(sessionEntity)
                Timber.d("CustomChallengeRepository: Practice session added successfully")
            } catch (e: Exception) {
                Timber.e(e, "CustomChallengeRepository: Failed to add practice session")
                throw e
            }
        }

        override suspend fun clearChallengeSessions(challengeId: String) {
            try {
                Timber.d("CustomChallengeRepository: Clearing sessions for challenge id=$challengeId")
                dao.clearChallengeSessions(challengeId)
                Timber.d("CustomChallengeRepository: Challenge sessions cleared successfully")
            } catch (e: Exception) {
                Timber.e(e, "CustomChallengeRepository: Failed to clear challenge sessions")
                throw e
            }
        }

        override fun observeAllChallenges(): Flow<List<CustomChallenge>> =
            dao.getAllChallenges().map { entities ->
                CustomChallengeMapper.toDomainList(entities)
            }

        override fun observeActiveChallenges(): Flow<List<CustomChallenge>> =
            dao.observeActiveChallenges().map { entities ->
                CustomChallengeMapper.toDomainList(entities)
            }
    }
