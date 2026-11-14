package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ActivityItemEntity::class,
        CleaningSessionEntity::class,
        CleaningSessionItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TikTokCleanerDatabase : RoomDatabase() {
    abstract fun activityItemDao(): ActivityItemDao
    abstract fun cleaningHistoryDao(): CleaningHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: TikTokCleanerDatabase? = null

        fun getInstance(context: Context): TikTokCleanerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TikTokCleanerDatabase::class.java,
                    "tiktok_cleaner.db"
                )
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
