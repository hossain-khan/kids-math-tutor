package dev.hossain.mathtutor.domain.service

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.generator.ProblemGenerator
import dev.hossain.mathtutor.domain.model.ChallengeImportSpec
import dev.hossain.mathtutor.domain.model.ChallengePracticeSession
import dev.hossain.mathtutor.domain.model.ChallengeType
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.NumberRange
import dev.hossain.mathtutor.domain.model.ProblemSpec
import dev.hossain.mathtutor.domain.repository.CustomChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant

class CustomChallengeServiceImplTest {
    private lateinit var fakeRepository: FakeCustomChallengeRepository
    private lateinit var fakeProblemGenerator: FakeProblemGenerator
    private lateinit var service: CustomChallengeServiceImpl

    @Before
    fun setup() {
        fakeRepository = FakeCustomChallengeRepository()
        fakeProblemGenerator = FakeProblemGenerator()
        service = CustomChallengeServiceImpl(fakeRepository, fakeProblemGenerator)
    }

    // ==================== Generated Challenge Tests ====================

    @Test
    fun `createChallengeFromSpec creates generated challenge successfully`() =
        runTest {
            val spec =
                ChallengeImportSpec.Generated(
                    title = "Addition Practice",
                    subtitle = "Focus on carrying over",
                    operation = MathOperation.ADDITION,
                    problemCount = 10,
                    numberRange = NumberRange(10, 99),
                )

            val result = service.createChallengeFromSpec(spec)

            assertThat(result.isSuccess).isTrue()
            val challenge = result.getOrNull()
            assertThat(challenge).isNotNull()
            assertThat(challenge?.title).isEqualTo("Addition Practice")
            assertThat(challenge?.subtitle).isEqualTo("Focus on carrying over")
            assertThat(challenge?.type).isEqualTo(ChallengeType.GENERATED)
            assertThat(challenge?.problems).isNotEmpty()
            assertThat(fakeRepository.savedChallenges).hasSize(1)
        }

    @Test
    fun `createChallengeFromSpec generates problems with correct operation`() =
        runTest {
            val spec =
                ChallengeImportSpec.Generated(
                    title = "Multiplication",
                    subtitle = null,
                    operation = MathOperation.MULTIPLICATION,
                    problemCount = 5,
                    numberRange = NumberRange(2, 12),
                )

            val result = service.createChallengeFromSpec(spec)

            assertThat(result.isSuccess).isTrue()
            assertThat(fakeProblemGenerator.generatedProblems).isNotEmpty()
            assertThat(fakeProblemGenerator.lastOperation).isEqualTo(MathOperation.MULTIPLICATION)
            assertThat(fakeProblemGenerator.lastGradeLevel).isEqualTo(GradeLevel.GRADE_2)
        }

    @Test
    fun `createChallengeFromSpec fails with empty problem set`() =
        runTest {
            // Configure fake generator to return empty list
            fakeProblemGenerator.returnEmptyList = true

            val spec =
                ChallengeImportSpec.Generated(
                    title = "Empty Challenge",
                    subtitle = null,
                    operation = MathOperation.ADDITION,
                    problemCount = 10,
                    numberRange = NumberRange(1, 10),
                )

            val result = service.createChallengeFromSpec(spec)

            assertThat(result.isFailure).isTrue()
            assertThat(fakeRepository.savedChallenges).isEmpty()
        }

    // ==================== Explicit Challenge Tests ====================

    @Test
    fun `createChallengeFromSpec creates explicit challenge successfully`() =
        runTest {
            val spec =
                ChallengeImportSpec.Explicit(
                    title = "Emma's Challenges",
                    subtitle = "Mixed practice problems",
                    problems =
                        listOf(
                            ProblemSpec(15, 3, MathOperation.DIVISION),
                            ProblemSpec(8, 4, MathOperation.ADDITION),
                            ProblemSpec(20, 5, MathOperation.SUBTRACTION),
                        ),
                )

            val result = service.createChallengeFromSpec(spec)

            assertThat(result.isSuccess).isTrue()
            val challenge = result.getOrNull()
            assertThat(challenge).isNotNull()
            assertThat(challenge?.title).isEqualTo("Emma's Challenges")
            assertThat(challenge?.type).isEqualTo(ChallengeType.EXPLICIT)
            assertThat(challenge?.problems).hasSize(3)
            assertThat(fakeRepository.savedChallenges).hasSize(1)
        }

