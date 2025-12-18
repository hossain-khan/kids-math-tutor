package dev.hossain.mathtutor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import java.time.Instant

// Shared DataStore for user preferences (also used by UserPreferencesRepository)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

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
    ) : UserProfileRepository {
        private object PreferencesKeys {
            val NAME_KEY = stringPreferencesKey("profile_name")
            val GRADE_KEY = stringPreferencesKey("profile_grade")
            val CREATED_AT_KEY = longPreferencesKey("profile_created_at")
            val ADAPTIVE_KEY = booleanPreferencesKey("profile_adaptive_difficulty")
        }

        override fun getProfile(): Flow<UserProfile?> =
            context.dataStore.data.map { preferences ->
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
                            adaptiveDifficultyEnabled = preferences[PreferencesKeys.ADAPTIVE_KEY] ?: true,
                        )
                    } catch (e: IllegalArgumentException) {
                        // If grade level string is invalid, return null
                        null
                    }
                }
            }

        override suspend fun saveProfile(profile: UserProfile) {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.NAME_KEY] =
                    profile.name
                        ?: "" // Store empty string instead of null
                preferences[PreferencesKeys.GRADE_KEY] = profile.gradeLevel.name
                preferences[PreferencesKeys.CREATED_AT_KEY] = profile.createdAt.toEpochMilli()
                preferences[PreferencesKeys.ADAPTIVE_KEY] = profile.adaptiveDifficultyEnabled
            }
        }

        override suspend fun updateGradeLevel(gradeLevel: GradeLevel) {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.GRADE_KEY] = gradeLevel.name
            }
        }

        override suspend fun updateName(name: String?) {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.NAME_KEY] = name ?: ""
            }
        }
    }
