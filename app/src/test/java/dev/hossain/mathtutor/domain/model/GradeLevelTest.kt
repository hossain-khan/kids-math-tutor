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
}
