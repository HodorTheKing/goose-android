package com.block.goose.data.db.dao

import androidx.room.*
import com.block.goose.data.db.entity.PendingMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingMessageDao {
    @Query("SELECT * FROM pending_messages ORDER BY priority DESC, createdAt ASC")
    fun getAllPendingMessages(): Flow<List<PendingMessageEntity>>
    
    @Query("SELECT * FROM pending_messages WHERE nextRetryAt IS NULL OR nextRetryAt <= :currentTime ORDER BY priority DESC, createdAt ASC")
    suspend fun getMessagesReadyForRetry(currentTime: Long = System.currentTimeMillis()): List<PendingMessageEntity>
    
    @Query("SELECT * FROM pending_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    suspend fun getPendingMessagesForSession(sessionId: String): List<PendingMessageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingMessage(message: PendingMessageEntity)
    
    @Delete
    suspend fun deletePendingMessage(message: PendingMessageEntity)
    
    @Query("DELETE FROM pending_messages WHERE id = :messageId")
    suspend fun deletePendingMessageById(messageId: String)
    
    @Query("DELETE FROM pending_messages WHERE sessionId = :sessionId")
    suspend fun deletePendingMessagesForSession(sessionId: String)
    
    @Query("UPDATE pending_messages SET retryCount = retryCount + 1, nextRetryAt = :nextRetryAt, lastError = :lastError WHERE id = :messageId")
    suspend fun updateRetryInfo(messageId: String, nextRetryAt: Long?, lastError: String?)
    
    @Query("SELECT COUNT(*) FROM pending_messages")
    suspend fun getPendingCount(): Int
    
    @Query("DELETE FROM pending_messages")
    suspend fun deleteAllPendingMessages()
    
    @Query("SELECT * FROM pending_messages WHERE retryCount >= :maxRetries ORDER BY createdAt ASC")
    suspend fun getFailedMessages(maxRetries: Int = 3): List<PendingMessageEntity>
}
