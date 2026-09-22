package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.InstagramProfileEntity
import com.example.data.model.SavedPostEntity
import com.example.data.model.ScheduledPostEntity

@Database(
    entities = [
        SavedPostEntity::class,
        ScheduledPostEntity::class,
        InstagramProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QuoteStudioDatabase : RoomDatabase() {
    abstract fun savedPostDao(): SavedPostDao
    abstract fun scheduledPostDao(): ScheduledPostDao
    abstract fun instagramProfileDao(): InstagramProfileDao

    companion object {
        @Volatile
        private var INSTANCE: QuoteStudioDatabase? = null

        fun getInstance(context: Context): QuoteStudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuoteStudioDatabase::class.java,
                    "quotestudio.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
