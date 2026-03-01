package com.block.goose.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MessageStatus {
    PENDING, SENDING, SENT, FAILED, DELIVERED
}

enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

// Simple content storage for Room - serialize to JSON
@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["createdAt"]),
        Index(value = ["status"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val role: MessageRole,
    val content: String, // JSON serialized MessageContent list
    val createdAt: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val lastRetryAt: Long? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val isBookmarked: Boolean = false
)
