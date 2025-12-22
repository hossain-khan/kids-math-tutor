package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChallengeImportSpecTest {
    @Test
    fun `Generated spec contains all required properties`() {
        val numberRange = NumberRange(min = 1, max = 10)
        val spec =
            ChallengeImportSpec.Generated(
                title = "Addition Practice",
                subtitle = "Focus on carrying over",
                operation = MathOperation.ADDITION,
                problemCount = 10,
                numberRange = numberRange,
            )

        assertThat(spec.title).isEqualTo("Addition Practice")
        assertThat(spec.subtitle).isEqualTo("Focus on carrying over")
        assertThat(spec.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(spec.problemCount).isEqualTo(10)
        assertThat(spec.numberRange).isEqualTo(numberRange)
    }

    @Test
    fun `Generated spec allows null subtitle`() {
        val spec =
            ChallengeImportSpec.Generated(
                title = "Multiplication Drills",
                subtitle = null,
                operation = MathOperation.MULTIPLICATION,
                problemCount = 15,
                numberRange = NumberRange(min = 1, max = 12),
            )

        assertThat(spec.subtitle).isNull()
    }

    @Test
    fun `Explicit spec contains all required properties`() {
        val problems =
            listOf(
                ProblemSpec(operand1 = 15, operand2 = 3, operation = MathOperation.DIVISION),
                ProblemSpec(operand1 = 20, operand2 = 4, operation = MathOperation.DIVISION),
            )
        val spec =
            ChallengeImportSpec.Explicit(
                title = "Emma's Challenges",
                subtitle = "Mixed practice problems",
                problems = problems,
            )

        assertThat(spec.title).isEqualTo("Emma's Challenges")
        assertThat(spec.subtitle).isEqualTo("Mixed practice problems")
        assertThat(spec.problems).hasSize(2)
        assertThat(spec.problems).isEqualTo(problems)
    }

    @Test
    fun `Explicit spec allows null subtitle`() {
        val spec =
            ChallengeImportSpec.Explicit(
                title = "Quick Practice",
                subtitle = null,
                problems = emptyList(),
            )

        assertThat(spec.subtitle).isNull()
    }

    @Test
    fun `Explicit spec can have empty problems list`() {
        val spec =
            ChallengeImportSpec.Explicit(
                title = "Empty Challenge",
                subtitle = null,
                problems = emptyList(),
            )

        assertThat(spec.problems).isEmpty()
    }

    @Test
    fun `Generated and Explicit are distinct types`() {
        val generated =
            ChallengeImportSpec.Generated(
                title = "Test",
                subtitle = null,
                operation = MathOperation.ADDITION,
                problemCount = 5,
                numberRange = NumberRange(1, 10),
            )
        val explicit =
            ChallengeImportSpec.Explicit(
                title = "Test",
                subtitle = null,
                problems = emptyList(),
            )

        assertThat(generated).isInstanceOf(ChallengeImportSpec.Generated::class.java)
        assertThat(explicit).isInstanceOf(ChallengeImportSpec.Explicit::class.java)
        assertThat(generated).isNotEqualTo(explicit)
    }

    @Test
    fun `Generated spec supports all math operations`() {
        val operations =
            listOf(
                MathOperation.ADDITION,
                MathOperation.SUBTRACTION,
                MathOperation.MULTIPLICATION,
                MathOperation.DIVISION,
            )

        operations.forEach { operation ->
            val spec =
                ChallengeImportSpec.Generated(
                    title = "Test",
                    subtitle = null,
                    operation = operation,
                    problemCount = 5,
                    numberRange = NumberRange(1, 10),
                )

            assertThat(spec.operation).isEqualTo(operation)
        }
    }
}
