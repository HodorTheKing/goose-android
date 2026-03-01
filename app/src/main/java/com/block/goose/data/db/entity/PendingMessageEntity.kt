package com.block.goose.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Queue for messages that need to be sent when connection is restored
@Entity(
    tableName = "pending_messages",
    indices = [Index(value = ["priority", "createdAt"])]
)
data class PendingMessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val content: String, // JSON serialized
    val workingDir: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val priority: Int = 0, // Higher = more urgent
    val retryCount: Int = 0,
    val nextRetryAt: Long? = null,
    val lastError: String? = null
)
