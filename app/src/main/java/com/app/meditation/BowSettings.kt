package com.app.meditation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bow_settings")
data class BowSettings(
    @PrimaryKey val id: Int = 1,
    var interval: Double = 10.0,
    var planCount: Int = 111,
    var keepScreenOn: Boolean = true
)