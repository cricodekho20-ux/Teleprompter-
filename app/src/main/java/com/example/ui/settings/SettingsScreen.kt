package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.data.model.TeleprompterConfig
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioOutlinedButton
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioSliderWithLabel
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioBorderLight
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioGray
import com.example.ui.theme.StudioGrayLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioWhite
import androidx.compose.material.icons.filled.AdminPanelSettings

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit = {}
) {
    val context = LocalContext.current
    val config by viewModel.teleprompterConfig.collectAsStateWithLifecycle()

    var showClearScriptsDialog by remember { mutableStateOf(false) }
    var showClearVideosDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "Settings",
                onBack = onBack
            )
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // TELEPROMPTER DEFAULTS
            Text(
                text = "TELEPROMPTER DEFAULTS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = StudioGrayLight
            )

            StudioCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StudioSliderWithLabel(
                        title = "Default Font Size",
                        valueLabel = "${config.fontSizeSp.toInt()} sp",
                        value = config.fontSizeSp,
                        range = 16f..72f,
                        onValueChange = { viewModel.updateConfig(config.copy(fontSizeSp = it)) }
                    )

                    StudioSliderWithLabel(
                        title = "Default Line Spacing",
                        valueLabel = "${String.format("%.1f", config.lineSpacingMultiplier)}x",
                        value = config.lineSpacingMultiplier,
                        range = 1.0f..2.2f,
                        onValueChange = { viewModel.updateConfig(config.copy(lineSpacingMultiplier = it)) }
                    )

                    StudioSliderWithLabel(
                        title = "Background Opacity",
                        valueLabel = "${(config.backgroundOpacity * 100).toInt()}%",
                        value = config.backgroundOpacity,
                        range = 0.0f..1.0f,
                        onValueChange = { viewModel.updateConfig(config.copy(backgroundOpacity = it)) }
                    )

                    StudioSliderWithLabel(
                        title = "Default Scroll Speed",
                        valueLabel = "${String.format("%.2f", config.scrollSpeedMultiplier)}x",
                        value = config.scrollSpeedMultiplier,
                        range = 0.5f..3.0f,
                        onValueChange = { viewModel.updateConfig(config.copy(scrollSpeedMultiplier = it)) }
                    )
                }
            }

            // CAMERA & AUDIO
            Text(
                text = "CAMERA & AUDIO",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = StudioGrayLight
            )

            StudioCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Countdown duration picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Countdown Timer", color = StudioWhite, fontWeight = FontWeight.SemiBold)
                            Text("Delay before recording starts", color = StudioGrayLight, fontSize = 11.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TeleprompterConfig.AVAILABLE_COUNTDOWNS.forEach { count ->
                                val isSelected = config.countdownSeconds == count
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) StudioRed else StudioCardBgElevated)
                                        .clickable { viewModel.updateConfig(config.copy(countdownSeconds = count)) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (count == 0) "OFF" else "${count}s",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioWhite
                                    )
                                }
                            }
                        }
                    }

                    // Resolution selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Recording Quality", color = StudioWhite, fontWeight = FontWeight.SemiBold)
                            Text("Target capture resolution", color = StudioGrayLight, fontSize = 11.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("720p", "1080p", "4K").forEach { res ->
                                val isSelected = config.preferredResolution == res
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) StudioRed else StudioCardBgElevated)
                                        .clickable { viewModel.updateConfig(config.copy(preferredResolution = res)) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = res,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioWhite
                                    )
                                }
                            }
                        }
                    }

                    // Frame rate selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Frame Rate", color = StudioWhite, fontWeight = FontWeight.SemiBold)
                            Text("Target FPS for video capture", color = StudioGrayLight, fontSize = 11.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(24, 30, 60).forEach { fps ->
                                val isSelected = config.frameRateFps == fps
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) StudioRed else StudioCardBgElevated)
                                        .clickable { viewModel.updateConfig(config.copy(frameRateFps = fps)) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${fps}fps",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioWhite
                                    )
                                }
                            }
                        }
                    }

                    // Camera Mirror toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Camera Mirror Mode", color = StudioWhite, fontWeight = FontWeight.SemiBold)
                            Text("Mirrors front selfie camera", color = StudioGrayLight, fontSize = 11.sp)
                        }
                        Switch(
                            checked = config.mirrorCamera,
                            onCheckedChange = { viewModel.updateConfig(config.copy(mirrorCamera = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StudioWhite,
                                checkedTrackColor = StudioRed,
                                uncheckedThumbColor = StudioGray,
                                uncheckedTrackColor = StudioBorder
                            )
                        )
                    }

                    // Audio mic default
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Record Microphone Audio", color = StudioWhite, fontWeight = FontWeight.SemiBold)
                            Text("Includes voice in recorded video", color = StudioGrayLight, fontSize = 11.sp)
                        }
                        Switch(
                            checked = config.micEnabled,
                            onCheckedChange = { viewModel.updateConfig(config.copy(micEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StudioWhite,
                                checkedTrackColor = StudioRed,
                                uncheckedThumbColor = StudioGray,
                                uncheckedTrackColor = StudioBorder
                            )
                        )
                    }
                }
            }

            // APP DATA MANAGEMENT
            Text(
                text = "DATA & STORAGE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = StudioGrayLight
            )

            StudioCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StudioOutlinedButton(
                        text = "Reset Teleprompter Defaults",
                        onClick = {
                            viewModel.resetConfig()
                            Toast.makeText(context, "Settings reset to studio defaults", Toast.LENGTH_SHORT).show()
                        },
                        icon = Icons.Default.RestartAlt,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "settings_reset_button"
                    )

                    StudioOutlinedButton(
                        text = "Clear Saved Scripts",
                        onClick = { showClearScriptsDialog = true },
                        icon = Icons.Default.Description,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "clear_scripts_button"
                    )

                    StudioOutlinedButton(
                        text = "Clear Saved Recordings",
                        onClick = { showClearVideosDialog = true },
                        icon = Icons.Default.VideoLibrary,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "clear_videos_button"
                    )
                }
            }

            // ADMIN & MONETIZATION
            Text(
                text = "ADMIN & MONETIZATION",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = StudioGrayLight
            )

            StudioCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Manage App Freeze, Killswitches, and Google AdMob revenue settings.",
                        color = StudioGrayLight,
                        fontSize = 12.sp
                    )
                    StudioPrimaryButton(
                        text = "OPEN ADMIN CONTROL HUB",
                        onClick = onOpenAdmin,
                        icon = Icons.Default.AdminPanelSettings,
                        testTag = "settings_open_admin_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Clear Scripts Dialog
        if (showClearScriptsDialog) {
            AlertDialog(
                onDismissRequest = { showClearScriptsDialog = false },
                title = { Text("Clear All Scripts?", color = StudioWhite) },
                text = { Text("Are you sure you want to delete all saved scripts? This cannot be undone.", color = StudioGrayLight) },
                containerColor = StudioCardBg,
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearAllScripts()
                            showClearScriptsDialog = false
                            Toast.makeText(context, "All scripts cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = StudioRed)
                    ) {
                        Text("Delete All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearScriptsDialog = false }) {
                        Text("Cancel", color = StudioWhite)
                    }
                }
            )
        }

        // Clear Videos Dialog
        if (showClearVideosDialog) {
            AlertDialog(
                onDismissRequest = { showClearVideosDialog = false },
                title = { Text("Clear All Recordings?", color = StudioWhite) },
                text = { Text("Are you sure you want to delete all recorded video files from the app? This cannot be undone.", color = StudioGrayLight) },
                containerColor = StudioCardBg,
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearAllVideos()
                            showClearVideosDialog = false
                            Toast.makeText(context, "All recordings cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = StudioRed)
                    ) {
                        Text("Delete All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearVideosDialog = false }) {
                        Text("Cancel", color = StudioWhite)
                    }
                }
            )
        }
    }
}
