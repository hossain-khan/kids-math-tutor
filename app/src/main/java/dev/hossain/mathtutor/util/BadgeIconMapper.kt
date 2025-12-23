package dev.hossain.mathtutor.util

import androidx.annotation.DrawableRes
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.domain.model.BadgeIcon

/**
 * Utility object for mapping BadgeIcon enum values to drawable resource IDs.
 * This indirection ensures database stability, as resource IDs can change between builds
 * but enum values remain constant.
 */
object BadgeIconMapper {
    /**
     * Maps a BadgeIcon enum to its corresponding drawable resource ID.
     *
     * @param icon The badge icon enum value
     * @return Drawable resource ID for the badge image
     */
    @DrawableRes
    fun toDrawableRes(icon: BadgeIcon): Int =
        when (icon) {
            // Getting Started badges
            BadgeIcon.FIRST_STEPS -> R.drawable.badge_first_steps

            BadgeIcon.PERFECT_START -> R.drawable.badge_perfect_star

            BadgeIcon.PERFECT_10 -> R.drawable.badge_perfect_10

            // Volume badges
            BadgeIcon.MATH_ROOKIE -> R.drawable.badge_math_rookie

            BadgeIcon.MATH_EXPLORER -> R.drawable.badge_math_explorer

            BadgeIcon.MATH_CHAMPION -> R.drawable.badge_math_champion

            BadgeIcon.MATH_LEGEND -> R.drawable.badge_math_legend

            // Operation Mastery badges
            BadgeIcon.ADDITION_EXPERT -> R.drawable.badge_addition_expert

            BadgeIcon.SUBTRACTION_STAR -> R.drawable.badge_subtraction_star

            BadgeIcon.MIX_MASTER -> R.drawable.badge_mix_master

            // Speed & Accuracy badges
            BadgeIcon.QUICK_THINKER -> R.drawable.badge_quick_thinker

            BadgeIcon.SHARP_SHOOTER -> R.drawable.badge_sharp_shooter

            BadgeIcon.PERFECTIONIST -> R.drawable.badge_perfectionist

            // Streak badges
            BadgeIcon.STREAK_STARTER -> R.drawable.badge_streak_starter

            BadgeIcon.DEDICATION_AWARD -> R.drawable.badge_dedication_award

            // Games badges
            BadgeIcon.GAME_MASTER -> R.drawable.badge_game_master

            BadgeIcon.SPEED_DEMON -> R.drawable.badge_speed_demon

            BadgeIcon.RACING_CHAMPION -> R.drawable.badge_racing_champion

            BadgeIcon.PERFECT_RACE -> R.drawable.badge_perfect_race

            // Memory Match badges
            BadgeIcon.MEMORY_MASTER -> R.drawable.badge_memory_master

            BadgeIcon.SHARP_MEMORY -> R.drawable.badge_sharp_memory

            BadgeIcon.LIGHTNING_MATCH -> R.drawable.badge_lightning_match

            BadgeIcon.PERFECT_MEMORY -> R.drawable.badge_perfect_memory

            // Number Sequence badges
            BadgeIcon.SEQUENCE_SOLVER -> R.drawable.badge_game_master

            // Reuse existing icon for now

            BadgeIcon.PATTERN_MASTER -> R.drawable.badge_quick_thinker

            // Reuse existing icon for now

            BadgeIcon.SEQUENCE_PRO -> R.drawable.badge_math_champion

            // Reuse existing icon for now

            BadgeIcon.QUICK_SEQUENCER -> R.drawable.badge_lightning_match // Reuse existing icon for now
        }
}
