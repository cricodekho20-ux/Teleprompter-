package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AdConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("teleprompter_ads_prefs", Context.MODE_PRIVATE)

    private val _adConfig = MutableStateFlow(loadAdConfig())
    val adConfig: StateFlow<AdConfig> = _adConfig.asStateFlow()

    private fun loadAdConfig(): AdConfig {
        return AdConfig(
            adsEnabled = prefs.getBoolean(KEY_ADS_ENABLED, true),
            testMode = prefs.getBoolean(KEY_TEST_MODE, true),
            adMobAppId = prefs.getString(KEY_ADMOB_APP_ID, "ca-app-pub-3940256099942544~3347511713") ?: "ca-app-pub-3940256099942544~3347511713",
            bannerAdUnitId = prefs.getString(KEY_BANNER_ID, "ca-app-pub-3940256099942544/6300978111") ?: "ca-app-pub-3940256099942544/6300978111",
            interstitialAdUnitId = prefs.getString(KEY_INTERSTITIAL_ID, "ca-app-pub-3940256099942544/1033173712") ?: "ca-app-pub-3940256099942544/1033173712",
            rewardedAdUnitId = prefs.getString(KEY_REWARDED_ID, "ca-app-pub-3940256099942544/5224354917") ?: "ca-app-pub-3940256099942544/5224354917",
            interstitialFrequency = prefs.getInt(KEY_FREQUENCY, 2),
            impressionsCount = prefs.getLong(KEY_IMPRESSIONS, 0L),
            clicksCount = prefs.getLong(KEY_CLICKS, 0L),
            estimatedEarningsUsd = prefs.getFloat(KEY_EARNINGS, 0.0f).toDouble()
        )
    }

    fun updateAdConfig(config: AdConfig) {
        _adConfig.value = config
        prefs.edit().apply {
            putBoolean(KEY_ADS_ENABLED, config.adsEnabled)
            putBoolean(KEY_TEST_MODE, config.testMode)
            putString(KEY_ADMOB_APP_ID, config.adMobAppId)
            putString(KEY_BANNER_ID, config.bannerAdUnitId)
            putString(KEY_INTERSTITIAL_ID, config.interstitialAdUnitId)
            putString(KEY_REWARDED_ID, config.rewardedAdUnitId)
            putInt(KEY_FREQUENCY, config.interstitialFrequency)
            putLong(KEY_IMPRESSIONS, config.impressionsCount)
            putLong(KEY_CLICKS, config.clicksCount)
            putFloat(KEY_EARNINGS, config.estimatedEarningsUsd.toFloat())
            apply()
        }
    }

    fun recordImpression(type: String = "banner") {
        val current = _adConfig.value
        val newImpressions = current.impressionsCount + 1
        // Typical eCPM approx $1.50 per 1000 banner impressions, $8.00 per 1000 interstitial/rewarded
        val addedRevenue = when (type) {
            "interstitial", "rewarded" -> 0.008
            else -> 0.0015
        }
        val updated = current.copy(
            impressionsCount = newImpressions,
            estimatedEarningsUsd = current.estimatedEarningsUsd + addedRevenue
        )
        updateAdConfig(updated)
    }

    fun recordClick() {
        val current = _adConfig.value
        val newClicks = current.clicksCount + 1
        val updated = current.copy(
            clicksCount = newClicks,
            estimatedEarningsUsd = current.estimatedEarningsUsd + 0.15 // average CPC $0.15
        )
        updateAdConfig(updated)
    }

    fun resetStats() {
        val current = _adConfig.value
        val updated = current.copy(
            impressionsCount = 0L,
            clicksCount = 0L,
            estimatedEarningsUsd = 0.0
        )
        updateAdConfig(updated)
    }

    companion object {
        private const val KEY_ADS_ENABLED = "ads_enabled"
        private const val KEY_TEST_MODE = "test_mode"
        private const val KEY_ADMOB_APP_ID = "admob_app_id"
        private const val KEY_BANNER_ID = "banner_id"
        private const val KEY_INTERSTITIAL_ID = "interstitial_id"
        private const val KEY_REWARDED_ID = "rewarded_id"
        private const val KEY_FREQUENCY = "frequency"
        private const val KEY_IMPRESSIONS = "impressions"
        private const val KEY_CLICKS = "clicks"
        private const val KEY_EARNINGS = "earnings"
    }
}
