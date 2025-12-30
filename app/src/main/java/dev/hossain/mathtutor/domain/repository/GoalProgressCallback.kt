package dev.hossain.mathtutor.domain.repository

/**
 * Callback interface for notifying the goal system when practice sessions are completed.
 * Allows the practice session repository to integrate with the goals feature.
 *
 * This enables automatic progress tracking when a child completes a practice session:
 * - If an active goal exists, its progress is updated
 * - Component progress is tracked across sessions
 * - Completion is detected automatically
 */
interface GoalProgressCallback {
    /**
     * Called when a practice session is completed.
     *
     * @param sessionId The ID of the completed practice session
     * @param accuracy The accuracy achieved in this session (0-100)
     * @param durationSeconds The duration of the session in seconds
     *
     * Implementation should:
     * - Find the current active goal, if any
     * - Update the current component's progress
     * - Check if the component is completed
     * - Move to next component or mark goal complete
     */
    suspend fun onSessionCompleted(
        sessionId: String,
        accuracy: Float,
        durationSeconds: Long,
    )
}
