package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.CreatorAsset
import com.example.data.model.SocialPlatform
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.RubyGlowButton
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorderRuby
import com.example.ui.theme.GlassBorderSpecular
import com.example.ui.theme.GlassFillDark
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RoseGoldAccent
import com.example.ui.theme.RubyBright
import com.example.ui.theme.RubyCrimson
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RedtorViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun CreatorSuitePanel(
    viewModel: RedtorViewModel,
    modifier: Modifier = Modifier
) {
    val scriptState by viewModel.scriptState.collectAsStateWithLifecycle()
    val subtitleState by viewModel.subtitleState.collectAsStateWithLifecycle()
    val trendState by viewModel.trendRadarState.collectAsStateWithLifecycle()
    val savedAssetsState by viewModel.creatorAssets.collectAsStateWithLifecycle()

    var activeToolIndex by remember { mutableStateOf(0) } // 0 Script, 1 Subtitles, 2 Trend Radar
    var topicInput by remember { mutableStateOf("") }
    var targetPlatform by remember { mutableStateOf(SocialPlatform.TIKTOK) }
    var toneOrStyle by remember { mutableStateOf("Energetic & Viral") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tool Picker Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple(0, "Script & Caption", Icons.Filled.AutoStories),
                Triple(1, "Auto-Subtitles", Icons.Filled.ClosedCaption),
                Triple(2, "AI Trend Radar", Icons.Filled.TrendingUp)
            ).forEach { (idx, label, icon) ->
                FilterChip(
                    selected = activeToolIndex == idx,
                    onClick = { activeToolIndex = idx },
                    leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RubyCrimson.copy(alpha = 0.35f),
                        selectedLabelColor = RoseGoldAccent,
                        selectedLeadingIconColor = RoseGoldAccent,
                        containerColor = GlassFillDark,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        if (activeToolIndex == 2) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), highlightRuby = true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_trend_radar_banner),
                        contentDescription = "AI Trend Radar Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC0A0306))))
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text("AI Trend Radar", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Deteksi format & hook konten yang sedang naik daun",
                            color = RoseGoldAccent,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SocialPlatform.entries.forEach { p ->
                        FilterChip(
                            selected = targetPlatform == p,
                            onClick = { targetPlatform = p },
                            label = { Text(p.displayName, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RubyCrimson.copy(alpha = 0.3f),
                                selectedLabelColor = RoseGoldAccent,
                                containerColor = GlassFillDark,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = topicInput,
                    onValueChange = { topicInput = it },
                    label = {
                        Text(
                            when (activeToolIndex) {
                                0 -> "Topik video / produk untuk Script & Caption..."
                                1 -> "Tempel narasi suara atau transkrip video..."
                                else -> "Niche konten (misal: Tech Review, Fashion, Gaming)..."
                            }
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
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
                        .testTag("creator_suite_input")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val options = if (activeToolIndex == 1) {
                        listOf("Neon Pop-Up", "Hormozi Bold", "Minimalist Glass", "Cinema Serif")
                    } else {
                        listOf("Energetic & Viral", "Luxury & Elegan", "Storytelling", "Edukatif Singkat")
                    }
                    options.forEach { opt ->
                        FilterChip(
                            selected = toneOrStyle == opt,
                            onClick = { toneOrStyle = opt },
                            label = { Text(opt, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RubyCrimson.copy(alpha = 0.3f),
                                selectedLabelColor = RoseGoldAccent,
                                containerColor = GlassFillDark,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                val isBusy = (activeToolIndex == 0 && scriptState is UiState.Loading) ||
                    (activeToolIndex == 1 && subtitleState is UiState.Loading) ||
                    (activeToolIndex == 2 && trendState is UiState.Loading)

                RubyGlowButton(
                    text = when (activeToolIndex) {
                        0 -> "Generate Script & Caption"
                        1 -> "Generate Pop-Up Subtitles"
                        else -> "Scan Trend Radar"
                    },
                    loading = isBusy,
                    onClick = {
                        when (activeToolIndex) {
                            0 -> viewModel.generateCreatorScript(targetPlatform, topicInput, toneOrStyle)
                            1 -> viewModel.generateAutoSubtitles(topicInput, toneOrStyle)
                            else -> viewModel.runTrendRadar(topicInput, targetPlatform)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "creator_suite_run_button"
                )
            }
        }

        // Active Output Card
        when (activeToolIndex) {
            0 -> when (val s = scriptState) {
                is UiState.Loading -> CircularProgressIndicator(color = RubyBright)
                is UiState.Error -> Text(s.message, color = RubyBright)
                is UiState.Success -> LiquidGlassCard(modifier = Modifier.fillMaxWidth(), highlightRuby = true) {
                    val res = s.data
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("HOOK (0-3s)", color = RoseGoldAccent, style = MaterialTheme.typography.labelMedium)
                        Text(res.hook, color = TextPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("BODY", color = RoseGoldAccent, style = MaterialTheme.typography.labelMedium)
                        Text(res.body, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        Text("CTA & CAPTION", color = RoseGoldAccent, style = MaterialTheme.typography.labelMedium)
                        Text("${res.cta}\n\n${res.caption}", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text(res.hashtags.joinToString(" "), color = RoseGoldAccent, style = MaterialTheme.typography.labelSmall)
                    }
                }
                else -> Unit
            }
            1 -> when (val s = subtitleState) {
                is UiState.Loading -> CircularProgressIndicator(color = RubyBright)
                is UiState.Error -> Text(s.message, color = RubyBright)
                is UiState.Success -> LiquidGlassCard(modifier = Modifier.fillMaxWidth(), highlightRuby = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("AI Pop-Up Subtitle Cues", color = RoseGoldAccent, style = MaterialTheme.typography.titleMedium)
                        s.data.forEach { cue ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ObsidianElevated.copy(alpha = 0.7f))
                                    .border(1.dp, GlassBorderRuby.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cue.timeRange, color = RoseGoldAccent, style = MaterialTheme.typography.labelSmall)
                                    Text(cue.text, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(cue.popStyle, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                else -> Unit
            }
            else -> when (val s = trendState) {
                is UiState.Loading -> CircularProgressIndicator(color = RubyBright)
                is UiState.Error -> Text(s.message, color = RubyBright)
                is UiState.Success -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    s.data.forEach { item ->
                        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), highlightRuby = true) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.topic, color = TextPrimary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(EmeraldSuccess.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("${item.viralScore}% Viral", color = EmeraldSuccess, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Text("Hook: ${item.hookIdea}", color = RoseGoldAccent, style = MaterialTheme.typography.bodyMedium)
                                Text(item.whyItWorks, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                Text(item.hashtags.joinToString(" "), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                else -> Unit
            }
        }

        // Saved Creator Assets
        if (savedAssetsState is UiState.Success) {
            val assets = (savedAssetsState as UiState.Success<List<CreatorAsset>>).data
            if (assets.isNotEmpty()) {
                Text("Aset Kreator Tersimpan", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                assets.take(6).forEach { asset ->
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${asset.assetType} • ${asset.platform}",
                                    color = RoseGoldAccent,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = asset.topic,
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = asset.content,
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { viewModel.deleteCreatorAsset(asset.id) }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete asset", tint = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
