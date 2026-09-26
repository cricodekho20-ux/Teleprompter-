package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.MainViewModel
import com.example.ui.admin.AdminPanelScreen
import com.example.ui.editor.ScriptEditorScreen
import com.example.ui.format.AspectRatioSelectionScreen
import com.example.ui.home.HomeScreen
import com.example.ui.library.ScriptLibraryScreen
import com.example.ui.library.VideoLibraryScreen
import com.example.ui.preview.VideoPreviewScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.studio.RecordingStudioScreen

@Composable
fun AppNavigation(
    viewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onCreateVideo = {
                    navController.navigate(Screen.ScriptEditor.createRoute())
                },
                onOpenScripts = {
                    navController.navigate(Screen.ScriptLibrary.route)
                },
                onOpenVideos = {
                    navController.navigate(Screen.VideoLibrary.route)
                },
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onOpenAdmin = {
                    navController.navigate(Screen.AdminPanel.route)
                },
                onQuickRecordScript = { script ->
                    viewModel.setScript(script)
                    navController.navigate(Screen.FormatSelection.route)
                }
            )
        }

        // Script Editor Screen
        composable(
            route = Screen.ScriptEditor.route,
            arguments = listOf(
                navArgument("scriptId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val scriptId = backStackEntry.arguments?.getString("scriptId")?.toLongOrNull()
            ScriptEditorScreen(
                viewModel = viewModel,
                existingScriptId = scriptId,
                onBack = { navController.popBackStack() },
                onContinueToFormat = {
                    navController.navigate(Screen.FormatSelection.route)
                }
            )
        }

        // Format / Aspect Ratio Selection Screen
        composable(Screen.FormatSelection.route) {
            AspectRatioSelectionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onLaunchStudio = {
                    navController.navigate(Screen.Studio.route)
                }
            )
        }

        // Main Teleprompter Recording Studio Screen
        composable(Screen.Studio.route) {
            RecordingStudioScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRecordingFinished = {
                    navController.navigate(Screen.VideoPreview.route) {
                        popUpTo(Screen.Studio.route) { inclusive = true }
                    }
                }
            )
        }

        // Video Preview / Playback Result Screen
        composable(
            route = Screen.VideoPreview.route,
            arguments = listOf(
                navArgument("videoId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId")?.toLongOrNull()
            VideoPreviewScreen(
                viewModel = viewModel,
                videoId = videoId,
                onBack = { navController.popBackStack() },
                onRecordAgain = {
                    navController.navigate(Screen.Studio.route) {
                        popUpTo(Screen.VideoPreview.route) { inclusive = true }
                    }
                },
                onGoHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Script Library Screen
        composable(Screen.ScriptLibrary.route) {
            ScriptLibraryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNewScript = {
                    navController.navigate(Screen.ScriptEditor.createRoute())
                },
                onEditScript = { scriptId ->
                    navController.navigate(Screen.ScriptEditor.createRoute(scriptId))
                },
                onRecordScript = { script ->
                    viewModel.setScript(script)
                    navController.navigate(Screen.FormatSelection.route)
                }
            )
        }

        // Video Library Screen
        composable(Screen.VideoLibrary.route) {
            VideoLibraryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onPlayVideo = { videoId ->
                    navController.navigate(Screen.VideoPreview.createRoute(videoId))
                },
                onRecordNew = {
                    navController.navigate(Screen.ScriptEditor.createRoute())
                }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenAdmin = { navController.navigate(Screen.AdminPanel.route) }
            )
        }

        // Admin Panel Screen
        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
