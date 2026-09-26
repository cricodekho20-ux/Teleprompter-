package com.example.ui.studio

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.camera.CameraManager
import com.example.camera.RecordingStatus
import com.example.data.model.AspectRatio
import com.example.data.model.TeleprompterConfig
import com.example.ui.studio.components.CountdownOverlay
import com.example.ui.studio.components.EyeLineGuide
import com.example.ui.studio.components.StudioSettingsBottomSheet
import com.example.ui.studio.components.TeleprompterOverlay
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioBorderLight
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingStudioScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onRecordingFinished: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val currentScript by viewModel.currentScript.collectAsStateWithLifecycle()
    val aspectRatio by viewModel.selectedAspectRatio.collectAsStateWithLifecycle()
    val config by viewModel.teleprompterConfig.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()

    val cameraManager = remember { CameraManager(context) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var isCameraReady by remember { mutableStateOf(false) }

    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordedDurationMs by remember { mutableLongStateOf(0L) }
    var isFinalizing by remember { mutableStateOf(false) }

    var isScrolling by remember { mutableStateOf(false) }
    var isCountingDown by remember { mutableStateOf(false) }
    var countdownValue by remember { mutableIntStateOf(3) }

    var showSettingsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Check if recording is frozen by Admin
    LaunchedEffect(adminConfig.isRecordingFrozen) {
        if (adminConfig.isRecordingFrozen) {
            Toast.makeText(context, "Recording is currently frozen by administrator", Toast.LENGTH_LONG).show()
        }
    }

    // Release camera safely on exit
    DisposableEffect(Unit) {
        onDispose {
            cameraManager.release()
        }
    }

    // Handle Back Press safely
    BackHandler {
        if (isRecording) {
            cameraManager.stopRecording()
            isRecording = false
        }
        onBack()
    }

    fun startRecordingFlow() {
        if (adminConfig.isRecordingFrozen) {
            Toast.makeText(context, "Recording is disabled by Admin", Toast.LENGTH_SHORT).show()
            return
        }

        val count = config.countdownSeconds
        if (count == 0) {
            isRecording = true
            isScrolling = true
            cameraManager.isAudioEnabled = config.micEnabled
            cameraManager.startRecording(
                outputTitle = currentScript?.title ?: "Video",
                onStatusUpdate = { status ->
                    handleRecordingStatus(
                        status = status,
                        context = context,
                        viewModel = viewModel,
                        currentScriptTitle = currentScript?.title ?: "Video",
                        aspectRatioStr = aspectRatio.id,
                        onSuccess = {
                            isRecording = false
                            isFinalizing = false
                            isScrolling = false
                            onRecordingFinished()
                        },
                        onError = {
                            isRecording = false
                            isFinalizing = false
                            isScrolling = false
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        },
                        onDurationUpdate = { dur, paused ->
                            recordedDurationMs = dur
                            isPaused = paused
                        }
                    )
                }
            )
        } else {
            isCountingDown = true
            countdownValue = count
            coroutineScope.launch {
                for (i in count downTo 1) {
                    countdownValue = i
                    delay(1000L)
                }
                isCountingDown = false
                isRecording = true
                isScrolling = true
                cameraManager.isAudioEnabled = config.micEnabled
                cameraManager.startRecording(
                    outputTitle = currentScript?.title ?: "Video",
                    onStatusUpdate = { status ->
                        handleRecordingStatus(
                            status = status,
                            context = context,
                            viewModel = viewModel,
                            currentScriptTitle = currentScript?.title ?: "Video",
                            aspectRatioStr = aspectRatio.id,
                            onSuccess = {
                                isRecording = false
                                isFinalizing = false
                                isScrolling = false
                                onRecordingFinished()
                            },
                            onError = {
                                isRecording = false
                                isFinalizing = false
                                isScrolling = false
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            },
                            onDurationUpdate = { dur, paused ->
                                recordedDurationMs = dur
                                isPaused = paused
                            }
                        )
                    }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview Layer Framed by Aspect Ratio
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = if (aspectRatio == AspectRatio.RATIO_9_16) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio.ratioValue)
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            if (isRecording) 2.dp else 1.dp,
                            if (isRecording) StudioRed else StudioBorder,
                            RoundedCornerShape(14.dp)
                        )
                }
            ) {
                val mirrorScale = if (cameraManager.isFrontCamera && config.mirrorCamera) -1f else 1f

                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            previewView = this
                            cameraManager.initCamera(
                                lifecycleOwner = lifecycleOwner,
                                previewView = this,
                                selectedAspectRatio = aspectRatio,
                                resolutionQuality = config.preferredResolution,
                                useFrontCamera = config.preferredCameraFacing == "FRONT"
                            ) { ready ->
                                isCameraReady = ready
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scaleX = mirrorScale, scaleY = 1f)
                        .testTag("camera_preview_view")
                )
            }
        }

        // Teleprompter Layer
        TeleprompterOverlay(
            scriptText = currentScript?.content ?: "",
            config = config,
            isScrolling = isScrolling,
            onToggleScrolling = { isScrolling = !isScrolling },
            onRestartScrolling = { isScrolling = true },
            onUpdateConfig = { viewModel.updateConfig(it) },
            modifier = Modifier.fillMaxSize()
        )

        // Eye Line Guide Layer
        if (config.eyeLineGuideEnabled) {
            EyeLineGuide(
                positionPercent = config.eyeLinePositionPercent,
                onPositionChanged = { newPos ->
                    viewModel.updateConfig(config.copy(eyeLinePositionPercent = newPos))
                }
            )
        }

        // Studio Top Bar (Back, Ratio, Cam Flip, Mic, Settings)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, StudioBorderLight, CircleShape)
                    .clickable {
                        if (isRecording) {
                            cameraManager.stopRecording()
                        }
                        onBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = StudioWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Ratio Badge & Live Recording Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, if (isRecording) StudioRed else StudioBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(StudioRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatTimer(recordedDurationMs),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioWhite
                    )
                } else {
                    Text(
                        text = "${aspectRatio.id} • ${config.preferredResolution}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioWhite
                    )
                }
            }

            // Quick Studio Controls (Flip Cam, Flash, Mic, Settings)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Camera Flip
                IconButton(
                    onClick = {
                        previewView?.let { pv ->
                            cameraManager.switchCamera(lifecycleOwner, pv, config.preferredResolution)
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("switch_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = StudioWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Microphone toggle
                IconButton(
                    onClick = {
                        val nextMic = !config.micEnabled
                        viewModel.updateConfig(config.copy(micEnabled = nextMic))
                        cameraManager.isAudioEnabled = nextMic
                        Toast.makeText(
                            context,
                            if (nextMic) "Microphone ON" else "Microphone OFF",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("toggle_mic_button")
                ) {
                    Icon(
                        imageVector = if (config.micEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Toggle Mic",
                        tint = if (config.micEnabled) StudioWhite else StudioRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Torch toggle
                IconButton(
                    onClick = {
                        val isOn = cameraManager.toggleTorch()
                        Toast.makeText(
                            context,
                            if (isOn) "Torch ON" else "Torch OFF",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("toggle_torch_button")
                ) {
                    Icon(
                        imageVector = if (cameraManager.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Torch",
                        tint = if (cameraManager.isTorchOn) StudioRed else StudioWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Settings Bottom Sheet Button
                IconButton(
                    onClick = { showSettingsSheet = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("recording_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Prompter Settings",
                        tint = StudioWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Countdown Screen Overlay (3, 2, 1, RECORD!)
        AnimatedVisibility(
            visible = isCountingDown,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CountdownOverlay(count = countdownValue)
        }

        // Finalizing Loading Indicator
        if (isFinalizing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = StudioRed)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Auto-Saving to Gallery...",
                        color = StudioWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Studio Bottom Control Bar (Main RECORD / STOP Button)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (isRecording) {
                    // Pause / Resume recording
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .border(1.dp, StudioBorderLight, CircleShape)
                            .clickable {
                                if (isPaused) {
                                    cameraManager.resumeRecording()
                                    isPaused = false
                                } else {
                                    cameraManager.pauseRecording()
                                    isPaused = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = StudioWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Main STOP Button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.8f))
                            .border(3.dp, StudioRed, CircleShape)
                            .clickable {
                                isFinalizing = true
                                cameraManager.stopRecording()
                            }
                            .testTag("stop_recording_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(StudioRed)
                        )
                    }

                    Spacer(modifier = Modifier.size(48.dp))
                } else {
                    // Main RECORD Button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.8f))
                            .border(3.dp, StudioWhite, CircleShape)
                            .clickable {
                                startRecordingFlow()
                            }
                            .testTag("start_recording_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(StudioRed)
                        )
                    }
                }
            }
        }

        // Studio Settings Bottom Sheet
        if (showSettingsSheet) {
            StudioSettingsBottomSheet(
                sheetState = sheetState,
                config = config,
                onUpdateConfig = { viewModel.updateConfig(it) },
                onResetDefaults = { viewModel.resetConfig() },
                onDismiss = { showSettingsSheet = false }
            )
        }
    }
}

private fun handleRecordingStatus(
    status: RecordingStatus,
    context: Context,
    viewModel: MainViewModel,
    currentScriptTitle: String,
    aspectRatioStr: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onDurationUpdate: (Long, Boolean) -> Unit
) {
    when (status) {
        is RecordingStatus.Recording -> {
            onDurationUpdate(status.durationMs, status.isPaused)
        }
        is RecordingStatus.Success -> {
            val parsedRatio = AspectRatio.fromId(aspectRatioStr)
            viewModel.onVideoRecorded(
                title = currentScriptTitle,
                filePath = status.file.absolutePath,
                durationMs = status.durationMs,
                aspectRatio = aspectRatioStr,
                resolution = parsedRatio.recommendedResolution,
                fileSizeBytes = status.file.length()
            ) {
                onSuccess()
            }
        }
        is RecordingStatus.Error -> {
            onError(status.message)
        }
        RecordingStatus.Idle -> {}
    }
}

private fun formatTimer(millis: Long): String {
    val totalSeconds = (millis / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
