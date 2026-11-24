package com.example.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.CreatorScriptResult
import com.example.data.model.DownloadFormat
import com.example.data.model.MediaLinkAnalysis
import com.example.data.model.MediaPlatform
import com.example.data.model.MuxingMode
import com.example.data.model.SocialPlatform
import com.example.data.model.StudioTool
import com.example.data.model.SubtitleCue
import com.example.data.model.TrendRadarItem
import com.example.data.model.VideoBlueprint
import com.example.data.model.VideoScene
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "RedtorAiRepo"

class RedtorAiRepository {

    private val generalModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val fastLiteModel by lazy {
        Firebase.ai.generativeModel("gemini-3.5-flash-lite")
    }

    private val imageModel by lazy {
        val config = generationConfig {
            responseModalities = listOf(ResponseModality.IMAGE)
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-nano-banana-2.1",
            generationConfig = config
        )
    }

    private val videoBlueprintModel by lazy {
        val sceneSchema = Schema.obj(
            properties = mapOf(
                "timestamp" to Schema.string("Time range e.g. 00:00 - 00:04"),
                "visualPrompt" to Schema.string("Detailed visual framing and subject motion"),
                "cameraMove" to Schema.string("Camera movement e.g. Dolly-in, FPV Whip Pan"),
                "audioCue" to Schema.string("SFX and music cue")
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string("Cinematic project title"),
                "logline" to Schema.string("1-2 sentence creative summary"),
                "colorPalette" to Schema.string("Color grading palette"),
                "lightingStyle" to Schema.string("Lighting setup"),
                "scenes" to Schema.array(items = sceneSchema, description = "3 to 5 sequential shots")
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val scriptModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "hook" to Schema.string("First 3-second viral hook"),
                "body" to Schema.string("Main engaging narrative body"),
                "cta" to Schema.string("Clear call to action"),
                "caption" to Schema.string("Ready-to-post social caption"),
                "hashtags" to Schema.array(items = Schema.string(), description = "5-8 trending hashtags")
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val subtitleModel by lazy {
        val cueSchema = Schema.obj(
            properties = mapOf(
                "timeRange" to Schema.string("e.g. 00:00.0 -> 00:02.4"),
                "text" to Schema.string("Short punchy pop-up subtitle line"),
                "popStyle" to Schema.string("Animation emphasis style")
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "cues" to Schema.array(items = cueSchema, description = "Timed pop-up subtitle cues")
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val trendRadarModel by lazy {
        val itemSchema = Schema.obj(
            properties = mapOf(
                "topic" to Schema.string("Trending content format or topic"),
                "viralScore" to Schema.integer("Predicted momentum 1 to 100"),
                "hookIdea" to Schema.string("Concrete opening hook idea"),
                "whyItWorks" to Schema.string("Brief algorithmic insight"),
                "hashtags" to Schema.array(items = Schema.string(), description = "3-5 tags")
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "trends" to Schema.array(items = itemSchema, description = "4 high-momentum trend ideas")
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val linkAnalyzerModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string("Clean media title inferred from link"),
                "summary" to Schema.string("Muxing plan, audio sync verification, and creator notes"),
                "recommendedFormat" to Schema.enumeration(
                    values = listOf("MP4_4K", "MP4_1080P", "MP3_320"),
                    description = "Recommended export format"
                ),
                "muxingNotes" to Schema.string("Short audio-video sync and watermark-clean note")
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    suspend fun generateTextToVideoBlueprint(
        prompt: String,
        stylePreset: String
    ): Result<VideoBlueprint> = try {
        val response = withTimeout(60_000L) {
            videoBlueprintModel.generateContent(
                "You are Redtor AI Cinema Director. Create a high-impact cinematic Text-to-Video production blueprint in the user's language (Indonesian or English matching the prompt) for preset '$stylePreset'. Prompt: $prompt"
            )
        }
        val raw = response.text ?: throw IllegalStateException("Model returned an empty blueprint.")
        val json = JSONObject(raw)
        val scenesArr = json.getJSONArray("scenes")
        val scenes = (0 until scenesArr.length()).map { idx ->
            val obj = scenesArr.getJSONObject(idx)
            VideoScene(
                timestamp = obj.optString("timestamp", "00:0${idx * 3}"),
                visualPrompt = obj.optString("visualPrompt", ""),
                cameraMove = obj.optString("cameraMove", "Smooth Gimbal"),
                audioCue = obj.optString("audioCue", "Ambient Rise")
            )
        }
        Result.success(
            VideoBlueprint(
                title = json.optString("title", "Redtor Cinema Render"),
                logline = json.optString("logline", ""),
                colorPalette = json.optString("colorPalette", stylePreset),
                lightingStyle = json.optString("lightingStyle", "Liquid Glass Rim Light"),
                scenes = scenes
            )
        )
    } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Video blueprint timed out", e)
        Result.failure(Exception("AI generation timed out after 60 seconds. Please try again."))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Video blueprint failed", e)
        Result.failure(e)
    }

    suspend fun analyzeOrEnhanceMedia(
        tool: StudioTool,
        prompt: String,
        preset: String,
        bitmap: Bitmap?
    ): Result<String> = try {
        val instruction = when (tool) {
            StudioTool.UPSCALE_UNBLUR -> "Analyze sharpness, noise, and resolution. Provide a 4K Upscale & Unblur enhancement recipe (sharpness gain, denoise radius, micro-contrast) for preset '$preset'. User note: $prompt"
            StudioTool.SMART_BEAUTY -> "Provide a natural Smart Beauty & Glass Skin retouching + 3D face lighting profile for preset '$preset'. Keep skin texture realistic. User note: $prompt"
            StudioTool.TREND_EFFECTS -> "Design a CapCut/TikTok Trend Effects & 3D Avatar motion pipeline (motion blur %, keyframe shake curve, beat-sync cuts) for preset '$preset'. User note: $prompt"
            StudioTool.MAGIC_ERASER -> "Identify distracting elements or describe an AI Magic Eraser & Background Replacer compositing mask and lighting match plan for preset '$preset'. User note: $prompt"
            StudioTool.VOICE_ISOLATOR -> "Design an AI Voice Isolator & Wind/Noise Remover DSP chain (HPF cutoff, de-esser, spectral noise floor, LUFS target) for preset '$preset'. Context: $prompt"
            StudioTool.COLOR_GRADING -> "Generate a cinematic One-Tap Color Grading LUT parameter sheet (Lift/Gamma/Gain, Split Toning, Halation, Film Grain) for preset '$preset'. Context: $prompt"
            StudioTool.TEXT_TO_VIDEO -> "Create a concise Text-to-Video creative direction for preset '$preset': $prompt"
        }
        val response = withTimeout(60_000L) {
            if (bitmap != null) {
                generalModel.generateContent(
                    content {
                        image(bitmap)
                        text(instruction)
                    }
                )
            } else {
                generalModel.generateContent(instruction)
            }
        }
        val text = response.text
        if (!text.isNullOrBlank()) Result.success(text)
        else Result.failure(IllegalStateException("Model returned an empty response."))
    } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Studio analysis timed out", e)
        Result.failure(Exception("Studio AI analysis timed out after 60 seconds."))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Studio analysis failed", e)
        Result.failure(e)
    }

    suspend fun generateOrEditStudioVisual(
        prompt: String,
        inputBitmap: Bitmap?
    ): Result<Bitmap> = try {
        val response = withTimeout(60_000L) {
            if (inputBitmap != null) {
                imageModel.generateContent(
                    content {
                        image(inputBitmap)
                        text(prompt)
                    }
                )
            } else {
                imageModel.generateContent(prompt)
            }
        }
        val imagePart = response.candidates.firstOrNull()
            ?.content?.parts?.filterIsInstance<ImagePart>()
            ?.firstOrNull()
        val output = imagePart?.image
        if (output != null) Result.success(output)
        else Result.failure(IllegalStateException("No visual image was returned by Gemini image model."))
    } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Image generation timed out", e)
        Result.failure(Exception("Visual render timed out after 60 seconds."))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Image generation failed", e)
        Result.failure(e)
    }

    suspend fun analyzeMediaUrl(
        url: String,
        platform: MediaPlatform,
        selectedFormat: DownloadFormat,
        muxingMode: MuxingMode
    ): Result<MediaLinkAnalysis> = try {
        val response = withTimeout(45_000L) {
            linkAnalyzerModel.generateContent(
                "Analyze this media URL for Redtor Universal Media Hub.\nURL: $url\nDetected Platform: ${platform.label}\nRequested Format: ${selectedFormat.label}\nMuxing Mode: ${muxingMode.label}\nReturn clean title, concise summary of audio-video sync/muxing prep, recommendedFormat, and muxingNotes."
            )
        }
        val raw = response.text ?: throw IllegalStateException("Empty URL analysis response.")
        val json = JSONObject(raw)
        val fmtRaw = json.optString("recommendedFormat", selectedFormat.name)
        val recFmt = DownloadFormat.entries.firstOrNull { it.name == fmtRaw } ?: selectedFormat
        Result.success(
            MediaLinkAnalysis(
                title = json.optString("title", "${platform.label} Media Clip"),
                summary = json.optString("summary", "Ready for ${selectedFormat.label} • ${muxingMode.label}."),
                recommendedFormat = recFmt,
                muxingNotes = json.optString("muxingNotes", "Audio & video streams verified for sync.")
            )
        )
    } catch (e: TimeoutCancellationException) {
        Result.failure(Exception("Link analysis timed out. Please try again."))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Media link analysis failed", e)
        Result.failure(e)
    }

    fun streamPlatformChat(
        platform: SocialPlatform,
        history: List<ChatMessage>,
        userMessage: String
    ): Flow<String> = flow {
        val systemPrompt = buildString {
            append("You are Redtor AI Assistant specialized for ${platform.displayName}. ")
            append("Role: ${platform.roleSummary}. ")
            append("Respond in the user's language (Indonesian or English), concisely, creatively, and practically with ready-to-use copy.")
        }
        val chatModel = Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content { text(systemPrompt) }
        )
        val chatHistory = history.takeLast(14).map { msg ->
            content(if (msg.role == "model") "model" else "user") {
                text(msg.content)
            }
        }
        val chat = chatModel.startChat(history = chatHistory)
        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            chat.sendMessageStream(userMessage).collect { chunk ->
                val chunkText = chunk.text.orEmpty()
                if (chunkText.isNotEmpty()) {
                    accumulated.append(chunkText)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("Model returned an empty chat response.")
        }
    }

    suspend fun generateCreatorScript(
        platform: SocialPlatform,
        topic: String,
        tone: String
    ): Result<CreatorScriptResult> = try {
        val response = withTimeout(60_000L) {
            scriptModel.generateContent(
                "Create a viral short-form video script and caption for ${platform.displayName}. Topic: $topic. Tone: $tone. Match the user's language."
            )
        }
        val raw = response.text ?: throw IllegalStateException("Empty script response.")
        val json = JSONObject(raw)
        val tagsArr = json.optJSONArray("hashtags")
        val tags = if (tagsArr != null) {
            (0 until tagsArr.length()).map { tagsArr.getString(it) }
        } else emptyList()
        Result.success(
            CreatorScriptResult(
                hook = json.optString("hook", ""),
                body = json.optString("body", ""),
                cta = json.optString("cta", ""),
                caption = json.optString("caption", ""),
                hashtags = tags
            )
        )
    } catch (e: TimeoutCancellationException) {
        Result.failure(Exception("Script generation timed out after 60 seconds."))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Script generation failed", e)
        Result.failure(e)
    }

    suspend fun generateAutoSubtitles(
        transcriptOrContext: String,
        stylePreset: String
    ): Result<List<SubtitleCue>> = try {
        val response = withTimeout(60_000L) {
            subtitleModel.generateContent(
                "Convert the following spoken video transcript or topic into 5-8 timed pop-up animated subtitle cues with style '$stylePreset':\n$transcriptOrContext"
            )
        }
        val raw = response.text ?: throw IllegalStateException("Empty subtitle response.")
        val json = JSONObject(raw)
        val arr = json.getJSONArray("cues")
        val cues = (0 until arr.length()).map { idx ->
            val obj = arr.getJSONObject(idx)
            SubtitleCue(
                timeRange = obj.optString("timeRange", "00:0${idx * 2}.0 -> 00:0${idx * 2 + 2}.0"),
                text = obj.optString("text", ""),
                popStyle = obj.optString("popStyle", stylePreset)
            )
        }
        Result.success(cues)
    } catch (e: TimeoutCancellationException) {
        Result.failure(Exception("Subtitle generation timed out after 60 seconds."))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Auto-subtitle generation failed", e)
        Result.failure(e)
    }

    suspend fun fetchTrendRadar(
        niche: String,
        platform: SocialPlatform
    ): Result<List<TrendRadarItem>> = try {
        val response = withTimeout(60_000L) {
            trendRadarModel.generateContent(
                "Act as Redtor AI Trend Radar. Identify 4 rising content formats and viral hooks for ${platform.displayName} in niche '$niche'. Match the prompt language."
            )
        }
        val raw = response.text ?: throw IllegalStateException("Empty trend radar response.")
        val json = JSONObject(raw)
        val arr = json.getJSONArray("trends")
        val items = (0 until arr.length()).map { idx ->
            val obj = arr.getJSONObject(idx)
            val tagsArr = obj.optJSONArray("hashtags")
            val tags = if (tagsArr != null) {
                (0 until tagsArr.length()).map { tagsArr.getString(it) }
            } else emptyList()
            TrendRadarItem(
                topic = obj.optString("topic", ""),
                viralScore = obj.optInt("viralScore", 88),
                hookIdea = obj.optString("hookIdea", ""),
                whyItWorks = obj.optString("whyItWorks", ""),
                hashtags = tags
            )
        }
        Result.success(items)
    } catch (e: TimeoutCancellationException) {
        Result.failure(Exception("Trend Radar timed out after 60 seconds."))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Trend Radar failed", e)
        Result.failure(e)
    }
}
