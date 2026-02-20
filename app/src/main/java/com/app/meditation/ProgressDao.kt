package com.app.meditation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRound(round: ProgressRound): Long

    @Query("SELECT * FROM progress_rounds ORDER BY round DESC")
    fun getAllRounds(): LiveData<List<ProgressRound>>

    @Query("SELECT * FROM progress_rounds")
    suspend fun getAllRoundsList(): List<ProgressRound>

    @Query("DELETE FROM progress_rounds WHERE id = :roundId")
    suspend fun deleteRoundById(roundId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: ProgressRecord)

    @Query("SELECT * FROM progress_records WHERE roundId = :roundId")
    fun getRecordsForRound(roundId: Long): LiveData<List<ProgressRecord>>

    @Query("SELECT * FROM progress_records WHERE roundId = :roundId AND day = :day LIMIT 1")
    suspend fun getRecordForDay(roundId: Long, day: Int): ProgressRecord?
}