package com.example.ui.format

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhotoCameraFront
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.example.data.model.AspectRatio
import com.example.ui.components.PermissionGate
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioTopBar
import com.example.ui.components.checkCameraAndAudioPermissions
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioBorderLight
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioGray
import com.example.ui.theme.StudioGrayLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioWhite

@Composable
fun AspectRatioSelectionScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onLaunchStudio: () -> Unit
) {
    val context = LocalContext.current
    val selectedRatio by viewModel.selectedAspectRatio.collectAsStateWithLifecycle()
    val config by viewModel.teleprompterConfig.collectAsStateWithLifecycle()

    var showPermissionGate by remember { mutableStateOf(false) }

    if (showPermissionGate) {
        PermissionGate(
            requireAudio = config.micEnabled,
            onPermissionsGranted = {
                showPermissionGate = false
                onLaunchStudio()
            },
            onBack = { showPermissionGate = false }
        )
        return
    }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "Video Format",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "SELECT ASPECT RATIO",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = StudioGrayLight
            )

            Text(
                text = "Choose the video aspect ratio for your recording output and camera framing.",
                style = MaterialTheme.typography.bodySmall,
                color = StudioGray
            )

            // 5 Required Aspect Ratio Cards
            AspectRatio.entries.forEach { ratio ->
                val isSelected = selectedRatio == ratio
                RatioSelectionCard(
                    ratio = ratio,
                    isSelected = isSelected,
                    onSelect = { viewModel.setAspectRatio(ratio) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Camera Setup Preferences
            Text(
                text = "CAMERA & AUDIO PREFERENCE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = StudioGrayLight
            )

            StudioCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Front / Back Camera Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (config.preferredCameraFacing == "FRONT") Icons.Default.PhotoCameraFront else Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = StudioWhite,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Camera Preference",
                                    fontWeight = FontWeight.SemiBold,
                                    color = StudioWhite
                                )
                                Text(
                                    text = if (config.preferredCameraFacing == "FRONT") "Front Selfie Camera" else "Rear Main Camera",
                                    fontSize = 12.sp,
                                    color = StudioGrayLight
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioCardBgElevated)
                                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            CameraToggleOption(
                                label = "Front",
                                isSelected = config.preferredCameraFacing == "FRONT",
                                onClick = {
                                    viewModel.updateConfig(config.copy(preferredCameraFacing = "FRONT"))
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            CameraToggleOption(
                                label = "Rear",
                                isSelected = config.preferredCameraFacing == "BACK",
                                onClick = {
                                    viewModel.updateConfig(config.copy(preferredCameraFacing = "BACK"))
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Microphone Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (config.micEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = null,
                                tint = if (config.micEnabled) StudioWhite else StudioRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Record Microphone Audio",
                                    fontWeight = FontWeight.SemiBold,
                                    color = StudioWhite
                                )
                                Text(
                                    text = if (config.micEnabled) "Captures voice from mic" else "Muted audio recording",
                                    fontSize = 12.sp,
                                    color = StudioGrayLight
                                )
                            }
                        }

                        Switch(
                            checked = config.micEnabled,
                            onCheckedChange = {
                                viewModel.updateConfig(config.copy(micEnabled = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StudioWhite,
                                checkedTrackColor = StudioRed,
                                uncheckedThumbColor = StudioGray,
                                uncheckedTrackColor = StudioBorder
                            ),
                            modifier = Modifier.testTag("mic_preference_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Start Recording CTA
            StudioPrimaryButton(
                text = "OPEN TELEPROMPTER STUDIO",
                onClick = {
                    if (checkCameraAndAudioPermissions(context, requireAudio = config.micEnabled)) {
                        onLaunchStudio()
                    } else {
                        showPermissionGate = true
                    }
                },
                icon = Icons.Default.Videocam,
                testTag = "open_teleprompter_button"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RatioSelectionCard(
    ratio: AspectRatio,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) StudioRed else StudioBorder
    val bgColor = if (isSelected) StudioCardBgElevated else StudioCardBg

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .testTag("ratio_card_${ratio.id.replace(':', '_')}")
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Visual Miniature Ratio Box
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(
                            width = when (ratio) {
                                AspectRatio.RATIO_9_16 -> 20.dp
                                AspectRatio.RATIO_4_5 -> 28.dp
                                AspectRatio.RATIO_1_1 -> 34.dp
                                AspectRatio.RATIO_16_9 -> 40.dp
                                AspectRatio.RATIO_3_4 -> 28.dp
                            },
                            height = when (ratio) {
                                AspectRatio.RATIO_9_16 -> 36.dp
                                AspectRatio.RATIO_4_5 -> 35.dp
                                AspectRatio.RATIO_1_1 -> 34.dp
                                AspectRatio.RATIO_16_9 -> 23.dp
                                AspectRatio.RATIO_3_4 -> 38.dp
                            }
                        )
                        .background(
                            if (isSelected) StudioRed.copy(alpha = 0.25f) else StudioCardBg,
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.5.dp,
                            if (isSelected) StudioRed else StudioGrayLight,
                            RoundedCornerShape(4.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ratio.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = StudioWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ratio.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) StudioWhite else StudioGrayLight
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Resolution: ${ratio.recommendedResolution}",
                    fontSize = 11.sp,
                    color = StudioGray
                )
            }

            // Radio Checkmark
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(
                        if (isSelected) StudioRed else StudioCardBgElevated,
                        CircleShape
                    )
                    .border(
                        1.dp,
                        if (isSelected) StudioRed else StudioBorderLight,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = StudioWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraToggleOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) StudioRed else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) StudioWhite else StudioGrayLight
        )
    }
}
