package dev.hossain.mathtutor.data.repository

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.data.local.dao.CustomChallengeDao
import dev.hossain.mathtutor.data.local.entity.ChallengePracticeSessionEntity
import dev.hossain.mathtutor.data.local.entity.ChallengeProblemsEntity
import dev.hossain.mathtutor.data.local.entity.CustomChallengeEntity
import dev.hossain.mathtutor.data.local.entity.CustomChallengeWithDetails
import dev.hossain.mathtutor.domain.model.ChallengePracticeSession
import dev.hossain.mathtutor.domain.model.ChallengeType
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant

class CustomChallengeRepositoryImplTest {
    private lateinit var fakeDao: FakeCustomChallengeDao
    private lateinit var repository: CustomChallengeRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeCustomChallengeDao()
        repository = CustomChallengeRepositoryImpl(fakeDao)
    }

    @Test
    fun `saveChallenge inserts challenge and problems correctly`() =
        runTest {
            val problems =
                listOf(
                    MathProblem(id = "p1", num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8),
                    MathProblem(id = "p2", num1 = 10, num2 = 2, operation = MathOperation.SUBTRACTION, correctAnswer = 8),
                )
            val challenge =
                CustomChallenge(
                    id = "c1",
                    title = "Test Challenge",
                    subtitle = "Test Subtitle",
                    type = ChallengeType.EXPLICIT,
                    problems = problems,
                    createdAt = Instant.now(),
                    isArchived = false,
                )

            repository.saveChallenge(challenge)

            assertThat(fakeDao.insertedChallenges).hasSize(1)
            assertThat(fakeDao.insertedChallenges[0].id).isEqualTo("c1")
            assertThat(fakeDao.insertedChallenges[0].title).isEqualTo("Test Challenge")
            assertThat(fakeDao.insertedProblems).hasSize(2)
            assertThat(fakeDao.insertedProblems[0].id).isEqualTo("p1")
            assertThat(fakeDao.insertedProblems[1].id).isEqualTo("p2")
        }

    @Test
    fun `saveChallenge with no problems inserts only challenge`() =
        runTest {
            val challenge =
                CustomChallenge(
                    id = "c1",
                    title = "Empty Challenge",
                    subtitle = null,
                    type = ChallengeType.GENERATED,
                    problems = emptyList(),
                    createdAt = Instant.now(),
                    isArchived = false,
                )

            repository.saveChallenge(challenge)

            assertThat(fakeDao.insertedChallenges).hasSize(1)
            assertThat(fakeDao.insertedProblems).isEmpty()
        }

    @Test
    fun `getAllChallenges returns all challenges from DAO`() =
        runTest {
            val entity1 = createChallengeWithDetails("c1", "Challenge 1", false)
            val entity2 = createChallengeWithDetails("c2", "Challenge 2", true)
            fakeDao.allChallenges = listOf(entity1, entity2)

            val challenges = repository.getAllChallenges()

            assertThat(challenges).hasSize(2)
            assertThat(challenges[0].id).isEqualTo("c1")
            assertThat(challenges[1].id).isEqualTo("c2")
        }

    @Test
    fun `getAllChallenges returns empty list when no challenges exist`() =
        runTest {
            fakeDao.allChallenges = emptyList()

            val challenges = repository.getAllChallenges()

            assertThat(challenges).isEmpty()
        }

    @Test
    fun `getChallengeById returns challenge when found`() =
        runTest {
            val entity = createChallengeWithDetails("c1", "Challenge 1", false)
            fakeDao.challengeById["c1"] = entity

            val challenge = repository.getChallengeById("c1")

            assertThat(challenge).isNotNull()
            assertThat(challenge?.id).isEqualTo("c1")
            assertThat(challenge?.title).isEqualTo("Challenge 1")
        }

    @Test
    fun `getChallengeById returns null when not found`() =
        runTest {
            val challenge = repository.getChallengeById("nonexistent")

            assertThat(challenge).isNull()
        }

    @Test
    fun `archiveChallenge calls DAO archive method`() =
        runTest {
            repository.archiveChallenge("c1")

            assertThat(fakeDao.archivedChallengeIds).contains("c1")
        }

    @Test
    fun `deleteChallenge calls DAO delete method`() =
        runTest {
            repository.deleteChallenge("c1")

            assertThat(fakeDao.deletedChallengeIds).contains("c1")
        }

    @Test
    fun `addPracticeSession inserts session correctly`() =
        runTest {
            val session =
                ChallengePracticeSession(
                    sessionId = "s1",
                    startTime = Instant.now(),
                    endTime = Instant.now(),
                    problemsAttempted = 10,
                    correctAnswers = 8,
                    totalTimeMs = 120000,
                )

            repository.addPracticeSession("c1", session)

            assertThat(fakeDao.insertedSessions).hasSize(1)
            assertThat(fakeDao.insertedSessions[0].sessionId).isEqualTo("s1")
            assertThat(fakeDao.insertedSessions[0].challengeId).isEqualTo("c1")
        }

    @Test
    fun `observeActiveChallenges emits challenges from DAO`() =
        runTest {
            val entity1 = createChallengeWithDetails("c1", "Active 1", false)
            val entity2 = createChallengeWithDetails("c2", "Active 2", false)
            fakeDao.activeChallenges.value = listOf(entity1, entity2)

            val challenges = repository.observeActiveChallenges().first()

            assertThat(challenges).hasSize(2)
            assertThat(challenges[0].id).isEqualTo("c1")
            assertThat(challenges[1].id).isEqualTo("c2")
            assertThat(challenges[0].isArchived).isFalse()
            assertThat(challenges[1].isArchived).isFalse()
        }

    @Test
    fun `observeActiveChallenges updates when data changes`() =
        runTest {
            val entity1 = createChallengeWithDetails("c1", "Challenge 1", false)
            fakeDao.activeChallenges.value = listOf(entity1)

            val flow = repository.observeActiveChallenges()
            val firstEmission = flow.first()
            assertThat(firstEmission).hasSize(1)

            // Update the flow
            val entity2 = createChallengeWithDetails("c2", "Challenge 2", false)
            fakeDao.activeChallenges.value = listOf(entity1, entity2)

            val secondEmission = flow.first()
            assertThat(secondEmission).hasSize(2)
        }

    @Test
    fun `saveChallenge with practice history inserts challenge only`() =
        runTest {
            val challenge =
                CustomChallenge(
                    id = "c1",
                    title = "Challenge with History",
                    subtitle = null,
                    type = ChallengeType.EXPLICIT,
                    problems = listOf(MathProblem(num1 = 1, num2 = 1, operation = MathOperation.ADDITION, correctAnswer = 2)),
                    createdAt = Instant.now(),
                    isArchived = false,
                    practiceHistory =
                        listOf(
                            ChallengePracticeSession(
                                sessionId = "s1",
                                startTime = Instant.now(),
                                endTime = Instant.now(),
                                problemsAttempted = 5,
                                correctAnswers = 4,
                                totalTimeMs = 60000,
                            ),
                        ),
                )

            repository.saveChallenge(challenge)

            assertThat(fakeDao.insertedChallenges).hasSize(1)
            assertThat(fakeDao.insertedProblems).hasSize(1)
            // Note: saveChallenge doesn't save practice history - that's done via addPracticeSession
            assertThat(fakeDao.insertedSessions).isEmpty()
        }

    @Test
    fun `getAllChallenges includes problems and sessions`() =
        runTest {
            val problems =
                listOf(
                    ChallengeProblemsEntity(
                        id = "p1",
                        challengeId = "c1",
                        operand1 = 5,
                        operand2 = 3,
                        operation = MathOperation.ADDITION,
                        answer = 8,
                        orderIndex = 0,
                    ),
                )
            val sessions =
                listOf(
                    ChallengePracticeSessionEntity(
                        sessionId = "s1",
                        challengeId = "c1",
                        startTime = Instant.now(),
                        endTime = Instant.now(),
                        problemsAttempted = 5,
                        correctAnswers = 4,
                        totalTimeMs = 60000,
                    ),
                )
            val entity = createChallengeWithDetails("c1", "Challenge", false, problems, sessions)
            fakeDao.allChallenges = listOf(entity)

            val challenges = repository.getAllChallenges()

            assertThat(challenges).hasSize(1)
            assertThat(challenges[0].problems).hasSize(1)
            assertThat(challenges[0].practiceHistory).hasSize(1)
        }

    private fun createChallengeWithDetails(
        id: String,
        title: String,
        isArchived: Boolean,
        problems: List<ChallengeProblemsEntity> = emptyList(),
        sessions: List<ChallengePracticeSessionEntity> = emptyList(),
    ): CustomChallengeWithDetails =
        CustomChallengeWithDetails(
            challenge =
                CustomChallengeEntity(
                    id = id,
                    title = title,
                    subtitle = null,
                    type = ChallengeType.EXPLICIT,
                    createdAt = Instant.now(),
                    isArchived = isArchived,
                ),
            problems = problems,
            sessions = sessions,
        )
}

