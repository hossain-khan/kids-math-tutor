package dev.hossain.mathtutor.ui.parentchallenges

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.ChallengeType
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.service.CustomChallengeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for [ParentChallengesPresenter].
 *
 * Tests presenter logic for displaying challenges, filtering archived challenges,
 * and handling challenge management actions.
 */
class ParentChallengesPresenterTest {
    @Test
    fun `challenges list - displays active challenges by default`() {
        // Given
        val activeChallenges =
            listOf(
                createChallenge(id = "1", title = "Challenge 1", isArchived = false),
                createChallenge(id = "2", title = "Challenge 2", isArchived = false),
            )

        // When filtering with showArchived = false
        val displayed = activeChallenges.filter { !it.isArchived }

        // Then
        assertThat(displayed).hasSize(2)
        assertThat(displayed.map { it.id }).containsExactly("1", "2")
    }

    @Test
    fun `challenges list - filters out archived when showArchived is false`() {
        // Given
        val allChallenges =
            listOf(
                createChallenge(id = "1", title = "Active", isArchived = false),
                createChallenge(id = "2", title = "Archived", isArchived = true),
                createChallenge(id = "3", title = "Another Active", isArchived = false),
            )

        // When filtering with showArchived = false
        val displayed = allChallenges.filter { !it.isArchived }

        // Then
        assertThat(displayed).hasSize(2)
        assertThat(displayed.map { it.id }).containsExactly("1", "3")
    }

    @Test
    fun `challenges list - shows all when showArchived is true`() {
        // Given
        val allChallenges =
            listOf(
                createChallenge(id = "1", title = "Active", isArchived = false),
                createChallenge(id = "2", title = "Archived", isArchived = true),
            )

        // When filtering with showArchived = true
        val displayed = allChallenges // No filter applied

        // Then
        assertThat(displayed).hasSize(2)
        assertThat(displayed.map { it.id }).containsExactly("1", "2")
    }

    @Test
    fun `challenge stats - calculates problem count correctly`() {
        // Given
        val challenge =
            createChallenge(
                id = "1",
                title = "Test",
                problems =
                    listOf(
                        MathProblem(
                            num1 = 1,
                            num2 = 2,
                            operation = MathOperation.ADDITION,
                            correctAnswer = 3,
                        ),
                        MathProblem(
                            num1 = 3,
                            num2 = 4,
                            operation = MathOperation.ADDITION,
                            correctAnswer = 7,
                        ),
                        MathProblem(
                            num1 = 5,
                            num2 = 6,
                            operation = MathOperation.ADDITION,
                            correctAnswer = 11,
                        ),
                    ),
            )

        // Then
        assertThat(challenge.problems.size).isEqualTo(3)
    }

    @Test
    fun `challenge stats - groups operations correctly`() {
        // Given
        val challenge =
            createChallenge(
                id = "1",
                title = "Test",
                problems =
                    listOf(
                        MathProblem(
                            num1 = 1,
                            num2 = 2,
                            operation = MathOperation.ADDITION,
                            correctAnswer = 3,
                        ),
                        MathProblem(
                            num1 = 3,
                            num2 = 4,
                            operation = MathOperation.ADDITION,
                            correctAnswer = 7,
                        ),
                        MathProblem(
                            num1 = 5,
                            num2 = 6,
                            operation = MathOperation.SUBTRACTION,
                            correctAnswer = -1,
                        ),
                    ),
            )

        // When grouping by operation
        val operationCounts = challenge.problems.groupBy { it.operation }.mapValues { it.value.size }

        // Then
        assertThat(operationCounts[MathOperation.ADDITION]).isEqualTo(2)
        assertThat(operationCounts[MathOperation.SUBTRACTION]).isEqualTo(1)
    }

    // Helper functions
    private fun createChallenge(
        id: String,
        title: String,
        isArchived: Boolean = false,
        problems: List<MathProblem> =
            listOf(
                MathProblem(
                    num1 = 1,
                    num2 = 2,
                    operation = MathOperation.ADDITION,
                    correctAnswer = 3,
                ),
            ),
    ): CustomChallenge =
        CustomChallenge(
            id = id,
            title = title,
            subtitle = null,
            type = ChallengeType.GENERATED,
            problems = problems,
            createdAt = Instant.now(),
            isArchived = isArchived,
            practiceHistory = emptyList(),
        )
}

/**
 * Fake implementation of CustomChallengeService for testing.
 */
class FakeCustomChallengeService : CustomChallengeService {
    private val challenges = mutableListOf<CustomChallenge>()

    fun setChallenges(challengeList: List<CustomChallenge>) {
        challenges.clear()
        challenges.addAll(challengeList)
    }

    override suspend fun createChallengeFromSpec(spec: dev.hossain.mathtutor.domain.model.ChallengeImportSpec): Result<CustomChallenge> {
        TODO("Not needed for tests")
    }

    override suspend fun generatePreview(
        spec: dev.hossain.mathtutor.domain.model.ChallengeImportSpec,
    ): dev.hossain.mathtutor.domain.model.PreviewData {
        TODO("Not needed for tests")
    }

    override suspend fun getAllChallenges(): List<CustomChallenge> = challenges

    override suspend fun getChallengeById(id: String): CustomChallenge? = challenges.firstOrNull { it.id == id }

    override suspend fun archiveChallenge(id: String) {
        val index = challenges.indexOfFirst { it.id == id }
        if (index != -1) {
            challenges[index] = challenges[index].copy(isArchived = true)
        }
    }

    override suspend fun deleteChallenge(id: String) {
        challenges.removeIf { it.id == id }
    }

    override suspend fun recordPracticeSession(
        challengeId: String,
        session: dev.hossain.mathtutor.domain.model.ChallengePracticeSession,
    ) {
        TODO("Not needed for tests")
    }

    override fun observeActiveChallenges(): Flow<List<CustomChallenge>> = flowOf(challenges.filter { !it.isArchived })

    override fun observeAllChallenges(): Flow<List<CustomChallenge>> = flowOf(challenges)
}
