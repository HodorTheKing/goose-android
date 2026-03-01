package com.block.goose.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.block.goose.data.api.ApiResult
import com.block.goose.data.api.GooseApiService
import com.block.goose.data.local.dao.PendingMessageDao
import com.block.goose.data.local.entity.PendingMessageEntity
import com.block.goose.data.local.entity.PendingMessageStatus
import com.block.goose.data.model.Message
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.UnknownHostException
import kotlin.math.min

@HiltWorker
class PendingMessageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: GooseApiService,
    private val pendingMessageDao: PendingMessageDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "pending_message_sync"
        private const val TAG = "PendingMessageWorker"
        private const val MAX_RETRIES = 3
        private const val INITIAL_DELAY_MS = 1000L
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting sync for pending messages")

        val pendingMessages = pendingMessageDao.getPendingMessagesOrderedByTimestamp()

        if (pendingMessages.isEmpty()) {
            Log.d(TAG, "No pending messages to sync")
            return@withContext Result.success()
        }

        Log.d(TAG, "Found ${pendingMessages.size} pending messages")

        val messagesToProcess = pendingMessages.toList()
        var hasFailures = false

        // Group messages by session for efficient batching
        val messagesBySession = messagesToProcess.groupBy { it.sessionId }

        messagesBySession.forEach { (sessionId, sessionMessages) ->
            // Ensure session is active before sending
            val sessionActive = ensureSessionActive(sessionId)
            if (!sessionActive) {
                Log.w(TAG, "Could not activate session $sessionId, skipping messages")
                hasFailures = true
                return@forEach
            }

            sessionMessages.forEach { pendingMessage ->
                val success = sendMessageWithRetry(pendingMessage)
                if (!success) {
                    hasFailures = true
                }
            }
        }

        return@withContext if (hasFailures) {
            Log.w(TAG, "Some messages failed to sync, will retry later")
            Result.retry()
        } else {
            Log.d(TAG, "All pending messages synced successfully")
            Result.success()
        }
    }

    private suspend fun ensureSessionActive(sessionId: String): Boolean {
        return try {
            // First try to resume the session
            when (val result = apiService.resumeAgent(sessionId, loadModelAndExtensions = true)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Session $sessionId resumed successfully")
                    true
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Failed to resume session: ${result.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring session active", e)
            false
        }
    }

    private suspend fun sendMessageWithRetry(pendingMessage: PendingMessageEntity): Boolean {
        var retryCount = 0
        var lastError: Throwable? = null

        // Update status to SENDING
        pendingMessageDao.updatePendingMessageStatus(pendingMessage.id, PendingMessageStatus.SENDING)

        while (retryCount < MAX_RETRIES) {
            try {
                if (retryCount > 0) {
                    val delayMs = INITIAL_DELAY_MS * (1 shl retryCount) // Exponential backoff
                    Log.d(TAG, "Retry ${retryCount + 1} for message ${pendingMessage.id}, waiting ${delayMs}ms")
                    delay(delayMs)
                }

                val success = sendActualMessage(pendingMessage)
                if (success) {
                    // Mark as completed and delete from queue
                    pendingMessageDao.deletePendingMessage(pendingMessage.id)
                    Log.d(TAG, "Successfully sent message ${pendingMessage.id}")
                    return true
                } else {
                    throw Exception("API returned failure")
                }
            } catch (e: UnknownHostException) {
                lastError = e
                Log.w(TAG, "No network connection for message ${pendingMessage.id}")
                // Don't retry immediately for network errors
                break
            } catch (e: Exception) {
                lastError = e
                Log.e(TAG, "Failed to send message ${pendingMessage.id} (attempt ${retryCount + 1})", e)
                retryCount++
            }
        }

        // Update status to FAILED after all retries exhausted
        pendingMessageDao.updatePendingMessageStatus(
            pendingMessage.id,
            PendingMessageStatus.FAILED,
            lastError?.message ?: "Unknown error"
        )

        return false
    }

    private suspend fun sendActualMessage(pendingMessage: PendingMessageEntity): Boolean {
        return try {
            // Parse the stored messages
            val messages = parseMessagesFromJson(pendingMessage.messagesJson)

            // Make the actual API call
            val chatRequestBody = buildChatRequestBody(messages, pendingMessage.sessionId)

            // Use the API service to send the message
            when (val result = apiService.sendChatRequest(chatRequestBody)) {
                is ApiResult.Success -> {
                    // Message sent successfully
                    Log.d(TAG, "API call successful for message ${pendingMessage.id}")
                    true
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "API call failed: ${result.message} (code: ${result.code})")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending actual message", e)
            throw e // Re-throw to trigger retry logic
        }
    }

    private fun parseMessagesFromJson(json: String): List<Message> {
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse messages from JSON", e)
            emptyList()
        }
    }

    private fun buildChatRequestBody(messages: List<Message>, sessionId: String): String {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        return json.encodeToString(mapOf(
            "messages" to json.encodeToString(messages),
            "session_id" to sessionId
        ))
    }
}