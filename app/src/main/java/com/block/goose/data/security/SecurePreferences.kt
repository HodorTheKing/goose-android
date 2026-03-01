package com.block.goose.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context.applicationContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context.applicationContext,
        SECURE_PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val SECURE_PREFS_FILE_NAME = "goose_secure_prefs"
        private const val KEY_SECRET_KEY = "secret_key"
        private const val KEY_PIN_CERTIFICATE = "pin_certificate"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_LAST_UNLOCKED = "last_unlocked"
    }

    fun saveSecretKey(apiSecret: String) {
        sharedPreferences.edit().putString(KEY_SECRET_KEY, apiSecret).apply()
    }

    fun getSecretKey(): String? {
        return sharedPreferences.getString(KEY_SECRET_KEY, null)
    }

    fun clearSecretKey() {
        sharedPreferences.edit().remove(KEY_SECRET_KEY).apply()
    }

    fun isSecretKeyStored(): Boolean {
        return sharedPreferences.contains(KEY_SECRET_KEY)
    }

    fun setCertificatePinning(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_PIN_CERTIFICATE, enabled).apply()
    }

    fun isCertificatePinningEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_PIN_CERTIFICATE, false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun updateLastUnlocked() {
        sharedPreferences.edit().putLong(KEY_LAST_UNLOCKED, System.currentTimeMillis()).apply()
    }

    fun getLastUnlocked(): Long {
        return sharedPreferences.getLong(KEY_LAST_UNLOCKED, 0)
    }

    fun shouldRequireBiometric(sessionTimeoutMinutes: Int = 30): Boolean {
        if (!isBiometricEnabled()) return false
        val lastUnlocked = getLastUnlocked()
        val timeoutMs = sessionTimeoutMinutes * 60 * 1000
        return System.currentTimeMillis() - lastUnlocked > timeoutMs
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
