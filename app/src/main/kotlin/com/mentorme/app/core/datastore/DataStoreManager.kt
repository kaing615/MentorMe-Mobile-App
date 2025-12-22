package com.mentorme.app.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
    }

    fun getToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    fun getUserId(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    suspend fun saveUserInfo(
        userId: String,
        email: String,
        name: String,
        role: String
    ) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_NAME_KEY] = name
            preferences[USER_ROLE_KEY] = role
        }
    }

    suspend fun clearUserInfo() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_ROLE_KEY)
        }
    }

    fun getUserRole(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_ROLE_KEY]
        }
    }

    fun getFcmToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[FCM_TOKEN_KEY]
        }
    }

    suspend fun saveFcmToken(token: String) {
        dataStore.edit { preferences ->
            preferences[FCM_TOKEN_KEY] = token
        }
    }
}
