package com.ssquad.ar.drawing.sketch.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.baseproject.models.ImageModel
import com.example.baseproject.models.LessonModel
import com.example.baseproject.models.RecentModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageRepositories(val dao: ImageDao) {
    companion object {
        lateinit var INSTANCE: ImageRepositories
    }

    fun getImageByCategory(category: Int): LiveData<List<ImageModel>> {
        return dao.getImageByCategory(category)
    }

    fun getLessonByLevel(level: Int): LiveData<List<LessonModel>> {
        return dao.getLessonByLevel(level)
    }

    fun insertAllImage(image: ImageModel) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insertImage(image)
        }
    }

    fun insertLesson(lessonModel: LessonModel) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insertLesson(lessonModel)
        }
    }

    fun addToRecent(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.addToRecent(RecentModel(id, System.currentTimeMillis()))
        }
    }

    fun updateImageFavorite(isFavorite: Boolean, id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (isFavorite) {
                dao.updateImageFavorite(id, 0)
            } else {
                dao.updateImageFavorite(id, 1)
            }
        }
    }

    fun updateLessonFavorite(isFavorite: Boolean, id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (isFavorite) {
                dao.updateLessonFavorite(0, id)
            } else {
                dao.updateLessonFavorite(1, id)
            }
        }
    }

    fun getRecentImage(): LiveData<List<ImageModel>> {
        return dao.getRecentImage()
    }

    fun getAllFavoriteImage(): LiveData<List<ImageModel>> {
        return dao.getAllFavoriteImage()
    }

    fun getAllFavoriteLesson(): LiveData<List<LessonModel>> {
        return dao.getAllFavoriteLesson()
    }

    suspend fun getLessonById(id: Int): LessonModel? {
        return dao.getLessonById(id)
    }

    suspend fun markDone(id: Int) {
        return dao.markDone(id)
    }

    fun getDone(level: Int): LiveData<Int> {
        return dao.getDone(level)
    }

//    fun getTrendingImages(): LiveData<List<ImageModel>> {
//        return dao.getRandomImages(22)
//    }

    private val _trendingImages = MutableLiveData<List<ImageModel>>()
    val trendingImages: LiveData<List<ImageModel>> = _trendingImages

    fun loadTrendingImagesIfNeed() {
        if (_trendingImages.value.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val list = dao.getRandomImagesList(22)
                _trendingImages.postValue(list)
            }
        }
    }

    fun updateTrendingFavorite(id: Int, isFav: Boolean) {
        val currentList = _trendingImages.value ?: return
        val updatedList = currentList.map {
            if (it.id == id) it.copy(isFavorite = !isFav) else it
        }
        _trendingImages.postValue(updatedList)
    }
}