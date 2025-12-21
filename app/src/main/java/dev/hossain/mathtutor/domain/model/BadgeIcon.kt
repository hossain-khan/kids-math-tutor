package dev.hossain.mathtutor.domain.model

/**
 * Enum representing all available badge icons in the app.
 * Each enum value maps to a specific drawable resource at runtime.
 * Using enum instead of resource IDs ensures database stability across builds,
 * since resource IDs can change during compilation.
 *
 * Naming convention: Matches the badge ID for clarity and consistency.
 */
enum class BadgeIcon {
    // Getting Started badges
    FIRST_STEPS,
    PERFECT_START,
    PERFECT_10,

    // Volume badges
    MATH_ROOKIE,
    MATH_EXPLORER,
    MATH_CHAMPION,
    MATH_LEGEND,

    // Operation Mastery badges
    ADDITION_EXPERT,
    SUBTRACTION_STAR,
    MIX_MASTER,

    // Speed & Accuracy badges
    QUICK_THINKER,
    SHARP_SHOOTER,
    PERFECTIONIST,

    // Streak badges
    STREAK_STARTER,
    DEDICATION_AWARD,

    // Games badges
    GAME_MASTER,
    SPEED_DEMON,
    RACING_CHAMPION,
    PERFECT_RACE,

    // Memory Match badges
    MEMORY_MASTER,
    SHARP_MEMORY,
    LIGHTNING_MATCH,
    PERFECT_MEMORY,
}
