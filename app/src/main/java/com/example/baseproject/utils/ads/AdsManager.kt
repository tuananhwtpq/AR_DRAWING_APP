package com.example.baseproject.utils.ads

import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.models.AdmobBannerCollapsibleModel
import com.snake.squad.adslib.models.AdmobInterModel
import com.snake.squad.adslib.models.AdmobNativeModel

object AdsManager {

//    const val AOA_SPLASH = "ca-app-pub-2845820995656145/8148172426"
//    val interSplashModel = AdmobInterModel("ca-app-pub-2845820995656145/3426661926")
//    val nativeCollapsibleSplashModel = AdmobNativeModel("ca-app-pub-2845820995656145/5466168763")
//    val nativeLanguageModel = AdmobNativeModel("ca-app-pub-2845820995656145/9679696195")
//    val nativeLanguage2Model = AdmobNativeModel("ca-app-pub-2845820995656145/3813674739")
//    val interLanguageModel = AdmobInterModel("ca-app-pub-2845820995656145/5117634656")
//    val nativeIntroModel = AdmobNativeModel("ca-app-pub-2845820995656145/9257573104")
//    val nativeFullScreenIntroModel = AdmobNativeModel("ca-app-pub-2845820995656145/6239144633")
//    val nativeFullScreenIntro2Model = AdmobNativeModel("ca-app-pub-2845820995656145/2975736797")
//    val interIntroModel = AdmobInterModel("ca-app-pub-2845820995656145/1239482287")
//    val interHomeModel = AdmobInterModel("ca-app-pub-2845820995656145/4017355729")
//    val interTabHomeModel = AdmobInterModel("ca-app-pub-2845820995656145/9800498584")
//    val interBackToHomeModel = AdmobInterModel("ca-app-pub-2845820995656145/5861253577")
//    val nativeHomeModel = AdmobNativeModel("ca-app-pub-2845820995656145/1762226462")
//    val nativeCollapsibleHomeModel = AdmobNativeModel("ca-app-pub-2845820995656145/5657740454")
//    val interOtherModel = AdmobInterModel("ca-app-pub-2845820995656145/4484259587")
//    val nativeOtherModel = AdmobNativeModel("ca-app-pub-2845820995656145/5138865709")
//    const val BANNER_OTHER = "ca-app-pub-2845820995656145/4947294019"
//    val nativeFullScreenAfterInterModel = AdmobNativeModel("ca-app-pub-2845820995656145/5740451186")
//    val nativeSettingModel = AdmobNativeModel("ca-app-pub-2845820995656145/3622103041")
//    const val ON_RESUME = "ca-app-pub-2845820995656145/7743797999"
//
//    val bannerCollapseListItemModel = AdmobBannerCollapsibleModel(BANNER_OTHER)
//    val bannerCollapsePreviewVideoModel = AdmobBannerCollapsibleModel(BANNER_OTHER)
//
//    var lastInterShown = 0L
//    val isShowInter: Boolean
//        get() = System.currentTimeMillis() - lastInterShown >= RemoteConfig.remoteTimeShowInter
//
//    private var isRatingShown = false
//    fun isShowRating(): Boolean {
//        if (!isRatingShown) {
//            isRatingShown = true
//            return true
//        }
//
//        return false
//    }
//
//    private var countInterHome = 0
//    fun isShowInterHome(): Boolean {
//        if (RemoteConfig.remoteInterHome < 1L) return false
//        countInterHome++
//        return isShowInter && countInterHome % RemoteConfig.remoteInterHome == 0L
//    }
//
//    private var countInterBack = 0
//    fun isShowInterBack(): Boolean {
//        if (RemoteConfig.remoteInterBack < 1L) return false
//        countInterBack++
//        return isShowInter && countInterBack % RemoteConfig.remoteInterBack == 0L
//    }
//
//    private var countInterTabHome = 0
//    fun isShowInterTabHome(): Boolean {
//        if (RemoteConfig.remoteInterTabHome < 1L) return false
//        countInterTabHome++
//        return isShowInter && countInterTabHome % RemoteConfig.remoteInterTabHome == 0L
//    }
//
//    private var countInterStart = 0
//    fun isShowInterStart(): Boolean {
//        if (RemoteConfig.remoteInterStart < 1L) return false
//        countInterStart++
//        return isShowInter && countInterStart % RemoteConfig.remoteInterStart == 0L
//    }
//
//    private var countInterDone = 0
//    fun isShowInterDone(): Boolean {
//        if (RemoteConfig.remoteInterDone < 1L) return false
//        countInterDone++
//        return isShowInter && countInterDone % RemoteConfig.remoteInterDone == 0L
//    }
//
//    fun isShowNativeFullScreen(): Boolean {
//        return RemoteConfig.remoteNativeFullScreenAfterInter == 1L && !AdmobLib.getCheckTestDevice()
//    }
//
//
//    fun reset() {
//        isRatingShown = false
//        lastInterShown = 0L
//        countInterHome = 0
//        countInterBack = 0
//        countInterStart = 0
//        countInterDone = 0
//    }
}