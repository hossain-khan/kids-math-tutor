package dev.hossain.mathtutor.analytics

/**
 * Predefined analytics event names.
 * Following Firebase Analytics naming conventions: lowercase with underscores.
 */
object AnalyticsEvent {
    // Screen events (automatically logged)
    const val SCREEN_VIEW = "screen_view"

    // Onboarding events
    const val ONBOARDING_STARTED = "onboarding_started"
    const val ONBOARDING_COMPLETED = "onboarding_completed"
    const val GRADE_SELECTED = "grade_selected"
    const val NAME_ENTERED = "name_entered"

    // Practice events
    const val PRACTICE_SESSION_STARTED = "practice_session_started"
    const val PRACTICE_SESSION_COMPLETED = "practice_session_completed"
    const val PROBLEM_ANSWERED = "problem_answered"
    const val PROBLEM_CORRECT = "problem_correct"
    const val PROBLEM_INCORRECT = "problem_incorrect"

    // Operation selection
    const val OPERATION_SELECTED = "operation_selected"
    const val MIXED_OPERATIONS_SELECTED = "mixed_operations_selected"

    // Badge events
    const val BADGE_UNLOCKED = "badge_unlocked"
    const val BADGES_VIEWED = "badges_viewed"

    // Game events
    const val GAME_STARTED = "game_started"
    const val GAME_COMPLETED = "game_completed"
    const val GAME_HIGH_SCORE = "game_high_score"

    // Settings events
    const val SETTINGS_CHANGED = "settings_changed"
    const val AUDIO_TOGGLED = "audio_toggled"
    const val HAPTICS_TOGGLED = "haptics_toggled"

    // Error events
    const val ERROR_OCCURRED = "error_occurred"
}

/**
 * Predefined analytics parameter keys.
 */
object AnalyticsParam {
    // Screen parameters
    const val SCREEN_NAME = "screen_name"
    const val SCREEN_CLASS = "screen_class"

    // User parameters
    const val GRADE_LEVEL = "grade_level"
    const val USER_NAME = "user_name"

    // Practice parameters
    const val OPERATION_TYPE = "operation_type"
    const val PROBLEM_COUNT = "problem_count"
    const val CORRECT_ANSWERS = "correct_answers"
    const val ACCURACY = "accuracy"
    const val SESSION_DURATION = "session_duration"
    const val SOLVE_TIME = "solve_time"

    // Badge parameters
    const val BADGE_ID = "badge_id"
    const val BADGE_NAME = "badge_name"
    const val BADGE_CATEGORY = "badge_category"

    // Game parameters
    const val GAME_ID = "game_id"
    const val GAME_SCORE = "game_score"
    const val GAME_DURATION = "game_duration"
    const val IS_NEW_RECORD = "is_new_record"

    // Settings parameters
    const val SETTING_NAME = "setting_name"
    const val SETTING_VALUE = "setting_value"

    // Error parameters
    const val ERROR_MESSAGE = "error_message"
    const val ERROR_CONTEXT = "error_context"
    const val IS_FATAL = "is_fatal"
}

/**
 * User property keys for analytics segmentation.
 */
object UserProperty {
    const val GRADE_LEVEL = "grade_level"
    const val HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
    const val TOTAL_PROBLEMS_SOLVED = "total_problems_solved"
    const val CURRENT_STREAK = "current_streak"
    const val TOTAL_BADGES_UNLOCKED = "total_badges_unlocked"
    const val GAMES_UNLOCKED = "games_unlocked"
}
