package dev.hossain.mathtutor.domain.service

import dev.hossain.mathtutor.domain.generator.ProblemGenerator
import dev.hossain.mathtutor.domain.model.ChallengeImportSpec
import dev.hossain.mathtutor.domain.model.ChallengePracticeSession
import dev.hossain.mathtutor.domain.model.ChallengeType
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PreviewData
import dev.hossain.mathtutor.domain.model.ProblemSpec
import dev.hossain.mathtutor.domain.repository.CustomChallengeRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation of [CustomChallengeService] that handles custom challenge business logic.
 *
 * This service converts import specifications into practice-ready custom challenges,
 * performs validation, generates problems, and manages challenge lifecycle.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class CustomChallengeServiceImpl
    constructor(
        private val repository: CustomChallengeRepository,
        private val problemGenerator: ProblemGenerator,
    ) : CustomChallengeService {
        companion object {
            /**
             * Number of sample problems to include in preview (3-5).
             */
            const val PREVIEW_SAMPLE_SIZE = 5

            /**
             * Multiplier for over-generating division problems to account for filtering.
             * Division problems are filtered for whole number results, so we generate extra.
             */
            private const val DIVISION_OVER_GENERATE_MULTIPLIER = 2

            /**
             * Estimated seconds per problem for completion time calculation.
             * Varies by operation complexity.
             */
            private const val SECONDS_PER_ADDITION = 15
            private const val SECONDS_PER_SUBTRACTION = 18
            private const val SECONDS_PER_MULTIPLICATION = 20
            private const val SECONDS_PER_DIVISION = 25
        }

        override suspend fun createChallengeFromSpec(spec: ChallengeImportSpec): Result<CustomChallenge> =
            runCatching {
                Timber.d("Creating challenge from spec: type=${spec::class.simpleName}, title=${spec.title}")

                val (problems, challengeType) =
                    when (spec) {
                        is ChallengeImportSpec.Generated -> {
                            val generatedProblems = generateProblemsFromSpec(spec)
                            generatedProblems to ChallengeType.GENERATED
                        }

                        is ChallengeImportSpec.Explicit -> {
                            val explicitProblems = processExplicitProblems(spec.problems)
                            explicitProblems to ChallengeType.EXPLICIT
                        }
                    }

                if (problems.isEmpty()) {
                    throw IllegalArgumentException("No valid problems could be generated or processed")
                }

                val challenge =
                    CustomChallenge(
                        title = spec.title,
                        subtitle = spec.subtitle,
                        type = challengeType,
                        problems = problems,
                    )

                repository.saveChallenge(challenge)
                Timber.d("Challenge created successfully: id=${challenge.id}, problemCount=${problems.size}")

                challenge
            }.onFailure { e ->
                Timber.e(e, "Failed to create challenge from spec")
            }

        override suspend fun generatePreview(spec: ChallengeImportSpec): PreviewData {
            Timber.d("Generating preview for spec: type=${spec::class.simpleName}, title=${spec.title}")

            val allProblems =
                when (spec) {
                    is ChallengeImportSpec.Generated -> generateProblemsFromSpec(spec)
                    is ChallengeImportSpec.Explicit -> processExplicitProblems(spec.problems)
                }

            val sampleProblems = allProblems.take(PREVIEW_SAMPLE_SIZE)
            val operationsSummary = calculateOperationsSummary(allProblems)
            val estimatedDuration = calculateEstimatedDuration(allProblems)

            return PreviewData(
                title = spec.title,
                subtitle = spec.subtitle,
                problemCount = allProblems.size,
                operationsSummary = operationsSummary,
                sampleProblems = sampleProblems,
                estimatedDuration = estimatedDuration,
            )
        }

        override suspend fun getAllChallenges(): List<CustomChallenge> = repository.getAllChallenges()

        override suspend fun getChallengeById(id: String): CustomChallenge? = repository.getChallengeById(id)

        override suspend fun archiveChallenge(id: String) {
            repository.archiveChallenge(id)
        }

        override suspend fun deleteChallenge(id: String) {
            repository.deleteChallenge(id)
        }

        override suspend fun recordPracticeSession(
            challengeId: String,
            session: ChallengePracticeSession,
        ) {
            repository.addPracticeSession(challengeId, session)
        }

        override suspend fun clearChallengeSessions(challengeId: String) {
            repository.clearChallengeSessions(challengeId)
        }

        override fun observeAllChallenges(): Flow<List<CustomChallenge>> = repository.observeAllChallenges()

        override fun observeActiveChallenges(): Flow<List<CustomChallenge>> = repository.observeActiveChallenges()

        /**
         * Generates problems from a generated specification using the ProblemGenerator.
         *
         * Applies number range constraints and filters invalid problems.
         */
        private fun generateProblemsFromSpec(spec: ChallengeImportSpec.Generated): List<MathProblem> {
            Timber.d(
                "Generating problems: operation=${spec.operation}, count=${spec.problemCount}, " +
                    "range=${spec.numberRange.min}-${spec.numberRange.max}",
            )

            // Generate more problems than needed to account for filtering
            val overGenerateMultiplier =
                if (spec.operation == MathOperation.DIVISION) DIVISION_OVER_GENERATE_MULTIPLIER else 1
            val totalToGenerate = spec.problemCount * overGenerateMultiplier

            // Use Grade 2 as the default grade level for custom challenges
            // since parents can specify any range they want
            val allProblems = problemGenerator.generateProblems(totalToGenerate, spec.operation, GradeLevel.GRADE_2)

            // Filter problems to match the specified number range and apply validation
            val filteredProblems =
                allProblems
                    .mapNotNull { problem ->
                        adjustProblemToRange(problem, spec.numberRange.min, spec.numberRange.max)
                    }.take(spec.problemCount)

            Timber.d("Generated ${filteredProblems.size} valid problems after filtering")
            return filteredProblems
        }

        /**
         * Adjusts a problem to fit within the specified number range.
         * Returns null if the problem cannot be adjusted or is invalid.
         */
        private fun adjustProblemToRange(
            problem: MathProblem,
            min: Int,
            max: Int,
        ): MathProblem? {
            // For generated problems, regenerate with the specified range
            val num1 = Random.nextInt(min, max + 1)
            val num2 =
                when (problem.operation) {
                    MathOperation.SUBTRACTION -> {
                        // Ensure num2 <= num1 to avoid negative results
                        // Also ensure num2 >= min to stay within range
                        val upperBound = minOf(num1 + 1, max + 1)
                        if (min >= upperBound) {
                            // Invalid range, return null
                            return null
                        }
                        Random.nextInt(min, upperBound)
                    }

                    MathOperation.DIVISION -> {
                        // Generate valid divisor within range
                        // Ensure divisor is at least 1 and within the specified range
                        val divisorMin = maxOf(1, min)
                        val divisorMax = max
                        if (divisorMin > divisorMax) {
                            // Invalid range, return null
                            return null
                        }
                        val divisor = Random.nextInt(divisorMin, divisorMax + 1)
                        if (divisor == 0 || num1 % divisor != 0) {
                            // Skip if division is not clean
                            return null
                        }
                        divisor
                    }

                    else -> {
                        Random.nextInt(min, max + 1)
                    }
                }

            // Validate operands don't cause overflow
            if (!validateOperands(num1, num2, problem.operation)) {
                return null
            }

            // For division, ensure clean division
            if (problem.operation == MathOperation.DIVISION && !validateDivisionProblem(num1, num2)) {
                return null
            }

            val answer =
                try {
                    problem.operation.calculate(num1, num2)
                } catch (e: Exception) {
                    Timber.w("Problem calculation failed: $num1 ${problem.operation.symbol} $num2")
                    return null
                }

            return MathProblem(
                num1 = num1,
                num2 = num2,
                operation = problem.operation,
                correctAnswer = answer,
            )
        }

        /**
         * Processes explicit problem specifications into MathProblem domain objects.
         *
         * Applies validation and filters out invalid problems (non-whole division, overflow).
         */
        private fun processExplicitProblems(specs: List<ProblemSpec>): List<MathProblem> {
            Timber.d("Processing ${specs.size} explicit problems")

            val problems =
                specs.mapNotNull { spec ->
                    processExplicitProblem(spec)
                }

            Timber.d("Processed ${problems.size} valid problems from ${specs.size} specifications")
            return problems
        }

        /**
         * Processes a single explicit problem specification.
         * Returns null if the problem is invalid.
         */
        private fun processExplicitProblem(spec: ProblemSpec): MathProblem? {
            // Validate operands don't cause overflow
            if (!validateOperands(spec.operand1, spec.operand2, spec.operation)) {
                Timber.w("Overflow detected for problem: ${spec.operand1} ${spec.operation.symbol} ${spec.operand2}")
                return null
            }

            // For division, validate that result is a whole number
            if (spec.operation == MathOperation.DIVISION && !validateDivisionProblem(spec.operand1, spec.operand2)) {
                Timber.w("Division with non-whole result skipped: ${spec.operand1} ÷ ${spec.operand2}")
                return null
            }

            val answer =
                try {
                    spec.operation.calculate(spec.operand1, spec.operand2)
                } catch (e: ArithmeticException) {
                    Timber.w("Arithmetic error for problem: ${spec.operand1} ${spec.operation.symbol} ${spec.operand2}")
                    return null
                }

            return MathProblem(
                num1 = spec.operand1,
                num2 = spec.operand2,
                operation = spec.operation,
                correctAnswer = answer,
            )
        }

        /**
         * Validates that a division problem results in a whole number.
         *
         * @return true if the division is valid (no remainder), false otherwise
         */
        private fun validateDivisionProblem(
            operand1: Int,
            operand2: Int,
        ): Boolean = operand2 != 0 && operand1 % operand2 == 0

        /**
         * Validates that operands won't cause integer overflow.
         *
         * @return true if the operation is safe, false if overflow would occur
         */
        private fun validateOperands(
            operand1: Int,
            operand2: Int,
            operation: MathOperation,
        ): Boolean =
            when (operation) {
                MathOperation.ADDITION -> operand1.toLong() + operand2 <= Int.MAX_VALUE
                MathOperation.MULTIPLICATION -> operand1.toLong() * operand2 <= Int.MAX_VALUE
                else -> true // Subtraction and division won't overflow in our use case
            }

        /**
         * Calculates the breakdown of operations in the problem set.
         */
        private fun calculateOperationsSummary(problems: List<MathProblem>): Map<MathOperation, Int> =
            problems
                .groupBy { it.operation }
                .mapValues { it.value.size }

        /**
         * Estimates the completion time based on problem count and operation complexity.
         */
        private fun calculateEstimatedDuration(problems: List<MathProblem>): Duration {
            val totalSeconds =
                problems.sumOf { problem ->
                    when (problem.operation) {
                        MathOperation.ADDITION -> SECONDS_PER_ADDITION
                        MathOperation.SUBTRACTION -> SECONDS_PER_SUBTRACTION
                        MathOperation.MULTIPLICATION -> SECONDS_PER_MULTIPLICATION
                        MathOperation.DIVISION -> SECONDS_PER_DIVISION
                        MathOperation.MIXED -> SECONDS_PER_ADDITION // Fallback, shouldn't happen
                    }
                }

            return totalSeconds.seconds
        }
    }
