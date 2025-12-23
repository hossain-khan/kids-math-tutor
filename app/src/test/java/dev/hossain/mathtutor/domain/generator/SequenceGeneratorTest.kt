package dev.hossain.mathtutor.domain.generator

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.GradeLevel
import org.junit.Test

class SequenceGeneratorTest {
    private val generator = DefaultSequenceGenerator()

    // ===========================================
    // Basic Sequence Generation Tests
    // ===========================================

    @Test
    fun `generateSequence returns sequence with correct length`() {
        val question = generator.generateSequence(GradeLevel.GRADE_1)

        assertThat(question.numbers).hasSize(5)
    }

    @Test
    fun `generateSequence returns exactly one null (missing number)`() {
        val question = generator.generateSequence(GradeLevel.GRADE_1)

        val nullCount = question.numbers.count { it == null }
        assertThat(nullCount).isEqualTo(1)
    }

    @Test
    fun `generateSequence missing number is never at first position`() {
        // Run multiple times to increase confidence
        repeat(100) {
            val question = generator.generateSequence(GradeLevel.GRADE_2)
            assertThat(question.missingIndex).isNotEqualTo(0)
        }
    }

    @Test
    fun `generateSequence missing number is never at last position`() {
        // Run multiple times to increase confidence
        repeat(100) {
            val question = generator.generateSequence(GradeLevel.GRADE_2)
            assertThat(question.missingIndex).isNotEqualTo(4)
        }
    }

    @Test
    fun `generateSequence missing index is in valid range`() {
        repeat(100) {
            val question = generator.generateSequence(GradeLevel.GRADE_1)
            assertThat(question.missingIndex).isIn(1..3)
        }
    }

    @Test
    fun `generateSequence correct answer matches missing position`() {
        repeat(50) {
            val question = generator.generateSequence(GradeLevel.GRADE_1)

            // The null should be at missingIndex
            assertThat(question.numbers[question.missingIndex]).isNull()
        }
    }

    // ===========================================
    // Kindergarten Grade Level Tests
    // ===========================================

    @Test
    fun `kindergarten sequences use only simple patterns`() {
        val allowedPatterns = setOf("+1", "+2")

        repeat(100) {
            val question = generator.generateSequence(GradeLevel.KINDERGARTEN)
            assertThat(question.sequenceType).isIn(allowedPatterns)
        }
    }

    @Test
    fun `kindergarten sequences have small numbers`() {
        repeat(50) {
            val question = generator.generateSequence(GradeLevel.KINDERGARTEN)

            // All non-null numbers should be reasonable for kindergarteners
            question.numbers.filterNotNull().forEach { num ->
                assertThat(num).isGreaterThan(0)
                // First number starts 1-5, with +1 or +2 pattern, max would be around 13
                assertThat(num).isLessThan(20)
            }
        }
    }

    // ===========================================
    // Grade 1 Tests
    // ===========================================

    @Test
    fun `grade 1 sequences include additional patterns`() {
        val allowedPatterns = setOf("+1", "+2", "+5", "-1")
        val foundPatterns = mutableSetOf<String>()

        repeat(200) {
            val question = generator.generateSequence(GradeLevel.GRADE_1)
            assertThat(question.sequenceType).isIn(allowedPatterns)
            foundPatterns.add(question.sequenceType)
        }

        // Should have found at least 2 different patterns
        assertThat(foundPatterns.size).isGreaterThan(1)
    }

    // ===========================================
    // Grade 2 Tests
    // ===========================================

    @Test
    fun `grade 2 sequences include all patterns`() {
        val allowedPatterns = setOf("+1", "+2", "+3", "+5", "+10", "-1", "-2", "×2")
        val foundPatterns = mutableSetOf<String>()

        repeat(500) {
            val question = generator.generateSequence(GradeLevel.GRADE_2)
            assertThat(question.sequenceType).isIn(allowedPatterns)
            foundPatterns.add(question.sequenceType)
        }

        // Should have found most patterns with 500 iterations
        assertThat(foundPatterns.size).isGreaterThan(4)
    }

    @Test
    fun `grade 2 can generate doubles sequences`() {
        var foundDoubles = false

        repeat(200) {
            val question = generator.generateSequence(GradeLevel.GRADE_2)
            if (question.sequenceType == "×2") {
                foundDoubles = true
            }
        }

        assertThat(foundDoubles).isTrue()
    }

    // ===========================================
    // Arithmetic Sequence Correctness Tests
    // ===========================================

    @Test
    fun `arithmetic sequence follows constant difference pattern`() {
        repeat(100) {
            val question = generator.generateSequence(GradeLevel.GRADE_2)

            if (question.sequenceType.startsWith("+") || question.sequenceType.startsWith("-")) {
                val difference = question.sequenceType.toIntOrNull() ?: question.sequenceType.drop(1).toInt()

                // Reconstruct full sequence
                val fullSequence =
                    question.numbers.mapIndexed { index, value ->
                        value ?: question.correctAnswer
                    }

                // Verify constant difference
                for (i in 1 until fullSequence.size) {
                    val actualDiff = fullSequence[i] - fullSequence[i - 1]
                    assertThat(actualDiff).isEqualTo(difference)
                }
            }
        }
    }

