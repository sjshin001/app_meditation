package com.app.meditation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress_rounds")
data class ProgressRound(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val round: Int
)