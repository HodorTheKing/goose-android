package com.block.goose.data.repository

import com.block.goose.data.db.dao.SessionDao
import com.block.goose.data.db.entity.SessionEntity
import com.block.goose.data.model.ChatSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    fun getAllActiveSessions(): Flow<List<ChatSession>> =
        sessionDao.getAllActiveSessions().map { entities ->
            entities.map { it.toChatSession() }
        }

    suspend fun getSessionById(sessionId: String): ChatSession? =
        sessionDao.getSessionById(sessionId)?.toChatSession()

    suspend fun insertSession(session: ChatSession) {
        sessionDao.insertSession(session.toEntity())
    }

    suspend fun updateSessionDescription(sessionId: String, description: String) {
        sessionDao.updateSessionDescription(sessionId, description)
    }

    suspend fun togglePinSession(sessionId: String, isPinned: Boolean) {
        sessionDao.togglePinSession(sessionId, isPinned)
    }

    suspend fun archiveSession(sessionId: String) {
        sessionDao.archiveSession(sessionId)
    }

    suspend fun deleteSession(sessionId: String) {
        sessionDao.deleteSession(sessionId)
    }

    suspend fun deleteAllArchivedSessions(): Int {
        return sessionDao.deleteAllArchivedSessions()
    }

    suspend fun updateMessageCount(sessionId: String, count: Int) {
        sessionDao.updateMessageCount(sessionId, count)
    }

    suspend fun searchSessions(query: String): List<ChatSession> {
        return sessionDao.searchSessions(query).map { it.toChatSession() }
    }

    suspend fun clearAllSessions() {
        sessionDao.deleteAllSessions()
    }

    // Mappers
    private fun SessionEntity.toChatSession(): ChatSession {
        return ChatSession(
            id = id,
            description = description,
            messageCount = messageCount,
            createdAt = java.time.Instant.ofEpochMilli(createdAt).toString(),
            updatedAt = java.time.Instant.ofEpochMilli(updatedAt).toString(),
            workingDir = workingDir,
            isArchived = isArchived,
            isPinned = isPinned
        )
    }

    private fun ChatSession.toEntity(): SessionEntity {
        return SessionEntity(
            id = id,
            description = description,
            workingDir = workingDir,
            createdAt = java.time.Instant.parse(createdAt).toEpochMilli(),
            updatedAt = java.time.Instant.parse(updatedAt).toEpochMilli(),
            messageCount = messageCount,
            isArchived = isArchived,
            isPinned = isPinned
        )
    }
}
