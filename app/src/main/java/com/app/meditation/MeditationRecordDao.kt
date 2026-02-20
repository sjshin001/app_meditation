package com.app.meditation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.Date

@Dao
interface MeditationRecordDao {
    @Insert
    suspend fun insert(record: MeditationRecord)

    @Query("SELECT * FROM meditation_records WHERE date BETWEEN :startDate AND :endDate")
    fun getRecordsForDay(startDate: Date, endDate: Date): LiveData<List<MeditationRecord>>

    @Query("SELECT * FROM meditation_records WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getRecordsForDayNonLive(startDate: Date, endDate: Date): List<MeditationRecord>

    @Query("SELECT * FROM meditation_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsForMonth(startDate: Date, endDate: Date): LiveData<List<MeditationRecord>>

    @Query("""
        SELECT MIN(date) as date, SUM(duration) as total_duration, GROUP_CONCAT(id) as ids
        FROM meditation_records
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY date / 86400000
        ORDER BY date DESC
    """)
    fun getMeditationRecordSummaries(startDate: Date, endDate: Date): LiveData<List<MeditationRecordSummary>>

    @Query("SELECT * FROM meditation_records WHERE id IN (:ids)")
    suspend fun getRecordsByIds(ids: List<Long>): List<MeditationRecord>
}

data class MeditationRecordSummary(
    val date: Date,
    val total_duration: Int,
    val ids: String
)