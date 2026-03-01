package com.block.goose.data.db.dao

import androidx.room.*
import com.block.goose.data.db.entity.MessageEntity
import com.block.goose.data.db.entity.MessageStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE sessionId = :sessionId AND isDeleted = 0 ORDER BY createdAt ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE sessionId = :sessionId AND isDeleted = 0 ORDER BY createdAt ASC")
    suspend fun getMessagesForSessionSync(sessionId: String): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)
    
    @Query("UPDATE messages SET status = :status, errorMessage = :errorMessage WHERE id = :messageId")
    suspend fun markMessageFailed(messageId: String, status: MessageStatus = MessageStatus.FAILED, errorMessage: String?)
    
    @Query("UPDATE messages SET status = :status, retryCount = retryCount + 1, lastRetryAt = :timestamp WHERE id = :messageId")
    suspend fun incrementRetryCount(messageId: String, status: MessageStatus = MessageStatus.PENDING, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE messages SET isDeleted = 1, deletedAt = :timestamp WHERE id = :messageId")
    suspend fun softDeleteMessage(messageId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun hardDeleteMessage(messageId: String)
    
    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)
    
    @Query("SELECT * FROM messages WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getMessagesByStatus(status: MessageStatus): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE status IN ('PENDING', 'FAILED') ORDER BY createdAt ASC")
    fun getPendingAndFailedMessages(): Flow<List<MessageEntity>>
    
    @Query("SELECT COUNT(*) FROM messages WHERE sessionId = :sessionId AND isDeleted = 0")
    suspend fun getMessageCountForSession(sessionId: String): Int
    
    @Query("UPDATE messages SET isBookmarked = :isBookmarked WHERE id = :messageId")
    suspend fun setBookmarked(messageId: String, isBookmarked: Boolean)
    
    @Query("SELECT * FROM messages WHERE isBookmarked = 1 AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getBookmarkedMessages(): Flow<List<MessageEntity>>
    
    /**
     * Full-text search using FTS virtual table.
     * We join on messageId (UUID) since FTS rowid is numeric and messages.id is string UUID.
     */
    @Query("""
        SELECT messages.* FROM messages 
        JOIN messages_fts ON messages.id = messages_fts.messageId 
        WHERE messages_fts.content MATCH :query 
        AND messages.isDeleted = 0
        ORDER BY messages.createdAt DESC
    """)
    suspend fun searchMessages(query: String): List<MessageEntity>
    
    @Query("DELETE FROM messages WHERE isDeleted = 1 AND deletedAt < :cutoffTime")
    suspend fun deleteOldSoftDeletedMessages(cutoffTime: Long)
    
    @Query("DELETE FROM messages WHERE createdAt < :cutoffTime AND isBookmarked = 0")
    suspend fun deleteMessagesOlderThan(cutoffTime: Long): Int
    
    @Query("SELECT COUNT(*) FROM messages WHERE sessionId = :sessionId AND role = 'USER' AND isDeleted = 0")
    suspend fun getUserMessageCount(sessionId: String): Int
    
    @Query("SELECT COUNT(*) FROM messages WHERE sessionId = :sessionId AND role = 'ASSISTANT' AND isDeleted = 0")
    suspend fun getAssistantMessageCount(sessionId: String): Int
}