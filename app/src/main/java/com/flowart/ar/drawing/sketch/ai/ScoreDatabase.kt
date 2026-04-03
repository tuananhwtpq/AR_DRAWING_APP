package com.flowart.ar.drawing.sketch.ai

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * ScoreDatabase — Room Database riêng cho Score History.
 *
 * Tách riêng khỏi ImageDB để:
 * - Không ảnh hưởng DB cũ (không cần migration)
 * - Dễ quản lý, dễ hiểu
 */
@Database(entities = [ScoreRecord::class], version = 1, exportSchema = false)
abstract class ScoreDatabase : RoomDatabase() {

    abstract fun scoreDao(): ScoreDao

    companion object {
        @Volatile
        private var INSTANCE: ScoreDatabase? = null

        fun getInstance(context: Context): ScoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScoreDatabase::class.java,
                    "score_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
