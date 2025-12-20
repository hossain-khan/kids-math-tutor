package dev.hossain.mathtutor.domain.generator

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.DifficultyAdjustment
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.OperationPerformance
import dev.hossain.mathtutor.domain.repository.PerformanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
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

            assertThat(result.problems.size).isEqualTo(5)
            assertThat(result.actualGradeLevel).isEqualTo(GradeLevel.GRADE_1)
            assertThat(result.baseGradeLevel).isEqualTo(GradeLevel.GRADE_1)
            assertThat(result.adjustment).isEqualTo(DifficultyAdjustment.CURRENT)
            assertThat(result.wasAdjusted).isFalse()
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

            assertThat(result.actualGradeLevel).isEqualTo(GradeLevel.GRADE_2) // Increased from Grade 1
            assertThat(result.baseGradeLevel).isEqualTo(GradeLevel.GRADE_1)
            assertThat(result.adjustment).isEqualTo(DifficultyAdjustment.HARDER)
            assertThat(result.wasAdjusted).isTrue()
            assertThat(result.wasIncreased).isTrue()
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

            assertThat(result.actualGradeLevel).isEqualTo(GradeLevel.GRADE_1) // Decreased from Grade 2
            assertThat(result.baseGradeLevel).isEqualTo(GradeLevel.GRADE_2)
            assertThat(result.adjustment).isEqualTo(DifficultyAdjustment.EASIER)
            assertThat(result.wasAdjusted).isTrue()
            assertThat(result.wasDecreased).isTrue()
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

            assertThat(result.actualGradeLevel).isEqualTo(GradeLevel.GRADE_2) // Stays at Grade 2
            assertThat(result.adjustment).isEqualTo(DifficultyAdjustment.CURRENT)
            assertThat(result.wasAdjusted).isFalse()
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

            assertThat(result.actualGradeLevel).isEqualTo(GradeLevel.KINDERGARTEN) // Stays at K
            assertThat(result.adjustment).isEqualTo(DifficultyAdjustment.CURRENT)
            assertThat(result.wasAdjusted).isFalse()
        }

    @Test
    fun `getNextGradeLevel returns correct next level`() {
        assertThat(
            adaptiveProblemGenerator.getNextGradeLevel(GradeLevel.KINDERGARTEN).isEqualTo(GradeLevel.GRADE_1),
        )
        assertThat(adaptiveProblemGenerator.getNextGradeLevel(GradeLevel.GRADE_1).isEqualTo(GradeLevel.GRADE_2))
        assertThat(adaptiveProblemGenerator.getNextGradeLevel(GradeLevel.GRADE_2).isEqualTo(GradeLevel.GRADE_2)) // Max
    }

    @Test
    fun `getPreviousGradeLevel returns correct previous level`() {
        assertThat(
            adaptiveProblemGenerator.getPreviousGradeLevel(GradeLevel.KINDERGARTEN).isEqualTo(GradeLevel.KINDERGARTEN),
        ) // Min
        assertThat(adaptiveProblemGenerator.getPreviousGradeLevel(GradeLevel.GRADE_1).isEqualTo(GradeLevel.KINDERGARTEN))
        assertThat(adaptiveProblemGenerator.getPreviousGradeLevel(GradeLevel.GRADE_2).isEqualTo(GradeLevel.GRADE_1))
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

            assertThat(result.problems.size).isEqualTo(10)
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
            assertThat(result.actualGradeLevel).isEqualTo(GradeLevel.GRADE_1)
            // Grade 1 addition can have sums > 18 (K max), so at least some problems
            // should have numbers in the 1-20 range
            assertThat(result.problems.any { it.num1 > 10 || it.num2 > 10 || it.correctAnswer > 18 }).isTrue()
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
