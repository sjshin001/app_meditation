package com.app.meditation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MeditationDao {
    @Query("SELECT * FROM meditations ORDER BY id DESC")
    fun getAll(): LiveData<List<Meditation>>

    @Query("SELECT * FROM meditations")
    suspend fun getAllList(): List<Meditation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meditation: Meditation)

    @Update
    suspend fun update(meditation: Meditation)

    @Query("DELETE FROM meditations WHERE id = :id")
    suspend fun deleteById(id: Long)
}