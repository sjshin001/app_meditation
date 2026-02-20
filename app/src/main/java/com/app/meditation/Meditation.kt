package com.app.meditation

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "meditations")
data class Meditation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String,
    var time: Int // 분 단위
) : Parcelable