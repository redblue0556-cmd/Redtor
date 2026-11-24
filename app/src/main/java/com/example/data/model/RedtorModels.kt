package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class StudioTool(
    val id: String,
    val title: String,
    val subtitle: String,
    val defaultPreset: String,
    val supportsImageInput: Boolean
) {
    TEXT_TO_VIDEO(
        id = "text_to_video",
        title = "Text-to-Video",
        subtitle = "Cinematic Gemini 3.8 storyboard & AI keyframe",
        defaultPreset = "Ruby Noir 24fps",
        supportsImageInput = false
    ),
    UPSCALE_UNBLUR(
        id = "upscale_4k",
        title = "AI Upscale 4K",
        subtitle = "Deblur, sharpen details & 4K clarity plan",
        defaultPreset = "4K Ultra Sharp",
        supportsImageInput = true
    ),
    SMART_BEAUTY(
        id = "smart_beauty",
        title = "Smart Beauty FX",
        subtitle = "Glass skin, 3D contour & natural makeup",
        defaultPreset = "Glass Skin Glow",
        supportsImageInput = true
    ),
    TREND_EFFECTS(
        id = "trend_fx",
        title = "Trend FX & 3D",
        subtitle = "Motion blur, camera shake & 3D/Anime avatar",
        defaultPreset = "3D Anime Pulse",
        supportsImageInput = true
    ),
    MAGIC_ERASER(
        id = "magic_eraser",
        title = "Magic Eraser",
        subtitle = "Remove objects & replace scene backdrop",
        defaultPreset = "Clean Matte BG",
        supportsImageInput = true
    ),
    VOICE_ISOLATOR(
        id = "voice_isolator",
        title = "Voice Isolator",
        subtitle = "Vocal clarity & wind/noise removal profile",
        defaultPreset = "Studio Vocal Pro",
        supportsImageInput = false
    ),
    COLOR_GRADING(
        id = "color_grade",
        title = "Color Grading",
        subtitle = "One-tap cinematic LUTs & batch pipeline",
        defaultPreset = "Ruby Noir LUT",
        supportsImageInput = true
    )
}

enum class MediaPlatform(val label: String) {
    YOUTUBE("YouTube"),
    TIKTOK("TikTok"),
    INSTAGRAM("Instagram Reels"),
    TWITTER("X / Twitter"),
    DIRECT("Direct Media")
}

enum class DownloadFormat(val label: String, val extension: String) {
    MP4_4K("MP4 4K HDR", "mp4"),
    MP4_1080P("MP4 1080p HD", "mp4"),
    MP3_320("MP3 320kbps", "mp3")
}

enum class MuxingMode(val label: String) {
    AUDIO_VIDEO_SYNC("Audio-Video Sync Mux"),
    NO_WATERMARK_CLEAN("Clean No-Watermark"),
    AUDIO_ONLY("Pure Audio Extract")
}

enum class DownloadStatus {
    READY, DOWNLOADING, COMPLETED, FAILED
}

enum class SocialPlatform(
    val key: String,
    val displayName: String,
    val shortBadge: String,
    val roleSummary: String,
    val starterPrompt: String
) {
    WHATSAPP(
        key = "WHATSAPP",
        displayName = "WhatsApp",
        shortBadge = "WA",
        roleSummary = "Asisten pesan cepat, broadcast bisnis & balasan elegan",
        starterPrompt = "Buatkan pesan broadcast promo studio kreatif yang singkat, hangat, dan persuasif."
    ),
    INSTAGRAM(
        key = "INSTAGRAM",
        displayName = "Instagram",
        shortBadge = "IG",
        roleSummary = "Strategi Reels, Carousel aesthetic & caption viral",
        starterPrompt = "Buatkan konsep Reels 15 detik dengan hook kuat dan 10 hashtag relevan."
    ),
    TIKTOK(
        key = "TIKTOK",
        displayName = "TikTok",
        shortBadge = "TT",
        roleSummary = "Hook 3 detik FYP, trend audio & pacing editing cepat",
        starterPrompt = "Buatkan skrip TikTok FYP bertema before-after color grading sinematik."
    ),
    YOUTUBE(
        key = "YOUTUBE",
        displayName = "YouTube",
        shortBadge = "YT",
        roleSummary = "Struktur retensi tinggi, judul CTR & deskripsi SEO",
        starterPrompt = "Rancang 5 ide judul YouTube dengan CTR tinggi dan outline video 8 menit."
    ),
    TWITTER(
        key = "TWITTER",
        displayName = "X / Twitter",
        shortBadge = "X",
        roleSummary = "Thread tajam, punchline viral & storytelling ringkas",
        starterPrompt = "Buatkan thread 5 tweet tentang workflow produksi konten pakai AI."
    ),
    CREATOR_SUITE(
        key = "CREATOR_SUITE",
        displayName = "Redtor AI",
        shortBadge = "R",
        roleSummary = "Direktur kreatif all-in-one Gemini untuk semua platform",
        starterPrompt = "Bantu saya merencanakan kalender konten 7 hari lintas TikTok, IG, dan YouTube."
    )
}

enum class CreatorAssetType {
    SCRIPT, SUBTITLES, TREND_RADAR
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Redtor Creator",
    val email: String? = null,
    val photoUrl: String? = null,
    val tier: String = "PRO",
    val dailyAiQuotaUsed: Long = 0L,
    val dailyAiQuotaLimit: Long = 100L,
    val autoDeleteCache: Boolean = true,
    val preferredColorGrade: String = "Ruby Noir",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class StudioProject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val toolId: String = StudioTool.TEXT_TO_VIDEO.id,
    val title: String = "",
    val prompt: String = "",
    val aiOutput: String = "",
    val presetTag: String = "Ruby Noir",
    val status: String = "COMPLETED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DownloadTask(
    @DocumentId val id: String = "",
    val userId: String = "",
    val url: String = "",
    val platform: String = MediaPlatform.YOUTUBE.name,
    val title: String = "",
    val format: String = DownloadFormat.MP4_4K.name,
    val muxingMode: String = MuxingMode.AUDIO_VIDEO_SYNC.name,
    val status: String = DownloadStatus.READY.name,
    val aiSummary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val platform: String = SocialPlatform.TIKTOK.key,
    val role: String = "user",
    val content: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CreatorAsset(
    @DocumentId val id: String = "",
    val userId: String = "",
    val assetType: String = CreatorAssetType.SCRIPT.name,
    val platform: String = SocialPlatform.TIKTOK.key,
    val topic: String = "",
    val content: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class VideoScene(
    val timestamp: String,
    val visualPrompt: String,
    val cameraMove: String,
    val audioCue: String
)

data class VideoBlueprint(
    val title: String,
    val logline: String,
    val colorPalette: String,
    val lightingStyle: String,
    val scenes: List<VideoScene>
)

data class MediaLinkAnalysis(
    val title: String,
    val summary: String,
    val recommendedFormat: DownloadFormat,
    val muxingNotes: String
)

data class CreatorScriptResult(
    val hook: String,
    val body: String,
    val cta: String,
    val caption: String,
    val hashtags: List<String>
)

data class SubtitleCue(
    val timeRange: String,
    val text: String,
    val popStyle: String
)

data class TrendRadarItem(
    val topic: String,
    val viralScore: Int,
    val hookIdea: String,
    val whyItWorks: String,
    val hashtags: List<String>
)
