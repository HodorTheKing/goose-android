package com.block.goose.data.db.converter

import androidx.room.TypeConverter
import com.block.goose.data.db.entity.MessageRole
import com.block.goose.data.db.entity.MessageStatus

class MessageStatusConverter {
    @TypeConverter
    fun fromMessageStatus(status: MessageStatus): String = status.name

    @TypeConverter
    fun toMessageStatus(status: String): MessageStatus = 
        MessageStatus.valueOf(status)
}

class MessageRoleConverter {
    @TypeConverter
    fun fromMessageRole(role: MessageRole): String = role.name

    @TypeConverter
    fun toMessageRole(role: String): MessageRole = 
        MessageRole.valueOf(role)
}