/**
 * Fake implementation of CustomChallengeDao for testing.
 */
class FakeCustomChallengeDao : CustomChallengeDao {
    val insertedChallenges = mutableListOf<CustomChallengeEntity>()
    val insertedProblems = mutableListOf<ChallengeProblemsEntity>()
    val insertedSessions = mutableListOf<ChallengePracticeSessionEntity>()
    val archivedChallengeIds = mutableListOf<String>()
    val deletedChallengeIds = mutableListOf<String>()
    val activeChallenges = MutableStateFlow<List<CustomChallengeWithDetails>>(emptyList())
    var allChallenges: List<CustomChallengeWithDetails> = emptyList()
    val challengeById = mutableMapOf<String, CustomChallengeWithDetails>()

    override fun observeActiveChallenges(): Flow<List<CustomChallengeWithDetails>> = activeChallenges

    override fun getAllChallenges(): Flow<List<CustomChallengeWithDetails>> = activeChallenges.map { it.filter { detail -> !detail.challenge.isArchived } }

    override suspend fun getChallengeWithDetails(id: String): CustomChallengeWithDetails? = challengeById[id]

    override suspend fun getAllChallengesWithDetails(): List<CustomChallengeWithDetails> = allChallenges

    override suspend fun insertChallenge(challenge: CustomChallengeEntity) {
        insertedChallenges.add(challenge)
    }

    override suspend fun insertProblems(problems: List<ChallengeProblemsEntity>) {
        insertedProblems.addAll(problems)
    }

    override suspend fun insertPracticeSession(session: ChallengePracticeSessionEntity) {
        insertedSessions.add(session)
    }

    override suspend fun archiveChallenge(id: String) {
        archivedChallengeIds.add(id)
    }

    override suspend fun deleteChallenge(id: String) {
        deletedChallengeIds.add(id)
    }
}
