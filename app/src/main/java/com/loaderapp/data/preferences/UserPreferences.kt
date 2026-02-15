package com.loaderapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    
    companion object {
        private val CURRENT_USER_ID = longPreferencesKey("current_user_id")
    }
    
    val currentUserId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_USER_ID]
    }
    
    suspend fun setCurrentUserId(userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID] = userId
        }
    }
    
    suspend fun clearCurrentUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(CURRENT_USER_ID)
        }
    }
}
