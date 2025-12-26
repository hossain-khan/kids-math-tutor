package dev.hossain.mathtutor.domain.model

/**
 * Defines the initial set of 27 badges available in the app.
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
     * - Games (4 badges for Math Race, 4 for Memory Match, 4 for Number Sequence)
     *
     * @return List of all 27 badges
     */
    fun getAllBadges(): List<Badge> =
        listOf(
            // Getting Started Badges
            Badge(
                id = "first_steps",
                name = "First Steps",
                description = "Solve your first problem",
                icon = BadgeIcon.FIRST_STEPS,
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(1),
            ),
            Badge(
                id = "perfect_start",
                name = "Perfect Start",
                description = "Get 5 correct in a row",
                icon = BadgeIcon.PERFECT_START,
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ConsecutiveCorrect(5),
            ),
            Badge(
                id = "perfect_10",
                name = "Perfect 10",
                description = "Complete a session with 10/10 correct",
                icon = BadgeIcon.PERFECT_10,
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.SessionAccuracy(100f, 1),
            ),
            // Volume Badges
            Badge(
                id = "math_rookie",
                name = "Math Rookie",
                description = "Solve 25 total problems",
                icon = BadgeIcon.MATH_ROOKIE,
                category = BadgeCategory.VOLUME,
                requirement = BadgeRequirement.ProblemCount(25),
            ),
            Badge(
                id = "math_explorer",
                name = "Math Explorer",
                description = "Solve 50 total problems",
                icon = BadgeIcon.MATH_EXPLORER,
                category = BadgeCategory.VOLUME,
                requirement = BadgeRequirement.ProblemCount(50),
            ),
            Badge(
                id = "math_champion",
                name = "Math Champion",
                description = "Solve 100 total problems",
                icon = BadgeIcon.MATH_CHAMPION,
                category = BadgeCategory.VOLUME,
                requirement = BadgeRequirement.ProblemCount(100),
            ),
            Badge(
                id = "math_legend",
                name = "Math Legend",
                description = "Solve 500 total problems",
                icon = BadgeIcon.MATH_LEGEND,
                category = BadgeCategory.VOLUME,
                requirement = BadgeRequirement.ProblemCount(500),
            ),
            // Operation Mastery Badges
            Badge(
                id = "addition_expert",
                name = "Addition Expert",
                description = "Solve 50 addition problems",
                icon = BadgeIcon.ADDITION_EXPERT,
                category = BadgeCategory.OPERATION_MASTERY,
                requirement = BadgeRequirement.OperationCount(MathOperation.ADDITION, 50),
            ),
            Badge(
                id = "subtraction_star",
                name = "Subtraction Star",
                description = "Solve 50 subtraction problems",
                icon = BadgeIcon.SUBTRACTION_STAR,
                category = BadgeCategory.OPERATION_MASTERY,
                requirement = BadgeRequirement.OperationCount(MathOperation.SUBTRACTION, 50),
            ),
            Badge(
                id = "mix_master",
                name = "Mix Master",
                description = "Complete 10 mixed mode sessions",
                icon = BadgeIcon.MIX_MASTER,
                category = BadgeCategory.OPERATION_MASTERY,
                requirement = BadgeRequirement.MixedSessions(10),
            ),
            // Speed & Accuracy Badges
            Badge(
                id = "quick_thinker",
                name = "Quick Thinker",
                description = "Solve a problem in under 3 seconds",
                icon = BadgeIcon.QUICK_THINKER,
                category = BadgeCategory.SPEED_ACCURACY,
                requirement = BadgeRequirement.ProblemSpeed(3),
            ),
            Badge(
                id = "sharp_shooter",
                name = "Sharp Shooter",
                description = "Get 90%+ accuracy in a session",
                icon = BadgeIcon.SHARP_SHOOTER,
                category = BadgeCategory.SPEED_ACCURACY,
                requirement = BadgeRequirement.SessionAccuracy(90f, 1),
            ),
            Badge(
                id = "perfectionist",
                name = "Perfectionist",
                description = "Get 100% accuracy in 3 sessions",
                icon = BadgeIcon.PERFECTIONIST,
                category = BadgeCategory.SPEED_ACCURACY,
                requirement = BadgeRequirement.SessionAccuracy(100f, 3),
            ),
            // Streak Badges
            Badge(
                id = "streak_starter",
                name = "Streak Starter",
                description = "Practice 3 days in a row",
                icon = BadgeIcon.STREAK_STARTER,
                category = BadgeCategory.STREAK,
                requirement = BadgeRequirement.DailyStreak(3),
            ),
            Badge(
                id = "dedication_award",
                name = "Dedication Award",
                description = "Practice 7 days in a row",
                icon = BadgeIcon.DEDICATION_AWARD,
                category = BadgeCategory.STREAK,
                requirement = BadgeRequirement.DailyStreak(7),
            ),
            // Game Badges
            Badge(
                id = "game_master",
                name = "Game Master",
                description = "Play 10 games",
                icon = BadgeIcon.GAME_MASTER,
                category = BadgeCategory.GAMES,
                requirement = BadgeRequirement.GameCount(10),
            ),
            Badge(
                id = "speed_demon",
                name = "Speed Demon",
                description = "Score 20+ in Math Race",
                icon = BadgeIcon.SPEED_DEMON,
                category = BadgeCategory.GAMES,
                requirement = BadgeRequirement.MathRaceScore(20),
            ),
            Badge(
                id = "racing_champion",
                name = "Racing Champion",
                description = "Score 30+ in Math Race",
                icon = BadgeIcon.RACING_CHAMPION,
                category = BadgeCategory.GAMES,
                requirement = BadgeRequirement.MathRaceScore(30),
            ),
            Badge(
                id = "perfect_race",
                name = "Perfect Race",
                description = "100% accuracy in a game",
                icon = BadgeIcon.PERFECT_RACE,
                category = BadgeCategory.GAMES,
                requirement = BadgeRequirement.PerfectGameAccuracy,
            ),
            // Memory Match Badges
            Badge(
                id = "memory_master",
                name = "Memory Master",
                description = "Complete your first Memory Match",
                icon = BadgeIcon.MEMORY_MASTER,
                category = BadgeCategory.GAMES,
                requirement = BadgeRequirement.MemoryMatchCount(1),
            ),
            Badge(
                id = "sharp_memory",
                name = "Sharp Memory",
                description = "Complete Memory Match in 12 or fewer moves",
                icon = BadgeIcon.SHARP_MEMORY,
                category = BadgeCategory.GAMES,
                requirement = BadgeRequirement.MemoryMatchMoves(12),
            ),
            Badge(
                id = "lightning_match",
                name = "Lightning Match",
                description = "Complete Memory Match in under 60 seconds",
                icon = BadgeIcon.LIGHTNING_MATCH,
                category = BadgeCategory.GAMES,
                requirement = BadgeRequirement.MemoryMatchTime(60),
            ),
            Badge(
                id = "perfect_memory",
                name = "Perfect Memory",
                description = "Complete with exactly 8 moves (perfect game)",
                icon = BadgeIcon.PERFECT_MEMORY,
                category = BadgeCategory.GAMES,
                requirement = BadgeRequirement.PerfectMemoryMatch,
            ),
            // Number Sequence Badges
            Badge(
                id = "sequence_solver",
                name = "Sequence Solver",
                description = "Complete your first Number Sequence game",
                icon = BadgeIcon.SEQUENCE_SOLVER,
                category = BadgeCategory.GAMES,
                requirement = BadgeRequirement.NumberSequenceCount(1),
            ),
            Badge(
                id = "pattern_master",
                name = "Pattern Master",
                description = "Score 10+ in Number Sequence",
                icon = BadgeIcon.PATTERN_MASTER,
                category = BadgeCategory.GAMES,
                requirement = BadgeRequirement.NumberSequenceScore(10),
            ),
            Badge(
                id = "sequence_pro",
                name = "Sequence Pro",
                description = "Score 15+ in Number Sequence",
                icon = BadgeIcon.SEQUENCE_PRO,
                category = BadgeCategory.GAMES,
                requirement = BadgeRequirement.NumberSequenceScore(15),
            ),
            Badge(
                id = "quick_sequencer",
                name = "Quick Sequencer",
                description = "Complete Number Sequence in under 60 seconds",
                icon = BadgeIcon.QUICK_SEQUENCER,
                category = BadgeCategory.GAMES,
                requirement = BadgeRequirement.NumberSequenceTime(60),
            ),
        )
}
