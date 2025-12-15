package dev.hossain.mathtutor.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dev.hossain.mathtutor.di.ApplicationContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

interface UserPreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class UserPreferencesRepositoryImpl
    constructor(
        @ApplicationContext private val context: Context,
    ) : UserPreferencesRepository {
        private object PreferencesKeys {
            val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        }

        override val isOnboardingCompleted: Flow<Boolean> =
            context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
            }

        override suspend fun setOnboardingCompleted(completed: Boolean) {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
            }
        }
    }
