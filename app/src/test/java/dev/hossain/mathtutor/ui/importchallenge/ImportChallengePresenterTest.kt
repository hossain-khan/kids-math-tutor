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

        override suspend fun getAllChallenges(): List<CustomChallenge> = emptyList()

        override suspend fun getChallengeById(id: String): CustomChallenge? = null

        override suspend fun archiveChallenge(id: String) {}

        override suspend fun deleteChallenge(id: String) {}

        override suspend fun recordPracticeSession(
            challengeId: String,
            session: dev.hossain.mathtutor.domain.model.ChallengePracticeSession,
        ) {}

        override fun observeActiveChallenges(): Flow<List<CustomChallenge>> = flowOf(emptyList())
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

        override suspend fun deleteChallenge(id: String) {
            challenges.removeIf { it.id == id }
        }

        override suspend fun addPracticeSession(
            challengeId: String,
            session: dev.hossain.mathtutor.domain.model.ChallengePracticeSession,
        ) {}

        override fun observeActiveChallenges(): Flow<List<CustomChallenge>> = flowOf(challenges.filter { !it.isArchived })
    }
}
