package dev.hossain.mathtutor.ui.operationselector

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Test

/**
 * Unit tests for [OperationSelectorScreen] and grade-level aware operation selection.
 *
 * Tests that:
 * - Grade level is correctly included in state
 * - Available operations are correct for each grade
 * - Session history tracking works
 */
class OperationSelectorPresenterTest {
    @Test
    fun `presenter state includes grade level`() {
        // Given
        val gradeLevel = GradeLevel.GRADE_2

        // When
        val state =
            OperationSelectorScreen.State(
                gradeLevel = gradeLevel,
                hasSessionHistory = false,
                eventSink = {},
            )

        // Then
        assertThat(state.gradeLevel).isEqualTo(GradeLevel.GRADE_2)
    }

    @Test
    fun `state has correct grade levels for each profile`() {
        // Test that state can be created with different grade levels
        for (grade in GradeLevel.values()) {
            val state =
                OperationSelectorScreen.State(
                    gradeLevel = grade,
                    hasSessionHistory = false,
                    eventSink = {},
                )
            assertThat(state.gradeLevel).isEqualTo(grade)
        }
    }

    @Test
    fun `kindergarten state has correct operations`() {
        // Given
        val state =
            OperationSelectorScreen.State(
                gradeLevel = GradeLevel.KINDERGARTEN,
                hasSessionHistory = false,
                eventSink = {},
            )

        // When
        val operations = state.gradeLevel.getAvailableOperations()

        // Then
        assertThat(operations).contains(MathOperation.ADDITION)
        assertThat(operations).contains(MathOperation.SUBTRACTION)
        assertThat(operations).doesNotContain(MathOperation.MULTIPLICATION)
        assertThat(operations).doesNotContain(MathOperation.DIVISION)
    }

    @Test
    fun `grade 1 state has multiplication but not division`() {
        // Given
        val state =
            OperationSelectorScreen.State(
                gradeLevel = GradeLevel.GRADE_1,
                hasSessionHistory = false,
                eventSink = {},
            )

        // When
        val operations = state.gradeLevel.getAvailableOperations()

        // Then
        assertThat(operations).contains(MathOperation.MULTIPLICATION)
        assertThat(operations).doesNotContain(MathOperation.DIVISION)
    }

    @Test
    fun `grade 2 state has both multiplication and division`() {
        // Given
        val state =
            OperationSelectorScreen.State(
                gradeLevel = GradeLevel.GRADE_2,
                hasSessionHistory = false,
                eventSink = {},
            )

        // When
        val operations = state.gradeLevel.getAvailableOperations()

        // Then
        assertThat(operations).contains(MathOperation.MULTIPLICATION)
        assertThat(operations).contains(MathOperation.DIVISION)
    }

    @Test
    fun `state tracks session history correctly`() {
        // Given - no session history
        val stateNoHistory =
            OperationSelectorScreen.State(
                gradeLevel = GradeLevel.GRADE_2,
                hasSessionHistory = false,
                eventSink = {},
            )

        // When & Then
        assertThat(stateNoHistory.hasSessionHistory).isFalse()

        // Given - with session history
        val stateWithHistory =
            OperationSelectorScreen.State(
                gradeLevel = GradeLevel.GRADE_2,
                hasSessionHistory = true,
                eventSink = {},
            )

        // When & Then
        assertThat(stateWithHistory.hasSessionHistory).isTrue()
    }

    @Test
    fun `event sink is callable for operation selected`() {
        // Given
        var eventReceived: OperationSelectorScreen.Event? = null
        val state =
            OperationSelectorScreen.State(
                gradeLevel = GradeLevel.GRADE_2,
                hasSessionHistory = false,
                eventSink = { event -> eventReceived = event },
            )

        // When
        state.eventSink(
            OperationSelectorScreen.Event.OperationSelected(MathOperation.DIVISION),
        )

        // Then
        assertThat(eventReceived).isNotNull()
        assertThat(eventReceived).isInstanceOf(
            OperationSelectorScreen.Event.OperationSelected::class.java,
        )
        assertThat(
            (eventReceived as OperationSelectorScreen.Event.OperationSelected).operation,
        ).isEqualTo(MathOperation.DIVISION)
    }

    @Test
    fun `grade 2 has all five operations`() {
        // Given
        val state =
            OperationSelectorScreen.State(
                gradeLevel = GradeLevel.GRADE_2,
                hasSessionHistory = false,
                eventSink = {},
            )

        // When
        val operations = state.gradeLevel.getAvailableOperations()

        // Then - should have exactly 5 operations
        assertThat(operations.size).isEqualTo(5)
        assertThat(operations).containsExactly(
            MathOperation.ADDITION,
            MathOperation.SUBTRACTION,
            MathOperation.MULTIPLICATION,
            MathOperation.DIVISION,
            MathOperation.MIXED,
        )
    }

    @Test
    fun `kindergarten has three operations`() {
        // Given
        val state =
            OperationSelectorScreen.State(
                gradeLevel = GradeLevel.KINDERGARTEN,
                hasSessionHistory = false,
                eventSink = {},
            )

        // When
        val operations = state.gradeLevel.getAvailableOperations()

        // Then - should have exactly 3 operations
        assertThat(operations.size).isEqualTo(3)
    }

    @Test
    fun `grade 1 has four operations`() {
        // Given
        val state =
            OperationSelectorScreen.State(
                gradeLevel = GradeLevel.GRADE_1,
                hasSessionHistory = false,
                eventSink = {},
            )

        // When
        val operations = state.gradeLevel.getAvailableOperations()

        // Then - should have exactly 4 operations
        assertThat(operations.size).isEqualTo(4)
    }
}
