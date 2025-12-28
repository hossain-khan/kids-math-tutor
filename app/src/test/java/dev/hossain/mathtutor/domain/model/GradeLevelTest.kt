package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GradeLevelTest {
    @Test
    fun `kindergarten addition range is 1 to 5`() {
        val range = GradeLevel.KINDERGARTEN.getNumberRange(MathOperation.ADDITION)
        assertThat(range).isEqualTo(1 to 5)
    }

    @Test
    fun `kindergarten subtraction range is 1 to 5`() {
        val range = GradeLevel.KINDERGARTEN.getNumberRange(MathOperation.SUBTRACTION)
        assertThat(range).isEqualTo(1 to 5)
    }

    @Test
    fun `kindergarten multiplication range is 1 to 2`() {
        val range = GradeLevel.KINDERGARTEN.getNumberRange(MathOperation.MULTIPLICATION)
        assertThat(range).isEqualTo(1 to 2)
    }

    @Test
    fun `kindergarten division range is 1 to 2`() {
        val range = GradeLevel.KINDERGARTEN.getNumberRange(MathOperation.DIVISION)
        assertThat(range).isEqualTo(1 to 2)
    }

    @Test
    fun `kindergarten mixed range is 1 to 5`() {
        val range = GradeLevel.KINDERGARTEN.getNumberRange(MathOperation.MIXED)
        assertThat(range).isEqualTo(1 to 5)
    }

    @Test
    fun `grade 1 addition range is 1 to 10`() {
        val range = GradeLevel.GRADE_1.getNumberRange(MathOperation.ADDITION)
        assertThat(range).isEqualTo(1 to 10)
    }

    @Test
    fun `grade 1 subtraction range is 1 to 10`() {
        val range = GradeLevel.GRADE_1.getNumberRange(MathOperation.SUBTRACTION)
        assertThat(range).isEqualTo(1 to 10)
    }

    @Test
    fun `grade 1 multiplication range is 1 to 5`() {
        val range = GradeLevel.GRADE_1.getNumberRange(MathOperation.MULTIPLICATION)
        assertThat(range).isEqualTo(1 to 5)
    }

    @Test
    fun `grade 1 division range is 1 to 5`() {
        val range = GradeLevel.GRADE_1.getNumberRange(MathOperation.DIVISION)
        assertThat(range).isEqualTo(1 to 5)
    }

    @Test
    fun `grade 1 mixed range is 1 to 10`() {
        val range = GradeLevel.GRADE_1.getNumberRange(MathOperation.MIXED)
        assertThat(range).isEqualTo(1 to 10)
    }

    @Test
    fun `grade 2 addition range is 1 to 20`() {
        val range = GradeLevel.GRADE_2.getNumberRange(MathOperation.ADDITION)
        assertThat(range).isEqualTo(1 to 20)
    }

    @Test
    fun `grade 2 subtraction range is 1 to 20`() {
        val range = GradeLevel.GRADE_2.getNumberRange(MathOperation.SUBTRACTION)
        assertThat(range).isEqualTo(1 to 20)
    }

    @Test
    fun `grade 2 multiplication range is 1 to 10`() {
        val range = GradeLevel.GRADE_2.getNumberRange(MathOperation.MULTIPLICATION)
        assertThat(range).isEqualTo(1 to 10)
    }

    @Test
    fun `grade 2 division range is 1 to 10`() {
        val range = GradeLevel.GRADE_2.getNumberRange(MathOperation.DIVISION)
        assertThat(range).isEqualTo(1 to 10)
    }

    @Test
    fun `grade 2 mixed range is 1 to 20`() {
        val range = GradeLevel.GRADE_2.getNumberRange(MathOperation.MIXED)
        assertThat(range).isEqualTo(1 to 20)
    }

    @Test
    fun `display names are correct`() {
        assertThat(GradeLevel.KINDERGARTEN.displayName).isEqualTo("Kindergarten")
        assertThat(GradeLevel.GRADE_1.displayName).isEqualTo("Grade 1")
        assertThat(GradeLevel.GRADE_2.displayName).isEqualTo("Grade 2")
    }

    @Test
    fun `kindergarten available operations are correct`() {
        val operations = GradeLevel.KINDERGARTEN.getAvailableOperations()
        assertThat(operations).containsExactly(
            MathOperation.ADDITION,
            MathOperation.SUBTRACTION,
            MathOperation.MIXED,
        )
    }

    @Test
    fun `kindergarten does not have multiplication`() {
        val operations = GradeLevel.KINDERGARTEN.getAvailableOperations()
        assertThat(operations).doesNotContain(MathOperation.MULTIPLICATION)
    }

    @Test
    fun `kindergarten does not have division`() {
        val operations = GradeLevel.KINDERGARTEN.getAvailableOperations()
        assertThat(operations).doesNotContain(MathOperation.DIVISION)
    }

    @Test
    fun `grade 1 available operations are correct`() {
        val operations = GradeLevel.GRADE_1.getAvailableOperations()
        assertThat(operations).containsExactly(
            MathOperation.ADDITION,
            MathOperation.SUBTRACTION,
            MathOperation.MULTIPLICATION,
            MathOperation.MIXED,
        )
    }

    @Test
    fun `grade 1 has multiplication but not division`() {
        val operations = GradeLevel.GRADE_1.getAvailableOperations()
        assertThat(operations).contains(MathOperation.MULTIPLICATION)
        assertThat(operations).doesNotContain(MathOperation.DIVISION)
    }

    @Test
    fun `grade 2 available operations are correct`() {
        val operations = GradeLevel.GRADE_2.getAvailableOperations()
        assertThat(operations).containsExactly(
            MathOperation.ADDITION,
            MathOperation.SUBTRACTION,
            MathOperation.MULTIPLICATION,
            MathOperation.DIVISION,
            MathOperation.MIXED,
        )
    }

    @Test
    fun `grade 2 has all operations`() {
        val operations = GradeLevel.GRADE_2.getAvailableOperations()
        assertThat(operations).contains(MathOperation.ADDITION)
        assertThat(operations).contains(MathOperation.SUBTRACTION)
        assertThat(operations).contains(MathOperation.MULTIPLICATION)
        assertThat(operations).contains(MathOperation.DIVISION)
        assertThat(operations).contains(MathOperation.MIXED)
    }

    @Test
    fun `all grades have addition and subtraction`() {
        for (grade in GradeLevel.values()) {
            val operations = grade.getAvailableOperations()
            assertThat(operations).contains(MathOperation.ADDITION)
            assertThat(operations).contains(MathOperation.SUBTRACTION)
        }
    }

    @Test
    fun `all grades have mixed operation`() {
        for (grade in GradeLevel.values()) {
            val operations = grade.getAvailableOperations()
            assertThat(operations).contains(MathOperation.MIXED)
        }
    }
}
