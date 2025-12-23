package dev.hossain.mathtutor.domain.generator

import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.random.Random

/**
 * Represents a number sequence puzzle where the player must find the missing number.
 *
 * @property numbers The sequence of numbers, where null represents the missing number
 * @property correctAnswer The value of the missing number
 * @property missingIndex The index in the sequence where the number is missing
 * @property sequenceType Description of the pattern (e.g., "+2", "×2")
 */
data class SequenceQuestion(
    val numbers: List<Int?>,
    val correctAnswer: Int,
    val missingIndex: Int,
    val sequenceType: String,
)

/**
 * Type of sequence pattern.
 */
enum class SequenceType(
    val description: String,
) {
    ADD_ONE("+1"),
    ADD_TWO("+2"),
    ADD_THREE("+3"),
    ADD_FIVE("+5"),
    ADD_TEN("+10"),
    SUBTRACT_ONE("-1"),
    SUBTRACT_TWO("-2"),
    DOUBLES("×2"),
    ;

    companion object {
        /**
         * Returns the available sequence types for a given grade level.
         */
        fun forGradeLevel(gradeLevel: GradeLevel): List<SequenceType> =
            when (gradeLevel) {
                GradeLevel.KINDERGARTEN -> {
                    listOf(
                        ADD_ONE,
                        ADD_TWO,
                    )
                }

                GradeLevel.GRADE_1 -> {
                    listOf(
                        ADD_ONE,
                        ADD_TWO,
                        ADD_FIVE,
                        SUBTRACT_ONE,
                    )
                }

                GradeLevel.GRADE_2 -> {
                    listOf(
                        ADD_ONE,
                        ADD_TWO,
                        ADD_THREE,
                        ADD_FIVE,
                        ADD_TEN,
                        SUBTRACT_ONE,
                        SUBTRACT_TWO,
                        DOUBLES,
                    )
                }
            }
    }
}

/**
 * Interface for generating number sequence puzzles.
 */
interface SequenceGenerator {
    /**
     * Generates a sequence puzzle appropriate for the given grade level.
     *
     * @param gradeLevel The grade level to generate a sequence for
     * @return A SequenceQuestion with a missing number to find
     */
    fun generateSequence(gradeLevel: GradeLevel): SequenceQuestion
}

/**
 * Default implementation of [SequenceGenerator].
 * Generates arithmetic sequences with varying patterns based on grade level.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DefaultSequenceGenerator : SequenceGenerator {
    companion object {
        /** Number of elements in each sequence */
        private const val SEQUENCE_LENGTH = 5

        /** Minimum index for the missing number (never the first element) */
        private const val MIN_MISSING_INDEX = 1

        /** Maximum index for the missing number (never the last element) */
        private const val MAX_MISSING_INDEX = 3
    }

    override fun generateSequence(gradeLevel: GradeLevel): SequenceQuestion {
        val availableTypes = SequenceType.forGradeLevel(gradeLevel)
        val sequenceType = availableTypes.random()

        return when (sequenceType) {
            SequenceType.ADD_ONE -> generateArithmeticSequence(1, gradeLevel)
            SequenceType.ADD_TWO -> generateArithmeticSequence(2, gradeLevel)
            SequenceType.ADD_THREE -> generateArithmeticSequence(3, gradeLevel)
            SequenceType.ADD_FIVE -> generateArithmeticSequence(5, gradeLevel)
            SequenceType.ADD_TEN -> generateArithmeticSequence(10, gradeLevel)
            SequenceType.SUBTRACT_ONE -> generateArithmeticSequence(-1, gradeLevel)
            SequenceType.SUBTRACT_TWO -> generateArithmeticSequence(-2, gradeLevel)
            SequenceType.DOUBLES -> generateDoublesSequence(gradeLevel)
        }
    }

    /**
     * Generates an arithmetic sequence with a constant difference.
     *
     * @param difference The constant difference between consecutive terms
     * @param gradeLevel The grade level for determining starting range
     * @return A SequenceQuestion with arithmetic progression
     */
    private fun generateArithmeticSequence(
        difference: Int,
        gradeLevel: GradeLevel,
    ): SequenceQuestion {
        // Determine appropriate starting range based on grade level
        val (minStart, maxStart) =
            when (gradeLevel) {
                GradeLevel.KINDERGARTEN -> 1 to 5
                GradeLevel.GRADE_1 -> 1 to 10
                GradeLevel.GRADE_2 -> 1 to 20
            }

        // For descending sequences, we need a higher starting point
        val startingNumber =
            if (difference < 0) {
                // Start high enough so we don't go below 1
                val minRequired = 1 - (difference * (SEQUENCE_LENGTH - 1))
                Random.nextInt(minRequired.coerceAtLeast(minStart), maxStart * 2 + 1)
            } else {
                Random.nextInt(minStart, maxStart + 1)
            }

        // Generate the full sequence
        val sequence =
            (0 until SEQUENCE_LENGTH).map { index ->
                startingNumber + (difference * index)
            }

        // Choose which position to hide (never first or last)
        val missingIndex = Random.nextInt(MIN_MISSING_INDEX, MAX_MISSING_INDEX + 1)
        val correctAnswer = sequence[missingIndex]

        // Create sequence with null for missing number
        val numbersWithHidden =
            sequence.mapIndexed { index, number ->
                if (index == missingIndex) null else number
            }

        val description =
            if (difference >= 0) {
                "+$difference"
            } else {
                "$difference"
            }

        return SequenceQuestion(
            numbers = numbersWithHidden,
            correctAnswer = correctAnswer,
            missingIndex = missingIndex,
            sequenceType = description,
        )
    }

    /**
     * Generates a sequence where each number is double the previous.
     *
     * @param gradeLevel The grade level for determining starting range
     * @return A SequenceQuestion with doubling pattern
     */
    private fun generateDoublesSequence(gradeLevel: GradeLevel): SequenceQuestion {
        // Start with small numbers since doubling grows quickly
        val maxStart =
            when (gradeLevel) {
                GradeLevel.KINDERGARTEN -> 2
                GradeLevel.GRADE_1 -> 3
                GradeLevel.GRADE_2 -> 5
            }

        val startingNumber = Random.nextInt(1, maxStart + 1)

        // Generate the sequence (1, 2, 4, 8, 16 or similar)
        val sequence =
            (0 until SEQUENCE_LENGTH).map { index ->
                startingNumber * (1 shl index) // 2^index multiplication
            }

        // Choose which position to hide (never first or last)
        val missingIndex = Random.nextInt(MIN_MISSING_INDEX, MAX_MISSING_INDEX + 1)
        val correctAnswer = sequence[missingIndex]

        // Create sequence with null for missing number
        val numbersWithHidden =
            sequence.mapIndexed { index, number ->
                if (index == missingIndex) null else number
            }

        return SequenceQuestion(
            numbers = numbersWithHidden,
            correctAnswer = correctAnswer,
            missingIndex = missingIndex,
            sequenceType = "×2",
        )
    }
}