    @Test
    fun `createChallengeFromSpec filters division with non-whole results`() =
        runTest {
            val spec =
                ChallengeImportSpec.Explicit(
                    title = "Division Test",
                    subtitle = null,
                    problems =
                        listOf(
                            ProblemSpec(10, 2, MathOperation.DIVISION), // Valid: 10 ÷ 2 = 5
                            ProblemSpec(10, 3, MathOperation.DIVISION), // Invalid: 10 ÷ 3 = 3.333...
                            ProblemSpec(15, 5, MathOperation.DIVISION), // Valid: 15 ÷ 5 = 3
                            ProblemSpec(7, 2, MathOperation.DIVISION), // Invalid: 7 ÷ 2 = 3.5
                        ),
                )

            val result = service.createChallengeFromSpec(spec)

            assertThat(result.isSuccess).isTrue()
            val challenge = result.getOrNull()
            assertThat(challenge?.problems).hasSize(2)
            assertThat(challenge?.problems?.get(0)?.num1).isEqualTo(10)
            assertThat(challenge?.problems?.get(0)?.num2).isEqualTo(2)
            assertThat(challenge?.problems?.get(1)?.num1).isEqualTo(15)
            assertThat(challenge?.problems?.get(1)?.num2).isEqualTo(5)
        }

    @Test
    fun `createChallengeFromSpec filters division by zero`() =
        runTest {
            val spec =
                ChallengeImportSpec.Explicit(
                    title = "Division by Zero Test",
                    subtitle = null,
                    problems =
                        listOf(
                            ProblemSpec(10, 0, MathOperation.DIVISION), // Invalid: division by zero
                            ProblemSpec(10, 2, MathOperation.DIVISION), // Valid
                        ),
                )

            val result = service.createChallengeFromSpec(spec)

            assertThat(result.isSuccess).isTrue()
            val challenge = result.getOrNull()
            assertThat(challenge?.problems).hasSize(1)
            assertThat(challenge?.problems?.get(0)?.num2).isEqualTo(2)
        }

    @Test
    fun `createChallengeFromSpec handles integer overflow for addition`() =
        runTest {
            val spec =
                ChallengeImportSpec.Explicit(
                    title = "Overflow Test",
                    subtitle = null,
                    problems =
                        listOf(
                            ProblemSpec(Int.MAX_VALUE, 1, MathOperation.ADDITION), // Overflow
                            ProblemSpec(10, 20, MathOperation.ADDITION), // Valid
                        ),
                )

            val result = service.createChallengeFromSpec(spec)

            assertThat(result.isSuccess).isTrue()
            val challenge = result.getOrNull()
            // Only the valid problem should be included
            assertThat(challenge?.problems).hasSize(1)
            assertThat(challenge?.problems?.get(0)?.num1).isEqualTo(10)
        }

    @Test
    fun `createChallengeFromSpec handles integer overflow for multiplication`() =
        runTest {
            val spec =
                ChallengeImportSpec.Explicit(
                    title = "Overflow Test",
                    subtitle = null,
                    problems =
                        listOf(
                            ProblemSpec(Int.MAX_VALUE, 2, MathOperation.MULTIPLICATION), // Overflow
                            ProblemSpec(10, 20, MathOperation.MULTIPLICATION), // Valid
                        ),
                )

            val result = service.createChallengeFromSpec(spec)

            assertThat(result.isSuccess).isTrue()
            val challenge = result.getOrNull()
            // Only the valid problem should be included
            assertThat(challenge?.problems).hasSize(1)
            assertThat(challenge?.problems?.get(0)?.num1).isEqualTo(10)
        }

    @Test
    fun `createChallengeFromSpec preserves problem order for explicit challenges`() =
        runTest {
            val spec =
                ChallengeImportSpec.Explicit(
                    title = "Order Test",
                    subtitle = null,
                    problems =
                        listOf(
                            ProblemSpec(5, 3, MathOperation.ADDITION),
                            ProblemSpec(10, 2, MathOperation.SUBTRACTION),
                            ProblemSpec(6, 4, MathOperation.MULTIPLICATION),
                        ),
                )

            val result = service.createChallengeFromSpec(spec)

            assertThat(result.isSuccess).isTrue()
            val challenge = result.getOrNull()
            assertThat(challenge?.problems).hasSize(3)
            assertThat(challenge?.problems?.get(0)?.operation).isEqualTo(MathOperation.ADDITION)
            assertThat(challenge?.problems?.get(1)?.operation).isEqualTo(MathOperation.SUBTRACTION)
            assertThat(challenge?.problems?.get(2)?.operation).isEqualTo(MathOperation.MULTIPLICATION)
        }

