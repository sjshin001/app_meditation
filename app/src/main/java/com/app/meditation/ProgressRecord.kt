package com.app.meditation

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "progress_records",
        indices = [Index(value = ["roundId"])], // Add index for foreign key
        foreignKeys = [ForeignKey(entity = ProgressRound::class, 
                                  parentColumns = ["id"], 
                                  childColumns = ["roundId"], 
                                  onDelete = ForeignKey.CASCADE)])
data class ProgressRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roundId: Long,
    val day: Int, // 1 to 100
    var date: Date?,
    val content: String?
)