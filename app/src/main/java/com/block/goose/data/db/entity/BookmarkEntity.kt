package com.block.goose.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["createdAt"], orders = [Index.Order.DESC])]
)
data class BookmarkEntity(
    @PrimaryKey
    val messageId: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
