package com.block.goose.data.repository

import com.block.goose.data.db.dao.BookmarkDao
import com.block.goose.data.db.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao
) {
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> =
        bookmarkDao.getAllBookmarks()

    suspend fun addBookmark(messageId: String, note: String? = null) {
        bookmarkDao.insertBookmark(BookmarkEntity(messageId = messageId, note = note))
    }

    suspend fun removeBookmark(messageId: String) {
        bookmarkDao.deleteBookmarkByMessageId(messageId)
    }

    suspend fun isBookmarked(messageId: String): Boolean =
        bookmarkDao.isBookmarked(messageId)

    suspend fun toggleBookmark(messageId: String, note: String? = null): Boolean {
        return if (isBookmarked(messageId)) {
            removeBookmark(messageId)
            false
        } else {
            addBookmark(messageId, note)
            true
        }
    }

    suspend fun getBookmarkCount(): Int =
        bookmarkDao.getBookmarkCount()
}
