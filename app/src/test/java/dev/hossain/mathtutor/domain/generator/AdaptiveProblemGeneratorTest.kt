package dev.hossain.mathtutor.domain.generator

import dev.hossain.mathtutor.domain.model.DifficultyAdjustment
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.OperationPerformance
import dev.hossain.mathtutor.domain.repository.PerformanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AdaptiveProblemGenerator].
 */
class AdaptiveProblemGeneratorTest {
    private lateinit var gradeAwareProblemGenerator: GradeAwareProblemGenerator
    private lateinit var fakePerformanceRepository: FakePerformanceRepository
    private lateinit var adaptiveProblemGenerator: AdaptiveProblemGenerator

    @Before
    fun setup() {
        gradeAwareProblemGenerator = GradeAwareProblemGenerator()
        fakePerformanceRepository = FakePerformanceRepository()
        adaptiveProblemGenerator =
            AdaptiveProblemGenerator(
                gradeAwareProblemGenerator = gradeAwareProblemGenerator,
                performanceRepository = fakePerformanceRepository,
            )
    }

    @Test
    fun `generates problems at current level when no performance data`() =
        runBlocking {
            val result =
                adaptiveProblemGenerator.generateAdaptiveProblems(
                    count = 5,
                    operation = MathOperation.ADDITION,
                    baseGradeLevel = GradeLevel.GRADE_1,
                )

            assertEquals(5, result.problems.size)
            assertEquals(GradeLevel.GRADE_1, result.actualGradeLevel)
            assertEquals(GradeLevel.GRADE_1, result.baseGradeLevel)
            assertEquals(DifficultyAdjustment.CURRENT, result.adjustment)
            assertFalse(result.wasAdjusted)
        }

    @Test
    fun `increases difficulty when accuracy is high`() =
        runBlocking {
            // Set up high-accuracy performance
            fakePerformanceRepository.setPerformance(
                OperationPerformance(
                    operation = MathOperation.ADDITION,
                    gradeLevel = GradeLevel.GRADE_1,
                    totalAttempts = 25,
                    correctAnswers = 22,
                    recentAccuracy = 0.90f,
                    recentAttempts = 20,
                ),
            )

            val result =
                adaptiveProblemGenerator.generateAdaptiveProblems(
                    count = 5,
                    operation = MathOperation.ADDITION,
                    baseGradeLevel = GradeLevel.GRADE_1,
                )

            assertEquals(GradeLevel.GRADE_2, result.actualGradeLevel) // Increased from Grade 1
            assertEquals(GradeLevel.GRADE_1, result.baseGradeLevel)
            assertEquals(DifficultyAdjustment.HARDER, result.adjustment)
            assertTrue(result.wasAdjusted)
            assertTrue(result.wasIncreased)
        }

    @Test
    fun `decreases difficulty when accuracy is low`() =
        runBlocking {
            // Set up low-accuracy performance
            fakePerformanceRepository.setPerformance(
                OperationPerformance(
                    operation = MathOperation.ADDITION,
                    gradeLevel = GradeLevel.GRADE_2,
                    totalAttempts = 15,
                    correctAnswers = 5,
                    recentAccuracy = 0.35f,
                    recentAttempts = 10,
                ),
            )

            val result =
                adaptiveProblemGenerator.generateAdaptiveProblems(
                    count = 5,
                    operation = MathOperation.ADDITION,
                    baseGradeLevel = GradeLevel.GRADE_2,
                )

            assertEquals(GradeLevel.GRADE_1, result.actualGradeLevel) // Decreased from Grade 2
            assertEquals(GradeLevel.GRADE_2, result.baseGradeLevel)
            assertEquals(DifficultyAdjustment.EASIER, result.adjustment)
            assertTrue(result.wasAdjusted)
            assertTrue(result.wasDecreased)
        }

    @Test
    fun `does not increase difficulty at max grade level`() =
        runBlocking {
            // Set up high-accuracy at Grade 2 (max)
            fakePerformanceRepository.setPerformance(
                OperationPerformance(
                    operation = MathOperation.ADDITION,
                    gradeLevel = GradeLevel.GRADE_2,
                    totalAttempts = 25,
                    correctAnswers = 22,
                    recentAccuracy = 0.90f,
                    recentAttempts = 20,
                ),
            )

            val result =
                adaptiveProblemGenerator.generateAdaptiveProblems(
                    count = 5,
                    operation = MathOperation.ADDITION,
                    baseGradeLevel = GradeLevel.GRADE_2,
                )

            assertEquals(GradeLevel.GRADE_2, result.actualGradeLevel) // Stays at Grade 2
            assertEquals(DifficultyAdjustment.CURRENT, result.adjustment)
            assertFalse(result.wasAdjusted)
        }

