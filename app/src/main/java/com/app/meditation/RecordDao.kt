package com.app.meditation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.Date

@Dao
interface RecordDao {
    @Query("SELECT * FROM daily_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsForMonth(startDate: Date, endDate: Date): LiveData<List<DailyRecord>>

    @Query("SELECT * FROM daily_records WHERE date BETWEEN :startDate AND :endDate")
    fun getRecordsForDay(startDate: Date, endDate: Date): LiveData<List<DailyRecord>>

    @Query("SELECT * FROM daily_records WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getRecordsForDayNonLive(startDate: Date, endDate: Date): List<DailyRecord>

    @Insert
    suspend fun insert(record: DailyRecord)

    @Query("""
        SELECT MIN(date) as date, SUM(count) as total_count, GROUP_CONCAT(id) as ids
        FROM daily_records
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY date / 86400000
        ORDER BY date DESC
    """)
    fun getDailyRecordSummaries(startDate: Date, endDate: Date): LiveData<List<DailyRecordSummary>>

    @Query("SELECT * FROM daily_records WHERE id IN (:ids)")
    suspend fun getRecordsByIds(ids: List<Long>): List<DailyRecord>
}

data class DailyRecordSummary(
    val date: Date,
    val total_count: Int,
    val ids: String
)