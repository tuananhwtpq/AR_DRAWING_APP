package com.ssquad.ar.drawing.sketch.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.baseproject.models.ImageModel
import com.example.baseproject.models.LessonModel
import com.example.baseproject.models.RecentModel

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lessonModel: LessonModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToRecent(recentModel: RecentModel)

    @Query("UPDATE image_model SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateImageFavorite(id: Int, isFavorite: Int)

    @Query("UPDATE lesson_model SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateLessonFavorite(isFavorite: Int, id: Int)

    @Query("SELECT image_model.* FROM image_model INNER JOIN recent_model ON image_model.id = recent_model.id ORDER BY recent_model.lastOpen DESC LIMIT 6 OFFSET 0")
    fun getRecentImage(): LiveData<List<ImageModel>>

    @Query("SELECT * FROM image_model WHERE category =:category")
    fun getImageByCategory(category: Int): LiveData<List<ImageModel>>

    @Query("SELECT * FROM lesson_model WHERE level =:level")
    fun getLessonByLevel(level: Int): LiveData<List<LessonModel>>

    @Query("SELECT * FROM image_model WHERE isFavorite = 1")
    fun getAllFavoriteImage(): LiveData<List<ImageModel>>

    @Query("SELECT * FROM lesson_model WHERE isFavorite = 1")
    fun getAllFavoriteLesson(): LiveData<List<LessonModel>>

    @Query("SELECT * FROM lesson_model WHERE id =:id")
    suspend fun getLessonById(id: Int): LessonModel?

    @Query("UPDATE lesson_model SET isDone = 1 WHERE id = :id")
    suspend fun markDone(id: Int)

    @Query("SELECT COUNT (*) FROM lesson_model WHERE isDone = 1 AND level = :level")
    fun getDone(level: Int): LiveData<Int>
}