package com.block.goose.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.block.goose.data.api.GooseApiService
import com.block.goose.data.api.UserSettings
import com.block.goose.data.repository.SessionRepository
import com.block.goose.data.repository.UserSettingsRepository
import com.block.goose.data.security.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val baseUrl: String = "",
    val secretKey: String = "",
    val isTrialMode: Boolean = false,
    val themeMode: UserSettings.ThemeMode = UserSettings.ThemeMode.SYSTEM,
    val textSize: UserSettings.TextSize = UserSettings.TextSize.NORMAL,
    val isLoading: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.UNKNOWN,
    val error: String? = null,
    val certificatePinning: Boolean = false,
    val biometricEnabled: Boolean = false,
    val autoClearDays: Int = 30,
    val notificationsEnabled: Boolean = true
)

enum class ConnectionStatus {
    UNKNOWN, CONNECTING, CONNECTED, FAILED
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiService: GooseApiService,
    private val settingsRepository: com.block.goose.data.api.SettingsRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val securePreferences: SecurePreferences,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val TAG = "SettingsViewModel"
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.baseUrlFlow.collect { url ->
                _uiState.update { it.copy(baseUrl = url) }
            }
        }
        
        viewModelScope.launch {
            settingsRepository.isTrialModeFlow.collect { isTrial ->
                _uiState.update { it.copy(isTrialMode = isTrial) }
            }
        }
        
        viewModelScope.launch {
            userSettingsRepository.themeFlow.collect { theme ->
                _uiState.update { it.copy(themeMode = theme) }
            }
        }
        
        viewModelScope.launch {
            userSettingsRepository.textSizeFlow.collect { size ->
                _uiState.update { it.copy(textSize = size) }
            }
        }
        
        // Load secure settings
        _uiState.update {
            it.copy(
                secretKey = securePreferences.getSecretKey() ?: "",
                certificatePinning = securePreferences.isCertificatePinningEnabled(),
                biometricEnabled = securePreferences.isBiometricEnabled()
            )
        }
    }
    
    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, connectionStatus = ConnectionStatus.CONNECTING, error = null) }
            
            when (val result = apiService.testConnection()) {
                is com.block.goose.data.api.ApiResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            connectionStatus = ConnectionStatus.CONNECTED
                        )
                    }
                }
                is com.block.goose.data.api.ApiResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            connectionStatus = ConnectionStatus.FAILED,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    fun saveSettings(baseUrl: String, secretKey: String) {
        viewModelScope.launch {
            settingsRepository.saveSettings(baseUrl, secretKey)
            securePreferences.saveSecretKey(secretKey)
            _uiState.update { it.copy(secretKey = secretKey) }
        }
    }
    
    fun resetToTrialMode() {
        viewModelScope.launch {
            settingsRepository.resetToTrialMode()
            securePreferences.clearSecretKey()
            _uiState.update { it.copy(secretKey = "") }
        }
    }
    
    fun setThemeMode(mode: UserSettings.ThemeMode) {
        userSettingsRepository.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }
    
    fun setTextSize(size: UserSettings.TextSize) {
        userSettingsRepository.setTextSize(size)
        _uiState.update { it.copy(textSize = size) }
    }
    
    fun setCertificatePinning(enabled: Boolean) {
        securePreferences.setCertificatePinning(enabled)
        _uiState.update { it.copy(certificatePinning = enabled) }
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        securePreferences.setBiometricEnabled(enabled)
        _uiState.update { it.copy(biometricEnabled = enabled) }
    }
    
    fun setAutoClearDays(days: Int) {
        userSettingsRepository.setAutoClearDays(days)
        _uiState.update { it.copy(autoClearDays = days) }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        userSettingsRepository.setNotificationsEnabled(enabled)
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }
    
    fun clearLocalData() {
        viewModelScope.launch {
            sessionRepository.clearAllSessions()
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
