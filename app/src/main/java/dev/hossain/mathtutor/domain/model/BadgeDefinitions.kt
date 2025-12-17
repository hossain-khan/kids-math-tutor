package dev.hossain.mathtutor.domain.model

/**
 * Defines the initial set of 15 badges available in the app.
 * Each badge has specific requirements that must be met to unlock it.
 */
object BadgeDefinitions {
    /**
     * Returns all available badges in the app.
     * Badges are organized by category:
     * - Getting Started (3 badges)
     * - Volume (4 badges)
     * - Operation Mastery (3 badges)
     * - Speed & Accuracy (3 badges)
     * - Streak (2 badges)
     *
     * @return List of all 15 badges
     */
    fun getAllBadges(): List<Badge> =
        listOf(
            // Getting Started Badges
            Badge(
                id = "first_steps",
                name = "First Steps",
                description = "Solve your first problem",
                icon = "🎯",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(1),
            ),
            Badge(
                id = "perfect_start",
                name = "Perfect Start",
                description = "Get 5 correct in a row",
                icon = "🚀",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ConsecutiveCorrect(5),
            ),
            Badge(
                id = "perfect_10",
                name = "Perfect 10",
                description = "Complete a session with 10/10 correct",
                icon = "🌟",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.SessionAccuracy(100f, 1),
            ),
            // Volume Badges
            Badge(
                id = "math_rookie",
                name = "Math Rookie",
                description = "Solve 25 total problems",
                icon = "🐣",
                category = BadgeCategory.VOLUME,
                requirement = BadgeRequirement.ProblemCount(25),
            ),
            Badge(
                id = "math_explorer",
                name = "Math Explorer",
                description = "Solve 50 total problems",
                icon = "🐤",
                category = BadgeCategory.VOLUME,
                requirement = BadgeRequirement.ProblemCount(50),
            ),
            Badge(
                id = "math_champion",
                name = "Math Champion",
                description = "Solve 100 total problems",
                icon = "🐥",
                category = BadgeCategory.VOLUME,
                requirement = BadgeRequirement.ProblemCount(100),
            ),
            Badge(
                id = "math_legend",
                name = "Math Legend",
                description = "Solve 500 total problems",
                icon = "🦅",
                category = BadgeCategory.VOLUME,
                requirement = BadgeRequirement.ProblemCount(500),
            ),
            // Operation Mastery Badges
            Badge(
                id = "addition_expert",
                name = "Addition Expert",
                description = "Solve 50 addition problems",
                icon = "➕",
                category = BadgeCategory.OPERATION_MASTERY,
                requirement = BadgeRequirement.OperationCount(MathOperation.ADDITION, 50),
            ),
            Badge(
                id = "subtraction_star",
                name = "Subtraction Star",
                description = "Solve 50 subtraction problems",
                icon = "➖",
                category = BadgeCategory.OPERATION_MASTERY,
                requirement = BadgeRequirement.OperationCount(MathOperation.SUBTRACTION, 50),
            ),
            Badge(
                id = "mix_master",
                name = "Mix Master",
                description = "Complete 10 mixed mode sessions",
                icon = "🔢",
                category = BadgeCategory.OPERATION_MASTERY,
                requirement = BadgeRequirement.MixedSessions(10),
            ),
            // Speed & Accuracy Badges
            Badge(
                id = "quick_thinker",
                name = "Quick Thinker",
                description = "Solve a problem in under 3 seconds",
                icon = "⚡",
                category = BadgeCategory.SPEED_ACCURACY,
                requirement = BadgeRequirement.ProblemSpeed(3),
            ),
            Badge(
                id = "sharp_shooter",
                name = "Sharp Shooter",
                description = "Get 90%+ accuracy in a session",
                icon = "🎯",
                category = BadgeCategory.SPEED_ACCURACY,
                requirement = BadgeRequirement.SessionAccuracy(90f, 1),
            ),
            Badge(
                id = "perfectionist",
                name = "Perfectionist",
                description = "Get 100% accuracy in 3 sessions",
                icon = "💯",
                category = BadgeCategory.SPEED_ACCURACY,
                requirement = BadgeRequirement.SessionAccuracy(100f, 3),
            ),
            // Streak Badges
            Badge(
                id = "streak_starter",
                name = "Streak Starter",
                description = "Practice 3 days in a row",
                icon = "🔥",
                category = BadgeCategory.STREAK,
                requirement = BadgeRequirement.DailyStreak(3),
            ),
            Badge(
                id = "dedication_award",
                name = "Dedication Award",
                description = "Practice 7 days in a row",
                icon = "🏆",
                category = BadgeCategory.STREAK,
                requirement = BadgeRequirement.DailyStreak(7),
            ),
        )
}
