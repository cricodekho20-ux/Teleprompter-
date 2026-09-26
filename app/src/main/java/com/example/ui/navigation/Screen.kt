package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object ScriptEditor : Screen("script_editor?scriptId={scriptId}") {
        fun createRoute(scriptId: Long? = null) = if (scriptId != null) "script_editor?scriptId=$scriptId" else "script_editor"
    }
    data object FormatSelection : Screen("format_selection")
    data object Studio : Screen("studio")
    data object VideoPreview : Screen("video_preview?videoId={videoId}") {
        fun createRoute(videoId: Long? = null) = if (videoId != null) "video_preview?videoId=$videoId" else "video_preview"
    }
    data object ScriptLibrary : Screen("script_library")
    data object VideoLibrary : Screen("video_library")
    data object Settings : Screen("settings")
    data object AdminPanel : Screen("admin_panel")
}
