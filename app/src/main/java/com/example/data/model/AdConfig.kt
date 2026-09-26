package com.example.data.model

data class AdConfig(
    val adsEnabled: Boolean = true,
    val testMode: Boolean = true,
    val adMobAppId: String = "ca-app-pub-3940256099942544~3347511713",
    val bannerAdUnitId: String = "ca-app-pub-3940256099942544/6300978111",
    val interstitialAdUnitId: String = "ca-app-pub-3940256099942544/1033173712",
    val rewardedAdUnitId: String = "ca-app-pub-3940256099942544/5224354917",
    val interstitialFrequency: Int = 2, // Every 2 recordings or script creations
    val impressionsCount: Long = 0L,
    val clicksCount: Long = 0L,
    val estimatedEarningsUsd: Double = 0.0
) {
    companion object {
        val DEFAULT = AdConfig()
    }
}
