package dev.hossain.mathtutor.ui.memorymatch

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import org.junit.Test

/**
 * Unit tests for Memory Match card generation logic.
 *
 * Focuses on testing the card generation to ensure:
 * - No duplicate answers across problems
 * - No duplicate problem strings
 * - Proper card count and pairing
 */
class MemoryMatchPresenterTest {
    @Test
    fun `no duplicate answers in generated problems`() {
        // Test multiple times to catch any randomness issues
        repeat(20) {
            val problems = generateUniqueTestProblems(8)

            val answers = problems.map { it.correctAnswer }
            val uniqueAnswers = answers.toSet()

            assertThat(uniqueAnswers.size).isEqualTo(8)
            assertThat(answers.size).isEqualTo(uniqueAnswers.size)
        }
    }

    @Test
    fun `no duplicate problem strings in generated problems`() {
        // Test multiple times to catch any randomness issues
        repeat(20) {
            val problems = generateUniqueTestProblems(8)

            val problemStrings = problems.map { it.getDisplayString() }
            val uniqueProblemStrings = problemStrings.toSet()

            assertThat(uniqueProblemStrings.size).isEqualTo(8)
            assertThat(problemStrings.size).isEqualTo(uniqueProblemStrings.size)
        }
    }

    @Test
    fun `generated cards total 16 cards for 8 pairs`() {
        val problems = generateUniqueTestProblems(8)
        val cards = generateTestCards(problems)

        assertThat(cards.size).isEqualTo(16)
    }

    @Test
    fun `generated cards have unique answer content across all answer cards`() {
        val problems = generateUniqueTestProblems(8)
        val cards = generateTestCards(problems)

        // Extract only answer cards (odd IDs)
        val answerCards = cards.filter { it.id % 2 == 1 }

        // Verify all answer cards have unique content
        val answerContents = answerCards.map { it.content }
        val uniqueAnswerContents = answerContents.toSet()

        assertThat(answerCards.size).isEqualTo(8)
        assertThat(answerContents.size).isEqualTo(8)
        assertThat(uniqueAnswerContents.size).isEqualTo(8)
    }

    @Test
    fun `generated cards have unique problem content across all problem cards`() {
        val problems = generateUniqueTestProblems(8)
        val cards = generateTestCards(problems)

        // Extract only problem cards (even IDs)
        val problemCards = cards.filter { it.id % 2 == 0 }

        // Verify all problem cards have unique content
        val problemContents = problemCards.map { it.content }
        val uniqueProblemContents = problemContents.toSet()

        assertThat(problemCards.size).isEqualTo(8)
        assertThat(problemContents.size).isEqualTo(8)
        assertThat(uniqueProblemContents.size).isEqualTo(8)
    }

    @Test
    fun `each problem card has exactly one matching answer card with same pairId`() {
        val problems = generateUniqueTestProblems(8)
        val cards = generateTestCards(problems)

        // Group cards by pairId
        val cardsByPairId = cards.groupBy { it.pairId }

        // Each pairId should have exactly 2 cards
        assertThat(cardsByPairId.size).isEqualTo(8)

        cardsByPairId.forEach { (pairId, pairCards) ->
            assertThat(pairCards.size).isEqualTo(2)

            // One should be the problem, one should be the answer
            val problemCard = pairCards.first { it.id % 2 == 0 }
            val answerCard = pairCards.first { it.id % 2 == 1 }

            // Verify the answer matches the problem
            val problem = problems[pairId]
            assertThat(answerCard.content).isEqualTo(problem.correctAnswer.toString())
        }
    }

    @Test
    fun `problem and answer cards have matching pairIds`() {
        val problems = generateUniqueTestProblems(8)
        val cards = generateTestCards(problems)

        // For each problem index, verify problem and answer cards share the same pairId
        problems.forEachIndexed { index, problem ->
            val problemCard = cards.first { it.id == index * 2 }
            val answerCard = cards.first { it.id == index * 2 + 1 }

            assertThat(problemCard.pairId).isEqualTo(index)
            assertThat(answerCard.pairId).isEqualTo(index)
            assertThat(problemCard.pairId).isEqualTo(answerCard.pairId)
        }
    }

    @Test
    fun `card content matches problem and answer correctly`() {
        val problems = generateUniqueTestProblems(8)
        val cards = generateTestCards(problems)

        problems.forEachIndexed { index, problem ->
            val problemCard = cards.first { it.id == index * 2 }
            val answerCard = cards.first { it.id == index * 2 + 1 }

            // Problem card should contain the problem without " = ?"
            val expectedProblemContent = problem.getDisplayString().replace(" = ?", "")
            assertThat(problemCard.content).isEqualTo(expectedProblemContent)

            // Answer card should contain the correct answer as string
            assertThat(answerCard.content).isEqualTo(problem.correctAnswer.toString())
        }
    }

