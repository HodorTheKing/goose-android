package com.block.goose.data.repository

import com.block.goose.data.db.dao.PendingMessageDao
import com.block.goose.data.db.entity.PendingMessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingMessageRepository @Inject constructor(
    private val pendingMessageDao: PendingMessageDao
) {
    fun getAllPendingMessages(): Flow<List<PendingMessageEntity>> =
        pendingMessageDao.getAllPendingMessages()

    suspend fun getMessagesReadyForRetry(): List<PendingMessageEntity> =
        pendingMessageDao.getMessagesReadyForRetry()

    suspend fun insertPendingMessage(message: PendingMessageEntity) {
        pendingMessageDao.insertPendingMessage(message)
    }

    suspend fun deletePendingMessage(message: PendingMessageEntity) {
        pendingMessageDao.deletePendingMessage(message)
    }

    suspend fun deletePendingMessageById(messageId: String) {
        pendingMessageDao.deletePendingMessageById(messageId)
    }

    suspend fun deletePendingMessagesForSession(sessionId: String) {
        pendingMessageDao.deletePendingMessagesForSession(sessionId)
    }

    suspend fun updateRetryInfo(messageId: String, nextRetryAt: Long?, lastError: String?) {
        pendingMessageDao.updateRetryInfo(messageId, nextRetryAt, lastError)
    }

    suspend fun getPendingCount(): Int =
        pendingMessageDao.getPendingCount()

    suspend fun getFailedMessages(maxRetries: Int = 3): List<PendingMessageEntity> =
        pendingMessageDao.getFailedMessages(maxRetries)

    suspend fun clearAllPendingMessages() {
        pendingMessageDao.deleteAllPendingMessages()
    }

    fun calculateNextRetryTime(retryCount: Int): Long {
        // Exponential backoff: 2^retryCount * 1000 ms, capped at 5 minutes
        val delayMs = minOf(Math.pow(2.0, retryCount.toDouble()).toLong() * 1000, 300000)
        return System.currentTimeMillis() + delayMs
    }
}
