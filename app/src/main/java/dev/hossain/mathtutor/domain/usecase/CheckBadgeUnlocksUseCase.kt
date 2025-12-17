package dev.hossain.mathtutor.domain.usecase

import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.repository.BadgeRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.domain.repository.StreakRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Instant

/**
 * Use case for checking and unlocking badges based on current user statistics.
 * Evaluates all locked badges against current progress and unlocks those that meet their requirements.
 */
@SingleIn(AppScope::class)
@Inject
class CheckBadgeUnlocksUseCase
    constructor(
        private val badgeRepository: BadgeRepository,
        private val sessionRepository: SessionRepository,
        private val streakRepository: StreakRepository,
    ) {
        /**
         * Checks all locked badges and unlocks any that meet their requirements.
         *
         * @return List of newly unlocked badges (empty list if none)
         */
        suspend fun checkAndUnlockBadges(): List<Badge> {
            val allBadges = badgeRepository.getAllBadges().first()
            val lockedBadges = allBadges.filter { !it.isUnlocked() }

            if (lockedBadges.isEmpty()) {
                Timber.d("No locked badges to check")
                return emptyList()
            }

            val newlyUnlocked = mutableListOf<Badge>()
            val unlockTime = Instant.now()

            lockedBadges.forEach { badge ->
                if (checkRequirement(badge.requirement)) {
                    Timber.d("Unlocking badge: ${badge.id} - ${badge.name}")
                    badgeRepository.unlockBadge(badge.id, unlockTime)
                    newlyUnlocked.add(badge.copy(unlockedAt = unlockTime))
                }
            }

            Timber.d("Unlocked ${newlyUnlocked.size} badges")
            return newlyUnlocked
        }

        /**
         * Checks if a specific badge requirement is met.
         *
         * @param requirement The badge requirement to check
         * @return true if the requirement is met, false otherwise
         */
        private suspend fun checkRequirement(requirement: BadgeRequirement): Boolean =
            when (requirement) {
                is BadgeRequirement.ProblemCount -> checkProblemCount(requirement)
                is BadgeRequirement.OperationCount -> checkOperationCount(requirement)
                is BadgeRequirement.ConsecutiveCorrect -> checkConsecutiveCorrect(requirement)
                is BadgeRequirement.SessionAccuracy -> checkSessionAccuracy(requirement)
                is BadgeRequirement.DailyStreak -> checkDailyStreak(requirement)
                is BadgeRequirement.ProblemSpeed -> checkProblemSpeed(requirement)
                is BadgeRequirement.MixedSessions -> checkMixedSessions(requirement)
            }

        /**
         * Checks if the total problem count requirement is met.
         */
        private suspend fun checkProblemCount(requirement: BadgeRequirement.ProblemCount): Boolean {
            val stats = sessionRepository.getOverallStats().first()
            return stats.totalProblems >= requirement.count
        }

        /**
         * Checks if the operation-specific problem count requirement is met.
         */
        private suspend fun checkOperationCount(requirement: BadgeRequirement.OperationCount): Boolean {
            val stats = sessionRepository.getStatsByOperation(requirement.operation).first()
            return stats.totalProblems >= requirement.count
        }

        /**
         * Checks if the consecutive correct answers requirement is met.
         * Note: This requires tracking individual problem answers, which is not yet implemented.
         * Returns false until individual problem tracking is available.
         */
        private suspend fun checkConsecutiveCorrect(requirement: BadgeRequirement.ConsecutiveCorrect): Boolean {
            // TODO: Implement when individual problem tracking is added
            // Need to track answer sequence across sessions to find consecutive correct streaks
            Timber.d("ConsecutiveCorrect check not yet implemented - requires individual problem tracking")
            return false
        }

        /**
         * Checks if the session accuracy requirement is met.
         * Verifies that the most recent N sessions all meet or exceed the required accuracy percentage.
         *
         * Example: To unlock a badge requiring 90%+ accuracy in 3 sessions:
         * - Requires: At least 3 sessions completed
         * - Checks: The 3 most recent sessions
         * - Unlocks: Only if all 3 sessions have >=90% accuracy
         */
        private suspend fun checkSessionAccuracy(requirement: BadgeRequirement.SessionAccuracy): Boolean {
            val recentSessions = sessionRepository.getRecentSessions(requirement.sessionCount).first()
            if (recentSessions.size < requirement.sessionCount) {
                return false
            }

            // Check if all of the N most recent sessions meet the accuracy threshold
            return recentSessions
                .take(requirement.sessionCount)
                .all { it.accuracy >= requirement.percentage }
        }

        /**
         * Checks if the daily streak requirement is met.
         */
        private suspend fun checkDailyStreak(requirement: BadgeRequirement.DailyStreak): Boolean {
            val streak = streakRepository.getStreak().first()
            if (streak == null) {
                Timber.d("No streak data found")
                return false
            }

            val meetsRequirement = streak.currentStreak >= requirement.days
            Timber.d("DailyStreak check - Current: ${streak.currentStreak}, Required: ${requirement.days}, Met: $meetsRequirement")
            return meetsRequirement
        }

        /**
         * Checks if the problem speed requirement is met.
         * Note: This requires tracking solve time per problem, which is not yet implemented.
         * Returns false until per-problem timing is available.
         */
        private suspend fun checkProblemSpeed(requirement: BadgeRequirement.ProblemSpeed): Boolean {
            // TODO: Implement when per-problem timing is added
            // Need to check if any problem was solved within requirement.maxSeconds
            Timber.d("ProblemSpeed check not yet implemented - requires per-problem timing")
            return false
        }

        /**
         * Checks if the mixed sessions requirement is met.
         */
        private suspend fun checkMixedSessions(requirement: BadgeRequirement.MixedSessions): Boolean {
            val mixedSessions = sessionRepository.getSessionsByOperation(MathOperation.MIXED).first()
            return mixedSessions.size >= requirement.count
        }
    }
