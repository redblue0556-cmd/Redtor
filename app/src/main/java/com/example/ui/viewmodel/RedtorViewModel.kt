package com.example.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.ChatMessage
import com.example.data.model.CreatorAsset
import com.example.data.model.CreatorAssetType
import com.example.data.model.CreatorScriptResult
import com.example.data.model.DownloadFormat
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTask
import com.example.data.model.MediaPlatform
import com.example.data.model.MuxingMode
import com.example.data.model.SocialPlatform
import com.example.data.model.StudioProject
import com.example.data.model.StudioTool
import com.example.data.model.SubtitleCue
import com.example.data.model.TrendRadarItem
import com.example.data.model.UserProfile
import com.example.data.model.VideoBlueprint
import com.example.data.repository.RedtorAiRepository
import com.example.data.repository.RedtorFirestoreRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Streaming(val partialText: String) : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface StudioResult {
    data class Blueprint(val blueprint: VideoBlueprint) : StudioResult
    data class TextReport(val tool: StudioTool, val report: String) : StudioResult
    data class VisualRender(val bitmap: Bitmap, val caption: String) : StudioResult
}

class RedtorViewModel(
    private val firestoreRepo: RedtorFirestoreRepository,
    private val aiRepo: RedtorAiRepository,
    val currentUserId: String,
    currentUser: FirebaseUser?
) : ViewModel() {

    init {
        if (currentUser != null) {
            viewModelScope.launch {
                firestoreRepo.ensureUserProfile(currentUser)
            }
        }
    }

    val userProfile: StateFlow<UiState<UserProfile?>> = firestoreRepo.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val fsError = unwrapFirestoreError(error)
            if (fsError == null && error is CancellationException) throw error
            emit(UiState.Error((fsError ?: error).message ?: "Failed to load profile"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val studioProjects: StateFlow<UiState<List<StudioProject>>> = firestoreRepo.observeStudioProjects(currentUserId)
        .map<List<StudioProject>, UiState<List<StudioProject>>> { UiState.Success(it) }
        .catch { error ->
            val fsError = unwrapFirestoreError(error)
            if (fsError == null && error is CancellationException) throw error
            emit(UiState.Error((fsError ?: error).message ?: "Failed to load studio projects"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val downloadTasks: StateFlow<UiState<List<DownloadTask>>> = firestoreRepo.observeDownloadTasks(currentUserId)
        .map<List<DownloadTask>, UiState<List<DownloadTask>>> { UiState.Success(it) }
        .catch { error ->
            val fsError = unwrapFirestoreError(error)
            if (fsError == null && error is CancellationException) throw error
            emit(UiState.Error((fsError ?: error).message ?: "Failed to load download queue"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val creatorAssets: StateFlow<UiState<List<CreatorAsset>>> = firestoreRepo.observeCreatorAssets(currentUserId)
        .map<List<CreatorAsset>, UiState<List<CreatorAsset>>> { UiState.Success(it) }
        .catch { error ->
            val fsError = unwrapFirestoreError(error)
            if (fsError == null && error is CancellationException) throw error
            emit(UiState.Error((fsError ?: error).message ?: "Failed to load creator assets"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    // Studio state
    private val _studioState = MutableStateFlow<UiState<StudioResult>>(UiState.Idle)
    val studioState: StateFlow<UiState<StudioResult>> = _studioState.asStateFlow()

    // Downloader state
    private val _downloaderActionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val downloaderActionState: StateFlow<UiState<String>> = _downloaderActionState.asStateFlow()

    // Chat state
    private val _activeSocialPlatform = MutableStateFlow(SocialPlatform.TIKTOK)
    val activeSocialPlatform: StateFlow<SocialPlatform> = _activeSocialPlatform.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chatMessages: StateFlow<UiState<List<ChatMessage>>> = _activeSocialPlatform
        .flatMapLatest { platform ->
            firestoreRepo.observeChatMessages(currentUserId, platform.key)
                .map<List<ChatMessage>, UiState<List<ChatMessage>>> { UiState.Success(it) }
                .catch { error ->
                    val fsError = unwrapFirestoreError(error)
                    if (fsError == null && error is CancellationException) throw error
                    emit(UiState.Error((fsError ?: error).message ?: "Failed to load chat"))
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val _chatStreamState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val chatStreamState: StateFlow<UiState<String>> = _chatStreamState.asStateFlow()
    private var chatJob: Job? = null

    // Creator Suite states
    private val _scriptState = MutableStateFlow<UiState<CreatorScriptResult>>(UiState.Idle)
    val scriptState: StateFlow<UiState<CreatorScriptResult>> = _scriptState.asStateFlow()

    private val _subtitleState = MutableStateFlow<UiState<List<SubtitleCue>>>(UiState.Idle)
    val subtitleState: StateFlow<UiState<List<SubtitleCue>>> = _subtitleState.asStateFlow()

    private val _trendRadarState = MutableStateFlow<UiState<List<TrendRadarItem>>>(UiState.Idle)
    val trendRadarState: StateFlow<UiState<List<TrendRadarItem>>> = _trendRadarState.asStateFlow()

    private val _profileStatusMessage = MutableStateFlow<String?>(null)
    val profileStatusMessage: StateFlow<String?> = _profileStatusMessage.asStateFlow()

    fun runStudioTool(
        tool: StudioTool,
        prompt: String,
        preset: String,
        inputBitmap: Bitmap?,
        renderVisualBitmap: Boolean
    ) {
        if (prompt.isBlank() && inputBitmap == null) {
            _studioState.value = UiState.Error("Masukkan prompt kreatif atau pilih foto terlebih dahulu.")
            return
        }
        viewModelScope.launch {
            _studioState.value = UiState.Loading
            if (renderVisualBitmap) {
                val imgRes = aiRepo.generateOrEditStudioVisual(
                    prompt = "${tool.title} ($preset): $prompt",
                    inputBitmap = inputBitmap
                )
                imgRes.onSuccess { bmp ->
                    _studioState.value = UiState.Success(
                        StudioResult.VisualRender(bmp, "${tool.title} • $preset")
                    )
                    firestoreRepo.saveStudioProject(
                        userId = currentUserId,
                        toolId = tool.id,
                        title = "${tool.title}: ${prompt.take(40)}".ifBlank { tool.title },
                        prompt = prompt.ifBlank { tool.subtitle },
                        aiOutput = "Generated AI Visual Render with preset $preset.",
                        presetTag = preset
                    )
                    firestoreRepo.incrementAiQuota(currentUserId)
                }.onFailure { err ->
                    _studioState.value = UiState.Error(err.localizedMessage ?: "Visual render failed.")
                }
            } else if (tool == StudioTool.TEXT_TO_VIDEO) {
                val res = aiRepo.generateTextToVideoBlueprint(prompt, preset)
                res.onSuccess { bp ->
                    _studioState.value = UiState.Success(StudioResult.Blueprint(bp))
                    val formatted = buildString {
                        appendLine(bp.logline)
                        appendLine("Palette: ${bp.colorPalette} | Lighting: ${bp.lightingStyle}")
                        bp.scenes.forEach { s ->
                            appendLine("[${s.timestamp}] (${s.cameraMove}) ${s.visualPrompt} — Audio: ${s.audioCue}")
                        }
                    }
                    firestoreRepo.saveStudioProject(
                        userId = currentUserId,
                        toolId = tool.id,
                        title = bp.title,
                        prompt = prompt,
                        aiOutput = formatted,
                        presetTag = preset
                    )
                    firestoreRepo.incrementAiQuota(currentUserId)
                }.onFailure { err ->
                    _studioState.value = UiState.Error(err.localizedMessage ?: "Blueprint failed.")
                }
            } else {
                val res = aiRepo.analyzeOrEnhanceMedia(
                    tool = tool,
                    prompt = prompt.ifBlank { tool.subtitle },
                    preset = preset,
                    bitmap = inputBitmap
                )
                res.onSuccess { report ->
                    _studioState.value = UiState.Success(StudioResult.TextReport(tool, report))
                    firestoreRepo.saveStudioProject(
                        userId = currentUserId,
                        toolId = tool.id,
                        title = "${tool.title} • $preset",
                        prompt = prompt.ifBlank { tool.subtitle },
                        aiOutput = report,
                        presetTag = preset
                    )
                    firestoreRepo.incrementAiQuota(currentUserId)
                }.onFailure { err ->
                    _studioState.value = UiState.Error(err.localizedMessage ?: "AI Studio failed.")
                }
            }
        }
    }

    fun deleteStudioProject(projectId: String) {
        viewModelScope.launch {
            firestoreRepo.deleteStudioProject(currentUserId, projectId)
        }
    }

    fun detectPlatform(url: String): MediaPlatform {
        val lower = url.lowercase()
        return when {
            "youtube.com" in lower || "youtu.be" in lower -> MediaPlatform.YOUTUBE
            "tiktok.com" in lower -> MediaPlatform.TIKTOK
            "instagram.com" in lower -> MediaPlatform.INSTAGRAM
            "twitter.com" in lower || "x.com" in lower -> MediaPlatform.TWITTER
            else -> MediaPlatform.DIRECT
        }
    }

    fun analyzeAndQueueMedia(
        rawUrl: String,
        format: DownloadFormat,
        muxingMode: MuxingMode,
        onDirectDownloadReady: (url: String, title: String, ext: String) -> Boolean
    ) {
        val url = rawUrl.trim()
        if (url.isBlank() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            _downloaderActionState.value = UiState.Error("Masukkan URL media yang valid (https://...).")
            return
        }
        val platform = detectPlatform(url)
        viewModelScope.launch {
            _downloaderActionState.value = UiState.Loading
            val analysisRes = aiRepo.analyzeMediaUrl(url, platform, format, muxingMode)
            val title = analysisRes.getOrNull()?.title ?: "${platform.label} Clip"
            val summary = analysisRes.fold(
                onSuccess = { "${it.summary}\n${it.muxingNotes}" },
                onFailure = { err ->
                    "Link queued (${format.label} • ${muxingMode.label}). Note: ${err.localizedMessage ?: "Direct queue"}"
                }
            )
            val isDirectMedia = platform == MediaPlatform.DIRECT &&
                listOf(".mp4", ".mp3", ".m4a", ".webm", ".mov").any { url.lowercase().contains(it) }
            val startedSystemDownload = if (isDirectMedia) {
                onDirectDownloadReady(url, title, format.extension)
            } else false

            val status = if (startedSystemDownload) DownloadStatus.DOWNLOADING else DownloadStatus.READY
            val saveRes = firestoreRepo.addDownloadTask(
                userId = currentUserId,
                url = url,
                platform = platform,
                title = title,
                format = format,
                muxingMode = muxingMode,
                status = status,
                aiSummary = summary
            )
            saveRes.onSuccess {
                firestoreRepo.incrementAiQuota(currentUserId)
                _downloaderActionState.value = UiState.Success(
                    if (startedSystemDownload) {
                        "Unduhan langsung dimulai via Android DownloadManager untuk $title"
                    } else {
                        "Tautan ${platform.label} dianalisis & disimpan ke antrean (${format.label})."
                    }
                )
            }.onFailure { err ->
                _downloaderActionState.value = UiState.Error(err.localizedMessage ?: "Gagal menyimpan antrean.")
            }
        }
    }

    fun markDownloadCompleted(taskId: String) {
        viewModelScope.launch {
            firestoreRepo.updateDownloadTaskStatus(currentUserId, taskId, DownloadStatus.COMPLETED)
        }
    }

    fun deleteDownloadTask(taskId: String) {
        viewModelScope.launch {
            firestoreRepo.deleteDownloadTask(currentUserId, taskId)
        }
    }

    fun selectSocialPlatform(platform: SocialPlatform) {
        _activeSocialPlatform.value = platform
        _chatStreamState.value = UiState.Idle
    }

    fun sendChatMessage(message: String) {
        val trimmed = message.trim()
        if (trimmed.isBlank()) return
        val platform = _activeSocialPlatform.value
        val currentHistory = (chatMessages.value as? UiState.Success)?.data ?: emptyList()

        chatJob?.cancel()
        chatJob = viewModelScope.launch {
            _chatStreamState.value = UiState.Loading
            firestoreRepo.saveChatMessage(currentUserId, platform.key, "user", trimmed)
            val latestBuffer = StringBuilder()
            try {
                aiRepo.streamPlatformChat(platform, currentHistory, trimmed).collect { partial ->
                    latestBuffer.clear()
                    latestBuffer.append(partial)
                    _chatStreamState.value = UiState.Streaming(partial)
                }
                val finalReply = latestBuffer.toString()
                if (finalReply.isNotBlank()) {
                    firestoreRepo.saveChatMessage(currentUserId, platform.key, "model", finalReply)
                    firestoreRepo.incrementAiQuota(currentUserId)
                    _chatStreamState.value = UiState.Idle
                } else {
                    _chatStreamState.value = UiState.Error("Gemini mengembalikan respons kosong.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _chatStreamState.value = UiState.Error(e.localizedMessage ?: "Gagal mengirim pesan ke Gemini.")
            }
        }
    }

    fun clearActivePlatformChat() {
        val platform = _activeSocialPlatform.value
        viewModelScope.launch {
            firestoreRepo.clearPlatformChat(currentUserId, platform.key)
            _chatStreamState.value = UiState.Idle
        }
    }

    fun generateCreatorScript(platform: SocialPlatform, topic: String, tone: String) {
        if (topic.isBlank()) {
            _scriptState.value = UiState.Error("Masukkan topik video terlebih dahulu.")
            return
        }
        viewModelScope.launch {
            _scriptState.value = UiState.Loading
            aiRepo.generateCreatorScript(platform, topic, tone).onSuccess { result ->
                _scriptState.value = UiState.Success(result)
                val fullText = "HOOK: ${result.hook}\n\nBODY: ${result.body}\n\nCTA: ${result.cta}\n\nCAPTION: ${result.caption}\n${result.hashtags.joinToString(" ")}"
                firestoreRepo.saveCreatorAsset(
                    userId = currentUserId,
                    assetType = CreatorAssetType.SCRIPT.name,
                    platformKey = platform.key,
                    topic = topic,
                    content = fullText
                )
                firestoreRepo.incrementAiQuota(currentUserId)
            }.onFailure { err ->
                _scriptState.value = UiState.Error(err.localizedMessage ?: "Gagal membuat script.")
            }
        }
    }

    fun generateAutoSubtitles(transcript: String, style: String) {
        if (transcript.isBlank()) {
            _subtitleState.value = UiState.Error("Masukkan narasi atau transkrip suara video.")
            return
        }
        viewModelScope.launch {
            _subtitleState.value = UiState.Loading
            aiRepo.generateAutoSubtitles(transcript, style).onSuccess { cues ->
                _subtitleState.value = UiState.Success(cues)
                val formatted = cues.joinToString("\n") { "[${it.timeRange}] ${it.text}" }
                firestoreRepo.saveCreatorAsset(
                    userId = currentUserId,
                    assetType = CreatorAssetType.SUBTITLES.name,
                    platformKey = SocialPlatform.TIKTOK.key,
                    topic = transcript.take(60),
                    content = formatted
                )
                firestoreRepo.incrementAiQuota(currentUserId)
            }.onFailure { err ->
                _subtitleState.value = UiState.Error(err.localizedMessage ?: "Gagal membuat subtitle.")
            }
        }
    }

    fun runTrendRadar(niche: String, platform: SocialPlatform) {
        val safeNiche = niche.trim().ifBlank { "AI Creative & Tech" }
        viewModelScope.launch {
            _trendRadarState.value = UiState.Loading
            aiRepo.fetchTrendRadar(safeNiche, platform).onSuccess { items ->
                _trendRadarState.value = UiState.Success(items)
                val summary = items.joinToString("\n\n") {
                    "• ${it.topic} (Viral ${it.viralScore}%)\nHook: ${it.hookIdea}"
                }
                firestoreRepo.saveCreatorAsset(
                    userId = currentUserId,
                    assetType = CreatorAssetType.TREND_RADAR.name,
                    platformKey = platform.key,
                    topic = safeNiche,
                    content = summary
                )
                firestoreRepo.incrementAiQuota(currentUserId)
            }.onFailure { err ->
                _trendRadarState.value = UiState.Error(err.localizedMessage ?: "Gagal memindai tren.")
            }
        }
    }

    fun deleteCreatorAsset(assetId: String) {
        viewModelScope.launch {
            firestoreRepo.deleteCreatorAsset(currentUserId, assetId)
        }
    }

    fun updateSettings(autoDeleteCache: Boolean, preferredColorGrade: String) {
        viewModelScope.launch {
            firestoreRepo.updateUserSettings(currentUserId, autoDeleteCache, preferredColorGrade)
                .onSuccess { _profileStatusMessage.value = "Pengaturan studio diperbarui." }
                .onFailure { _profileStatusMessage.value = it.localizedMessage ?: "Gagal menyimpan pengaturan." }
        }
    }

    fun clearStatusMessage() {
        _profileStatusMessage.value = null
    }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? {
        return generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
    }

    companion object {
        fun provideFactory(
            databaseId: String,
            currentUserId: String,
            currentUser: FirebaseUser?
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db = FirebaseFirestore.getInstance(databaseId)
                RedtorViewModel(
                    firestoreRepo = RedtorFirestoreRepository(db),
                    aiRepo = RedtorAiRepository(),
                    currentUserId = currentUserId,
                    currentUser = currentUser
                )
            }
        }
    }
}
