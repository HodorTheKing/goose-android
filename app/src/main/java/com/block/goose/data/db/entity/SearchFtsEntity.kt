package com.block.goose.data.db.entity

import androidx.room.Entity
import androidx.room.Fts4

// Full-text search for messages
@Fts4(contentEntity = MessageEntity::class)
@Entity(tableName = "messages_fts")
data class SearchFtsEntity(
    val content: String,
    val sessionId: String
)
