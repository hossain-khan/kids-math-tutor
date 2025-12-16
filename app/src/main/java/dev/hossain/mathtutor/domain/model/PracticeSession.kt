package dev.hossain.mathtutor.domain.model

/**
 * Represents a complete practice session containing multiple problems and their answers.
 *
 * @property totalProblems The total number of problems in this session
 * @property problems List of math problems for this session
 * @property answers Map of problem IDs to user answers
 */
data class PracticeSession(
    val totalProblems: Int = 10,
    val problems: List<MathProblem>,
    val answers: MutableMap<String, SessionAnswer> = mutableMapOf(),
) {
    /**
     * Gets the count of correct answers in this session.
     *
     * @return Number of correct answers
     */
    fun getCorrectCount(): Int = answers.values.count { it.isCorrect }

    /**
     * Calculates the accuracy percentage for this session.
     *
     * @return Accuracy as a percentage (0-100), or 0 if no answers yet
     */
    fun getAccuracy(): Float {
        if (answers.isEmpty()) return 0f
        return (getCorrectCount().toFloat() / answers.size) * 100
    }
}
