package com.block.goose.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    indices = [Index(value = ["updatedAt"], orders = [Index.Order.DESC])]
)
data class SessionEntity(
    @PrimaryKey
    val id: String,
    val description: String = "",
    val workingDir: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messageCount: Int = 0,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false
)
