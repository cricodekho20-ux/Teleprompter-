package com.example.ui.editor

import android.content.ClipDescription
import android.content.ClipboardManager
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.model.Script
import com.example.ui.components.StudioOutlinedButton
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

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AdBannerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptEditorScreen(
    viewModel: MainViewModel,
    existingScriptId: Long? = null,
    onBack: () -> Unit,
    onContinueToFormat: () -> Unit
) {
    val context = LocalContext.current
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    val adConfig by viewModel.adConfig.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var scriptId by remember { mutableStateOf<Long?>(existingScriptId) }

    LaunchedEffect(existingScriptId) {
        if (existingScriptId != null && existingScriptId > 0) {
            val script = viewModel.allScripts.value.find { it.id == existingScriptId }
            if (script != null) {
                title = script.title
                content = script.content
                scriptId = script.id
            }
        } else {
            val current = viewModel.currentScript.value
            if (current != null && content.isEmpty()) {
                title = current.title
                content = current.content
                scriptId = current.id
            }
        }
    }

    val wordCount = remember(content) { Script.calculateWordCount(content) }
    val estimatedSeconds = remember(wordCount) { Script.calculateEstimatedSeconds(wordCount) }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = if (scriptId != null && scriptId!! > 0) "Edit Script" else "Your Script",
                onBack = onBack,
                actions = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioCardBgElevated)
                            .clickable {
                                if (content.isNotBlank()) {
                                    viewModel.saveScript(title, content, scriptId) { newId ->
                                        scriptId = newId
                                        Toast.makeText(context, "Script Saved!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Please enter script text first", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("save_script_action_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                                tint = StudioWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save",
                                color = StudioWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
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
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Script Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Script Title", color = StudioGrayLight) },
                placeholder = { Text("e.g. Daily Tech News / Hindi Video", color = StudioGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("script_title_input"),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = StudioWhite,
                    unfocusedTextColor = StudioWhite,
                    focusedContainerColor = StudioCardBg,
                    unfocusedContainerColor = StudioCardBg,
                    focusedIndicatorColor = StudioRed,
                    unfocusedIndicatorColor = StudioBorder
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            // Editor Actions Toolbar (Paste, Clear, Templates)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Paste button
                ToolbarChip(
                    text = "Paste",
                    icon = Icons.Default.ContentPaste,
                    testTag = "paste_script_button",
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        if (clipboard.hasPrimaryClip() && (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true ||
                                    clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML) == true)) {
                            val item = clipboard.primaryClip?.getItemAt(0)
                            val pasteText = item?.text?.toString() ?: ""
                            if (pasteText.isNotBlank()) {
                                content = if (content.isBlank()) pasteText else "$content\n\n$pasteText"
                                Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Clear button
                ToolbarChip(
                    text = "Clear",
                    icon = Icons.Default.Clear,
                    testTag = "clear_script_button",
                    onClick = {
                        content = ""
                        title = ""
                        scriptId = null
                    }
                )

                // Hindi Sample template
                ToolbarChip(
                    text = "Hindi Sample",
                    icon = Icons.Default.AutoAwesome,
                    testTag = "sample_hindi_button",
                    onClick = {
                        title = "Motivational Hindi Short"
                        content = """नमस्ते दोस्तों! सफलता का सबसे बड़ा रहस्य क्या है?

पहला: अपनी सोच को हमेशा सकारात्मक रखें। जब आप खुद पर विश्वास करते हैं, तो आधी लड़ाई वहीं जीत ली जाती है।

दूसरा: हर दिन सिर्फ 1% बेहतर बनने की कोशिश करें। छोटे सुधार समय के साथ बड़े परिणाम लाते हैं।

और तीसरा: गलतियों से डरना बंद करें, उनसे सीखें और आगे बढ़ें।

अगर यह संदेश आपके दिल को छू गया, तो वीडियो को लाइक और शेयर जरूर करें!"""
                    }
                )
            }

            // Main Large Text Area (Supports Hindi Unicode, English, Emoticons)
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Teleprompter Script", color = StudioGrayLight) },
                placeholder = {
                    Text(
                        "Type or paste your script here in Hindi, English or any language...\n\nExample:\nआज हम बात करेंगे...\nToday we will discuss...",
                        color = StudioGray,
                        lineHeight = 22.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .testTag("script_content_editor"),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = StudioWhite,
                    unfocusedTextColor = StudioWhite,
                    focusedContainerColor = StudioCardBg,
                    unfocusedContainerColor = StudioCardBg,
                    focusedIndicatorColor = StudioRed,
                    unfocusedIndicatorColor = StudioBorder
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    color = StudioWhite
                )
            )

            // Statistics Card: Words & Estimated Speaking Duration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioCardBgElevated)
                    .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WORD COUNT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioGrayLight
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$wordCount words",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioWhite
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ESTIMATED READING TIME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioGrayLight
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "~$estimatedSeconds sec (${estimatedSeconds / 60}m ${estimatedSeconds % 60}s)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Continue Button
            StudioPrimaryButton(
                text = "CONTINUE TO FORMAT",
                onClick = {
                    if (content.isBlank()) {
                        Toast.makeText(context, "Please enter or paste a script to continue", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.setScriptFromText(title, content, scriptId)
                        onContinueToFormat()
                    }
                },
                testTag = "continue_to_format_button"
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ToolbarChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(StudioCardBgElevated)
            .border(1.dp, StudioBorderLight, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(testTag)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StudioGrayLight,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = StudioWhite
            )
        }
    }
}
