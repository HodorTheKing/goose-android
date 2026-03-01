package com.block.goose.data.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.block.goose.data.db.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllActiveSessions(): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM sessions WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllActiveSessionsPaged(): PagingSource<Int, SessionEntity>
    
    @Query("SELECT * FROM sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): SessionEntity?
    
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    fun getSessionByIdFlow(sessionId: String): Flow<SessionEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<SessionEntity>)
    
    @Update
    suspend fun updateSession(session: SessionEntity)
    
    @Query("UPDATE sessions SET description = :description, updatedAt = :timestamp WHERE id = :sessionId")
    suspend fun updateSessionDescription(sessionId: String, description: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE sessions SET isPinned = :isPinned WHERE id = :sessionId")
    suspend fun togglePinSession(sessionId: String, isPinned: Boolean)
    
    @Query("UPDATE sessions SET isArchived = 1, updatedAt = :timestamp WHERE id = :sessionId")
    suspend fun archiveSession(sessionId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE sessions SET isArchived = 0, updatedAt = :timestamp WHERE id = :sessionId")
    suspend fun unarchiveSession(sessionId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)
    
    @Query("DELETE FROM sessions WHERE isArchived = 1")
    suspend fun deleteAllArchivedSessions(): Int
    
    @Query("SELECT COUNT(*) FROM sessions WHERE isArchived = 0")
    suspend fun getActiveSessionCount(): Int
    
    @Query("UPDATE sessions SET messageCount = :count WHERE id = :sessionId")
    suspend fun updateMessageCount(sessionId: String, count: Int)
    
    @Query("SELECT * FROM sessions WHERE description LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    suspend fun searchSessions(query: String): List<SessionEntity>
    
    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()
}
