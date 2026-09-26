package com.example.ui.home

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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.data.model.Script
import com.example.ui.components.AdBannerView
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioBorderLight
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioGrayLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioWhite

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onCreateVideo: () -> Unit,
    onOpenScripts: () -> Unit,
    onOpenVideos: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdmin: () -> Unit,
    onQuickRecordScript: (Script) -> Unit
) {
    val scripts by viewModel.allScripts.collectAsStateWithLifecycle()
    val videos by viewModel.allVideos.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    val adConfig by viewModel.adConfig.collectAsStateWithLifecycle()

    // 1. APP FREEZE OVERLAY (Admin Killswitch)
    if (adminConfig.isAppFrozen) {
        Scaffold(containerColor = StudioDarkBg) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("app_frozen_overlay"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, StudioRed)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(StudioRed.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "App Frozen",
                                tint = StudioRed,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Application Paused",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = adminConfig.freezeMessage,
                            fontSize = 14.sp,
                            color = StudioGrayLight,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onOpenAdmin,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Admin Portal Unlock", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
        return
    }

    // Normal Home Screen
    Scaffold(
        containerColor = StudioDarkBg,
        bottomBar = {
            if (adConfig.adsEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // App Brand Header with Admin & Settings buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(StudioRed, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STUDIO PRO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = StudioRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "TELEPROMPTER\nCAMERA",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                lineHeight = 32.sp
                            ),
                            color = StudioWhite
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Admin Panel Portal Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(StudioCardBgElevated)
                                .border(1.dp, StudioBorder, CircleShape)
                                .clickable { onOpenAdmin() }
                                .testTag("home_admin_icon"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = StudioWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Settings Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(StudioCardBgElevated)
                                .border(1.dp, StudioBorder, CircleShape)
                                .clickable { onOpenSettings() }
                                .testTag("home_settings_icon"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = StudioWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Public Announcement Banner if activated by Admin
            if (adminConfig.announcementBannerEnabled && adminConfig.announcementMessage.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("announcement_banner"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = "Announcement", tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = adminConfig.announcementMessage,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Main Primary Hero CTA: CREATE VIDEO
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(StudioCardBgElevated, StudioCardBg)
                            )
                        )
                        .border(1.5.dp, StudioRed.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ready to record?",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = StudioGrayLight
                            )
                            Box(
                                modifier = Modifier
                                    .background(StudioRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "DIRECT GALLERY SAVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Write your script, pick ratio & record. Video automatically saves directly into your phone gallery.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StudioWhite.copy(alpha = 0.9f),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        StudioPrimaryButton(
                            text = "CREATE VIDEO",
                            onClick = onCreateVideo,
                            icon = Icons.Default.Videocam,
                            testTag = "create_video_button"
                        )
                    }
                }
            }

            // 4 Navigation Grid Cards
            item {
                Text(
                    text = "STUDIO NAVIGATION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = StudioGrayLight
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NavTile(
                        title = "Create Video",
                        subtitle = "New Recording",
                        icon = Icons.Default.Videocam,
                        modifier = Modifier.weight(1f),
                        testTag = "nav_tile_create",
                        onClick = onCreateVideo
                    )
                    NavTile(
                        title = "My Scripts",
                        subtitle = "${scripts.size} Saved",
                        icon = Icons.Default.Description,
                        badgeCount = scripts.size,
                        modifier = Modifier.weight(1f),
                        testTag = "nav_tile_scripts",
                        onClick = onOpenScripts
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NavTile(
                        title = "My Videos",
                        subtitle = "${videos.size} Recorded",
                        icon = Icons.Default.VideoLibrary,
                        badgeCount = videos.size,
                        modifier = Modifier.weight(1f),
                        testTag = "nav_tile_videos",
                        onClick = onOpenVideos
                    )
                    NavTile(
                        title = "Admin Panel",
                        subtitle = "Freeze & Ads",
                        icon = Icons.Default.AdminPanelSettings,
                        modifier = Modifier.weight(1f),
                        testTag = "nav_tile_admin",
                        onClick = onOpenAdmin
                    )
                }
            }

            // Quick Starter Scripts
            if (scripts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUICK START SCRIPTS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = StudioGrayLight
                        )
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = StudioRed,
                            modifier = Modifier.clickable { onOpenScripts() }
                        )
                    }
                }

                items(scripts.take(3)) { script ->
                    StudioCard(
                        onClick = { onQuickRecordScript(script) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = script.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = StudioWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${script.wordCount} words • ~${script.estimatedSeconds}s reading time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StudioGrayLight
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(StudioRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Record",
                                    tint = StudioWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun NavTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
    testTag: String = "",
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(StudioCardBg)
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(StudioCardBgElevated, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = StudioRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (badgeCount != null && badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .background(StudioBorderLight, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StudioWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = StudioGrayLight
            )
        }
    }
}
