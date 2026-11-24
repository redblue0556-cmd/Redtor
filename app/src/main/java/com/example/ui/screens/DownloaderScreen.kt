package com.example.ui.screens

import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DownloadFormat
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTask
import com.example.data.model.MuxingMode
import com.example.ui.components.GlassSectionHeader
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.RubyGlowButton
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorderSpecular
import com.example.ui.theme.GlassFillDark
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RoseGoldAccent
import com.example.ui.theme.RubyBright
import com.example.ui.theme.RubyCrimson
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RedtorViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun DownloaderScreen(
    viewModel: RedtorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tasksState by viewModel.downloadTasks.collectAsStateWithLifecycle()
    val actionState by viewModel.downloaderActionState.collectAsStateWithLifecycle()

    var urlInput by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(DownloadFormat.MP4_4K) }
    var selectedMuxing by remember { mutableStateOf(MuxingMode.AUDIO_VIDEO_SYNC) }

    val detectedPlatform = remember(urlInput) {
        if (urlInput.isBlank()) null else viewModel.detectPlatform(urlInput)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            GlassSectionHeader(
                badge = "UNIVERSAL MEDIA HUB",
                title = "Downloader & Mux Queue",
                subtitle = "Deteksi clipboard otomatis, profil muxing audio-video sinkron, & antrean media",
                modifier = Modifier.widthIn(max = 600.dp)
            )
        }

        item {
            LiquidGlassCard(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
                highlightRuby = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Smart Link Input",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (detectedPlatform != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RubyCrimson.copy(alpha = 0.25f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = detectedPlatform.label,
                                    color = RoseGoldAccent,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Tempel tautan YouTube, TikTok, Reels, X, atau MP4/MP3") },
                        leadingIcon = {
                            Icon(Icons.Filled.Link, contentDescription = null, tint = RoseGoldAccent)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RubyBright,
                            unfocusedBorderColor = GlassBorderSpecular,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = RoseGoldAccent,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = RubyBright,
                            focusedContainerColor = ObsidianElevated.copy(alpha = 0.55f),
                            unfocusedContainerColor = ObsidianElevated.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("downloader_url_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clipText = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
                                if (!clipText.isNullOrBlank()) {
                                    urlInput = clipText.trim()
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("downloader_paste_button")
                        ) {
                            Icon(Icons.Filled.ContentPaste, contentDescription = null, tint = RoseGoldAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clipboard", color = TextPrimary)
                        }

                        RubyGlowButton(
                            text = "Analyze & Queue",
                            icon = Icons.Filled.Download,
                            loading = actionState is UiState.Loading,
                            onClick = {
                                viewModel.analyzeAndQueueMedia(
                                    rawUrl = urlInput,
                                    format = selectedFormat,
                                    muxingMode = selectedMuxing
                                ) { directUrl, title, ext ->
                                    enqueueSystemDownload(context, directUrl, title, ext)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "downloader_queue_button"
                        )
                    }

                    // Format Chips
                    Text("Format Output", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DownloadFormat.entries.forEach { fmt ->
                            FilterChip(
                                selected = selectedFormat == fmt,
                                onClick = { selectedFormat = fmt },
                                label = { Text(fmt.label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RubyCrimson.copy(alpha = 0.35f),
                                    selectedLabelColor = RoseGoldAccent,
                                    containerColor = GlassFillDark,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    // Muxing Mode Chips
                    Text("Engine Muxing", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MuxingMode.entries.forEach { mode ->
                            FilterChip(
                                selected = selectedMuxing == mode,
                                onClick = { selectedMuxing = mode },
                                label = { Text(mode.label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RubyCrimson.copy(alpha = 0.35f),
                                    selectedLabelColor = RoseGoldAccent,
                                    containerColor = GlassFillDark,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    when (val aState = actionState) {
                        is UiState.Success -> Text(
                            text = aState.data,
                            color = EmeraldSuccess,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        is UiState.Error -> Text(
                            text = aState.message,
                            color = RubyBright,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        else -> Unit
                    }
                }
            }
        }

        item {
            Text(
                text = "Download Queue",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
            )
        }

        when (val qState = tasksState) {
            is UiState.Loading -> {
                item { CircularProgressIndicator(color = RubyBright) }
            }
            is UiState.Error -> {
                item { Text(qState.message, color = RubyBright) }
            }
            is UiState.Success -> {
                if (qState.data.isEmpty()) {
                    item {
                        LiquidGlassCard(
                            modifier = Modifier
                                .widthIn(max = 600.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Antrean masih kosong. Tempel URL media untuk dianalisis Gemini dan disiapkan ke antrean unduhan.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(qState.data, key = { it.id }) { task ->
                        DownloadTaskCard(
                            task = task,
                            onComplete = { viewModel.markDownloadCompleted(task.id) },
                            onOpen = {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(task.url))
                                    context.startActivity(intent)
                                }
                            },
                            onShare = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, "${task.title}\n${task.url}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Media"))
                            },
                            onDelete = { viewModel.deleteDownloadTask(task.id) },
                            modifier = Modifier
                                .widthIn(max = 600.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun DownloadTaskCard(
    task: DownloadTask,
    onComplete: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${task.platform} • ${task.format} • ${task.status}",
                        color = if (task.status == DownloadStatus.COMPLETED.name) EmeraldSuccess else RoseGoldAccent,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Row {
                    if (task.status != DownloadStatus.COMPLETED.name) {
                        IconButton(onClick = onComplete) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Mark complete", tint = EmeraldSuccess)
                        }
                    }
                    IconButton(onClick = onOpen) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = "Open link", tint = TextSecondary)
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Filled.Share, contentDescription = "Share link", tint = TextSecondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete item", tint = TextSecondary)
                    }
                }
            }
            if (task.aiSummary.isNotBlank()) {
                Text(
                    text = task.aiSummary,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = task.url,
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun enqueueSystemDownload(
    context: Context,
    url: String,
    title: String,
    ext: String
): Boolean {
    return try {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return false
        val safeFileName = title.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(40) + ".$ext"
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(title)
            .setDescription("Redtor Media Hub")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeFileName)
        dm.enqueue(request)
        true
    } catch (_: Exception) {
        false
    }
}
