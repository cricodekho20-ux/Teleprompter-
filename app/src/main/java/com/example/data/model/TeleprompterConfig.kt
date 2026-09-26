package com.example.data.model

enum class TeleprompterLayoutMode(val displayName: String, val description: String) {
    TOP("Mode 1: Top", "Script docked at top of screen"),
    CENTER("Mode 2: Center", "Script placed at eye-level center"),
    BOTTOM("Mode 3: Bottom", "Script docked at bottom of screen"),
    OVERLAY("Mode 4: Overlay", "Script covers full frame with adjustable opacity"),
    SPLIT("Mode 5: Split", "Screen split into script area and camera preview")
}

enum class ScriptTextAlign(val label: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right")
}

data class TeleprompterConfig(
    val fontSizeSp: Float = 36f, // 16sp to 72sp
    val lineSpacingMultiplier: Float = 1.35f, // 1.0x to 2.5x
    val scrollSpeedMultiplier: Float = 1.0f, // 0.5x, 0.75x, 1x, 1.25x, 1.5x, 2x, 3x
    val backgroundOpacity: Float = 0.75f, // 0.0 (transparent) to 1.0 (solid dark)
    val backgroundColorHex: String = "#000000", // Pitch Black, Charcoal, Navy, Crimson, Emerald, Purple
    val boxHeightFactor: Float = 0.35f, // Height of prompter window (Chhoti: 0.20f, Normal: 0.35f, Lambi: 0.60f, Full: 0.90f)
    val boxWidthFactor: Float = 0.92f, // Width of prompter window (Compact: 0.70f, Standard: 0.92f, Full: 1.0f)
    val textColorHex: String = "#FFFFFF",
    val textAlign: ScriptTextAlign = ScriptTextAlign.CENTER,
    val layoutMode: TeleprompterLayoutMode = TeleprompterLayoutMode.TOP,
    val verticalPositionPercent: Float = 0.15f, // 0.0 (top) to 1.0 (bottom)
    val eyeLineGuideEnabled: Boolean = false,
    val eyeLinePositionPercent: Float = 0.28f, // Ideal eye level near camera punch hole
    val mirrorCamera: Boolean = true, // Default front camera mirror
    val mirrorText: Boolean = false, // Default normal readable text
    val countdownSeconds: Int = 3, // OFF (0), 3, 5, 10
    val micEnabled: Boolean = true,
    val preferredCameraFacing: String = "FRONT", // "FRONT" or "BACK"
    val preferredResolution: String = "1080p",
    val frameRateFps: Int = 30
) {
    companion object {
        val DEFAULT = TeleprompterConfig()
        
        val AVAILABLE_SPEEDS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f)
        val AVAILABLE_COUNTDOWNS = listOf(0, 3, 5, 10)
        val AVAILABLE_TEXT_COLORS = listOf(
            "#FFFFFF" to "Pure White",
            "#FFE600" to "Studio Yellow",
            "#00FF66" to "Vibrant Green",
            "#00E5FF" to "Cyan Blue",
            "#FF9900" to "Warm Amber"
        )
        val AVAILABLE_BG_COLORS = listOf(
            "#000000" to "Pitch Black",
            "#18181B" to "Deep Charcoal",
            "#0F172A" to "Midnight Navy",
            "#2A0808" to "Dark Crimson",
            "#062817" to "Forest Emerald",
            "#1E0B2B" to "Deep Purple"
        )
        val AVAILABLE_HEIGHTS = listOf(
            0.20f to "Chhoti (20%)",
            0.35f to "Standard (35%)",
            0.55f to "Lambi (55%)",
            0.85f to "Full Frame (85%)"
        )
        val AVAILABLE_WIDTHS = listOf(
            0.70f to "Compact (70%)",
            0.92f to "Standard (92%)",
            1.00f to "Full Width (100%)"
        )
    }
}
