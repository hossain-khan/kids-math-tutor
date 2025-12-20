package dev.hossain.mathtutor.domain.model

/**
 * Represents the available mini-games in the Kids Math Tutor app.
 * Each game has specific requirements to unlock and provides a unique way to practice math.
 *
 * @property displayName The human-readable name of the game shown in the UI
 * @property description Brief description of what the game involves
 * @property icon Emoji icon representing the game
 * @property unlockRequirement Number of total problems that must be solved to unlock this game
 * @property durationSeconds Duration of the game in seconds (e.g., 60 for Math Race)
 */
enum class Game(
    val displayName: String,
    val description: String,
    val icon: String,
    val unlockRequirement: Int,
    val durationSeconds: Int,
) {
    /**
     * Math Race: Solve as many problems as possible in 60 seconds.
     * First game to unlock - designed to be accessible early in the user's journey.
     */
    MATH_RACE(
        displayName = "Math Race",
        description = "Solve as many problems as you can in 60 seconds!",
        icon = "⏱️",
        unlockRequirement = 50,
        durationSeconds = 60,
    ),

    /**
     * Memory Match: Match math problems with their answers.
     * Second game to unlock - requires more practice before access.
     */
    MEMORY_MATCH(
        displayName = "Memory Match",
        description = "Match problems with answers!",
        icon = "🧩",
        unlockRequirement = 100,
        durationSeconds = 120,
    ),

    /**
     * Number Sequence: Find the missing number in a sequence.
     * Third game to unlock - rewards dedicated practice.
     */
    NUMBER_SEQUENCE(
        displayName = "Number Sequence",
        description = "Find the missing number!",
        icon = "🎲",
        unlockRequirement = 200,
        durationSeconds = 90,
    ),
    ;

    /**
     * Checks if the game is unlocked based on the total number of problems solved.
     *
     * @param totalProblemsSolved The total number of problems the user has solved
     * @return true if the game is unlocked, false otherwise
     */
    fun isUnlocked(totalProblemsSolved: Int): Boolean = totalProblemsSolved >= unlockRequirement

    /**
     * Calculates the progress towards unlocking this game.
     *
     * @param totalProblemsSolved The total number of problems the user has solved
     * @return Progress as a float between 0.0 (no progress) and 1.0 (unlocked)
     */
    fun unlockProgress(totalProblemsSolved: Int): Float = (totalProblemsSolved.toFloat() / unlockRequirement).coerceIn(0f, 1f)

    /**
     * Returns the number of problems remaining to unlock this game.
     *
     * @param totalProblemsSolved The total number of problems the user has solved
     * @return Number of problems still needed, or 0 if already unlocked
     */
    fun problemsUntilUnlock(totalProblemsSolved: Int): Int = (unlockRequirement - totalProblemsSolved).coerceAtLeast(0)
}
