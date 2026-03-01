package com.block.goose.data.db.dao

import androidx.room.*
import com.block.goose.data.db.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)
    
    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)
    
    @Query("DELETE FROM bookmarks WHERE messageId = :messageId")
    suspend fun deleteBookmarkByMessageId(messageId: String)
    
    @Query("SELECT * FROM bookmarks WHERE messageId = :messageId")
    suspend fun getBookmarkByMessageId(messageId: String): BookmarkEntity?
    
    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE messageId = :messageId)")
    suspend fun isBookmarked(messageId: String): Boolean
    
    @Query("SELECT COUNT(*) FROM bookmarks")
    suspend fun getBookmarkCount(): Int
}
