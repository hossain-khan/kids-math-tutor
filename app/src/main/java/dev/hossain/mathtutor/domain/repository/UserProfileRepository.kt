package dev.hossain.mathtutor.domain.repository

import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user profile data management.
 * Provides methods to save, retrieve, and update user profile information.
 */
interface UserProfileRepository {
    /**
     * Retrieves the current user profile.
     * Returns null if no profile has been created yet.
     *
     * @return Flow of UserProfile or null
     */
    fun getProfile(): Flow<UserProfile?>

    /**
     * Saves a complete user profile to persistent storage.
     *
     * @param profile The user profile to save
     */
    suspend fun saveProfile(profile: UserProfile)

    /**
     * Updates only the grade level in the user profile.
     *
     * @param gradeLevel The new grade level
     */
    suspend fun updateGradeLevel(gradeLevel: GradeLevel)

    /**
     * Updates only the user's name in the profile.
     *
     * @param name The new user name (can be null to clear the name)
     */
    suspend fun updateName(name: String?)

    /**
     * Updates the adaptive difficulty setting in the profile.
     *
     * @param enabled Whether adaptive difficulty should be enabled
     */
    suspend fun updateAdaptiveDifficulty(enabled: Boolean)
}
