package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ScriptTextAlign
import com.example.data.model.TeleprompterConfig
import com.example.data.model.TeleprompterLayoutMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("teleprompter_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<TeleprompterConfig> = _config.asStateFlow()

    private fun loadConfig(): TeleprompterConfig {
        return TeleprompterConfig(
            fontSizeSp = prefs.getFloat(KEY_FONT_SIZE, 36f),
            lineSpacingMultiplier = prefs.getFloat(KEY_LINE_SPACING, 1.35f),
            scrollSpeedMultiplier = prefs.getFloat(KEY_SCROLL_SPEED, 1.0f),
            backgroundOpacity = prefs.getFloat(KEY_BG_OPACITY, 0.75f),
            backgroundColorHex = prefs.getString(KEY_BG_COLOR, "#000000") ?: "#000000",
            boxHeightFactor = prefs.getFloat(KEY_BOX_HEIGHT, 0.35f),
            boxWidthFactor = prefs.getFloat(KEY_BOX_WIDTH, 0.92f),
            textColorHex = prefs.getString(KEY_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF",
            textAlign = try {
                ScriptTextAlign.valueOf(prefs.getString(KEY_TEXT_ALIGN, ScriptTextAlign.CENTER.name) ?: ScriptTextAlign.CENTER.name)
            } catch (_: Exception) {
                ScriptTextAlign.CENTER
            },
            layoutMode = try {
                TeleprompterLayoutMode.valueOf(prefs.getString(KEY_LAYOUT_MODE, TeleprompterLayoutMode.TOP.name) ?: TeleprompterLayoutMode.TOP.name)
            } catch (_: Exception) {
                TeleprompterLayoutMode.TOP
            },
            verticalPositionPercent = prefs.getFloat(KEY_VERT_POS, 0.15f),
            eyeLineGuideEnabled = prefs.getBoolean(KEY_EYE_LINE, false),
            eyeLinePositionPercent = prefs.getFloat(KEY_EYE_LINE_POS, 0.28f),
            mirrorCamera = prefs.getBoolean(KEY_MIRROR_CAM, true),
            mirrorText = prefs.getBoolean(KEY_MIRROR_TEXT, false),
            countdownSeconds = prefs.getInt(KEY_COUNTDOWN, 3),
            micEnabled = prefs.getBoolean(KEY_MIC, true),
            preferredCameraFacing = prefs.getString(KEY_CAM_FACING, "FRONT") ?: "FRONT",
            preferredResolution = prefs.getString(KEY_RESOLUTION, "1080p") ?: "1080p",
            frameRateFps = prefs.getInt(KEY_FPS, 30)
        )
    }

    fun updateConfig(newConfig: TeleprompterConfig) {
        _config.value = newConfig
        prefs.edit().apply {
            putFloat(KEY_FONT_SIZE, newConfig.fontSizeSp)
            putFloat(KEY_LINE_SPACING, newConfig.lineSpacingMultiplier)
            putFloat(KEY_SCROLL_SPEED, newConfig.scrollSpeedMultiplier)
            putFloat(KEY_BG_OPACITY, newConfig.backgroundOpacity)
            putString(KEY_BG_COLOR, newConfig.backgroundColorHex)
            putFloat(KEY_BOX_HEIGHT, newConfig.boxHeightFactor)
            putFloat(KEY_BOX_WIDTH, newConfig.boxWidthFactor)
            putString(KEY_TEXT_COLOR, newConfig.textColorHex)
            putString(KEY_TEXT_ALIGN, newConfig.textAlign.name)
            putString(KEY_LAYOUT_MODE, newConfig.layoutMode.name)
            putFloat(KEY_VERT_POS, newConfig.verticalPositionPercent)
            putBoolean(KEY_EYE_LINE, newConfig.eyeLineGuideEnabled)
            putFloat(KEY_EYE_LINE_POS, newConfig.eyeLinePositionPercent)
            putBoolean(KEY_MIRROR_CAM, newConfig.mirrorCamera)
            putBoolean(KEY_MIRROR_TEXT, newConfig.mirrorText)
            putInt(KEY_COUNTDOWN, newConfig.countdownSeconds)
            putBoolean(KEY_MIC, newConfig.micEnabled)
            putString(KEY_CAM_FACING, newConfig.preferredCameraFacing)
            putString(KEY_RESOLUTION, newConfig.preferredResolution)
            putInt(KEY_FPS, newConfig.frameRateFps)
            apply()
        }
    }

    fun resetToDefaults() {
        val defaultConfig = TeleprompterConfig.DEFAULT
        updateConfig(defaultConfig)
    }

    companion object {
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_LINE_SPACING = "line_spacing"
        private const val KEY_SCROLL_SPEED = "scroll_speed"
        private const val KEY_BG_OPACITY = "bg_opacity"
        private const val KEY_BG_COLOR = "bg_color"
        private const val KEY_BOX_HEIGHT = "box_height"
        private const val KEY_BOX_WIDTH = "box_width"
        private const val KEY_TEXT_COLOR = "text_color"
        private const val KEY_TEXT_ALIGN = "text_align"
        private const val KEY_LAYOUT_MODE = "layout_mode"
        private const val KEY_VERT_POS = "vert_pos"
        private const val KEY_EYE_LINE = "eye_line"
        private const val KEY_EYE_LINE_POS = "eye_line_pos"
        private const val KEY_MIRROR_CAM = "mirror_cam"
        private const val KEY_MIRROR_TEXT = "mirror_text"
        private const val KEY_COUNTDOWN = "countdown"
        private const val KEY_MIC = "mic_enabled"
        private const val KEY_CAM_FACING = "cam_facing"
        private const val KEY_RESOLUTION = "resolution"
        private const val KEY_FPS = "fps"
    }
}
