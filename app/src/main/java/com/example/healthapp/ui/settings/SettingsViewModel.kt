package com.example.healthapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.data.prefs.UserPrefsDataStore
import com.example.healthapp.data.prefs.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel exposing editable settings backed by DataStore.
 */
class SettingsViewModel(
    private val userPrefs: UserPrefsDataStore
) : ViewModel() {

    data class UiState(
        val preferences: UserPreferences = UserPreferences(
            userId = "user-demo",
            baseUrl = "",
            token = null,
            allowHttp = false,
            demoMode = false
        )
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userPrefs.data.collectLatest { prefs ->
                _state.update { it.copy(preferences = prefs) }
            }
        }
    }

    fun updateUserId(userId: String) {
        viewModelScope.launch { userPrefs.updateUserId(userId) }
    }

    fun updateBaseUrl(url: String) {
        viewModelScope.launch { userPrefs.updateBaseUrl(url) }
    }

    fun updateToken(token: String) {
        viewModelScope.launch { userPrefs.updateToken(token) }
    }

    fun updateAllowHttp(allow: Boolean) {
        viewModelScope.launch { userPrefs.updateAllowHttp(allow) }
    }

    fun updateDemoMode(enabled: Boolean) {
        viewModelScope.launch { userPrefs.updateDemoMode(enabled) }
    }
}
