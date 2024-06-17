package com.example.project_phairu.DataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSessionDataStore (private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")
        val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        // Add other keys for session data as needed
    }

    // Function to check if the user is logged in
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }

    // Function to get the stored user ID (or null if not logged in)
    val userIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    // Function to store user session data
    suspend fun storeUserSession(isLoggedIn: Boolean, userId: String) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = isLoggedIn
            preferences[USER_ID_KEY] = userId
            // Store other session data as needed
        }
    }

    // Function to clear user session data (logout)
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

}