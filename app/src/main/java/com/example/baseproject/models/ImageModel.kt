package com.example.baseproject.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_model")
data class ImageModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val img: String,
    val categoryName: String,
    val category: Int,
    var isFavorite: Boolean = false,
) {
    companion object {
        val EMPTY_ITEM = ImageModel(-1, "", "", -1, false)
    }
}