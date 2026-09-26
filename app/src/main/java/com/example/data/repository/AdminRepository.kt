package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AdminConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("teleprompter_admin_prefs", Context.MODE_PRIVATE)

    private val _adminConfig = MutableStateFlow(loadAdminConfig())
    val adminConfig: StateFlow<AdminConfig> = _adminConfig.asStateFlow()

    private fun loadAdminConfig(): AdminConfig {
        return AdminConfig(
            adminPin = prefs.getString(KEY_PIN, "1234") ?: "1234",
            isAppFrozen = prefs.getBoolean(KEY_APP_FROZEN, false),
            freezeMessage = prefs.getString(KEY_FREEZE_MSG, "The application is currently under maintenance. Please check back later or contact admin.") ?: "The application is currently under maintenance.",
            isRecordingFrozen = prefs.getBoolean(KEY_RECORDING_FROZEN, false),
            isScriptCreationFrozen = prefs.getBoolean(KEY_SCRIPT_FROZEN, false),
            freeScriptsLimit = prefs.getInt(KEY_SCRIPTS_LIMIT, -1),
            maxFreeDurationSeconds = prefs.getInt(KEY_MAX_DURATION, -1),
            announcementBannerEnabled = prefs.getBoolean(KEY_ANNOUNCEMENT_ENABLED, false),
            announcementMessage = prefs.getString(KEY_ANNOUNCEMENT_MSG, "🌟 Welcome to Teleprompter Camera Studio! Enjoy high quality pro recording.") ?: "Welcome to Teleprompter Camera Studio!",
            requireRewardedAdForHd = prefs.getBoolean(KEY_REQUIRE_REWARDED_HD, false)
        )
    }

    fun updateAdminConfig(config: AdminConfig) {
        _adminConfig.value = config
        prefs.edit().apply {
            putString(KEY_PIN, config.adminPin)
            putBoolean(KEY_APP_FROZEN, config.isAppFrozen)
            putString(KEY_FREEZE_MSG, config.freezeMessage)
            putBoolean(KEY_RECORDING_FROZEN, config.isRecordingFrozen)
            putBoolean(KEY_SCRIPT_FROZEN, config.isScriptCreationFrozen)
            putInt(KEY_SCRIPTS_LIMIT, config.freeScriptsLimit)
            putInt(KEY_MAX_DURATION, config.maxFreeDurationSeconds)
            putBoolean(KEY_ANNOUNCEMENT_ENABLED, config.announcementBannerEnabled)
            putString(KEY_ANNOUNCEMENT_MSG, config.announcementMessage)
            putBoolean(KEY_REQUIRE_REWARDED_HD, config.requireRewardedAdForHd)
            apply()
        }
    }

    fun verifyPin(enteredPin: String): Boolean {
        return _adminConfig.value.adminPin == enteredPin
    }

    fun setAdminPin(newPin: String) {
        val updated = _adminConfig.value.copy(adminPin = newPin)
        updateAdminConfig(updated)
    }

    companion object {
        private const val KEY_PIN = "admin_pin"
        private const val KEY_APP_FROZEN = "is_app_frozen"
        private const val KEY_FREEZE_MSG = "freeze_msg"
        private const val KEY_RECORDING_FROZEN = "is_recording_frozen"
        private const val KEY_SCRIPT_FROZEN = "is_script_frozen"
        private const val KEY_SCRIPTS_LIMIT = "scripts_limit"
        private const val KEY_MAX_DURATION = "max_duration"
        private const val KEY_ANNOUNCEMENT_ENABLED = "announcement_enabled"
        private const val KEY_ANNOUNCEMENT_MSG = "announcement_msg"
        private const val KEY_REQUIRE_REWARDED_HD = "require_rewarded_hd"
    }
}
