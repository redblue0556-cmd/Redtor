package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.VideoCameraBack
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.StudioProject
import com.example.data.model.StudioTool
import com.example.ui.components.GlassSectionHeader
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.RubyGlowButton
import com.example.ui.theme.GlassBorderRuby
import com.example.ui.theme.GlassBorderSpecular
import com.example.ui.theme.GlassFillDark
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RoseGoldAccent
import com.example.ui.theme.RubyBright
import com.example.ui.theme.RubyCrimson
import com.example.ui.theme.RubyDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RedtorViewModel
import com.example.ui.viewmodel.StudioResult
import com.example.ui.viewmodel.UiState

@Composable
fun AiStudioScreen(
    viewModel: RedtorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val studioState by viewModel.studioState.collectAsStateWithLifecycle()
    val projectsState by viewModel.studioProjects.collectAsStateWithLifecycle()

    var selectedTool by remember { mutableStateOf(StudioTool.TEXT_TO_VIDEO) }
    var promptText by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf(StudioTool.TEXT_TO_VIDEO.defaultPreset) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val colorPresets = remember {
        listOf("Ruby Noir 24fps", "Cyber Obsidian", "Glass Skin Glow", "3D Anime Pulse", "Golden Cinema", "Teal & Orange")
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
        selectedBitmap = uri?.let { loadScaledBitmap(context, it) }
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
                badge = "GEMINI 3.8 PRO STUDIO",
                title = "AI Photo & Video Studio",
                subtitle = "Text-to-Video, 4K Upscale, Beauty FX, Magic Eraser, Voice Isolator & Color LUTs",
                modifier = Modifier.widthIn(max = 600.dp)
            )
        }

        // Cinematic Hero Banner Card
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
                highlightRuby = true
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(144.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_studio_hero),
                            contentDescription = "Redtor AI Studio Cinema Engine",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xCC0B0407))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = selectedTool.title,
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = selectedTool.subtitle,
                                color = RoseGoldAccent,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Tool Selector Pills (Clean, non-overlapping)
        item {
            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StudioTool.entries.forEach { tool ->
                    val active = tool == selectedTool
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (active) RubyCrimson.copy(alpha = 0.28f) else GlassFillDark
                            )
                            .border(
                                width = 1.dp,
                                color = if (active) RubyBright else GlassBorderSpecular,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                selectedTool = tool
                                selectedPreset = tool.defaultPreset
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("studio_tool_${tool.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = toolIcon(tool),
                                contentDescription = null,
                                tint = if (active) RoseGoldAccent else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tool.title,
                                color = if (active) TextPrimary else TextSecondary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        // Studio Workspace Card
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (selectedTool.supportsImageInput) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Media Input (Photo Picker)",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (selectedImageUri != null) "Foto siap diproses Gemini Vision" else "Pilih foto dari galeri untuk analisis/edit AI",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            RubyGlowButton(
                                text = if (selectedImageUri == null) "Pilih Foto" else "Ganti",
                                icon = Icons.Filled.Image,
                                onClick = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                testTag = "studio_pick_photo_button"
                            )
                        }

                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected media preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, GlassBorderRuby, RoundedCornerShape(16.dp))
                            )
                        }
                    }

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        label = {
                            Text(
                                when (selectedTool) {
                                    StudioTool.TEXT_TO_VIDEO -> "Deskripsi adegan video sinematik..."
                                    StudioTool.VOICE_ISOLATOR -> "Catatan sumber audio / gangguan bising..."
                                    else -> "Instruksi kreatif / detail preset..."
                                }
                            )
                        },
                        placeholder = {
                            Text(
                                when (selectedTool) {
                                    StudioTool.TEXT_TO_VIDEO -> "Contoh: Mobil sport merah melaju di jalanan Tokyo hujan malam, efek kaca cair..."
                                    StudioTool.MAGIC_ERASER -> "Contoh: Hapus orang di belakang dan ganti latar neon studio..."
                                    else -> "Contoh: Tajamkan detail wajah, jernihkan pencahayaan studio..."
                                }
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
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
                            .testTag("studio_prompt_input")
                    )

                    // Preset / LUT Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorPresets.forEach { preset ->
                            FilterChip(
                                selected = selectedPreset == preset,
                                onClick = { selectedPreset = preset },
                                label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RubyCrimson.copy(alpha = 0.35f),
                                    selectedLabelColor = RoseGoldAccent,
                                    containerColor = GlassFillDark,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    // Primary Execution Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RubyGlowButton(
                            text = if (selectedTool == StudioTool.TEXT_TO_VIDEO) "Generate Storyboard" else "Run AI Studio",
                            icon = Icons.Filled.AutoAwesome,
                            loading = studioState is UiState.Loading,
                            onClick = {
                                viewModel.runStudioTool(
                                    tool = selectedTool,
                                    prompt = promptText,
                                    preset = selectedPreset,
                                    inputBitmap = selectedBitmap,
                                    renderVisualBitmap = false
                                )
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "studio_run_button"
                        )
                        if (selectedTool == StudioTool.TEXT_TO_VIDEO || selectedTool.supportsImageInput) {
                            RubyGlowButton(
                                text = "Render Frame",
                                icon = Icons.Filled.MovieCreation,
                                loading = studioState is UiState.Loading,
                                onClick = {
                                    viewModel.runStudioTool(
                                        tool = selectedTool,
                                        prompt = promptText.ifBlank { selectedTool.subtitle },
                                        preset = selectedPreset,
                                        inputBitmap = selectedBitmap,
                                        renderVisualBitmap = true
                                    )
                                },
                                modifier = Modifier.weight(0.85f),
                                testTag = "studio_render_frame_button"
                            )
                        }
                    }
                }
            }
        }

        // Studio Result Card
        item {
            when (val state = studioState) {
                is UiState.Idle -> Unit
                is UiState.Loading, is UiState.Streaming -> {
                    LiquidGlassCard(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = RubyBright, modifier = Modifier.size(24.dp))
                            Text(
                                text = "Gemini 3.8 sedang memproses studio pipeline...",
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    LiquidGlassCard(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth(),
                        highlightRuby = true
                    ) {
                        Text(
                            text = state.message,
                            color = RubyBright,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is UiState.Success -> {
                    StudioResultCard(
                        result = state.data,
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }

        // Saved Studio Projects Header
        item {
            Text(
                text = "Proyek Studio Tersimpan",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
            )
        }

        when (val pState = projectsState) {
            is UiState.Loading -> {
                item {
                    CircularProgressIndicator(color = RubyBright)
                }
            }
            is UiState.Error -> {
                item {
                    Text(pState.message, color = RubyBright)
                }
            }
            is UiState.Success -> {
                if (pState.data.isEmpty()) {
                    item {
                        LiquidGlassCard(
                            modifier = Modifier
                                .widthIn(max = 600.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Belum ada proyek tersimpan. Coba buat storyboard Text-to-Video atau jalankan preset AI di atas.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(pState.data, key = { it.id }) { project ->
                        StudioProjectItemCard(
                            project = project,
                            onDelete = { viewModel.deleteStudioProject(project.id) },
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
private fun StudioResultCard(
    result: StudioResult,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(modifier = modifier, highlightRuby = true) {
        when (result) {
            is StudioResult.Blueprint -> {
                val bp = result.blueprint
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(bp.title, color = RoseGoldAccent, style = MaterialTheme.typography.titleLarge)
                    Text(bp.logline, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Palette: ${bp.colorPalette} • Lighting: ${bp.lightingStyle}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    bp.scenes.forEach { scene ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(ObsidianElevated.copy(alpha = 0.7f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "${scene.timestamp} • ${scene.cameraMove}",
                                color = RoseGoldAccent,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = scene.visualPrompt,
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Audio: ${scene.audioCue}",
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            is StudioResult.TextReport -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${result.tool.title} • Output",
                        color = RoseGoldAccent,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = result.report,
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is StudioResult.VisualRender -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = result.caption,
                        color = RoseGoldAccent,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Image(
                        bitmap = result.bitmap.asImageBitmap(),
                        contentDescription = "Gemini AI Visual Render",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioProjectItemCard(
    project: StudioProject,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${project.presetTag} • ${project.status}",
                        color = RoseGoldAccent,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete project",
                        tint = TextSecondary
                    )
                }
            }
            Text(
                text = project.aiOutput,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun toolIcon(tool: StudioTool): ImageVector = when (tool) {
    StudioTool.TEXT_TO_VIDEO -> Icons.Filled.VideoCameraBack
    StudioTool.UPSCALE_UNBLUR -> Icons.Filled.Hd
    StudioTool.SMART_BEAUTY -> Icons.Filled.FaceRetouchingNatural
    StudioTool.TREND_EFFECTS -> Icons.Filled.AutoFixHigh
    StudioTool.MAGIC_ERASER -> Icons.Filled.Brush
    StudioTool.VOICE_ISOLATOR -> Icons.Filled.GraphicEq
    StudioTool.COLOR_GRADING -> Icons.Filled.ColorLens
}

private fun loadScaledBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        val maxDim = 1280
        if (raw.width <= maxDim && raw.height <= maxDim) {
            raw
        } else {
            val ratio = minOf(maxDim.toFloat() / raw.width, maxDim.toFloat() / raw.height)
            Bitmap.createScaledBitmap(
                raw,
                (raw.width * ratio).toInt().coerceAtLeast(1),
                (raw.height * ratio).toInt().coerceAtLeast(1),
                true
            )
        }
    } catch (_: Exception) {
        null
    }
}
