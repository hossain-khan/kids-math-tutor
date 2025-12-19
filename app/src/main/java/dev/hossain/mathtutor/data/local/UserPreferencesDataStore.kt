package dev.hossain.mathtutor.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Singleton DataStore for user preferences.
 * Shared by UserPreferencesRepository and UserProfileRepository.
 */
val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
