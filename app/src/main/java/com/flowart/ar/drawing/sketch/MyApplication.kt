package com.flowart.ar.drawing.sketch

import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.snake.squad.adslib.AdsApplication
import com.ssquad.ar.drawing.sketch.db.ImageDB
import com.ssquad.ar.drawing.sketch.db.ImageRepositories

class MyApplication : AdsApplication("", isProduction = true, isEnabledAdjust = false) {

    override fun onCreate() {
        super.onCreate()
        SharedPrefManager.init(this)
        ImageRepositories.INSTANCE = ImageRepositories(ImageDB.getDatabase(this).imageDao())
    }
}