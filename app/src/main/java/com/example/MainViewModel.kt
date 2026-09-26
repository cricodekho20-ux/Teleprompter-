package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AdConfig
import com.example.data.model.AdminConfig
import com.example.data.model.AspectRatio
import com.example.data.model.RecordedVideo
import com.example.data.model.Script
import com.example.data.model.TeleprompterConfig
import com.example.data.repository.AdRepository
import com.example.data.repository.AdminRepository
import com.example.data.repository.ScriptRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val scriptRepository = ScriptRepository(database.scriptDao())
    private val videoRepository = VideoRepository(database.videoDao())
    private val settingsRepository = SettingsRepository(application)
    private val adminRepository = AdminRepository(application)
    private val adRepository = AdRepository(application)

    val allScripts: StateFlow<List<Script>> = scriptRepository.allScripts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVideos: StateFlow<List<RecordedVideo>> = videoRepository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teleprompterConfig: StateFlow<TeleprompterConfig> = settingsRepository.config
    val adminConfig: StateFlow<AdminConfig> = adminRepository.adminConfig
    val adConfig: StateFlow<AdConfig> = adRepository.adConfig

    // Active session state
    private val _currentScript = MutableStateFlow<Script?>(null)
    val currentScript: StateFlow<Script?> = _currentScript.asStateFlow()

    private val _selectedAspectRatio = MutableStateFlow(AspectRatio.RATIO_9_16)
    val selectedAspectRatio: StateFlow<AspectRatio> = _selectedAspectRatio.asStateFlow()

    private val _latestRecordedVideo = MutableStateFlow<RecordedVideo?>(null)
    val latestRecordedVideo: StateFlow<RecordedVideo?> = _latestRecordedVideo.asStateFlow()

    private val _storageUsedBytes = MutableStateFlow(0L)
    val storageUsedBytes: StateFlow<Long> = _storageUsedBytes.asStateFlow()

    // Ad Trigger State
    private val _showInterstitialAd = MutableStateFlow(false)
    val showInterstitialAd: StateFlow<Boolean> = _showInterstitialAd.asStateFlow()

    private val _showRewardedAd = MutableStateFlow(false)
    val showRewardedAd: StateFlow<Boolean> = _showRewardedAd.asStateFlow()

    private var actionCounter = 0

    init {
        refreshStorageInfo()
    }

    fun setScript(script: Script?) {
        _currentScript.value = script
    }

    fun setScriptFromText(title: String, content: String, existingId: Long? = null) {
        val wordCount = Script.calculateWordCount(content)
        val estimatedSec = Script.calculateEstimatedSeconds(wordCount)
        _currentScript.value = Script(
            id = existingId ?: 0L,
            title = title.ifBlank { "Untitled Script" },
            content = content,
            wordCount = wordCount,
            estimatedSeconds = estimatedSec
        )
    }

    fun setAspectRatio(ratio: AspectRatio) {
        _selectedAspectRatio.value = ratio
    }

    fun updateConfig(config: TeleprompterConfig) {
        settingsRepository.updateConfig(config)
    }

    fun resetConfig() {
        settingsRepository.resetToDefaults()
    }

    // Admin Controls
    fun updateAdminConfig(config: AdminConfig) {
        adminRepository.updateAdminConfig(config)
    }

    fun verifyAdminPin(enteredPin: String): Boolean {
        return adminRepository.verifyPin(enteredPin)
    }

    fun setAdminPin(newPin: String) {
        adminRepository.setAdminPin(newPin)
    }

    fun toggleAppFreeze(freeze: Boolean, message: String = "") {
        val current = adminConfig.value
        val updated = current.copy(
            isAppFrozen = freeze,
            freezeMessage = message.ifBlank { current.freezeMessage }
        )
        adminRepository.updateAdminConfig(updated)
    }

    // Ads & Monetization Controls
    fun updateAdConfig(config: AdConfig) {
        adRepository.updateAdConfig(config)
    }

    fun triggerAdImpression(type: String = "banner") {
        adRepository.recordImpression(type)
    }

    fun triggerAdClick() {
        adRepository.recordClick()
    }

    fun resetAdStats() {
        adRepository.resetStats()
    }

    fun notifyUserActionForAds() {
        actionCounter++
        val config = adConfig.value
        if (config.adsEnabled && actionCounter % config.interstitialFrequency == 0) {
            _showInterstitialAd.value = true
        }
    }

    fun dismissInterstitialAd() {
        _showInterstitialAd.value = false
    }

    fun requestRewardedAd() {
        _showRewardedAd.value = true
    }

    fun dismissRewardedAd() {
        _showRewardedAd.value = false
    }

    // Script Management
    fun saveScript(title: String, content: String, existingId: Long? = null, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = scriptRepository.saveScript(title, content, existingId)
            val updated = scriptRepository.getScriptById(id)
            _currentScript.value = updated
            notifyUserActionForAds()
            onSaved(id)
        }
    }

    fun duplicateScript(script: Script) {
        viewModelScope.launch {
            scriptRepository.duplicateScript(script)
        }
    }

    fun deleteScript(id: Long) {
        viewModelScope.launch {
            scriptRepository.deleteScript(id)
            if (_currentScript.value?.id == id) {
                _currentScript.value = null
            }
        }
    }

    fun clearAllScripts() {
        viewModelScope.launch {
            scriptRepository.clearAllScripts()
            _currentScript.value = null
        }
    }

    // Video Management
    fun onVideoRecorded(
        title: String,
        filePath: String,
        durationMs: Long,
        aspectRatio: String,
        resolution: String,
        fileSizeBytes: Long,
        onComplete: (RecordedVideo) -> Unit
    ) {
        viewModelScope.launch {
            val id = videoRepository.saveVideo(
                title = title,
                filePath = filePath,
                durationMs = durationMs,
                aspectRatio = aspectRatio,
                resolution = resolution,
                fileSizeBytes = fileSizeBytes
            )
            val savedVideo = videoRepository.getVideoById(id)
            _latestRecordedVideo.value = savedVideo
            refreshStorageInfo()
            notifyUserActionForAds()
            if (savedVideo != null) {
                onComplete(savedVideo)
            }
        }
    }

    fun setLatestVideo(video: RecordedVideo?) {
        _latestRecordedVideo.value = video
    }

    fun deleteVideo(video: RecordedVideo) {
        viewModelScope.launch {
            videoRepository.deleteVideo(video)
            if (_latestRecordedVideo.value?.id == video.id) {
                _latestRecordedVideo.value = null
            }
            refreshStorageInfo()
        }
    }

    fun clearAllVideos() {
        viewModelScope.launch {
            videoRepository.clearAllVideos()
            _latestRecordedVideo.value = null
            refreshStorageInfo()
        }
    }

    private fun refreshStorageInfo() {
        viewModelScope.launch {
            _storageUsedBytes.value = videoRepository.getTotalStorageUsedBytes()
        }
    }
}
