package com.flowart.ar.drawing.sketch

import android.app.Application
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.ssquad.ar.drawing.sketch.db.ImageDB
import com.ssquad.ar.drawing.sketch.db.ImageRepositories

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SharedPrefManager.init(this)
        ImageRepositories.INSTANCE = ImageRepositories(ImageDB.getDatabase(this).imageDao())
    }
}