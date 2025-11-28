package com.example.baseproject.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_model")
data class LessonModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val listStep: List<String>,
    var difficulty: Int = 0,
    var isFavorite: Boolean = false,
    val level: Int,
    var isDone: Boolean = false
) {
    companion object {
        val ITEM_ADS = LessonModel(-1, "", listOf(), 0, false, -1)
    }
}
