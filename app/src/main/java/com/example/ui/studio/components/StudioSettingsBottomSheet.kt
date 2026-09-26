package com.example.ui.studio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScriptTextAlign
import com.example.data.model.TeleprompterConfig
import com.example.data.model.TeleprompterLayoutMode
import com.example.ui.components.StudioOutlinedButton
import com.example.ui.components.StudioSliderWithLabel
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioBorderLight
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioGray
import com.example.ui.theme.StudioGrayLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudioSettingsBottomSheet(
    sheetState: SheetState,
    config: TeleprompterConfig,
    onUpdateConfig: (TeleprompterConfig) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioCardBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Teleprompter Settings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = StudioWhite
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = StudioWhite
                    )
                }
            }

            // Layout Mode Selector (TOP, CENTER, BOTTOM, OVERLAY, SPLIT)
            Column {
                Text(
                    text = "LAYOUT MODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioGrayLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeleprompterLayoutMode.entries.forEach { mode ->
                        val isSelected = config.layoutMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) StudioRed else StudioCardBgElevated)
                                .border(1.dp, if (isSelected) StudioRed else StudioBorder, RoundedCornerShape(8.dp))
                                .clickable { onUpdateConfig(config.copy(layoutMode = mode)) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = mode.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioWhite
                            )
                        }
                    }
                }
            }

            // Text Window Height (Chhoti / Lambi / Standard)
            Column {
                Text(
                    text = "TEXT WINDOW HEIGHT (CHHOTI / LAMBI)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioGrayLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeleprompterConfig.AVAILABLE_HEIGHTS.forEach { (factor, label) ->
                        val isSelected = (config.boxHeightFactor - factor).let { kotlin.math.abs(it) < 0.05f }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) StudioRed else StudioCardBgElevated)
                                .border(1.dp, if (isSelected) StudioRed else StudioBorder, RoundedCornerShape(8.dp))
                                .clickable { onUpdateConfig(config.copy(boxHeightFactor = factor)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label.split(" ").first(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioWhite
                            )
                        }
                    }
                }
            }

            // Text Window Width (Compact / Standard / Full)
            Column {
                Text(
                    text = "TEXT WINDOW WIDTH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioGrayLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeleprompterConfig.AVAILABLE_WIDTHS.forEach { (factor, label) ->
                        val isSelected = (config.boxWidthFactor - factor).let { kotlin.math.abs(it) < 0.05f }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) StudioRed else StudioCardBgElevated)
                                .border(1.dp, if (isSelected) StudioRed else StudioBorder, RoundedCornerShape(8.dp))
                                .clickable { onUpdateConfig(config.copy(boxWidthFactor = factor)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label.split(" ").first(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioWhite
                            )
                        }
                    }
                }
            }

            // Background Color Palette (Pitch Black, Charcoal, Navy, Crimson, Emerald, Purple)
            Column {
                Text(
                    text = "BACKGROUND COLOR & SHADE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioGrayLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TeleprompterConfig.AVAILABLE_BG_COLORS.forEach { (hex, name) ->
                        val isSelected = config.backgroundColorHex.equals(hex, ignoreCase = true)
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) StudioRed else StudioBorderLight,
                                    CircleShape
                                )
                                .clickable { onUpdateConfig(config.copy(backgroundColorHex = hex)) }
                        )
                    }
                }
            }

            // Background Darkness / Opacity
            StudioSliderWithLabel(
                title = "Background Darkness (Opacity)",
                valueLabel = "${(config.backgroundOpacity * 100).toInt()}%",
                value = config.backgroundOpacity,
                range = 0.0f..1.0f,
                onValueChange = { onUpdateConfig(config.copy(backgroundOpacity = it)) }
            )

            // Font Size Slider
            StudioSliderWithLabel(
                title = "Font Size",
                valueLabel = "${config.fontSizeSp.toInt()} sp",
                value = config.fontSizeSp,
                range = 16f..72f,
                onValueChange = { onUpdateConfig(config.copy(fontSizeSp = it)) }
            )

            // Line Spacing (Crucial for Hindi readability)
            StudioSliderWithLabel(
                title = "Line Spacing",
                valueLabel = "${String.format("%.1f", config.lineSpacingMultiplier)}x",
                value = config.lineSpacingMultiplier,
                range = 1.0f..2.2f,
                onValueChange = { onUpdateConfig(config.copy(lineSpacingMultiplier = it)) }
            )

            // Scroll Speed Presets & Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scroll Speed",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = StudioWhite
                    )
                    Text(
                        text = "${String.format("%.2f", config.scrollSpeedMultiplier)}x",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = StudioRed
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TeleprompterConfig.AVAILABLE_SPEEDS.forEach { speed ->
                        val isSelected = (config.scrollSpeedMultiplier - speed).let { kotlin.math.abs(it) < 0.05f }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) StudioRed else StudioCardBgElevated)
                                .clickable { onUpdateConfig(config.copy(scrollSpeedMultiplier = speed)) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${speed}x",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioWhite
                            )
                        }
                    }
                }
            }

            // Text Color Options
            Column {
                Text(
                    text = "TEXT FONT COLOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioGrayLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TeleprompterConfig.AVAILABLE_TEXT_COLORS.forEach { (hex, label) ->
                        val isSelected = config.textColorHex.equals(hex, ignoreCase = true)
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(if (isSelected) 3.dp else 1.dp, if (isSelected) StudioRed else StudioBorder, CircleShape)
                                .clickable { onUpdateConfig(config.copy(textColorHex = hex)) }
                        )
                    }
                }
            }

            // Text Alignment
            Column {
                Text(
                    text = "TEXT ALIGNMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioGrayLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScriptTextAlign.entries.forEach { align ->
                        val isSelected = config.textAlign == align
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) StudioRed else StudioCardBgElevated)
                                .clickable { onUpdateConfig(config.copy(textAlign = align)) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = align.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioWhite
                            )
                        }
                    }
                }
            }

            // Toggles: Eye-Line Guide, Text Mirror, Camera Mirror
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsSwitchRow(
                    label = "Eye-Line Guide",
                    description = "Shows alignment line to keep eyes on camera",
                    checked = config.eyeLineGuideEnabled,
                    onCheckedChange = { onUpdateConfig(config.copy(eyeLineGuideEnabled = it)) }
                )

                SettingsSwitchRow(
                    label = "Mirror Camera Preview",
                    description = "Mirrors selfie camera display",
                    checked = config.mirrorCamera,
                    onCheckedChange = { onUpdateConfig(config.copy(mirrorCamera = it)) }
                )

                SettingsSwitchRow(
                    label = "Mirror Text (Glass Rig)",
                    description = "Reverses script for physical beam splitter glass",
                    checked = config.mirrorText,
                    onCheckedChange = { onUpdateConfig(config.copy(mirrorText = it)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reset Teleprompter Button
            StudioOutlinedButton(
                text = "RESET TELEPROMPTER",
                onClick = onResetDefaults,
                icon = Icons.Default.RestartAlt,
                modifier = Modifier.fillMaxWidth(),
                testTag = "reset_teleprompter_button"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontWeight = FontWeight.SemiBold, color = StudioWhite, fontSize = 14.sp)
            Text(text = description, color = StudioGrayLight, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = StudioWhite,
                checkedTrackColor = StudioRed,
                uncheckedThumbColor = StudioGray,
                uncheckedTrackColor = StudioBorder
            )
        )
    }
}
