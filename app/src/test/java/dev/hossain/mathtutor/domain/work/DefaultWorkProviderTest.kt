package dev.hossain.mathtutor.domain.work

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DefaultWorkProvider].
 *
 * CRITICAL: These tests verify educational content shown to children.
 * All math calculations, step descriptions, and instructional content must be accurate.
 */
class DefaultWorkProviderTest {
    private lateinit var workProvider: DefaultWorkProvider

    @Before
    fun setup() {
        workProvider = DefaultWorkProvider()
    }

    // ============== ADDITION TESTS ==============

    @Test
    fun `addition breakdown has correct number of steps`() {
        val problem = MathProblem(num1 = 8, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 13)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
    }

    @Test
    fun `addition breakdown has correct emojis in order`() {
        val problem = MathProblem(num1 = 8, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 13)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[0].emoji).isEqualTo("🎯")
        assertThat(steps[1].emoji).isEqualTo("➕")
        assertThat(steps[2].emoji).isEqualTo("💡")
        assertThat(steps[3].emoji).isEqualTo("✅")
    }

    @Test
    fun `addition breakdown step 1 mentions starting number`() {
        val problem = MathProblem(num1 = 8, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 13)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[0].description).contains("8")
        assertThat(steps[0].description.lowercase()).contains("start")
    }

    @Test
    fun `addition breakdown step 2 mentions adding second number`() {
        val problem = MathProblem(num1 = 8, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 13)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[1].description).contains("5")
        assertThat(steps[1].description.lowercase()).contains("add")
    }

    @Test
    fun `addition breakdown step 3 shows count on strategy`() {
        val problem = MathProblem(num1 = 8, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 13)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[2].description.lowercase()).contains("count")
        assertThat(steps[2].description).contains("8")
        assertThat(steps[2].description).contains("9")
    }

    @Test
    fun `addition breakdown step 4 shows complete equation and answer`() {
        val problem = MathProblem(num1 = 8, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 13)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[3].description).contains("8")
        assertThat(steps[3].description).contains("5")
        assertThat(steps[3].description).contains("13")
    }

    @Test
    fun `addition with zero produces correct steps`() {
        val problem = MathProblem(num1 = 0, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 5)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
        assertThat(steps[0].description).contains("0")
        assertThat(steps[1].description).contains("5")
        assertThat(steps[3].description).contains("5")
    }

    @Test
    fun `addition with one produces correct steps`() {
        val problem = MathProblem(num1 = 1, num2 = 1, operation = MathOperation.ADDITION, correctAnswer = 2)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
        assertThat(steps[3].description).contains("2")
    }

    @Test
    fun `addition with large numbers produces correct steps`() {
        val problem = MathProblem(num1 = 87, num2 = 45, operation = MathOperation.ADDITION, correctAnswer = 132)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
        assertThat(steps[0].description).contains("87")
        assertThat(steps[1].description).contains("45")
        assertThat(steps[3].description).contains("132")
    }

    // ============== SUBTRACTION TESTS ==============

    @Test
    fun `subtraction breakdown has correct number of steps`() {
        val problem = MathProblem(num1 = 13, num2 = 3, operation = MathOperation.SUBTRACTION, correctAnswer = 10)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
    }

    @Test
    fun `subtraction breakdown has correct emojis in order`() {
        val problem = MathProblem(num1 = 13, num2 = 3, operation = MathOperation.SUBTRACTION, correctAnswer = 10)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[0].emoji).isEqualTo("🎯")
        assertThat(steps[1].emoji).isEqualTo("➖")
        assertThat(steps[2].emoji).isEqualTo("💭")
        assertThat(steps[3].emoji).isEqualTo("✅")
    }

    @Test
    fun `subtraction breakdown step 1 mentions starting number`() {
        val problem = MathProblem(num1 = 13, num2 = 3, operation = MathOperation.SUBTRACTION, correctAnswer = 10)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[0].description).contains("13")
        assertThat(steps[0].description.lowercase()).contains("start")
    }

    @Test
    fun `subtraction breakdown step 2 mentions taking away`() {
        val problem = MathProblem(num1 = 13, num2 = 3, operation = MathOperation.SUBTRACTION, correctAnswer = 10)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[1].description).contains("3")
        assertThat(steps[1].description.lowercase()).contains("take")
    }

    @Test
    fun `subtraction breakdown step 3 shows count back strategy`() {
        val problem = MathProblem(num1 = 13, num2 = 3, operation = MathOperation.SUBTRACTION, correctAnswer = 10)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[2].description.lowercase()).contains("count")
        assertThat(steps[2].description).contains("13")
        assertThat(steps[2].description).contains("12")
    }

    @Test
    fun `subtraction breakdown step 4 shows complete equation and answer`() {
        val problem = MathProblem(num1 = 13, num2 = 3, operation = MathOperation.SUBTRACTION, correctAnswer = 10)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[3].description).contains("13")
        assertThat(steps[3].description).contains("3")
        assertThat(steps[3].description).contains("10")
    }

    @Test
    fun `subtraction with zero produces correct steps`() {
        val problem = MathProblem(num1 = 5, num2 = 0, operation = MathOperation.SUBTRACTION, correctAnswer = 5)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
        assertThat(steps[0].description).contains("5")
        assertThat(steps[1].description).contains("0")
        assertThat(steps[3].description).contains("5")
    }

    @Test
    fun `subtraction to zero produces correct steps`() {
        val problem = MathProblem(num1 = 5, num2 = 5, operation = MathOperation.SUBTRACTION, correctAnswer = 0)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
        assertThat(steps[3].description).contains("0")
    }

    // ============== MULTIPLICATION TESTS ==============

    @Test
    fun `multiplication breakdown has correct number of steps`() {
        val problem = MathProblem(num1 = 4, num2 = 3, operation = MathOperation.MULTIPLICATION, correctAnswer = 12)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
    }

    @Test
    fun `multiplication breakdown has correct emojis in order`() {
        val problem = MathProblem(num1 = 4, num2 = 3, operation = MathOperation.MULTIPLICATION, correctAnswer = 12)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[0].emoji).isEqualTo("📦")
        assertThat(steps[1].emoji).isEqualTo("🎁")
        assertThat(steps[2].emoji).isEqualTo("➕")
        assertThat(steps[3].emoji).isEqualTo("✅")
    }

    @Test
    fun `multiplication breakdown step 1 mentions number of groups`() {
        val problem = MathProblem(num1 = 4, num2 = 3, operation = MathOperation.MULTIPLICATION, correctAnswer = 12)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[0].description).contains("4")
        assertThat(steps[0].description.lowercase()).contains("group")
    }

    @Test
    fun `multiplication breakdown step 2 mentions items per group`() {
        val problem = MathProblem(num1 = 4, num2 = 3, operation = MathOperation.MULTIPLICATION, correctAnswer = 12)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[1].description).contains("3")
        assertThat(steps[1].description.lowercase()).contains("group")
    }

    @Test
    fun `multiplication breakdown step 3 shows repeated addition concept`() {
        val problem = MathProblem(num1 = 4, num2 = 3, operation = MathOperation.MULTIPLICATION, correctAnswer = 12)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[2].description).contains("3")
        assertThat(steps[2].description).contains("4")
        assertThat(steps[2].description.lowercase()).contains("add")
    }

    @Test
    fun `multiplication breakdown step 4 shows complete equation and answer`() {
        val problem = MathProblem(num1 = 4, num2 = 3, operation = MathOperation.MULTIPLICATION, correctAnswer = 12)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[3].description).contains("4")
        assertThat(steps[3].description).contains("3")
        assertThat(steps[3].description).contains("12")
    }

    @Test
    fun `multiplication with one produces correct steps`() {
        val problem = MathProblem(num1 = 1, num2 = 5, operation = MathOperation.MULTIPLICATION, correctAnswer = 5)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
        assertThat(steps[0].description).contains("1")
        assertThat(steps[1].description).contains("5")
        assertThat(steps[3].description).contains("5")
    }

    @Test
    fun `multiplication with zero produces correct steps`() {
        val problem = MathProblem(num1 = 0, num2 = 5, operation = MathOperation.MULTIPLICATION, correctAnswer = 0)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
        assertThat(steps[3].description).contains("0")
    }

    // ============== DIVISION TESTS ==============

    @Test
    fun `division breakdown has correct number of steps`() {
        val problem = MathProblem(num1 = 12, num2 = 3, operation = MathOperation.DIVISION, correctAnswer = 4)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
    }

    @Test
    fun `division breakdown has correct emojis in order`() {
        val problem = MathProblem(num1 = 12, num2 = 3, operation = MathOperation.DIVISION, correctAnswer = 4)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[0].emoji).isEqualTo("🎁")
        assertThat(steps[1].emoji).isEqualTo("👥")
        assertThat(steps[2].emoji).isEqualTo("📊")
        assertThat(steps[3].emoji).isEqualTo("✅")
    }

    @Test
    fun `division breakdown step 1 mentions total items`() {
        val problem = MathProblem(num1 = 12, num2 = 3, operation = MathOperation.DIVISION, correctAnswer = 4)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[0].description).contains("12")
        assertThat(steps[0].description.lowercase()).contains("item")
    }

    @Test
    fun `division breakdown step 2 mentions number of groups or friends`() {
        val problem = MathProblem(num1 = 12, num2 = 3, operation = MathOperation.DIVISION, correctAnswer = 4)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[1].description).contains("3")
    }

    @Test
    fun `division breakdown step 3 mentions equal sharing`() {
        val problem = MathProblem(num1 = 12, num2 = 3, operation = MathOperation.DIVISION, correctAnswer = 4)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[2].description.lowercase()).contains("same")
    }

    @Test
    fun `division breakdown step 4 shows complete equation and answer`() {
        val problem = MathProblem(num1 = 12, num2 = 3, operation = MathOperation.DIVISION, correctAnswer = 4)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps[3].description).contains("12")
        assertThat(steps[3].description).contains("3")
        assertThat(steps[3].description).contains("4")
    }

    @Test
    fun `division by one produces correct steps`() {
        val problem = MathProblem(num1 = 10, num2 = 1, operation = MathOperation.DIVISION, correctAnswer = 10)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
        assertThat(steps[0].description).contains("10")
        assertThat(steps[1].description).contains("1")
        assertThat(steps[3].description).contains("10")
    }

    @Test
    fun `division of zero produces correct steps`() {
        val problem = MathProblem(num1 = 0, num2 = 5, operation = MathOperation.DIVISION, correctAnswer = 0)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
        assertThat(steps[0].description).contains("0")
        assertThat(steps[3].description).contains("0")
    }

    @Test
    fun `division producing one produces correct steps`() {
        val problem = MathProblem(num1 = 5, num2 = 5, operation = MathOperation.DIVISION, correctAnswer = 1)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).hasSize(4)
        assertThat(steps[3].description).contains("1")
    }

    // ============== MIXED OPERATION TESTS ==============

    @Test
    fun `mixed operation returns empty list`() {
        val problem = MathProblem(num1 = 8, num2 = 5, operation = MathOperation.MIXED, correctAnswer = 13)
        val steps = workProvider.getWorkBreakdown(problem)

        assertThat(steps).isEmpty()
    }

    // ============== CACHING TESTS ==============

    @Test
    fun `same problem returns cached result`() {
        val problem = MathProblem(num1 = 8, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 13)

        val firstCall = workProvider.getWorkBreakdown(problem)
        val secondCall = workProvider.getWorkBreakdown(problem)

        // Should return same instance from cache
        assertThat(firstCall).isSameInstanceAs(secondCall)
    }

    @Test
    fun `different problems return different results`() {
        val problem1 = MathProblem(num1 = 8, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 13)
        val problem2 = MathProblem(num1 = 10, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 13)

        val steps1 = workProvider.getWorkBreakdown(problem1)
        val steps2 = workProvider.getWorkBreakdown(problem2)

        assertThat(steps1).isNotSameInstanceAs(steps2)
        assertThat(steps1[0].description).isNotEqualTo(steps2[0].description)
    }

    @Test
    fun `same numbers different operations return different results`() {
        val addition = MathProblem(num1 = 12, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 15)
        val subtraction = MathProblem(num1 = 12, num2 = 3, operation = MathOperation.SUBTRACTION, correctAnswer = 9)
        val multiplication = MathProblem(num1 = 12, num2 = 3, operation = MathOperation.MULTIPLICATION, correctAnswer = 36)
        val division = MathProblem(num1 = 12, num2 = 3, operation = MathOperation.DIVISION, correctAnswer = 4)

        val addSteps = workProvider.getWorkBreakdown(addition)
        val subSteps = workProvider.getWorkBreakdown(subtraction)
        val mulSteps = workProvider.getWorkBreakdown(multiplication)
        val divSteps = workProvider.getWorkBreakdown(division)

        // Verify different emoji sequences for different operations
        assertThat(addSteps[1].emoji).isEqualTo("➕")
        assertThat(subSteps[1].emoji).isEqualTo("➖")
        assertThat(mulSteps[0].emoji).isEqualTo("📦")
        assertThat(divSteps[0].emoji).isEqualTo("🎁")
    }

    // ============== MATH ACCURACY VERIFICATION ==============

    @Test
    fun `verify addition math accuracy for multiple problems`() {
        val testCases =
            listOf(
                Triple(2, 3, 5),
                Triple(0, 5, 5),
                Triple(10, 10, 20),
                Triple(99, 1, 100),
            )

        testCases.forEach { (num1, num2, expected) ->
            val problem = MathProblem(num1 = num1, num2 = num2, operation = MathOperation.ADDITION, correctAnswer = expected)
            val steps = workProvider.getWorkBreakdown(problem)

            assertThat(steps[3].description).contains(expected.toString())
        }
    }

    @Test
    fun `verify subtraction math accuracy for multiple problems`() {
        val testCases =
            listOf(
                Triple(5, 3, 2),
                Triple(10, 0, 10),
                Triple(20, 20, 0),
                Triple(100, 99, 1),
            )

        testCases.forEach { (num1, num2, expected) ->
            val problem = MathProblem(num1 = num1, num2 = num2, operation = MathOperation.SUBTRACTION, correctAnswer = expected)
            val steps = workProvider.getWorkBreakdown(problem)

            assertThat(steps[3].description).contains(expected.toString())
        }
    }

    @Test
    fun `verify multiplication math accuracy for multiple problems`() {
        val testCases =
            listOf(
                Triple(2, 3, 6),
                Triple(0, 5, 0),
                Triple(1, 10, 10),
                Triple(9, 9, 81),
            )

        testCases.forEach { (num1, num2, expected) ->
            val problem = MathProblem(num1 = num1, num2 = num2, operation = MathOperation.MULTIPLICATION, correctAnswer = expected)
            val steps = workProvider.getWorkBreakdown(problem)

            assertThat(steps[3].description).contains(expected.toString())
        }
    }

    @Test
    fun `verify division math accuracy for multiple problems`() {
        val testCases =
            listOf(
                Triple(6, 2, 3),
                Triple(0, 5, 0),
                Triple(10, 1, 10),
                Triple(81, 9, 9),
            )

        testCases.forEach { (num1, num2, expected) ->
            val problem = MathProblem(num1 = num1, num2 = num2, operation = MathOperation.DIVISION, correctAnswer = expected)
            val steps = workProvider.getWorkBreakdown(problem)

            assertThat(steps[3].description).contains(expected.toString())
        }
    }
}
