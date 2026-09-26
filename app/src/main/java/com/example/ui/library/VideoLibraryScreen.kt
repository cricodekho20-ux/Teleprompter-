package com.example.ui.library

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.data.model.RecordedVideo
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioPrimaryButton
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VideoLibraryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onPlayVideo: (Long) -> Unit,
    onRecordNew: () -> Unit
) {
    val context = LocalContext.current
    val videos by viewModel.allVideos.collectAsStateWithLifecycle()
    val storageBytes by viewModel.storageUsedBytes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "My Videos (${videos.size})",
                onBack = onBack
            )
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
        ) {
            // Storage Information Card
            StudioCard(modifier = Modifier.padding(vertical = 8.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "APP VIDEO STORAGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioGrayLight
                        )
                        Text(
                            text = formatStorageSize(storageBytes),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioRed
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = (storageBytes.toFloat() / (500L * 1024 * 1024)).coerceIn(0.01f, 1.0f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = StudioRed,
                        trackColor = StudioBorder
                    )
                }
            }

            if (videos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = StudioGray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recorded videos yet",
                            color = StudioGrayLight,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        StudioPrimaryButton(
                            text = "RECORD FIRST VIDEO",
                            onClick = onRecordNew,
                            modifier = Modifier.width(220.dp),
                            testTag = "record_first_video_button"
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(videos, key = { it.id }) { video ->
                        VideoItemCard(
                            video = video,
                            onPlay = { onPlayVideo(video.id) },
                            onDelete = {
                                viewModel.deleteVideo(video)
                                Toast.makeText(context, "Video deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoItemCard(
    video: RecordedVideo,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(video.createdAt) {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(video.createdAt))
    }

    StudioCard(onClick = onPlay) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Video Thumbnail Box
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 74.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioCardBgElevated)
                    .border(1.dp, StudioBorderLight, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = StudioRed,
                    modifier = Modifier.size(28.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = video.aspectRatio,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioWhite
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = StudioWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Duration: ${formatDuration(video.durationMs)}",
                    fontSize = 12.sp,
                    color = StudioGrayLight
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = StudioGray
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp).testTag("delete_saved_video_${video.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = StudioGrayLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val sec = (millis / 1000).toInt()
    return "${sec / 60}m ${sec % 60}s"
}

private fun formatStorageSize(bytes: Long): String {
    val mb = bytes.toDouble() / (1024 * 1024)
    return String.format("%.1f MB used", mb)
}
