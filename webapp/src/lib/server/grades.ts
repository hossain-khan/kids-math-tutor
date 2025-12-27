/**
 * Grade detection utilities for custom worksheets.
 * Analyzes problem operands to automatically assign grade levels.
 *
 * Grading Logic (Option A):
 * - Kindergarten: operands 0-10
 * - Grade 1: operands 1-20
 * - Grade 2: operands 1-100
 */

import type { ProblemSpec, GradeLevel } from "@/lib/schemas/challenge-schema";

/**
 * Detect grade levels based on operand values in problems
 * @param problems - Array of math problems
 * @returns Array of detected grade levels
 */
export function detectGrades(problems: ProblemSpec[]): GradeLevel[] {
  if (!problems || problems.length === 0) return ["kindergarten"];

  let maxOperand = 0;

  // Find the maximum operand value across all problems
  problems.forEach((problem) => {
    const operand1 = Math.abs(problem.operand1);
    const operand2 = Math.abs(problem.operand2);
    maxOperand = Math.max(maxOperand, operand1, operand2);
  });

  const grades: GradeLevel[] = [];

  // Assign grades based on max operand value
  if (maxOperand <= 10) {
    grades.push("kindergarten");
  }
  if (maxOperand <= 20) {
    grades.push("grade1");
  }
  if (maxOperand <= 100) {
    grades.push("grade2");
  }

  // If no grades detected, default to kindergarten
  return grades.length > 0 ? grades : ["kindergarten"];
}

/**
 * Check if a worksheet is suitable for a specific grade
 * @param worksheet - Worksheet with grades array
 * @param gradeLevel - Grade to check for
 * @returns true if worksheet is suitable for the grade
 */
export function supportsGrade(
  grades: GradeLevel[],
  gradeLevel: GradeLevel,
): boolean {
  return grades.includes(gradeLevel);
}