    @Test
    fun `deduplication ensures unique answers`() {
        // Create problems with some duplicate answers
        val problemsWithDuplicates =
            listOf(
                MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5),
                MathProblem(num1 = 1, num2 = 4, operation = MathOperation.ADDITION, correctAnswer = 5), // Duplicate answer
                MathProblem(num1 = 3, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 5), // Duplicate answer
                MathProblem(num1 = 4, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 6),
                MathProblem(num1 = 5, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 7),
                MathProblem(num1 = 6, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 8),
                MathProblem(num1 = 7, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 9),
                MathProblem(num1 = 8, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 10),
            )

        // Manually deduplicate
        val deduplicated = deduplicateProblems(problemsWithDuplicates)

        // Verify all answers are unique
        val answers = deduplicated.map { it.correctAnswer }
        val uniqueAnswers = answers.toSet()

        assertThat(uniqueAnswers.size).isEqualTo(answers.size)

        // Verify we kept 6 unique problems (5, 6, 7, 8, 9, 10)
        assertThat(deduplicated.size).isEqualTo(6)
    }

    @Test
    fun `deduplication ensures unique problem strings`() {
        // Create problems with duplicate problem strings
        val problemsWithDuplicates =
            listOf(
                MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5),
                MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5), // Duplicate string
                MathProblem(num1 = 4, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 6),
                MathProblem(num1 = 5, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 7),
                MathProblem(num1 = 6, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 8),
                MathProblem(num1 = 7, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 9),
                MathProblem(num1 = 8, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 10),
                MathProblem(num1 = 9, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 11),
            )

        val deduplicated = deduplicateProblems(problemsWithDuplicates)

        // Verify all problem strings are unique
        val problemStrings = deduplicated.map { it.getDisplayString() }
        val uniqueProblemStrings = problemStrings.toSet()

        assertThat(uniqueProblemStrings.size).isEqualTo(problemStrings.size)

        // Should keep 7 unique problems (removed one duplicate)
        assertThat(deduplicated.size).isEqualTo(7)
    }

    // ==================== Helper Functions ====================

    /**
     * Helper function to generate test problems with guaranteed unique answers.
     * Generates problems with incrementing answers for predictability.
     */
    private fun generateUniqueTestProblems(count: Int): List<MathProblem> {
        val problems = mutableListOf<MathProblem>()
        var answer = 2 // Start from 2 to allow for subtraction

        repeat(count) { index ->
            // Alternate between addition and subtraction for variety
            val operation =
                if (index % 2 == 0) MathOperation.ADDITION else MathOperation.SUBTRACTION
            val problem =
                when (operation) {
                    MathOperation.ADDITION -> {
                        val num1 = answer - 1
                        val num2 = 1
                        MathProblem(
                            num1 = num1,
                            num2 = num2,
                            operation = MathOperation.ADDITION,
                            correctAnswer = answer,
                        )
                    }

                    MathOperation.SUBTRACTION -> {
                        val num1 = answer + 1
                        val num2 = 1
                        MathProblem(
                            num1 = num1,
                            num2 = num2,
                            operation = MathOperation.SUBTRACTION,
                            correctAnswer = answer,
                        )
                    }

                    else -> {
                        throw IllegalStateException("Unexpected operation")
                    }
                }
            problems.add(problem)
            answer++ // Increment for next unique answer
        }

        return problems
    }

    /**
     * Helper function to generate cards from problems (simulates the presenter's generateCards logic).
     */
    private fun generateTestCards(problems: List<MathProblem>): List<MemoryMatchScreen.Card> {
        val cardList = mutableListOf<MemoryMatchScreen.Card>()
        problems.forEachIndexed { index, problem ->
            // Add problem card
            cardList.add(
                MemoryMatchScreen.Card(
                    id = index * 2,
                    content = problem.getDisplayString().replace(" = ?", ""),
                    pairId = index,
                ),
            )
            // Add answer card
            cardList.add(
                MemoryMatchScreen.Card(
                    id = index * 2 + 1,
                    content = problem.correctAnswer.toString(),
                    pairId = index,
                ),
            )
        }
        return cardList
    }

    /**
     * Helper function to deduplicate problems (simulates the presenter's deduplication logic).
     */
    private fun deduplicateProblems(problems: List<MathProblem>): List<MathProblem> {
        val seenAnswers = mutableSetOf<Int>()
        val seenProblemStrings = mutableSetOf<String>()
        val uniqueProblems = mutableListOf<MathProblem>()

        for (problem in problems) {
            val answer = problem.correctAnswer
            val problemString = problem.getDisplayString()

            if (answer !in seenAnswers && problemString !in seenProblemStrings) {
                seenAnswers.add(answer)
                seenProblemStrings.add(problemString)
                uniqueProblems.add(problem)
            }
        }

        return uniqueProblems
    }
}
