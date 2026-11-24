package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatMessage
import com.example.data.model.SocialPlatform
import com.example.ui.components.GlassSectionHeader
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorderRuby
import com.example.ui.theme.GlassBorderSpecular
import com.example.ui.theme.GlassFillDark
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RoseGoldAccent
import com.example.ui.theme.RubyBright
import com.example.ui.theme.RubyCrimson
import com.example.ui.theme.RubyDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RedtorViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun SocialHubScreen(
    viewModel: RedtorViewModel,
    modifier: Modifier = Modifier
) {
    val activePlatform by viewModel.activeSocialPlatform.collectAsStateWithLifecycle()
    var inChatThread by remember { mutableStateOf(false) }
    var selectedSegment by remember { mutableStateOf(0) } // 0 = Social Chat Hub, 1 = Creator Suite

    if (inChatThread) {
        BackHandler { inChatThread = false }
        PlatformChatThreadScreen(
            platform = activePlatform,
            viewModel = viewModel,
            onBack = { inChatThread = false },
            modifier = modifier
        )
        return
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
                badge = "SOCIAL AI & CREATOR SUITE",
                title = "Social Chat Hub",
                subtitle = "Asisten Gemini khusus tiap platform & generator naskah, subtitle, dan tren",
                modifier = Modifier.widthIn(max = 600.dp)
            )
        }

        // Top Segmented Switcher
        item {
            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassFillDark)
                    .border(1.dp, GlassBorderSpecular, RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Platform Chatbots", "Creator Suite").forEachIndexed { idx, label ->
                    val active = selectedSegment == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (active) RubyCrimson.copy(alpha = 0.35f) else Color.Transparent)
                            .then(
                                if (active) Modifier.border(1.dp, RubyBright.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                else Modifier
                            )
                            .clickable { selectedSegment = idx }
                            .minimumInteractiveComponentSize()
                            .padding(vertical = 10.dp)
                            .testTag("hub_segment_$idx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (active) RoseGoldAccent else TextSecondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        if (selectedSegment == 0) {
            val rows = SocialPlatform.entries.chunked(2)
            items(rows) { rowPlatforms ->
                Row(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowPlatforms.forEach { platform ->
                        SocialPlatformGlassTile(
                            platform = platform,
                            onClick = {
                                viewModel.selectSocialPlatform(platform)
                                inChatThread = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowPlatforms.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            item {
                CreatorSuitePanel(
                    viewModel = viewModel,
                    modifier = Modifier.widthIn(max = 600.dp)
                )
            }
        }
    }
}

@Composable
private fun SocialPlatformGlassTile(
    platform: SocialPlatform,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = platformAccentColor(platform)
    LiquidGlassCard(
        modifier = modifier.testTag("social_tile_${platform.key}"),
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(accent.copy(alpha = 0.35f), RubyDeep.copy(alpha = 0.45f))
                        )
                    )
                    .border(1.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = platformIcon(platform),
                    contentDescription = "${platform.displayName} Assistant",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = platform.displayName,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = platform.roleSummary,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlatformChatThreadScreen(
    platform: SocialPlatform,
    viewModel: RedtorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messagesState by viewModel.chatMessages.collectAsStateWithLifecycle()
    val streamState by viewModel.chatStreamState.collectAsStateWithLifecycle()
    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val savedMessages = (messagesState as? UiState.Success)?.data ?: emptyList()

    LaunchedEffect(savedMessages.size, streamState) {
        val total = savedMessages.size + if (streamState is UiState.Streaming) 1 else 0
        if (total > 0) {
            listState.animateScrollToItem(total - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 94.dp)
    ) {
        // Header Bar
        LiquidGlassCard(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(top = 8.dp),
            highlightRuby = true
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Social Hub",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "${platform.displayName} AI",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Gemini 3.8 Multi-Turn Strategist",
                            color = RoseGoldAccent,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                IconButton(
                    onClick = { viewModel.clearActivePlatformChat() },
                    modifier = Modifier.testTag("chat_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Clear chat history",
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable Multi-Turn Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (savedMessages.isEmpty() && streamState !is UiState.Streaming && streamState !is UiState.Loading) {
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.sendChatMessage(platform.starterPrompt) }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Mulai Percakapan ${platform.displayName}",
                                color = RoseGoldAccent,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = platform.roleSummary,
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Ketuk untuk coba: \"${platform.starterPrompt}\"",
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            items(savedMessages, key = { it.id }) { msg ->
                ChatBubbleItem(msg = msg)
            }

            if (streamState is UiState.Loading) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = RubyBright, modifier = Modifier.size(18.dp))
                        Text("Gemini sedang mengetik...", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (streamState is UiState.Streaming) {
                item {
                    ChatBubbleItem(
                        msg = ChatMessage(
                            role = "model",
                            content = (streamState as UiState.Streaming).partialText
                        )
                    )
                }
            } else if (streamState is UiState.Error) {
                item {
                    Text(
                        text = (streamState as UiState.Error).message,
                        color = RubyBright,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Message Composer Bar
        Row(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                placeholder = { Text("Tanya ${platform.displayName} AI...") },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RubyBright,
                    unfocusedBorderColor = GlassBorderSpecular,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = RubyBright,
                    focusedContainerColor = ObsidianElevated.copy(alpha = 0.7f),
                    unfocusedContainerColor = ObsidianElevated.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(RubyCrimson)
                    .border(1.dp, RoseGoldAccent.copy(alpha = 0.5f), CircleShape)
                    .clickable(enabled = streamState !is UiState.Loading && streamState !is UiState.Streaming) {
                        val toSend = messageInput
                        if (toSend.isNotBlank()) {
                            messageInput = ""
                            viewModel.sendChatMessage(toSend)
                        }
                    }
                    .testTag("chat_send_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                    tint = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isUser) RubyCrimson.copy(alpha = 0.34f) else ObsidianElevated.copy(alpha = 0.85f)
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) GlassBorderRuby else GlassBorderSpecular,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = msg.content,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun platformIcon(platform: SocialPlatform): ImageVector = when (platform) {
    SocialPlatform.WHATSAPP -> Icons.Filled.Chat
    SocialPlatform.INSTAGRAM -> Icons.Filled.CameraAlt
    SocialPlatform.TIKTOK -> Icons.Filled.MusicNote
    SocialPlatform.YOUTUBE -> Icons.Filled.PlayCircle
    SocialPlatform.TWITTER -> Icons.Filled.Public
    SocialPlatform.CREATOR_SUITE -> Icons.Filled.AutoAwesome
}

private fun platformAccentColor(platform: SocialPlatform): Color = when (platform) {
    SocialPlatform.WHATSAPP -> EmeraldSuccess
    SocialPlatform.INSTAGRAM -> RoseGoldAccent
    SocialPlatform.TIKTOK -> RubyBright
    SocialPlatform.YOUTUBE -> RubyCrimson
    SocialPlatform.TWITTER -> CyanAccent
    SocialPlatform.CREATOR_SUITE -> RubyBright
}
