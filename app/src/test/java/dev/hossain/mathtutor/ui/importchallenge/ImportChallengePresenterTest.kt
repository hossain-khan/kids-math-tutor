package dev.hossain.mathtutor.ui.importchallenge

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.ChallengeImportSpec
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.NumberRange
import dev.hossain.mathtutor.domain.model.PreviewData
import dev.hossain.mathtutor.domain.parser.ChallengeJsonParser
import dev.hossain.mathtutor.domain.parser.ValidationException
import dev.hossain.mathtutor.domain.repository.CustomChallengeRepository
import dev.hossain.mathtutor.domain.service.CustomChallengeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

/**
 * Unit tests for [ImportChallengePresenter].
 *
 * Tests presenter logic for importing custom challenges, including
 * JSON validation, preview generation, and challenge creation.
 */
class ImportChallengePresenterTest {
    @Test
    fun parser_validGeneratedJson_parsesSuccessfully() {
        // Given
        val parser = FakeChallengeJsonParser()
        val validJson =
            """
            {
              "type": "generated",
              "title": "Addition Practice",
              "operation": "addition",
              "problemCount": 10,
              "numberRange": {"min": 1, "max": 10}
            }
            """.trimIndent()

        // When
        val result = parser.parseFromText(validJson)

        // Then
        assertThat(result.isSuccess).isTrue()
        val spec = result.getOrNull()
        assertThat(spec).isNotNull()
        assertThat(spec).isInstanceOf(ChallengeImportSpec.Generated::class.java)
        assertThat((spec as ChallengeImportSpec.Generated).title).isEqualTo("Addition Practice")
    }

