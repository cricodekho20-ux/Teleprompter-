package com.example.ui.admin

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.data.model.AdConfig
import com.example.data.model.AdminConfig
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioGray
import com.example.ui.theme.StudioGrayLight
import com.example.ui.theme.StudioTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    val adConfig by viewModel.adConfig.collectAsStateWithLifecycle()
    val allScripts by viewModel.allScripts.collectAsStateWithLifecycle()
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val storageBytes by viewModel.storageUsedBytes.collectAsStateWithLifecycle()

    var isAuthenticated by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }

    // State for local editing
    var freezeMsg by remember(adminConfig) { mutableStateOf(adminConfig.freezeMessage) }
    var announcementMsg by remember(adminConfig) { mutableStateOf(adminConfig.announcementMessage) }
    var newPinInput by remember { mutableStateOf("") }
    var showPinChangeDialog by remember { mutableStateOf(false) }

    var adMobAppId by remember(adConfig) { mutableStateOf(adConfig.adMobAppId) }
    var bannerUnitId by remember(adConfig) { mutableStateOf(adConfig.bannerAdUnitId) }
    var interstitialUnitId by remember(adConfig) { mutableStateOf(adConfig.interstitialAdUnitId) }
    var rewardedUnitId by remember(adConfig) { mutableStateOf(adConfig.rewardedAdUnitId) }

    if (!isAuthenticated) {
        // PIN Verification Screen
        Scaffold(
            topBar = {
                StudioTopBar(
                    title = "Admin Authentication",
                    onBack = onBack
                )
            },
            containerColor = StudioDarkBg
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(StudioAccent.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = "Admin Lock",
                                tint = StudioAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Admin Portal Access",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Enter your 4-digit Master PIN to manage freeze, monetization & settings (Default: 1234)",
                            fontSize = 13.sp,
                            color = StudioGrayLight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (it.length <= 8) {
                                    pinInput = it
                                    pinError = false
                                }
                            },
                            label = { Text("Master PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            isError = pinError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_pin_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioAccent,
                                unfocusedBorderColor = StudioBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        if (pinError) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Incorrect PIN. Default is 1234", color = StudioAccent, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        StudioPrimaryButton(
                            text = "UNLOCK ADMIN PANEL",
                            onClick = {
                                if (viewModel.verifyAdminPin(pinInput)) {
                                    isAuthenticated = true
                                    pinError = false
                                } else {
                                    pinError = true
                                }
                            },
                            icon = Icons.Default.Lock,
                            testTag = "unlock_admin_button"
                        )
                    }
                }
            }
        }
        return
    }

    // Main Authenticated Admin Screen
    Scaffold(
        topBar = {
            StudioTopBar(
                title = "Admin Control Hub",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showPinChangeDialog = true }) {
                        Icon(Icons.Default.Key, contentDescription = "Change PIN", tint = Color.White)
                    }
                }
            )
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Admin Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF141416),
                contentColor = StudioAccent,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = StudioAccent
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Freeze & Controls", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.PauseCircle, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Ads & AdMob", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("System & Stats", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 1: FREEZE & SYSTEM CONTROLS
                        AdminSectionHeader("Master App Freeze & Killswitches", Icons.Default.PauseCircle)

                        // Master App Freeze Toggle Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (adminConfig.isAppFrozen) Color(0xFF3B1212) else StudioCard
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (adminConfig.isAppFrozen) StudioAccent else StudioBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "App Freeze / Maintenance Mode",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (adminConfig.isAppFrozen) StudioAccent else Color.White
                                        )
                                        Text(
                                            text = if (adminConfig.isAppFrozen) "APP IS CURRENTLY FROZEN for normal users" else "App is active and running normally",
                                            fontSize = 12.sp,
                                            color = StudioGrayLight
                                        )
                                    }
                                    Switch(
                                        checked = adminConfig.isAppFrozen,
                                        onCheckedChange = { isFrozen ->
                                            viewModel.updateAdminConfig(adminConfig.copy(isAppFrozen = isFrozen, freezeMessage = freezeMsg))
                                            Toast.makeText(context, if (isFrozen) "App Frozen for Users" else "App Unfrozen", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = StudioAccent
                                        ),
                                        modifier = Modifier.testTag("app_freeze_switch")
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = freezeMsg,
                                    onValueChange = { freezeMsg = it },
                                    label = { Text("Freeze / Maintenance Notice Message") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = StudioAccent,
                                        unfocusedBorderColor = StudioBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        viewModel.updateAdminConfig(adminConfig.copy(freezeMessage = freezeMsg))
                                        Toast.makeText(context, "Maintenance message updated", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A)),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Message", fontSize = 12.sp)
                                }
                            }
                        }

                        // Feature Freezes
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Feature-Level Freezes", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)

                                AdminToggleRow(
                                    title = "Freeze Video Recording",
                                    subtitle = "Prevent users from starting new recordings",
                                    checked = adminConfig.isRecordingFrozen,
                                    onCheckedChange = {
                                        viewModel.updateAdminConfig(adminConfig.copy(isRecordingFrozen = it))
                                    }
                                )

                                Divider(color = StudioBorder)

                                AdminToggleRow(
                                    title = "Freeze Script Creation",
                                    subtitle = "Prevent users from adding new scripts",
                                    checked = adminConfig.isScriptCreationFrozen,
                                    onCheckedChange = {
                                        viewModel.updateAdminConfig(adminConfig.copy(isScriptCreationFrozen = it))
                                    }
                                )
                            }
                        }

                        // Live Announcement Broadcaster
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Public Announcement Alert", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                        Text("Show a broadcast banner at the top of Home screen", fontSize = 12.sp, color = StudioGrayLight)
                                    }
                                    Switch(
                                        checked = adminConfig.announcementBannerEnabled,
                                        onCheckedChange = {
                                            viewModel.updateAdminConfig(adminConfig.copy(announcementBannerEnabled = it, announcementMessage = announcementMsg))
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = announcementMsg,
                                    onValueChange = { announcementMsg = it },
                                    label = { Text("Announcement Text") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2563EB),
                                        unfocusedBorderColor = StudioBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        viewModel.updateAdminConfig(adminConfig.copy(announcementMessage = announcementMsg))
                                        Toast.makeText(context, "Announcement banner saved", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Broadcast Alert", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 2: ADS & GOOGLE ADMOB MONETIZATION
                        AdminSectionHeader("Google AdMob / AdSense Monetization", Icons.Default.MonetizationOn)

                        // Real-Time Ad Earnings Dashboard Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF14241B)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💰 Live Ad Performance", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 16.sp)
                                    IconButton(onClick = { viewModel.resetAdStats() }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Reset Stats", tint = StudioGrayLight, modifier = Modifier.size(20.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    StatItem("Estimated Rev", String.format("$%.3f", adConfig.estimatedEarningsUsd), Color(0xFF10B981))
                                    StatItem("Impressions", "${adConfig.impressionsCount}", Color.White)
                                    StatItem("Clicks", "${adConfig.clicksCount}", Color(0xFF60A5FA))
                                }
                            }
                        }

                        // Ads Master Switch
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                AdminToggleRow(
                                    title = "Enable In-App Ads",
                                    subtitle = "Show Banner, Interstitial and Rewarded ads",
                                    checked = adConfig.adsEnabled,
                                    onCheckedChange = {
                                        viewModel.updateAdConfig(adConfig.copy(adsEnabled = it))
                                    }
                                )

                                Divider(color = StudioBorder)

                                AdminToggleRow(
                                    title = "AdMob Test Mode",
                                    subtitle = "Use official Google test ads for safe verification before launching real ads",
                                    checked = adConfig.testMode,
                                    onCheckedChange = {
                                        viewModel.updateAdConfig(adConfig.copy(testMode = it))
                                    }
                                )

                                Divider(color = StudioBorder)

                                AdminToggleRow(
                                    title = "Require Rewarded Ad for 4K / HD",
                                    subtitle = "Users watch a 6s video ad to unlock highest resolution",
                                    checked = adminConfig.requireRewardedAdForHd,
                                    onCheckedChange = {
                                        viewModel.updateAdminConfig(adminConfig.copy(requireRewardedAdForHd = it))
                                    }
                                )
                            }
                        }

                        // Ad Units Configuration Form
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Google Ad Unit IDs (AdMob / AdSense)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)

                                Text(
                                    text = "Paste your real AdMob App ID and Ad Unit IDs below to earn revenue directly from user views and clicks.",
                                    color = StudioGrayLight,
                                    fontSize = 12.sp
                                )

                                OutlinedTextField(
                                    value = adMobAppId,
                                    onValueChange = { adMobAppId = it },
                                    label = { Text("AdMob App ID (ca-app-pub-...)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = StudioAccent,
                                        unfocusedBorderColor = StudioBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                OutlinedTextField(
                                    value = bannerUnitId,
                                    onValueChange = { bannerUnitId = it },
                                    label = { Text("Banner Ad Unit ID") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = StudioAccent,
                                        unfocusedBorderColor = StudioBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                OutlinedTextField(
                                    value = interstitialUnitId,
                                    onValueChange = { interstitialUnitId = it },
                                    label = { Text("Interstitial Ad Unit ID") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = StudioAccent,
                                        unfocusedBorderColor = StudioBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                OutlinedTextField(
                                    value = rewardedUnitId,
                                    onValueChange = { rewardedUnitId = it },
                                    label = { Text("Rewarded Video Ad Unit ID") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = StudioAccent,
                                        unfocusedBorderColor = StudioBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                StudioPrimaryButton(
                                    text = "SAVE ADMOB CONFIGURATION",
                                    onClick = {
                                        viewModel.updateAdConfig(
                                            adConfig.copy(
                                                adMobAppId = adMobAppId,
                                                bannerAdUnitId = bannerUnitId,
                                                interstitialAdUnitId = interstitialUnitId,
                                                rewardedAdUnitId = rewardedUnitId
                                            )
                                        )
                                        Toast.makeText(context, "AdMob settings saved successfully!", Toast.LENGTH_SHORT).show()
                                    },
                                    icon = Icons.Default.Save
                                )
                            }
                        }
                    }

                    2 -> {
                        // TAB 3: SYSTEM & DATABASE STATS
                        AdminSectionHeader("Database & Storage Overview", Icons.Default.Storage)

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Storage & Usage Statistics", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    StatItem("Saved Scripts", "${allScripts.size}", Color.White)
                                    StatItem("Recorded Videos", "${allVideos.size}", Color(0xFFF59E0B))
                                    StatItem("Disk Used", String.format("%.1f MB", storageBytes.toDouble() / (1024 * 1024)), Color(0xFFEF4444))
                                }

                                Divider(color = StudioBorder)

                                Text("Emergency Data Operations", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.clearAllScripts()
                                            Toast.makeText(context, "All scripts cleared", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F3F46)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Clear Scripts", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.clearAllVideos()
                                            Toast.makeText(context, "All videos cleared", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Clear Videos", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Change Master PIN Dialog
    if (showPinChangeDialog) {
        AlertDialog(
            onDismissRequest = { showPinChangeDialog = false },
            title = { Text("Change Admin Master PIN", color = Color.White) },
            text = {
                Column {
                    Text("Enter new 4 to 8 digit Master PIN:", color = StudioGrayLight, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 8) newPinInput = it },
                        label = { Text("New PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAccent,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length >= 4) {
                            viewModel.setAdminPin(newPinInput)
                            Toast.makeText(context, "Admin PIN changed successfully", Toast.LENGTH_SHORT).show()
                            showPinChangeDialog = false
                            newPinInput = ""
                        } else {
                            Toast.makeText(context, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioAccent)
                ) {
                    Text("Update PIN")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showPinChangeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A))
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF18181B)
        )
    }
}

@Composable
private fun AdminSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = StudioAccent, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun AdminToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
            Text(subtitle, fontSize = 12.sp, color = StudioGrayLight)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudioAccent)
        )
    }
}

@Composable
private fun StatItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, fontSize = 11.sp, color = StudioGrayLight)
    }
}
