package com.block.goose.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a chat session stored locally.
 */
@Entity(
    tableName = "sessions",
    indices = [Index(value = ["remoteId"], unique = true)]
)
data class SessionEntity(
    @PrimaryKey
    val id: String,
    val remoteId: String? = null,
    val description: String = "",
    val messageCount: Int = 0,
    val createdAt: String,
    val updatedAt: String,
    val workingDir: String? = null,
    val isArchived: Boolean = false,
    val isSynced: Boolean = false,
    val customName: String? = null,
    val bookmarked: Boolean = false
) {
    val displayName: String
        get() = customName?.takeIf { it.isNotBlank() } 
            ?: description.takeIf { it.isNotBlank() }
            ?: "Session ${id.take(8)}"
}