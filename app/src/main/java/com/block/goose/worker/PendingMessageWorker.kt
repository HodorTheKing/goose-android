package com.block.goose.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.block.goose.data.db.dao.PendingMessageDao
import com.block.goose.data.db.entity.PendingMessageEntity
import com.block.goose.data.api.GooseApiService
import com.block.goose.data.repository.PendingMessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@HiltWorker
class PendingMessageWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: GooseApiService,
    private val pendingMessageDao: PendingMessageDao,
    private val pendingMessageRepository: PendingMessageRepository,
    private val json: Json
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "pending_message_sync"
        const val MAX_RETRY_COUNT = 5
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val pendingMessages = pendingMessageRepository.getMessagesReadyForRetry()

            if (pendingMessages.isEmpty()) {
                return@withContext Result.success()
            }

            var someFailed = false
            var someSucceeded = false

            for (message in pendingMessages) {
                if (message.retryCount >= MAX_RETRY_COUNT) {
                    continue // Skip messages that have exceeded max retries
                }

                val result = sendMessage(message)

                if (result) {
                    pendingMessageDao.deletePendingMessage(message)
                    someSucceeded = true
                } else {
                    val nextRetryAt = pendingMessageRepository.calculateNextRetryTime(message.retryCount + 1)
                    pendingMessageDao.updateRetryInfo(
                        message.id,
                        nextRetryAt,
                        "Failed to send"
                    )
                    someFailed = true
                }
            }

            return@withContext when {
                someSucceeded && someFailed -> Result.retry()
                someFailed -> Result.retry()
                else -> Result.success()
            }
        } catch (e: Exception) {
            return@withContext Result.retry()
        }
    }

    private suspend fun sendMessage(message: PendingMessageEntity): Boolean {
        return try {
            // Deserialize and send
            // Implementation depends on your specific API structure
            true
        } catch (e: Exception) {
            false
        }
    }
}
