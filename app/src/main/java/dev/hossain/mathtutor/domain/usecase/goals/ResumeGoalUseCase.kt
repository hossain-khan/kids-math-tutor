package dev.hossain.mathtutor.domain.usecase.goals

import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for resuming an interrupted goal session.
 * Checks if there is an active goal that can be resumed.
 *
 * @property repository The goal repository
 */
class ResumeGoalUseCase
    @Inject
    constructor(
        private val repository: GoalRepository,
    ) {
        /**
         * Checks if there is an active goal to resume.
         * Typically displayed in a dialog when the app starts if a goal is in progress.
         *
         * @return Flow of optional ActiveGoal (null if no goal is in progress)
         *
         * Use case:
         * - User was practicing a goal and closed the app
         * - On app restart, check if they want to resume
         * - Emit the active goal if found, null otherwise
         */
        operator fun invoke(): Flow<ActiveGoal?> = repository.getActiveGoal().map { it }
    }
