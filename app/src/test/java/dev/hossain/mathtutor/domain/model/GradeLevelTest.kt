package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GradeLevelTest {
    @Test
    fun `kindergarten addition range is 1 to 5`() {
        val range = GradeLevel.KINDERGARTEN.getNumberRange(MathOperation.ADDITION)
        assertEquals(1 to 5, range)
    }

    @Test
    fun `kindergarten subtraction range is 1 to 5`() {
        val range = GradeLevel.KINDERGARTEN.getNumberRange(MathOperation.SUBTRACTION)
        assertEquals(1 to 5, range)
    }

    @Test
    fun `kindergarten multiplication range is 1 to 2`() {
        val range = GradeLevel.KINDERGARTEN.getNumberRange(MathOperation.MULTIPLICATION)
        assertEquals(1 to 2, range)
    }

    @Test
    fun `kindergarten division range is 1 to 2`() {
        val range = GradeLevel.KINDERGARTEN.getNumberRange(MathOperation.DIVISION)
        assertEquals(1 to 2, range)
    }

    @Test
    fun `kindergarten mixed range is 1 to 5`() {
        val range = GradeLevel.KINDERGARTEN.getNumberRange(MathOperation.MIXED)
        assertEquals(1 to 5, range)
    }

    @Test
    fun `grade 1 addition range is 1 to 10`() {
        val range = GradeLevel.GRADE_1.getNumberRange(MathOperation.ADDITION)
        assertEquals(1 to 10, range)
    }

    @Test
    fun `grade 1 subtraction range is 1 to 10`() {
        val range = GradeLevel.GRADE_1.getNumberRange(MathOperation.SUBTRACTION)
        assertEquals(1 to 10, range)
    }

    @Test
    fun `grade 1 multiplication range is 1 to 5`() {
        val range = GradeLevel.GRADE_1.getNumberRange(MathOperation.MULTIPLICATION)
        assertEquals(1 to 5, range)
    }

    @Test
    fun `grade 1 division range is 1 to 5`() {
        val range = GradeLevel.GRADE_1.getNumberRange(MathOperation.DIVISION)
        assertEquals(1 to 5, range)
    }

    @Test
    fun `grade 1 mixed range is 1 to 10`() {
        val range = GradeLevel.GRADE_1.getNumberRange(MathOperation.MIXED)
        assertEquals(1 to 10, range)
    }

    @Test
    fun `grade 2 addition range is 1 to 20`() {
        val range = GradeLevel.GRADE_2.getNumberRange(MathOperation.ADDITION)
        assertEquals(1 to 20, range)
    }

    @Test
    fun `grade 2 subtraction range is 1 to 20`() {
        val range = GradeLevel.GRADE_2.getNumberRange(MathOperation.SUBTRACTION)
        assertEquals(1 to 20, range)
    }

    @Test
    fun `grade 2 multiplication range is 1 to 10`() {
        val range = GradeLevel.GRADE_2.getNumberRange(MathOperation.MULTIPLICATION)
        assertEquals(1 to 10, range)
    }

    @Test
    fun `grade 2 division range is 1 to 10`() {
        val range = GradeLevel.GRADE_2.getNumberRange(MathOperation.DIVISION)
        assertEquals(1 to 10, range)
    }

    @Test
    fun `grade 2 mixed range is 1 to 20`() {
        val range = GradeLevel.GRADE_2.getNumberRange(MathOperation.MIXED)
        assertEquals(1 to 20, range)
    }

    @Test
    fun `display names are correct`() {
        assertEquals("Kindergarten", GradeLevel.KINDERGARTEN.displayName)
        assertEquals("Grade 1", GradeLevel.GRADE_1.displayName)
        assertEquals("Grade 2", GradeLevel.GRADE_2.displayName)
    }
}
