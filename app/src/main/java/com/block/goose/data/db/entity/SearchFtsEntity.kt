package com.block.goose.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

/**
 * Full-text search virtual table for messages.
 * 
 * NOTE: FTS tables don't support standard foreign keys. We store messageId (UUID) 
 * as a separate column so we can properly join back to messages table.
 * This is needed because FTS rowid is numeric while messages.id is a UUID string.
 */
@Fts4
@Entity(tableName = "messages_fts")
data class SearchFtsEntity(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowId: Int = 0,
    
    @ColumnInfo(name = "messageId")
    val messageId: String,  // UUID reference to messages.id
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "sessionId")
    val sessionId: String
)