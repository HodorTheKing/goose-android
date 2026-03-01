package com.block.goose.data.api

import kotlinx.serialization.Serializable

object UserSettings {
    enum class ThemeMode {
        SYSTEM, LIGHT, DARK
    }

    enum class TextSize {
        SMALL, NORMAL, LARGE, EXTRA_LARGE
    }

    @Serializable
    data class AppSettings(
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
        val textSize: TextSize = TextSize.NORMAL,
        val autoClearDays: Int = 30,
        val notificationsEnabled: Boolean = true,
        val certificatePinning: Boolean = false,
        val biometricEnabled: Boolean = false
    )
}
