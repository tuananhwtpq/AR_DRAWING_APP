package com.flowart.ar.drawing.sketch.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_model")
data class RecentModel(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val lastOpen: Long
)