    // ==================== Preview Tests ====================

    @Test
    fun `generatePreview returns preview with sample problems`() =
        runTest {
            val spec =
                ChallengeImportSpec.Generated(
                    title = "Preview Test",
                    subtitle = "Test subtitle",
                    operation = MathOperation.ADDITION,
                    problemCount = 20,
                    numberRange = NumberRange(1, 10),
                )

            val preview = service.generatePreview(spec)

            assertThat(preview.title).isEqualTo("Preview Test")
            assertThat(preview.subtitle).isEqualTo("Test subtitle")
            assertThat(preview.problemCount).isGreaterThan(0)
            assertThat(preview.sampleProblems).isNotEmpty()
            assertThat(preview.sampleProblems)
                .hasSize(CustomChallengeServiceImpl.PREVIEW_SAMPLE_SIZE)
            assertThat(preview.operationsSummary).isNotEmpty()
            assertThat(preview.estimatedDuration.inWholeSeconds).isGreaterThan(0)
        }

    @Test
    fun `generatePreview calculates operations summary correctly`() =
        runTest {
            val spec =
                ChallengeImportSpec.Explicit(
                    title = "Mixed Operations",
                    subtitle = null,
                    problems =
                        listOf(
                            ProblemSpec(5, 3, MathOperation.ADDITION),
                            ProblemSpec(10, 2, MathOperation.ADDITION),
                            ProblemSpec(8, 3, MathOperation.SUBTRACTION),
                            ProblemSpec(6, 2, MathOperation.MULTIPLICATION),
                        ),
                )

            val preview = service.generatePreview(spec)

            assertThat(preview.operationsSummary[MathOperation.ADDITION]).isEqualTo(2)
            assertThat(preview.operationsSummary[MathOperation.SUBTRACTION]).isEqualTo(1)
            assertThat(preview.operationsSummary[MathOperation.MULTIPLICATION]).isEqualTo(1)
        }

    @Test
    fun `generatePreview estimates duration based on operation complexity`() =
        runTest {
            val additionSpec =
                ChallengeImportSpec.Explicit(
                    title = "Addition Only",
                    subtitle = null,
                    problems = List(10) { ProblemSpec(5, 3, MathOperation.ADDITION) },
                )

            val divisionSpec =
                ChallengeImportSpec.Explicit(
                    title = "Division Only",
                    subtitle = null,
                    problems = List(10) { ProblemSpec(10, 2, MathOperation.DIVISION) },
                )

            val additionPreview = service.generatePreview(additionSpec)
            val divisionPreview = service.generatePreview(divisionSpec)

            // Division should take longer than addition
            assertThat(divisionPreview.estimatedDuration).isGreaterThan(additionPreview.estimatedDuration)
        }

    // ==================== CRUD Operation Tests ====================

    @Test
    fun `getAllChallenges returns all challenges from repository`() =
        runTest {
            val challenge1 =
                CustomChallenge(
                    id = "c1",
                    title = "Challenge 1",
                    subtitle = null,
                    type = ChallengeType.GENERATED,
                    problems = emptyList(),
                )
            val challenge2 =
                CustomChallenge(
                    id = "c2",
                    title = "Challenge 2",
                    subtitle = null,
                    type = ChallengeType.EXPLICIT,
                    problems = emptyList(),
                )
            fakeRepository.savedChallenges.addAll(listOf(challenge1, challenge2))

            val challenges = service.getAllChallenges()

            assertThat(challenges).hasSize(2)
            assertThat(challenges[0].id).isEqualTo("c1")
            assertThat(challenges[1].id).isEqualTo("c2")
        }

    @Test
    fun `getChallengeById returns challenge when found`() =
        runTest {
            val challenge =
                CustomChallenge(
                    id = "c1",
                    title = "Test Challenge",
                    subtitle = null,
                    type = ChallengeType.GENERATED,
                    problems = emptyList(),
                )
            fakeRepository.savedChallenges.add(challenge)

            val result = service.getChallengeById("c1")

            assertThat(result).isNotNull()
            assertThat(result?.id).isEqualTo("c1")
        }

    @Test
    fun `getChallengeById returns null when not found`() =
        runTest {
            val result = service.getChallengeById("nonexistent")

            assertThat(result).isNull()
        }

