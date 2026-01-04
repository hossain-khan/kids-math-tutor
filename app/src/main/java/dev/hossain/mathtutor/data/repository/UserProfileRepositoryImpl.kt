package dev.hossain.mathtutor.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.analytics.UserProperty
import dev.hossain.mathtutor.data.local.userPreferencesDataStore
import dev.hossain.mathtutor.di.ApplicationContext
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant

/**
 * Implementation of [UserProfileRepository] using DataStore Preferences.
 * Stores user profile information persistently using key-value pairs.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class UserProfileRepositoryImpl
    constructor(
        @param:ApplicationContext private val context: Context,
        private val analyticsService: AnalyticsService,
    ) : UserProfileRepository {
        private object PreferencesKeys {
            val NAME_KEY = stringPreferencesKey("profile_name")
            val GRADE_KEY = stringPreferencesKey("profile_grade")
            val CREATED_AT_KEY = longPreferencesKey("profile_created_at")
            val ADAPTIVE_KEY = booleanPreferencesKey("profile_adaptive_difficulty")
        }

        override fun getProfile(): Flow<UserProfile?> =
            context.userPreferencesDataStore.data.map { preferences ->
                val gradeString = preferences[PreferencesKeys.GRADE_KEY]
                val createdAtMillis = preferences[PreferencesKeys.CREATED_AT_KEY]

                // If grade or createdAt is missing, return null (no profile created yet)
                if (gradeString == null || createdAtMillis == null) {
                    null
                } else {
                    try {
                        UserProfile(
                            name = preferences[PreferencesKeys.NAME_KEY],
                            gradeLevel = GradeLevel.valueOf(gradeString),
                            createdAt = Instant.ofEpochMilli(createdAtMillis),
                            adaptiveDifficultyEnabled = preferences[PreferencesKeys.ADAPTIVE_KEY] ?: false,
                        )
                    } catch (e: IllegalArgumentException) {
                        // If grade level string is invalid, return null
                        null
                    }
                }
            }

        override suspend fun saveProfile(profile: UserProfile) {
            try {
                Timber.d(
                    "UserProfileRepository: Saving profile - name=${profile.name}, " +
                        "gradeLevel=${profile.gradeLevel}, adaptive=${profile.adaptiveDifficultyEnabled}",
                )
                context.userPreferencesDataStore.edit { preferences ->
                    preferences[PreferencesKeys.NAME_KEY] =
                        profile.name
                            ?: "" // Store empty string instead of null
                    preferences[PreferencesKeys.GRADE_KEY] = profile.gradeLevel.name
                    preferences[PreferencesKeys.CREATED_AT_KEY] = profile.createdAt.toEpochMilli()
                    preferences[PreferencesKeys.ADAPTIVE_KEY] = profile.adaptiveDifficultyEnabled
                }
                Timber.d("UserProfileRepository: Profile saved successfully")

                // Update analytics user properties
                analyticsService.setUserProperty(
                    UserProperty.GRADE_LEVEL,
                    profile.gradeLevel.name,
                )
                analyticsService.setUserProperty(
                    UserProperty.HAS_COMPLETED_ONBOARDING,
                    "true",
                )
            } catch (e: Exception) {
                Timber.e(e, "UserProfileRepository: Failed to save profile")
                analyticsService.logError(e, "Profile save failed", isFatal = false)
                throw e
            }
        }

        override suspend fun updateGradeLevel(gradeLevel: GradeLevel) {
            try {
                Timber.d("UserProfileRepository: Updating grade level to $gradeLevel")
                context.userPreferencesDataStore.edit { preferences ->
                    preferences[PreferencesKeys.GRADE_KEY] = gradeLevel.name
                }
                Timber.d("UserProfileRepository: Grade level updated successfully")

                // Update analytics user property
                analyticsService.setUserProperty(
                    UserProperty.GRADE_LEVEL,
                    gradeLevel.name,
                )
            } catch (e: Exception) {
                Timber.e(e, "UserProfileRepository: Failed to update grade level")
                analyticsService.logError(e, "Grade level update failed", isFatal = false)
                throw e
            }
        }

        override suspend fun updateName(name: String?) {
            try {
                Timber.d("UserProfileRepository: Updating name to '$name'")
                context.userPreferencesDataStore.edit { preferences ->
                    preferences[PreferencesKeys.NAME_KEY] = name ?: ""
                }
                Timber.d("UserProfileRepository: Name updated successfully")
            } catch (e: Exception) {
                Timber.e(e, "UserProfileRepository: Failed to update name")
                analyticsService.logError(e, "Name update failed", isFatal = false)
                throw e
            }
        }

        override suspend fun updateAdaptiveDifficulty(enabled: Boolean) {
            try {
                Timber.d("UserProfileRepository: Updating adaptive difficulty to $enabled")
                context.userPreferencesDataStore.edit { preferences ->
                    preferences[PreferencesKeys.ADAPTIVE_KEY] = enabled
                }
                Timber.d("UserProfileRepository: Adaptive difficulty updated successfully")
            } catch (e: Exception) {
                Timber.e(e, "UserProfileRepository: Failed to update adaptive difficulty")
                analyticsService.logError(e, "Adaptive difficulty update failed", isFatal = false)
                throw e
            }
        }
    }
