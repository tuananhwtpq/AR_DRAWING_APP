package com.ssquad.ar.drawing.sketch.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.baseproject.models.ImageModel
import com.example.baseproject.models.LessonModel
import com.example.baseproject.models.RecentModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ImageModel::class, LessonModel::class, RecentModel::class], version = 1)
@TypeConverters(MyConverter::class)
abstract class ImageDB : RoomDatabase() {
    abstract fun imageDao(): ImageDao

    companion object {
        @Volatile
        private var INSTANCE: ImageDB? = null
        fun getDatabase(context: Context): ImageDB {
            if (INSTANCE != null) {
                return INSTANCE!!
            } else {
                val callback = object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        val dao = INSTANCE?.imageDao()
                        CoroutineScope(Dispatchers.IO).launch {
                            context.assets.list("beginner")?.forEach {
                                val difficulty = when (it) {
                                    "1", "2", "3" -> 3
                                    "4", "6", "8" -> 2
                                    else -> 1
                                }
                                dao?.insertLesson(
                                    LessonModel(
                                        name = "beginner_$it",
                                        listStep = context.assets.list("beginner/$it")
                                            ?.sortedBy { it }
                                            ?.map { fileName -> "beginner/$it/$fileName" }
                                            ?: listOf(),
                                        isFavorite = false,
                                        difficulty = difficulty,
                                        level = 0))
                            }

                            context.assets.list("intermediate")?.forEach {
                                val difficulty = when (it) {
                                    "5", "6", "10" -> 6
                                    "7", "8", "9" -> 4
                                    else -> 5
                                }
                                dao?.insertLesson(
                                    LessonModel(
                                        name = "intermediate_$it",
                                        listStep = context.assets.list("intermediate/$it")
                                            ?.sortedBy { it }
                                            ?.map { fileName -> "intermediate/$it/$fileName" }
                                            ?: listOf(),
                                        isFavorite = false,
                                        difficulty = difficulty,
                                        level = 1))
                            }

                            context.assets.list("professional")?.forEach {
                                val difficulty = when (it) {
                                    "1" -> 10
                                    "2" -> 9
                                    "7", "9" -> 7
                                    else -> 8
                                }
                                dao?.insertLesson(
                                    LessonModel(
                                        name = "professional_$it",
                                        listStep = context.assets.list("professional/$it")
                                            ?.sortedBy { it }
                                            ?.map { fileName -> "professional/$it/$fileName" }
                                            ?: listOf(),
                                        isFavorite = false,
                                        difficulty = difficulty,
                                        level = 2))
                            }


                            context.assets.list("trending")?.forEach {
                                dao?.insertImage(
                                    ImageModel(
                                        category = 0,
                                        categoryName = "trending",
                                        img = "trending/$it",
                                        isFavorite = false,
                                    )
                                )
                            }

                            context.assets.list("anime")?.forEach {
                                dao?.insertImage(
                                    ImageModel(
                                        category = 1,
                                        categoryName = "anime",
                                        img = "anime/$it",
                                        isFavorite = false,
                                    )
                                )
                            }

                            context.assets.list("cartoon")?.forEach {
                                dao?.insertImage(
                                    ImageModel(
                                        category = 2,
                                        categoryName = "cartoon",
                                        img = "cartoon/$it",
                                        isFavorite = false,
                                    )
                                )
                            }

                            context.assets.list("animal")?.forEach {
                                dao?.insertImage(
                                    ImageModel(
                                        category = 3,
                                        categoryName = "animal",
                                        img = "animal/$it",
                                        isFavorite = false,
                                    )
                                )
                            }

                            context.assets.list("chibi")?.forEach {
                                dao?.insertImage(
                                    ImageModel(
                                        category = 4,
                                        categoryName = "chibi",
                                        img = "chibi/$it",
                                        isFavorite = false,
                                    )
                                )
                            }

                            context.assets.list("flower")?.forEach {
                                dao?.insertImage(
                                    ImageModel(
                                        category = 5,
                                        categoryName = "flower",
                                        img = "flower/$it",
                                        isFavorite = false,
                                    )
                                )
                            }
                        }

                    }
                }
                synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ImageDB::class.java,
                        "image_db"
                    ).fallbackToDestructiveMigration()
                        .addCallback(callback)
                        .build()
                    INSTANCE = instance
                    return instance
                }
            }
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    ImageDB::class.java,
//                    "image_db"
//                ).fallbackToDestructiveMigration()
//                    .addCallback(callback)
//                    .build()
//                INSTANCE = instance
//                instance
//            }
        }
    }
}