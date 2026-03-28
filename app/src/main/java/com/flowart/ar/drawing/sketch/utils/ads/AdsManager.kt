package com.flowart.ar.drawing.sketch.utils.ads

import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.models.AdmobInterModel
import com.snake.squad.adslib.models.AdmobNativeModel

object AdsManager {

    const val AOA_SPLASH = "ca-app-pub-8475252859305547/6948934183"
    val INTER_SPLASH = AdmobInterModel("ca-app-pub-8475252859305547/1147326054")
    val NATIVE_COLLAPSIBLE_SPLASH = AdmobNativeModel("ca-app-pub-8475252859305547/4388567066")
    val NATIVE_LANGUAGE = AdmobNativeModel("ca-app-pub-8475252859305547/3075485396")
    val NATIVE_LANGUAGE_2 = AdmobNativeModel("ca-app-pub-8475252859305547/6208081046")
    val INTER_LANGUAGE = AdmobInterModel("ca-app-pub-8475252859305547/7643795100")
    val NATIVE_INTRO = AdmobNativeModel("ca-app-pub-8475252859305547/9181728747")
    val NATIVE_FULL_SCREEN_INTRO = AdmobNativeModel("ca-app-pub-8475252859305547/6330713430")
    val NATIVE_FULL_SCREEN_INTRO_2 = AdmobNativeModel("ca-app-pub-8475252859305547/4359156211")
    val INTER_INTRO = AdmobInterModel("ca-app-pub-8475252859305547/1177732755")
    val INTER_HOME = AdmobInterModel("ca-app-pub-8475252859305547/7519779825")
    val INTER_BACK = AdmobInterModel("ca-app-pub-8475252859305547/5480666199")
    val NATIVE_HOME = AdmobNativeModel("ca-app-pub-8475252859305547/2718771481")
    val NATIVE_COLLAPSIBLE_HOME = AdmobNativeModel("ca-app-pub-8475252859305547/6757362495")
    val INTER_SKETCH_TRACE_PREVIEW = AdmobInterModel("ca-app-pub-8475252859305547/5444280827")
    val NATIVE_COLLAPSIBLE_DRAWING = AdmobNativeModel("ca-app-pub-8475252859305547/7532114462")
    val INTER_DONE = AdmobInterModel("ca-app-pub-8475252859305547/3580534819")
    val NATIVE_OTHER = AdmobNativeModel("ca-app-pub-8475252859305547/5947570056")
    val NATIVE_FULL_SCREEN_AFTER_INTER = AdmobNativeModel("ca-app-pub-8475252859305547/2512378617")
    val NATIVE_SETTING = AdmobNativeModel("ca-app-pub-8475252859305547/7779526474")
    const val ON_RESUME = "ca-app-pub-8475252859305547/1457660702"


    var isDebug = true
    var isShowAd = true

    private var lastInterShown = 0L

    fun updateTime() {
        lastInterShown = System.currentTimeMillis()
    }

    var countInterHome = 0
    var countInterBackHome = 0
    var countInterDrawSpin = 0
    var countInterDoneReview = 0
    var isShowedRate = false

    fun isShowNativeFullScreen(): Boolean {
        return RemoteConfig.remoteNativeFullScreenAfterInter != 0L && !AdmobLib.getCheckTestDevice()
    }

    private fun isShowInter(): Boolean {
        return (System.currentTimeMillis() - lastInterShown) > 15000L
    }

    fun isShowInterHome(): Boolean {
        if (RemoteConfig.remoteInterHome == 0L) return false
        countInterHome++
        return isShowInter() && (countInterHome % RemoteConfig.remoteInterHome == 0L)
    }

//    fun isShowInterBackHome() : Boolean {
//        if (RemoteConfig.remoteInterBackToHome == 0L ) return false
//        countInterBackHome++
//        return isShowInter() && (countInterBackHome % RemoteConfig.remoteInterBackToHome == 0L)
//    }
//
//    fun isShowInterDrawSpin() : Boolean {
//        if (RemoteConfig.remoteInterDrawSpin == 0L ) return false
//        countInterDrawSpin++
//        return isShowInter() && (countInterDrawSpin % RemoteConfig.remoteInterDrawSpin == 0L)
//    }
//
//    fun isShowInterDonePreview() : Boolean {
//        if (RemoteConfig.remoteInterDonePreview == 0L ) return false
//        countInterDoneReview++
//        return isShowInter() && (countInterDoneReview % RemoteConfig.remoteInterDonePreview == 0L)
//    }


}