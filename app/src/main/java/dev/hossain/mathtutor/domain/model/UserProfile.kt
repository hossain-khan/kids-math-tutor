package dev.hossain.mathtutor.domain.model

import java.time.Instant

/**
 * Represents a user profile with preferences for the math tutor app.
 *
 * @property name Optional user name
 * @property gradeLevel The user's current grade level
 * @property createdAt Timestamp when the profile was created
 * @property adaptiveDifficultyEnabled Whether adaptive difficulty is enabled (defaults to false)
 */
data class UserProfile(
    val name: String? = null,
    val gradeLevel: GradeLevel,
    val createdAt: Instant,
    val adaptiveDifficultyEnabled: Boolean = false,
)