    @Test
    fun `archiveChallenge calls repository archive method`() =
        runTest {
            service.archiveChallenge("c1")

            assertThat(fakeRepository.archivedChallengeIds).contains("c1")
        }

    @Test
    fun `unarchiveChallenge calls repository unarchive method`() =
        runTest {
            service.unarchiveChallenge("c1")

            assertThat(fakeRepository.unarchivedChallengeIds).contains("c1")
        }

    @Test
    fun `deleteChallenge calls repository delete method`() =
        runTest {
            service.deleteChallenge("c1")

            assertThat(fakeRepository.deletedChallengeIds).contains("c1")
        }

    @Test
    fun `recordPracticeSession adds session to repository`() =
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

            service.recordPracticeSession("c1", session)

            assertThat(fakeRepository.practiceSessions).hasSize(1)
            assertThat(fakeRepository.practiceSessions[0].first).isEqualTo("c1")
            assertThat(fakeRepository.practiceSessions[0].second).isEqualTo(session)
        }

    @Test
    fun `observeActiveChallenges returns flow from repository`() =
        runTest {
            val challenge =
                CustomChallenge(
                    id = "c1",
                    title = "Active Challenge",
                    subtitle = null,
                    type = ChallengeType.GENERATED,
                    problems = emptyList(),
                    isArchived = false,
                )
            fakeRepository.activeChallengesFlow.value = listOf(challenge)

            val challenges = service.observeActiveChallenges().first()

            assertThat(challenges).hasSize(1)
            assertThat(challenges[0].id).isEqualTo("c1")
            assertThat(challenges[0].isArchived).isFalse()
        }
}

/**
 * Fake implementation of CustomChallengeRepository for testing.
 */
class FakeCustomChallengeRepository : CustomChallengeRepository {
    val savedChallenges = mutableListOf<CustomChallenge>()
    val archivedChallengeIds = mutableListOf<String>()
    val unarchivedChallengeIds = mutableListOf<String>()
    val deletedChallengeIds = mutableListOf<String>()
    val practiceSessions = mutableListOf<Pair<String, ChallengePracticeSession>>()
    val activeChallengesFlow = MutableStateFlow<List<CustomChallenge>>(emptyList())

    override suspend fun saveChallenge(challenge: CustomChallenge) {
        savedChallenges.add(challenge)
    }

    override suspend fun getAllChallenges(): List<CustomChallenge> = savedChallenges

    override suspend fun getChallengeById(id: String): CustomChallenge? = savedChallenges.find { it.id == id }

    override suspend fun archiveChallenge(id: String) {
        archivedChallengeIds.add(id)
    }

    override suspend fun unarchiveChallenge(id: String) {
        unarchivedChallengeIds.add(id)
    }

    override suspend fun deleteChallenge(id: String) {
        deletedChallengeIds.add(id)
    }

    override suspend fun addPracticeSession(
        challengeId: String,
        session: ChallengePracticeSession,
    ) {
        practiceSessions.add(challengeId to session)
    }

    override fun observeActiveChallenges(): Flow<List<CustomChallenge>> = activeChallengesFlow

    override fun observeAllChallenges(): Flow<List<CustomChallenge>> = activeChallengesFlow

    override suspend fun clearChallengeSessions(challengeId: String) {
        practiceSessions.removeAll { it.first == challengeId }
    }
}

/**
 * Fake implementation of ProblemGenerator for testing.
 */
class FakeProblemGenerator : ProblemGenerator {
    var returnEmptyList = false
    val generatedProblems = mutableListOf<MathProblem>()
    var lastOperation: MathOperation? = null
    var lastGradeLevel: GradeLevel? = null

    override fun generateProblems(
        count: Int,
        operation: MathOperation,
        gradeLevel: GradeLevel,
    ): List<MathProblem> {
        lastOperation = operation
        lastGradeLevel = gradeLevel

        if (returnEmptyList) {
            return emptyList()
        }

        val problems =
            (1..count).map { index ->
                MathProblem(
                    id = "problem_$index",
                    num1 = 5 + index,
                    num2 = 3,
                    operation = operation,
                    correctAnswer =
                        when (operation) {
                            MathOperation.ADDITION -> 5 + index + 3
                            MathOperation.SUBTRACTION -> 5 + index - 3
                            MathOperation.MULTIPLICATION -> (5 + index) * 3
                            MathOperation.DIVISION -> (5 + index) / 3
                            MathOperation.MIXED -> 5 + index + 3
                        },
                )
            }

        generatedProblems.addAll(problems)
        return problems
    }
}