    @Test
    fun `does not decrease difficulty at min grade level`() =
        runBlocking {
            // Set up low-accuracy at Kindergarten (min)
            fakePerformanceRepository.setPerformance(
                OperationPerformance(
                    operation = MathOperation.ADDITION,
                    gradeLevel = GradeLevel.KINDERGARTEN,
                    totalAttempts = 15,
                    correctAnswers = 5,
                    recentAccuracy = 0.35f,
                    recentAttempts = 10,
                ),
            )

            val result =
                adaptiveProblemGenerator.generateAdaptiveProblems(
                    count = 5,
                    operation = MathOperation.ADDITION,
                    baseGradeLevel = GradeLevel.KINDERGARTEN,
                )

            assertEquals(GradeLevel.KINDERGARTEN, result.actualGradeLevel) // Stays at K
            assertEquals(DifficultyAdjustment.CURRENT, result.adjustment)
            assertFalse(result.wasAdjusted)
        }

    @Test
    fun `getNextGradeLevel returns correct next level`() {
        assertEquals(
            GradeLevel.GRADE_1,
            adaptiveProblemGenerator.getNextGradeLevel(GradeLevel.KINDERGARTEN),
        )
        assertEquals(GradeLevel.GRADE_2, adaptiveProblemGenerator.getNextGradeLevel(GradeLevel.GRADE_1))
        assertEquals(GradeLevel.GRADE_2, adaptiveProblemGenerator.getNextGradeLevel(GradeLevel.GRADE_2)) // Max
    }

    @Test
    fun `getPreviousGradeLevel returns correct previous level`() {
        assertEquals(
            GradeLevel.KINDERGARTEN,
            adaptiveProblemGenerator.getPreviousGradeLevel(GradeLevel.KINDERGARTEN),
        ) // Min
        assertEquals(GradeLevel.KINDERGARTEN, adaptiveProblemGenerator.getPreviousGradeLevel(GradeLevel.GRADE_1))
        assertEquals(GradeLevel.GRADE_1, adaptiveProblemGenerator.getPreviousGradeLevel(GradeLevel.GRADE_2))
    }

    @Test
    fun `generates correct number of problems`() =
        runBlocking {
            val result =
                adaptiveProblemGenerator.generateAdaptiveProblems(
                    count = 10,
                    operation = MathOperation.SUBTRACTION,
                    baseGradeLevel = GradeLevel.KINDERGARTEN,
                )

            assertEquals(10, result.problems.size)
        }

    @Test
    fun `problems use adjusted grade level`() =
        runBlocking {
            // Set up performance to trigger level increase from K to Grade 1
            fakePerformanceRepository.setPerformance(
                OperationPerformance(
                    operation = MathOperation.ADDITION,
                    gradeLevel = GradeLevel.KINDERGARTEN,
                    totalAttempts = 25,
                    correctAnswers = 22,
                    recentAccuracy = 0.88f,
                    recentAttempts = 20,
                ),
            )

            val result =
                adaptiveProblemGenerator.generateAdaptiveProblems(
                    count = 20, // Generate many problems
                    operation = MathOperation.ADDITION,
                    baseGradeLevel = GradeLevel.KINDERGARTEN,
                )

            // Verify problems are generated at Grade 1 level
            assertEquals(GradeLevel.GRADE_1, result.actualGradeLevel)
            // Grade 1 addition can have sums > 18 (K max), so at least some problems
            // should have numbers in the 1-20 range
            assertTrue(result.problems.any { it.num1 > 10 || it.num2 > 10 || it.correctAnswer > 18 })
        }
}

/**
 * Fake implementation of [PerformanceRepository] for testing.
 */
class FakePerformanceRepository : PerformanceRepository {
    private var performance: OperationPerformance? = null

    fun setPerformance(performance: OperationPerformance) {
        this.performance = performance
    }

    override suspend fun recordPerformance(
        operation: MathOperation,
        gradeLevel: GradeLevel,
        problemId: String,
        isCorrect: Boolean,
        timeSpentSeconds: Long,
    ): Long = 1L

    override fun getPerformance(
        operation: MathOperation,
        gradeLevel: GradeLevel,
    ): Flow<OperationPerformance> =
        flowOf(
            performance ?: OperationPerformance.empty(operation, gradeLevel),
        )

    override suspend fun getRecentAccuracy(
        operation: MathOperation,
        count: Int,
    ): Float? = performance?.recentAccuracy

    override suspend fun getRecentAttemptCount(
        operation: MathOperation,
        limit: Int,
    ): Int = performance?.recentAttempts ?: 0

    override suspend fun clearAll() {
        performance = null
    }
}
