package dev.hossain.mathtutor.domain.generator

import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem

/**
 * Interface for generating math problems.
 * Implementations should generate problems appropriate for the target audience and difficulty level.
 */
interface ProblemGenerator {
    /**
     * Generates a list of math problems.
     *
     * @param count The number of problems to generate
     * @param operation The mathematical operation for the problems
     * @param gradeLevel The grade level to generate problems for (affects number ranges and operation availability)
     * @return List of generated math problems
     */
    fun generateProblems(
        count: Int,
        operation: MathOperation,
        gradeLevel: GradeLevel,
    ): List<MathProblem>
}
