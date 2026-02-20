package com.app.meditation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [
    DailyRecord::class, 
    Meditation::class, 
    BowSettings::class, 
    ProgressRound::class, 
    ProgressRecord::class, 
    MeditationRecord::class
], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao
    abstract fun meditationDao(): MeditationDao
    abstract fun bowSettingsDao(): BowSettingsDao
    abstract fun progressDao(): ProgressDao
    abstract fun meditationRecordDao(): MeditationRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meditation_database"
                )
                .fallbackToDestructiveMigration()
                .setJournalMode(JournalMode.TRUNCATE)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}