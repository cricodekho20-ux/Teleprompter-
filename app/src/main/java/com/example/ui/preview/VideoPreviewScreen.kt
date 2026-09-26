package com.example.ui.preview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.data.model.AspectRatio
import com.example.ui.components.AdBannerView
import com.example.ui.components.InterstitialAdDialog
import com.example.ui.components.StudioOutlinedButton
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioGray
import com.example.ui.theme.StudioGrayLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioWhite
import java.io.File

@Composable
fun VideoPreviewScreen(
    viewModel: MainViewModel,
    videoId: Long? = null,
    onBack: () -> Unit,
    onRecordAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    val latestVideo by viewModel.latestRecordedVideo.collectAsStateWithLifecycle()
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val adConfig by viewModel.adConfig.collectAsStateWithLifecycle()
    val showInterstitial by viewModel.showInterstitialAd.collectAsStateWithLifecycle()

    val targetVideo = remember(videoId, latestVideo, allVideos) {
        if (videoId != null && videoId > 0) {
            allVideos.find { it.id == videoId } ?: latestVideo
        } else {
            latestVideo ?: allVideos.firstOrNull()
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    val videoFile = remember(targetVideo) {
        targetVideo?.let { File(it.filePath) }
    }

    DisposableEffect(Unit) {
        onDispose {
            videoViewRef?.stopPlayback()
        }
    }

    // Interstitial Ad Dialog if triggered
    if (showInterstitial && adConfig.adsEnabled) {
        InterstitialAdDialog(
            adConfig = adConfig,
            onDismiss = { viewModel.dismissInterstitialAd() },
            onImpression = { viewModel.triggerAdImpression("interstitial") },
            onClick = { viewModel.triggerAdClick() }
        )
    }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "Recording Result",
                onBack = onBack
            )
        },
        containerColor = StudioDarkBg,
        bottomBar = {
            if (adConfig.adsEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    AdBannerView(
                        adConfig = adConfig,
                        onImpression = { viewModel.triggerAdImpression("banner") },
                        onClick = { viewModel.triggerAdClick() }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (targetVideo == null || videoFile == null || !videoFile.exists()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(StudioCardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = StudioGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recorded video available",
                            color = StudioGrayLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                // Auto-Saved Banner (Direct Phone Gallery confirmation)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF102A1C)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth().testTag("gallery_saved_indicator")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Saved",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Auto-Saved to Phone Gallery",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Stored in Movies/Teleprompter album directly",
                                color = StudioGrayLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // In-App Video Player
                val parsedRatio = AspectRatio.fromId(targetVideo.aspectRatio)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(parsedRatio.ratioValue)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoPath(videoFile.absolutePath)
                                val mediaController = MediaController(ctx)
                                mediaController.setAnchorView(this)
                                setMediaController(mediaController)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                }
                                setOnCompletionListener {
                                    isPlaying = false
                                }
                                videoViewRef = this
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("preview_video_view")
                    )

                    // Overlay Play/Pause Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(56.dp)
                            .background(StudioDarkBg.copy(alpha = 0.65f), CircleShape)
                            .border(1.dp, StudioWhite.copy(alpha = 0.3f), CircleShape)
                            .clickable {
                                videoViewRef?.let { vv ->
                                    if (vv.isPlaying) {
                                        vv.pause()
                                        isPlaying = false
                                    } else {
                                        vv.start()
                                        isPlaying = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = StudioWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Metadata Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = targetVideo.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = StudioWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Duration: ${formatDuration(targetVideo.durationMs)}",
                                fontSize = 13.sp,
                                color = StudioGrayLight
                            )
                            Text(
                                text = "Format: ${targetVideo.aspectRatio} (${targetVideo.resolution})",
                                fontSize = 13.sp,
                                color = StudioGrayLight
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Size: ${formatFileSize(targetVideo.fileSizeBytes.coerceAtLeast(videoFile.length()))}",
                            fontSize = 12.sp,
                            color = StudioGray
                        )
                    }
                }

                // Primary Action: Share Video
                StudioPrimaryButton(
                    text = "SHARE VIDEO",
                    onClick = {
                        shareVideoFile(context, videoFile)
                    },
                    icon = Icons.Default.Share,
                    testTag = "share_video_button"
                )

                // Secondary Action Row: Record Again & Back to Home
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StudioOutlinedButton(
                        text = "Record Again",
                        onClick = onRecordAgain,
                        icon = Icons.Default.Refresh,
                        modifier = Modifier.weight(1f),
                        testTag = "record_again_button"
                    )

                    StudioOutlinedButton(
                        text = "Home",
                        onClick = onGoHome,
                        icon = Icons.Default.Home,
                        modifier = Modifier.weight(1f),
                        testTag = "go_home_button"
                    )
                }

                // Delete Recording Button
                StudioOutlinedButton(
                    text = "Delete Recording",
                    onClick = {
                        viewModel.deleteVideo(targetVideo)
                        Toast.makeText(context, "Recording deleted", Toast.LENGTH_SHORT).show()
                        onGoHome()
                    },
                    icon = Icons.Default.Delete,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "delete_video_button"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun shareVideoFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Teleprompter Video"))
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to share video file", Toast.LENGTH_SHORT).show()
    }
}

private fun formatDuration(millis: Long): String {
    val sec = (millis / 1000).toInt()
    return "${sec / 60}m ${sec % 60}s"
}

private fun formatFileSize(bytes: Long): String {
    val mb = bytes.toDouble() / (1024 * 1024)
    return String.format("%.1f MB", mb)
}
