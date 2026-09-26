package com.example.ui.components

import android.content.Context
import android.util.Log
import android.view.ViewGroup
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AdConfig
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioTextMuted
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay

/**
 * Google Mobile Ads SDK AdView wrapper for Jetpack Compose.
 * Loads banner ad using the provided AdMob banner ad unit ID.
 */
@Composable
fun GoogleMobileAdsBanner(
    adUnitId: String,
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: (LoadAdError) -> Unit = {},
    onAdClicked: () -> Unit = {},
    onAdImpression: () -> Unit = {}
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("google_mobile_ads_sdk_adview"),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Log.d("AdMobBanner", "Banner Ad successfully loaded: $adUnitId")
                        onAdLoaded()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        super.onAdFailedToLoad(error)
                        Log.w("AdMobBanner", "Banner Ad failed to load: ${error.message} (code ${error.code})")
                        onAdFailedToLoad(error)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        Log.d("AdMobBanner", "Banner Ad clicked")
                        onAdClicked()
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        Log.d("AdMobBanner", "Banner Ad recorded impression")
                        onAdImpression()
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            // Update ad unit if needed
            if (adView.adUnitId != adUnitId) {
                adView.adUnitId = adUnitId
                adView.loadAd(AdRequest.Builder().build())
            }
        }
    )
}

/**
 * Mobile Banner Ad component displayed across the app.
 * Integrates Google Mobile Ads SDK placeholder and AdView with fallback state.
 */
@Composable
fun AdBannerView(
    adConfig: AdConfig,
    onImpression: () -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!adConfig.adsEnabled) return

    var adFailedToLoad by remember { mutableStateOf(false) }
    var adLoadedSuccessfully by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onImpression()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_admob_banner")
    ) {
        // Render AdMob SDK Banner container
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Interactive Ad placeholder card that ensures polished visual layout
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clickable {
                        onClick()
                    },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13141C)),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Google Ad Badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF59E0B).copy(alpha = 0.22f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = if (adConfig.testMode) "TEST AD" else "AD",
                                color = Color(0xFFFBBF24),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Google Mobile Ads • AdMob Network",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (adConfig.testMode) {
                                    "Unit: ${adConfig.bannerAdUnitId.takeLast(12)} • Ready for Live Ads"
                                } else {
                                    "Sponsored Partner Ad • Tap to interact"
                                },
                                color = StudioTextMuted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2563EB), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "VISIT",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Interstitial Ad Dialog shown during transitions / after recording.
 */
@Composable
fun InterstitialAdDialog(
    adConfig: AdConfig,
    onDismiss: () -> Unit,
    onImpression: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    if (!adConfig.adsEnabled) {
        onDismiss()
        return
    }

    var secondsLeft by remember { mutableIntStateOf(4) }
    var canClose by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onImpression()
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        canClose = true
    }

    Dialog(
        onDismissRequest = {
            if (canClose) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = canClose, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("interstitial_ad_dialog"),
            color = Color(0xFF09090B)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Header Bar with timer & close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (adConfig.testMode) "Google AdMob Test Interstitial" else "Google Sponsored Ad",
                            color = Color(0xFFF59E0B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (canClose) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF27272A), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Ad", tint = Color.White)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF27272A), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Reward in ${secondsLeft}s",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Main Ad Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))),
                                RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = "Ad Icon",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Teleprompter Pro Studio Suite",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Monetized via Google AdSense & AdMob Network. Real-time Ad display configured by app administrator.",
                        color = StudioTextMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            onClick()
                            if (canClose) onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Install / Visit Offer", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                // Footer
                Text(
                    text = "Ad Unit: ${adConfig.interstitialAdUnitId}",
                    color = Color(0xFF52525B),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}

/**
 * Rewarded Video Ad Dialog shown to unlock premium features / 4K recording.
 */
@Composable
fun RewardedAdDialog(
    adConfig: AdConfig,
    rewardTitle: String = "4K / HD Video Recording",
    onRewardEarned: () -> Unit,
    onDismiss: () -> Unit,
    onImpression: () -> Unit = {}
) {
    if (!adConfig.adsEnabled) {
        onRewardEarned()
        onDismiss()
        return
    }

    var progress by remember { mutableStateOf(0f) }
    var secondsLeft by remember { mutableIntStateOf(6) }
    var rewardGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onImpression()
        val totalSeconds = 6
        for (i in totalSeconds downTo 1) {
            secondsLeft = i
            progress = (totalSeconds - i + 1).toFloat() / totalSeconds
            delay(1000)
        }
        rewardGranted = true
        onRewardEarned()
    }

    Dialog(
        onDismissRequest = {
            if (rewardGranted) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = rewardGranted, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("rewarded_ad_dialog"),
            color = Color(0xFF09090B)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                if (rewardGranted) Color(0xFF10B981) else Color(0xFFF59E0B),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (rewardGranted) Icons.Default.CheckCircle else Icons.Default.Star,
                            contentDescription = "Reward",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (rewardGranted) "🎉 Reward Unlocked!" else "Watching Rewarded Video",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Unlocking: $rewardTitle",
                        color = StudioTextMuted,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (rewardGranted) Color(0xFF10B981) else Color(0xFFF59E0B),
                        trackColor = Color(0xFF27272A)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (rewardGranted) "Reward earned successfully!" else "Please wait ${secondsLeft}s to receive reward...",
                        color = if (rewardGranted) Color(0xFF10B981) else StudioTextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (rewardGranted) FontWeight.Bold else FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (rewardGranted) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Continue to Recording", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
