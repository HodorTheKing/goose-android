package com.block.goose.data.repository

import com.block.goose.data.security.SecurePreferences
import com.block.goose.data.api.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepository @Inject constructor(
    private val securePreferences: SecurePreferences
) {
    private val _themeFlow = MutableStateFlow(UserSettings.ThemeMode.SYSTEM)
    private val _textSizeFlow = MutableStateFlow(UserSettings.TextSize.NORMAL)
    private val _biometricFlow = MutableStateFlow(false)

    val themeFlow: Flow<UserSettings.ThemeMode> = _themeFlow.asStateFlow()
    val textSizeFlow: Flow<UserSettings.TextSize> = _textSizeFlow.asStateFlow()
    val biometricFlow: Flow<Boolean> = _biometricFlow.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _themeFlow.value = getThemeMode()
        _textSizeFlow.value = getTextSize()
        _biometricFlow.value = securePreferences.isBiometricEnabled()
    }

    // Theme
    fun setThemeMode(mode: UserSettings.ThemeMode) {
        _themeFlow.value = mode
    }

    fun getThemeMode(): UserSettings.ThemeMode {
        // TODO: Store in DataStore and retrieve
        return _themeFlow.value
    }

    // Text Size
    fun setTextSize(size: UserSettings.TextSize) {
        _textSizeFlow.value = size
    }

    fun getTextSize(): UserSettings.TextSize {
        return _textSizeFlow.value
    }

    // Cleanup settings
    suspend fun clearAllSettings() {
        securePreferences.clearAll()
    }

    fun setAutoClearDays(days: Int) {
        // TODO: Implement in DataStore
    }

    fun getAutoClearDays(): Int {
        // TODO: Get from DataStore
        return 30 // default
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        // TODO: Implement
    }

    fun isNotificationsEnabled(): Boolean = true

    fun setPinCertificate(enabled: Boolean) {
        securePreferences.setCertificatePinning(enabled)
    }

    fun isPinCertificateEnabled(): Boolean =
        securePreferences.isCertificatePinningEnabled()

    fun setBiometricEnabled(enabled: Boolean) {
        securePreferences.setBiometricEnabled(enabled)
        _biometricFlow.value = enabled
    }

    fun isBiometricEnabled(): Boolean =
        securePreferences.isBiometricEnabled()
}
