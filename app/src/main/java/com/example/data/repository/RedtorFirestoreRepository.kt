package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.firebase.OperationType
import com.example.core.firebase.handleFirestoreError
import com.example.data.model.ChatMessage
import com.example.data.model.CreatorAsset
import com.example.data.model.DownloadFormat
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTask
import com.example.data.model.MediaPlatform
import com.example.data.model.MuxingMode
import com.example.data.model.StudioProject
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class RedtorFirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        if (expectedUserId != null) {
            check(uid == expectedUserId) { "Signed-in user changed; refusing cross-user access." }
        }
        return uid
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(user: FirebaseUser): Result<Unit> {
        val uid = user.uid
        val path = "users/$uid"
        return try {
            requireUserId(uid)
            val docRef = db.collection("users").document(uid)
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val safeName = user.displayName?.takeIf { it.isNotBlank() }?.take(120) ?: "Redtor Creator"
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "email" to user.email?.takeIf { it.isNotBlank() }?.take(160),
                    "photoUrl" to user.photoUrl?.toString()?.takeIf { it.isNotBlank() }?.take(500),
                    "tier" to "PRO",
                    "dailyAiQuotaUsed" to 0L,
                    "dailyAiQuotaLimit" to 100L,
                    "autoDeleteCache" to true,
                    "preferredColorGrade" to "Ruby Noir",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateUserSettings(
        userId: String,
        autoDeleteCache: Boolean,
        preferredColorGrade: String
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val safeLut = preferredColorGrade.trim().ifBlank { "Ruby Noir" }.take(60)
            db.collection("users").document(uid).update(
                mapOf(
                    "autoDeleteCache" to autoDeleteCache,
                    "preferredColorGrade" to safeLut,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun incrementAiQuota(userId: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid)
            val currentUsed = (docRef.get().await().getLong("dailyAiQuotaUsed") ?: 0L)
            if (currentUsed < 1000L) {
                docRef.update(
                    mapOf(
                        "dailyAiQuotaUsed" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeStudioProjects(userId: String): Flow<List<StudioProject>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/studioProjects"
        emitAll(
            db.collection("users").document(uid).collection("studioProjects")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(30)
                .snapshots()
                .map { it.toObjects(StudioProject::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveStudioProject(
        userId: String,
        toolId: String,
        title: String,
        prompt: String,
        aiOutput: String,
        presetTag: String
    ): Result<Unit> {
        val path = "users/$userId/studioProjects"
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid).collection("studioProjects").document()
            val payload = mapOf(
                "userId" to uid,
                "toolId" to toolId.trim().ifBlank { "text_to_video" }.take(40),
                "title" to title.trim().ifBlank { "Redtor Creation" }.take(120),
                "prompt" to prompt.trim().ifBlank { "AI Creative Render" }.take(2000),
                "aiOutput" to aiOutput.take(15000),
                "presetTag" to presetTag.trim().ifBlank { "Ruby Noir" }.take(60),
                "status" to "COMPLETED",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteStudioProject(userId: String, projectId: String): Result<Unit> {
        val path = "users/$userId/studioProjects/$projectId"
        return try {
            val uid = requireUserId(userId)
            require(projectId.isNotBlank())
            db.collection("users").document(uid).collection("studioProjects").document(projectId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeDownloadTasks(userId: String): Flow<List<DownloadTask>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/downloadTasks"
        emitAll(
            db.collection("users").document(uid).collection("downloadTasks")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(30)
                .snapshots()
                .map { it.toObjects(DownloadTask::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addDownloadTask(
        userId: String,
        url: String,
        platform: MediaPlatform,
        title: String,
        format: DownloadFormat,
        muxingMode: MuxingMode,
        status: DownloadStatus,
        aiSummary: String
    ): Result<String> {
        val path = "users/$userId/downloadTasks"
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid).collection("downloadTasks").document()
            val payload = mapOf(
                "userId" to uid,
                "url" to url.trim().ifBlank { "https://redtor.studio" }.take(1000),
                "platform" to platform.name,
                "title" to title.trim().ifBlank { "Media Queue Item" }.take(200),
                "format" to format.name,
                "muxingMode" to muxingMode.name,
                "status" to status.name,
                "aiSummary" to aiSummary.take(4000),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateDownloadTaskStatus(
        userId: String,
        taskId: String,
        status: DownloadStatus
    ): Result<Unit> {
        val path = "users/$userId/downloadTasks/$taskId"
        return try {
            val uid = requireUserId(userId)
            require(taskId.isNotBlank())
            db.collection("users").document(uid).collection("downloadTasks").document(taskId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteDownloadTask(userId: String, taskId: String): Result<Unit> {
        val path = "users/$userId/downloadTasks/$taskId"
        return try {
            val uid = requireUserId(userId)
            require(taskId.isNotBlank())
            db.collection("users").document(uid).collection("downloadTasks").document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeChatMessages(userId: String, platformKey: String): Flow<List<ChatMessage>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/chatMessages"
        emitAll(
            db.collection("users").document(uid).collection("chatMessages")
                .whereEqualTo("platform", platformKey)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveChatMessage(
        userId: String,
        platformKey: String,
        role: String,
        content: String
    ): Result<Unit> {
        val path = "users/$userId/chatMessages"
        return try {
            val uid = requireUserId(userId)
            val safeContent = content.trim()
            if (safeContent.isBlank()) return Result.success(Unit)
            val docRef = db.collection("users").document(uid).collection("chatMessages").document()
            val payload = mapOf(
                "userId" to uid,
                "platform" to platformKey.take(40),
                "role" to if (role == "model") "model" else "user",
                "content" to safeContent.take(15000),
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun clearPlatformChat(userId: String, platformKey: String): Result<Unit> {
        val path = "users/$userId/chatMessages"
        return try {
            val uid = requireUserId(userId)
            val docs = db.collection("users").document(uid).collection("chatMessages")
                .whereEqualTo("platform", platformKey)
                .get()
                .await()
            if (!docs.isEmpty) {
                val batch = db.batch()
                docs.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeCreatorAssets(userId: String): Flow<List<CreatorAsset>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/creatorAssets"
        emitAll(
            db.collection("users").document(uid).collection("creatorAssets")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(25)
                .snapshots()
                .map { it.toObjects(CreatorAsset::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveCreatorAsset(
        userId: String,
        assetType: String,
        platformKey: String,
        topic: String,
        content: String
    ): Result<Unit> {
        val path = "users/$userId/creatorAssets"
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid).collection("creatorAssets").document()
            val payload = mapOf(
                "userId" to uid,
                "assetType" to assetType,
                "platform" to platformKey.take(40),
                "topic" to topic.trim().ifBlank { "Creator Suite Output" }.take(200),
                "content" to content.trim().ifBlank { "Generated" }.take(15000),
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCreatorAsset(userId: String, assetId: String): Result<Unit> {
        val path = "users/$userId/creatorAssets/$assetId"
        return try {
            val uid = requireUserId(userId)
            require(assetId.isNotBlank())
            db.collection("users").document(uid).collection("creatorAssets").document(assetId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
