package com.app.meditation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BowSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: BowSettings)

    @Query("SELECT * FROM bow_settings WHERE id = 1")
    fun getSettings(): LiveData<BowSettings?>
}