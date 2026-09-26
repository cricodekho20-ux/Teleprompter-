package com.example.data.model

data class AdminConfig(
    val adminPin: String = "1234",
    val isAppFrozen: Boolean = false,
    val freezeMessage: String = "The application is currently under maintenance. Please check back later or contact admin.",
    val isRecordingFrozen: Boolean = false,
    val isScriptCreationFrozen: Boolean = false,
    val freeScriptsLimit: Int = -1, // -1 means unlimited
    val maxFreeDurationSeconds: Int = -1, // -1 means unlimited
    val announcementBannerEnabled: Boolean = false,
    val announcementMessage: String = "🌟 Welcome to Teleprompter Camera Studio! Enjoy high quality pro recording.",
    val requireRewardedAdForHd: Boolean = false
) {
    companion object {
        val DEFAULT = AdminConfig()
    }
}
