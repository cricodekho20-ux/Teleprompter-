package com.example.ui.studio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScriptTextAlign
import com.example.data.model.TeleprompterConfig
import com.example.data.model.TeleprompterLayoutMode
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioBorderLight
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioGrayLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioWhite
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun TeleprompterOverlay(
    scriptText: String,
    config: TeleprompterConfig,
    isScrolling: Boolean,
    onToggleScrolling: () -> Unit,
    onRestartScrolling: () -> Unit,
    onUpdateConfig: (TeleprompterConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Smooth auto-scroll loop
    LaunchedEffect(isScrolling, config.scrollSpeedMultiplier) {
        if (isScrolling) {
            val baseSpeedPixelsPerTick = 1.6f * config.scrollSpeedMultiplier
            while (isActive && isScrolling) {
                if (scrollState.value < scrollState.maxValue) {
                    val next = (scrollState.value + baseSpeedPixelsPerTick.roundToInt()).coerceAtMost(scrollState.maxValue)
                    scrollState.scrollTo(next)
                }
                kotlinx.coroutines.delay(16L) // ~60fps smooth scroll
            }
        }
    }

    val parsedTextColor = try {
        Color(android.graphics.Color.parseColor(config.textColorHex))
    } catch (_: Exception) {
        Color.White
    }

    val parsedBaseBgColor = try {
        Color(android.graphics.Color.parseColor(config.backgroundColorHex))
    } catch (_: Exception) {
        Color.Black
    }

    val textAlign = when (config.textAlign) {
        ScriptTextAlign.LEFT -> TextAlign.Left
        ScriptTextAlign.CENTER -> TextAlign.Center
        ScriptTextAlign.RIGHT -> TextAlign.Right
    }

    // Calculate background based on color + opacity
    val backgroundColor = parsedBaseBgColor.copy(alpha = config.backgroundOpacity)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val calculatedHeight = when (config.layoutMode) {
            TeleprompterLayoutMode.OVERLAY -> maxHeight
            TeleprompterLayoutMode.SPLIT -> maxHeight * 0.50f
            else -> maxHeight * config.boxHeightFactor
        }

        val overlayModifier = when (config.layoutMode) {
            TeleprompterLayoutMode.TOP -> Modifier
                .fillMaxWidth(config.boxWidthFactor)
                .height(calculatedHeight)
                .align(Alignment.TopCenter)

            TeleprompterLayoutMode.CENTER -> Modifier
                .fillMaxWidth(config.boxWidthFactor)
                .height(calculatedHeight)
                .align(Alignment.Center)

            TeleprompterLayoutMode.BOTTOM -> Modifier
                .fillMaxWidth(config.boxWidthFactor)
                .height(calculatedHeight)
                .align(Alignment.BottomCenter)

            TeleprompterLayoutMode.OVERLAY -> Modifier
                .fillMaxSize()

            TeleprompterLayoutMode.SPLIT -> Modifier
                .fillMaxWidth()
                .height(calculatedHeight)
                .align(Alignment.TopCenter)
        }

        // Script container card
        Box(
            modifier = overlayModifier
                .padding(
                    horizontal = if (config.layoutMode == TeleprompterLayoutMode.OVERLAY) 8.dp else 12.dp,
                    vertical = 6.dp
                )
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .border(
                    1.dp,
                    StudioBorderLight.copy(alpha = config.backgroundOpacity.coerceAtLeast(0.2f)),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Mini quick control bar (Move Up, Move Down, Font A-/A+, Height toggle, Play/Pause, Restart)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Manual scroll buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    scrollState.scrollTo((scrollState.value - 200).coerceAtLeast(0))
                                }
                            },
                            modifier = Modifier.size(32.dp).testTag("script_manual_up")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Move Up",
                                tint = StudioWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    scrollState.scrollTo((scrollState.value + 200).coerceAtMost(scrollState.maxValue))
                                }
                            },
                            modifier = Modifier.size(32.dp).testTag("script_manual_down")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Move Down",
                                tint = StudioWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Play / Pause auto scroll
                        IconButton(
                            onClick = onToggleScrolling,
                            modifier = Modifier.size(32.dp).testTag("script_toggle_scroll")
                        ) {
                            Icon(
                                imageVector = if (isScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isScrolling) "Pause Scroll" else "Play Scroll",
                                tint = if (isScrolling) StudioRed else StudioWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    scrollState.scrollTo(0)
                                }
                                onRestartScrolling()
                            },
                            modifier = Modifier.size(32.dp).testTag("script_restart_scroll")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restart",
                                tint = StudioWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Height (Chhoti/Lambi) & Font Size Quick Controls
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Quick Height Toggle: 20% -> 35% -> 55% -> 85%
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF27272A))
                                .clickable {
                                    val nextHeight = when {
                                        config.boxHeightFactor < 0.25f -> 0.35f
                                        config.boxHeightFactor < 0.45f -> 0.55f
                                        config.boxHeightFactor < 0.70f -> 0.85f
                                        else -> 0.20f
                                    }
                                    onUpdateConfig(config.copy(boxHeightFactor = nextHeight))
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = when {
                                    config.boxHeightFactor < 0.25f -> "Chhoti"
                                    config.boxHeightFactor > 0.65f -> "Lambi"
                                    else -> "Normal"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioWhite
                            )
                        }

                        // Font Size A-
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF27272A))
                                .clickable {
                                    val newSize = (config.fontSizeSp - 4f).coerceAtLeast(16f)
                                    onUpdateConfig(config.copy(fontSizeSp = newSize))
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("A-", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioWhite)
                        }

                        // Font Size A+
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF27272A))
                                .clickable {
                                    val newSize = (config.fontSizeSp + 4f).coerceAtMost(72f)
                                    onUpdateConfig(config.copy(fontSizeSp = newSize))
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("A+", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioWhite)
                        }
                    }
                }

                // Scrollable Text Display
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                coroutineScope.launch {
                                    val newPos = (scrollState.value - dragAmount.toInt())
                                        .coerceIn(0, scrollState.maxValue)
                                    scrollState.scrollTo(newPos)
                                }
                            }
                        }
                ) {
                    val scaleX = if (config.mirrorText) -1f else 1f

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .scale(scaleX = scaleX, scaleY = 1f)
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = scriptText.ifBlank { "No script loaded. Open settings or editor to add text." },
                            color = parsedTextColor,
                            fontSize = config.fontSizeSp.sp,
                            lineHeight = (config.fontSizeSp * config.lineSpacingMultiplier).sp,
                            textAlign = textAlign,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("teleprompter_text_content")
                        )

                        Spacer(modifier = Modifier.height(140.dp))
                    }

                    // Top & Bottom gradient fades for smooth reading
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        backgroundColor,
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        backgroundColor
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}