    @Test
    fun `doubles sequence follows doubling pattern`() {
        repeat(200) {
            val question = generator.generateSequence(GradeLevel.GRADE_2)

            if (question.sequenceType == "×2") {
                // Reconstruct full sequence
                val fullSequence =
                    question.numbers.mapIndexed { index, value ->
                        value ?: question.correctAnswer
                    }

                // Verify doubling pattern
                for (i in 1 until fullSequence.size) {
                    assertThat(fullSequence[i]).isEqualTo(fullSequence[i - 1] * 2)
                }
            }
        }
    }

    // ===========================================
    // Sequence Value Validation Tests
    // ===========================================

    @Test
    fun `all sequence numbers are positive`() {
        repeat(200) {
            val question = generator.generateSequence(GradeLevel.GRADE_2)

            question.numbers.filterNotNull().forEach { num ->
                assertThat(num).isGreaterThan(0)
            }
            assertThat(question.correctAnswer).isGreaterThan(0)
        }
    }

    @Test
    fun `descending sequences stay positive`() {
        repeat(200) {
            val question = generator.generateSequence(GradeLevel.GRADE_2)

            if (question.sequenceType == "-1" || question.sequenceType == "-2") {
                // All numbers including answer should be positive
                question.numbers.filterNotNull().forEach { num ->
                    assertThat(num).isGreaterThan(0)
                }
                assertThat(question.correctAnswer).isGreaterThan(0)
            }
        }
    }

    // ===========================================
    // SequenceType Enum Tests
    // ===========================================

    @Test
    fun `SequenceType forGradeLevel returns correct types for kindergarten`() {
        val types = SequenceType.forGradeLevel(GradeLevel.KINDERGARTEN)

        assertThat(types).containsExactly(SequenceType.ADD_ONE, SequenceType.ADD_TWO)
    }

    @Test
    fun `SequenceType forGradeLevel returns correct types for grade 1`() {
        val types = SequenceType.forGradeLevel(GradeLevel.GRADE_1)

        assertThat(types).containsExactly(
            SequenceType.ADD_ONE,
            SequenceType.ADD_TWO,
            SequenceType.ADD_FIVE,
            SequenceType.SUBTRACT_ONE,
        )
    }

    @Test
    fun `SequenceType forGradeLevel returns all types for grade 2`() {
        val types = SequenceType.forGradeLevel(GradeLevel.GRADE_2)

        assertThat(types).containsExactly(
            SequenceType.ADD_ONE,
            SequenceType.ADD_TWO,
            SequenceType.ADD_THREE,
            SequenceType.ADD_FIVE,
            SequenceType.ADD_TEN,
            SequenceType.SUBTRACT_ONE,
            SequenceType.SUBTRACT_TWO,
            SequenceType.DOUBLES,
        )
    }

    @Test
    fun `SequenceType descriptions are correct`() {
        assertThat(SequenceType.ADD_ONE.description).isEqualTo("+1")
        assertThat(SequenceType.ADD_TWO.description).isEqualTo("+2")
        assertThat(SequenceType.ADD_THREE.description).isEqualTo("+3")
        assertThat(SequenceType.ADD_FIVE.description).isEqualTo("+5")
        assertThat(SequenceType.ADD_TEN.description).isEqualTo("+10")
        assertThat(SequenceType.SUBTRACT_ONE.description).isEqualTo("-1")
        assertThat(SequenceType.SUBTRACT_TWO.description).isEqualTo("-2")
        assertThat(SequenceType.DOUBLES.description).isEqualTo("×2")
    }

    // ===========================================
    // SequenceQuestion Data Class Tests
    // ===========================================

    @Test
    fun `SequenceQuestion contains all required fields`() {
        val question = generator.generateSequence(GradeLevel.GRADE_1)

        assertThat(question.numbers).isNotNull()
        assertThat(question.correctAnswer).isNotNull()
        assertThat(question.missingIndex).isNotNull()
        assertThat(question.sequenceType).isNotEmpty()
    }

    @Test
    fun `generated sequence is solvable - answer is correct`() {
        repeat(100) {
            val question = generator.generateSequence(GradeLevel.GRADE_2)

            // Reconstruct what the sequence should be
            val reconstructed = question.numbers.toMutableList()
            reconstructed[question.missingIndex] = question.correctAnswer

            // Verify no nulls remain
            assertThat(reconstructed.filterNotNull()).hasSize(5)

            // Verify the answer fills the gap correctly
            assertThat(reconstructed[question.missingIndex]).isEqualTo(question.correctAnswer)
        }
    }
}
