package com.block.goose.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.block.goose.data.db.dao.BookmarkDao
import com.block.goose.data.db.dao.MessageDao
import com.block.goose.data.db.dao.PendingMessageDao
import com.block.goose.data.db.dao.SessionDao
import com.block.goose.data.db.entity.BookmarkEntity
import com.block.goose.data.db.entity.MessageEntity
import com.block.goose.data.db.entity.PendingMessageEntity
import com.block.goose.data.db.entity.SearchFtsEntity
import com.block.goose.data.db.entity.SessionEntity

@Database(
    entities = [
        SessionEntity::class,
        MessageEntity::class,
        PendingMessageEntity::class,
        SearchFtsEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
    abstract fun pendingMessageDao(): PendingMessageDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val DATABASE_NAME = "goose_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration for adding new tables/columns
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Future migrations go here
            }
        }

        private val ALL_MIGRATIONS = arrayOf(Migration(1, 2) { _ -> })
    }
}
