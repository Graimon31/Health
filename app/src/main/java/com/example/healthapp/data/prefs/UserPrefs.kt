package com.example.healthapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.healthapp.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Representation of user-configurable preferences.
 */
data class UserPreferences(
    val userId: String,
    val baseUrl: String,
    val token: String?,
    val allowHttp: Boolean,
    val demoMode: Boolean
)

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPrefsDataStore(private val context: Context) {

    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_BASE_URL = stringPreferencesKey("base_url")
    private val KEY_TOKEN = stringPreferencesKey("token")
    private val KEY_ALLOW_HTTP = booleanPreferencesKey("allow_http")
    private val KEY_DEMO_MODE = booleanPreferencesKey("demo_mode")

    val data: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            userId = prefs[KEY_USER_ID] ?: "user-demo",
            baseUrl = prefs[KEY_BASE_URL] ?: BuildConfig.DEFAULT_BASE_URL,
            token = prefs[KEY_TOKEN],
            allowHttp = prefs[KEY_ALLOW_HTTP] ?: BuildConfig.DEFAULT_ALLOW_HTTP,
            demoMode = prefs[KEY_DEMO_MODE] ?: false
        )
    }

    suspend fun current(): UserPreferences = data.first()

    suspend fun updateUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
        }
    }

    suspend fun updateBaseUrl(baseUrl: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BASE_URL] = baseUrl
        }
    }

    suspend fun updateToken(token: String) {
        context.dataStore.edit { prefs ->
            if (token.isBlank()) {
                prefs.remove(KEY_TOKEN)
            } else {
                prefs[KEY_TOKEN] = token
            }
        }
    }

    suspend fun updateAllowHttp(allow: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ALLOW_HTTP] = allow
        }
    }

    suspend fun updateDemoMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEMO_MODE] = enabled
        }
    }
}