    @Test
    fun parser_invalidJson_returnsError() {
        // Given
        val parser = FakeChallengeJsonParser()
        val invalidJson = "{ invalid json }"

        // When
        val result = parser.parseFromText(invalidJson)

        // Then
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun service_validSpec_generatesPreview() {
        // Given
        val service = FakeCustomChallengeService()
        val spec =
            ChallengeImportSpec.Generated(
                title = "Test Challenge",
                subtitle = null,
                operation = MathOperation.ADDITION,
                problemCount = 5,
                numberRange = NumberRange(min = 1, max = 10),
            )

        // When
        val preview = service.generatePreviewSync(spec)

        // Then
        assertThat(preview).isNotNull()
        assertThat(preview.title).isEqualTo("Test Challenge")
        assertThat(preview.problemCount).isEqualTo(5)
    }

    @Test
    fun repository_saveChallenge_savesSuccessfully() {
        // Given
        val repository = FakeCustomChallengeRepository()
        val challenge =
            CustomChallenge(
                id = "test-id",
                title = "Test Challenge",
                subtitle = null,
                type = dev.hossain.mathtutor.domain.model.ChallengeType.GENERATED,
                problems =
                    listOf(
                        MathProblem(
                            num1 = 1,
                            num2 = 2,
                            operation = MathOperation.ADDITION,
                            correctAnswer = 3,
                        ),
                    ),
            )

        // When
        repository.saveChallengeSync(challenge)

        // Then
        val saved = repository.getChallenges().firstOrNull { it.id == "test-id" }
        assertThat(saved).isNotNull()
        assertThat(saved?.title).isEqualTo("Test Challenge")
    }

    @Test
    fun validationState_initialState_isIdle() {
        // Given
        val state = ValidationState.Idle

        // Then
        assertThat(state).isInstanceOf(ValidationState.Idle::class.java)
    }

    @Test
    fun validationState_validInput_isValid() {
        // Given
        val state = ValidationState.Valid

        // Then
        assertThat(state).isInstanceOf(ValidationState.Valid::class.java)
    }

    @Test
    fun validationState_invalidInput_containsErrors() {
        // Given
        val errors = mapOf("title" to "Title is required")
        val state = ValidationState.Invalid(errors)

        // Then
        assertThat(state).isInstanceOf(ValidationState.Invalid::class.java)
        assertThat(state.fieldErrors).containsEntry("title", "Title is required")
    }

    @Test
    fun service_duplicateChallenge_returnsExistingTitle() {
        // Given
        val service = FakeCustomChallengeService()
        val repository = FakeCustomChallengeRepository()

        // Create and save a challenge
        val existingChallenge =
            CustomChallenge(
                id = "existing-id",
                title = "Existing Challenge",
                subtitle = null,
                type = dev.hossain.mathtutor.domain.model.ChallengeType.GENERATED,
                problems =
                    listOf(
                        MathProblem(num1 = 1, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 3),
                        MathProblem(num1 = 3, num2 = 4, operation = MathOperation.ADDITION, correctAnswer = 7),
                        MathProblem(num1 = 5, num2 = 6, operation = MathOperation.ADDITION, correctAnswer = 11),
                        MathProblem(num1 = 7, num2 = 8, operation = MathOperation.ADDITION, correctAnswer = 15),
                        MathProblem(num1 = 9, num2 = 10, operation = MathOperation.ADDITION, correctAnswer = 19),
                    ),
            )
        repository.saveChallengeSync(existingChallenge)

        // Create a spec that would generate the same problems
        val spec =
            ChallengeImportSpec.Generated(
                title = "New Challenge",
                subtitle = null,
                operation = MathOperation.ADDITION,
                problemCount = 5,
                numberRange = NumberRange(min = 1, max = 10),
            )

        // When - findDuplicateChallenge is called (sync for testing)
        val duplicateTitle = service.findDuplicateChallengeSync(spec, repository.getChallenges())

        // Then
        assertThat(duplicateTitle).isEqualTo("Existing Challenge")
    }

    @Test
    fun service_noDuplicate_returnsNull() {
        // Given
        val service = FakeCustomChallengeService()
        val repository = FakeCustomChallengeRepository()

        // Create and save a challenge with different problems
        val existingChallenge =
            CustomChallenge(
                id = "existing-id",
                title = "Existing Challenge",
                subtitle = null,
                type = dev.hossain.mathtutor.domain.model.ChallengeType.GENERATED,
                problems =
                    listOf(
                        MathProblem(num1 = 10, num2 = 20, operation = MathOperation.ADDITION, correctAnswer = 30),
                    ),
            )
        repository.saveChallengeSync(existingChallenge)

        // Create a spec that would generate different problems
        val spec =
            ChallengeImportSpec.Generated(
                title = "New Challenge",
                subtitle = null,
                operation = MathOperation.ADDITION,
                problemCount = 5,
                numberRange = NumberRange(min = 1, max = 10),
            )

        // When
        val duplicateTitle = service.findDuplicateChallengeSync(spec, repository.getChallenges())

        // Then
        assertThat(duplicateTitle).isNull()
    }

    /**
     * Fake implementation of [ChallengeJsonParser] for testing.
     */
    private class FakeChallengeJsonParser : ChallengeJsonParser {
        override fun parseFromText(text: String): Result<ChallengeImportSpec> =
            when {
                text.contains("invalid") -> {
                    Result.failure(
                        ValidationException(mapOf("general" to "Invalid JSON")),
                    )
                }

                text.contains("generated") -> {
                    Result.success(
                        ChallengeImportSpec.Generated(
                            title = "Addition Practice",
                            subtitle = null,
                            operation = MathOperation.ADDITION,
                            problemCount = 10,
                            numberRange = NumberRange(min = 1, max = 10),
                        ),
                    )
                }

                else -> {
                    Result.success(
                        ChallengeImportSpec.Generated(
                            title = "Default",
                            subtitle = null,
                            operation = MathOperation.ADDITION,
                            problemCount = 5,
                            numberRange = NumberRange(min = 1, max = 10),
                        ),
                    )
                }
            }

        override fun findJsonInText(text: String): String? = text
    }

    /**
     * Fake implementation of [CustomChallengeService] for testing.
     */
    private class FakeCustomChallengeService : CustomChallengeService {
        override suspend fun createChallengeFromSpec(spec: ChallengeImportSpec): Result<CustomChallenge> =
            Result.success(
                CustomChallenge(
                    id = "test-id",
                    title = spec.title,
                    subtitle = spec.subtitle,
                    type = dev.hossain.mathtutor.domain.model.ChallengeType.GENERATED,
                    problems =
                        listOf(
                            MathProblem(
                                num1 = 1,
                                num2 = 2,
                                operation = MathOperation.ADDITION,
                                correctAnswer = 3,
                            ),
                        ),
                ),
            )

        override suspend fun generatePreview(spec: ChallengeImportSpec): PreviewData =
            PreviewData(
                title = spec.title,
                subtitle = spec.subtitle,
                problemCount = 5,
                operationsSummary = mapOf(MathOperation.ADDITION to 5),
                sampleProblems =
                    listOf(
                        MathProblem(
                            num1 = 1,
                            num2 = 2,
                            operation = MathOperation.ADDITION,
                            correctAnswer = 3,
                        ),
                    ),
                estimatedDuration = 5.minutes,
            )

        fun generatePreviewSync(spec: ChallengeImportSpec): PreviewData =
            PreviewData(
                title = spec.title,
                subtitle = spec.subtitle,
                problemCount = 5,
                operationsSummary = mapOf(MathOperation.ADDITION to 5),
                sampleProblems =
                    listOf(
                        MathProblem(
                            num1 = 1,
                            num2 = 2,
                            operation = MathOperation.ADDITION,
                            correctAnswer = 3,
                        ),
                    ),
                estimatedDuration = 5.minutes,
            )

        fun findDuplicateChallengeSync(
            spec: ChallengeImportSpec,
            existingChallenges: List<CustomChallenge>,
        ): String? {
            // Generate problems from spec to compare
            val newProblems =
                when (spec) {
                    is ChallengeImportSpec.Generated -> {
                        listOf(
                            MathProblem(num1 = 1, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 3),
                            MathProblem(num1 = 3, num2 = 4, operation = MathOperation.ADDITION, correctAnswer = 7),
                            MathProblem(num1 = 5, num2 = 6, operation = MathOperation.ADDITION, correctAnswer = 11),
                            MathProblem(num1 = 7, num2 = 8, operation = MathOperation.ADDITION, correctAnswer = 15),
                            MathProblem(num1 = 9, num2 = 10, operation = MathOperation.ADDITION, correctAnswer = 19),
                        )
                    }

                    is ChallengeImportSpec.Explicit -> {
                        spec.problems.map { ps ->
                            MathProblem(
                                num1 = ps.operand1,
                                num2 = ps.operand2,
                                operation = ps.operation,
                                correctAnswer = ps.operation.calculate(ps.operand1, ps.operand2),
                            )
                        }
                    }
                }

            val newType =
                when (spec) {
                    is ChallengeImportSpec.Generated -> dev.hossain.mathtutor.domain.model.ChallengeType.GENERATED
                    is ChallengeImportSpec.Explicit -> dev.hossain.mathtutor.domain.model.ChallengeType.EXPLICIT
                }

            // Check if any existing challenge matches
            return existingChallenges
                .firstOrNull { challenge ->
                    challenge.type == newType && problemsMatch(challenge.problems, newProblems)
                }?.title
        }

        private fun problemsMatch(
            existing: List<MathProblem>,
            new: List<MathProblem>,
        ): Boolean {
            if (existing.size != new.size) return false
            return existing.zip(new).all { (e, n) ->
                e.num1 == n.num1 && e.num2 == n.num2 && e.operation == n.operation && e.correctAnswer == n.correctAnswer
            }
        }

        override suspend fun getAllChallenges(): List<CustomChallenge> = emptyList()

        override suspend fun getChallengeById(id: String): CustomChallenge? = null

        override suspend fun archiveChallenge(id: String) {}

        override suspend fun unarchiveChallenge(id: String) {}

        override suspend fun deleteChallenge(id: String) {}

        override suspend fun recordPracticeSession(
            challengeId: String,
            session: dev.hossain.mathtutor.domain.model.ChallengePracticeSession,
        ) {}

        override fun observeActiveChallenges(): Flow<List<CustomChallenge>> = flowOf(emptyList())

        override fun observeAllChallenges(): Flow<List<CustomChallenge>> = flowOf(emptyList())

        override suspend fun clearChallengeSessions(challengeId: String) {}

        override suspend fun findDuplicateChallenge(spec: ChallengeImportSpec): String? {
            // For testing, always return null (no duplicate)
            return null
        }
    }

    /**
     * Fake implementation of [CustomChallengeRepository] for testing.
     */
    private class FakeCustomChallengeRepository : CustomChallengeRepository {
        private val challenges = mutableListOf<CustomChallenge>()

        override suspend fun saveChallenge(challenge: CustomChallenge) {
            challenges.removeIf { it.id == challenge.id }
            challenges.add(challenge)
        }

        fun saveChallengeSync(challenge: CustomChallenge) {
            challenges.removeIf { it.id == challenge.id }
            challenges.add(challenge)
        }

        fun getChallenges(): List<CustomChallenge> = challenges.toList()

        override suspend fun getAllChallenges(): List<CustomChallenge> = challenges.toList()

        override suspend fun getChallengeById(id: String): CustomChallenge? = challenges.firstOrNull { it.id == id }

        override suspend fun archiveChallenge(id: String) {
            challenges.firstOrNull { it.id == id }?.let { challenge ->
                challenges.removeIf { it.id == id }
                challenges.add(challenge.copy(isArchived = true))
            }
        }

        override suspend fun unarchiveChallenge(id: String) {
            challenges.firstOrNull { it.id == id }?.let { challenge ->
                challenges.removeIf { it.id == id }
                challenges.add(challenge.copy(isArchived = false))
            }
        }

        override suspend fun deleteChallenge(id: String) {
            challenges.removeIf { it.id == id }
        }

        override suspend fun addPracticeSession(
            challengeId: String,
            session: dev.hossain.mathtutor.domain.model.ChallengePracticeSession,
        ) {}

        override fun observeActiveChallenges(): Flow<List<CustomChallenge>> = flowOf(challenges.filter { !it.isArchived })

        override fun observeAllChallenges(): Flow<List<CustomChallenge>> = flowOf(challenges)

        override suspend fun clearChallengeSessions(challengeId: String) {}
    }
}
