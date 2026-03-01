package com.block.goose.data.repository

import com.block.goose.data.db.dao.MessageDao
import com.block.goose.data.db.entity.MessageEntity
import com.block.goose.data.db.entity.MessageRole
import com.block.goose.data.db.entity.MessageStatus
import com.block.goose.data.model.Message
import com.block.goose.data.model.MessageContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val json: Json
) {
    fun getMessagesForSession(sessionId: String): Flow<List<Message>> =
        messageDao.getMessagesForSession(sessionId).map { entities ->
            entities.mapNotNull { it.toMessage(json) }
        }

    suspend fun getMessageById(messageId: String): Message? =
        messageDao.getMessageById(messageId)?.toMessage(json)

    suspend fun insertMessage(message: Message, sessionId: String, status: MessageStatus = MessageStatus.SENT) {
        messageDao.insertMessage(message.toEntity(sessionId, status, json))
    }

    suspend fun insertMessages(messages: List<Message>, sessionId: String) {
        messageDao.insertMessages(messages.map { it.toEntity(sessionId, MessageStatus.SENT, json) })
    }

    suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        messageDao.updateMessageStatus(messageId, status)
    }

    suspend fun markMessageFailed(messageId: String, errorMessage: String?) {
        messageDao.markMessageFailed(messageId, MessageStatus.FAILED, errorMessage)
    }

    suspend fun incrementRetryCount(messageId: String) {
        messageDao.incrementRetryCount(messageId)
    }

    suspend fun softDeleteMessage(messageId: String) {
        messageDao.softDeleteMessage(messageId)
    }

    suspend fun hardDeleteMessage(messageId: String) {
        messageDao.hardDeleteMessage(messageId)
    }

    suspend fun deleteMessagesForSession(sessionId: String) {
        messageDao.deleteMessagesForSession(sessionId)
    }

    suspend fun setBookmarked(messageId: String, isBookmarked: Boolean) {
        messageDao.setBookmarked(messageId, isBookmarked)
    }

    fun getBookmarkedMessages(): Flow<List<Message>> =
        messageDao.getBookmarkedMessages().map { entities ->
            entities.mapNotNull { it.toMessage(json) }
        }

    suspend fun searchMessages(query: String): List<Message> {
        return messageDao.searchMessages(query).mapNotNull { it.toMessage(json) }
    }

    suspend fun deleteMessagesOlderThan(days: Int): Int {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000)
        return messageDao.deleteMessagesOlderThan(cutoffTime)
    }

    suspend fun cleanupOldSoftDeletedMessages(days: Int) {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000)
        messageDao.deleteOldSoftDeletedMessages(cutoffTime)
    }

    suspend fun getPendingAndFailedMessages(): Flow<List<Message>> =
        messageDao.getPendingAndFailedMessages().map { entities ->
            entities.mapNotNull { it.toMessage(json) }
        }

    // Mappers
    private fun MessageEntity.toMessage(json: Json): Message? {
        return try {
            Message(
                id = id,
                role = when (role) {
                    MessageRole.USER -> com.block.goose.data.model.MessageRole.USER
                    MessageRole.ASSISTANT -> com.block.goose.data.model.MessageRole.ASSISTANT
                    MessageRole.SYSTEM -> com.block.goose.data.model.MessageRole.SYSTEM
                },
                content = json.decodeFromString(content),
                created = createdAt
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun Message.toEntity(sessionId: String, status: MessageStatus, json: Json): MessageEntity {
        return MessageEntity(
            id = id,
            sessionId = sessionId,
            role = when (role) {
                com.block.goose.data.model.MessageRole.USER -> MessageRole.USER
                com.block.goose.data.model.MessageRole.ASSISTANT -> MessageRole.ASSISTANT
                com.block.goose.data.model.MessageRole.SYSTEM -> MessageRole.SYSTEM
            },
            content = json.encodeToString(content),
            createdAt = created,
            status = status,
            isBookmarked = false
        )
    }
}
