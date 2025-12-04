package com.flowart.ar.drawing.sketch.utils.ads


import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object RemoteConfig {

    private var isInit = false
    private var isTimedOut = false

    private const val REMOTE_SPLASH_ADS = "remote_splash_ads"
    private const val REMOTE_NATIVE_COLLAPSIBLE_SPLASH = "remote_native_collapsible_splash"
    private const val REMOTE_NATIVE_LANGUAGE = "remote_native_language"
    private const val REMOTE_INTER_LANGUAGE = "remote_inter_language"
    private const val REMOTE_NATIVE_INTRO = "remote_native_intro"
    private const val REMOTE_NATIVE_FULL_SCREEN_INTRO = "remote_native_full_screen_intro"
    private const val REMOTE_NATIVE_FULL_SCREEN_INTRO_2 = "remote_native_full_screen_intro_2"
    private const val REMOTE_INTER_INTRO = "remote_inter_intro"
    private const val REMOTE_INTER_HOME = "remote_inter_home"
    private const val REMOTE_INTER_BACK = "remote_inter_back"
    private const val REMOTE_NATIVE_HOME = "remote_native_home"
    private const val REMOTE_NATIVE_COLLAPSIBLE_HOME = "remote_native_collapsible_home"
    private const val REMOTE_INTER_SKETCH_TRACE_PREVIEW = "remote_inter_sketch_trace_preview"
    private const val REMOTE_NATIVE_COLLAPSIBLE_DRAWING = "remote_native_collapsible_drawing"
    private const val REMOTE_INTER_DONE = "remote_inter_done"
    private const val REMOTE_NATIVE_OTHER = "remote_native_other"
    private const val REMOTE_NATIVE_FULL_SCREEN_AFTER_INTER =
        "remote_native_full_screen_after_inter"
    private const val REMOTE_NATIVE_SETTING = "remote_native_setting"
    private const val REMOTE_ON_RESUME = "remote_on_resume"
    private const val REMOTE_TIME_SHOW_INTER = "remote_time_show_inter"
    private const val REMOTE_TIME_LOAD_NATIVE = "remote_time_load_native"
    private const val REMOTE_LANGUAGE_INTRO_FIRST_OPEN = "remote_language_intro_first_open"


    var remoteSplashAds: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_SPLASH_ADS, 2L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_SPLASH_ADS, value)
        }
    var remoteNativeCollapsibleSplash: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_NATIVE_COLLAPSIBLE_SPLASH, 2L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_NATIVE_COLLAPSIBLE_SPLASH, value)
        }
    var remoteNativeLanguage: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_NATIVE_LANGUAGE, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_NATIVE_LANGUAGE, value)
        }
    var remoteInterLanguage: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_INTER_LANGUAGE, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_INTER_LANGUAGE, value)
        }
    var remoteNativeIntro: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_NATIVE_INTRO, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_NATIVE_INTRO, value)
        }
    var remoteNativeFullScreenIntro: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_NATIVE_FULL_SCREEN_INTRO, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_NATIVE_FULL_SCREEN_INTRO, value)
        }
    var remoteNativeFullScreenIntro2: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_NATIVE_FULL_SCREEN_INTRO_2, 2L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_NATIVE_FULL_SCREEN_INTRO_2, value)
        }
    var remoteInterIntro: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_INTER_INTRO, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_INTER_INTRO, value)
        }
    var remoteInterHome: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_INTER_HOME, 2L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_INTER_HOME, value)
        }
    var remoteInterBack: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_INTER_BACK, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_INTER_BACK, value)
        }
    var remoteNativeHome: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_NATIVE_HOME, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_NATIVE_HOME, value)
        }
    var remoteNativeCollapsibleHome: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_NATIVE_COLLAPSIBLE_HOME, 2L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_NATIVE_COLLAPSIBLE_HOME, value)
        }
    var remoteInterSketchTracePreview: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_INTER_SKETCH_TRACE_PREVIEW, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_INTER_SKETCH_TRACE_PREVIEW, value)
        }
    var remoteNativeCollapsibleDrawing: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_NATIVE_COLLAPSIBLE_DRAWING, 2L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_NATIVE_COLLAPSIBLE_DRAWING, value)
        }
    var remoteInterDone: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_INTER_DONE, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_INTER_DONE, value)
        }
    var remoteNativeOther: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_NATIVE_OTHER, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_NATIVE_OTHER, value)
        }
    var remoteNativeFullScreenAfterInter: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_NATIVE_FULL_SCREEN_AFTER_INTER, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_NATIVE_FULL_SCREEN_AFTER_INTER, value)
        }
    var remoteNativeSetting: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_NATIVE_SETTING, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_NATIVE_SETTING, value)
        }
    var remoteOnResume: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_ON_RESUME, 1L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_ON_RESUME, value)
        }
    var remoteTimeShowInter: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_TIME_SHOW_INTER, 15000L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_TIME_SHOW_INTER, value)
        }
    var remoteTimeLoadNative: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_TIME_LOAD_NATIVE, 15000L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_TIME_LOAD_NATIVE, value)
        }
    var remoteLanguageIntroFirstOpen: Long
        get() {
            return SharedPrefManager.getLong(REMOTE_LANGUAGE_INTRO_FIRST_OPEN, 0L)
        }
        set(value) {
            SharedPrefManager.putLong(REMOTE_LANGUAGE_INTRO_FIRST_OPEN, value)
        }


    fun initRemoteConfig(
        activity: AppCompatActivity,
        timeOut: Long = 8000,
        initListener: InitListener
    ) {
        val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_default)
        mFirebaseRemoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                mFirebaseRemoteConfig.activate().addOnCompleteListener {
                    isInit = true
                    if (!isTimedOut) initListener.onComplete()
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                isInit = false
                if (!isTimedOut) initListener.onFailure()
            }
        })
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                isInit = true
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!isTimedOut) initListener.onComplete()
                }, 2000)
            }
        }
        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeOut)
            if (!isInit) {
                isTimedOut = true
                initListener.onFailure()
            }
        }
    }

    fun getRemoteLongValue(key: String): Long {
        val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        return mFirebaseRemoteConfig.getLong(key)
    }

    fun getAllRemoteValueToLocal() {
        remoteSplashAds = getRemoteLongValue(REMOTE_SPLASH_ADS)
        remoteNativeCollapsibleSplash = getRemoteLongValue(REMOTE_NATIVE_COLLAPSIBLE_SPLASH)
        remoteNativeLanguage = getRemoteLongValue(REMOTE_NATIVE_LANGUAGE)
        remoteInterLanguage = getRemoteLongValue(REMOTE_INTER_LANGUAGE)
        remoteNativeIntro = getRemoteLongValue(REMOTE_NATIVE_INTRO)
        remoteNativeFullScreenIntro = getRemoteLongValue(REMOTE_NATIVE_FULL_SCREEN_INTRO)
        remoteNativeFullScreenIntro2 = getRemoteLongValue(REMOTE_NATIVE_FULL_SCREEN_INTRO_2)
        remoteInterIntro = getRemoteLongValue(REMOTE_INTER_INTRO)
        remoteInterHome = getRemoteLongValue(REMOTE_INTER_HOME)
        remoteInterBack = getRemoteLongValue(REMOTE_INTER_BACK)
        remoteNativeHome = getRemoteLongValue(REMOTE_NATIVE_HOME)
        remoteNativeCollapsibleHome = getRemoteLongValue(REMOTE_NATIVE_COLLAPSIBLE_HOME)
        remoteInterSketchTracePreview = getRemoteLongValue(REMOTE_INTER_SKETCH_TRACE_PREVIEW)
        remoteNativeCollapsibleDrawing = getRemoteLongValue(REMOTE_NATIVE_COLLAPSIBLE_DRAWING)
        remoteInterDone = getRemoteLongValue(REMOTE_INTER_DONE)
        remoteNativeOther = getRemoteLongValue(REMOTE_NATIVE_OTHER)
        remoteNativeFullScreenAfterInter = getRemoteLongValue(REMOTE_NATIVE_FULL_SCREEN_AFTER_INTER)
        remoteNativeSetting = getRemoteLongValue(REMOTE_NATIVE_SETTING)
        remoteOnResume = getRemoteLongValue(REMOTE_ON_RESUME)
        remoteTimeShowInter = getRemoteLongValue(REMOTE_TIME_SHOW_INTER)
        remoteTimeLoadNative = getRemoteLongValue(REMOTE_TIME_LOAD_NATIVE)
        remoteLanguageIntroFirstOpen = getRemoteLongValue(REMOTE_LANGUAGE_INTRO_FIRST_OPEN)
    }

    fun getDefaultRemoteValue() {
        remoteSplashAds = 2L
        remoteNativeCollapsibleSplash = 2L
        remoteNativeLanguage = 1L
        remoteInterLanguage = 1L
        remoteNativeIntro = 1L
        remoteNativeFullScreenIntro = 1L
        remoteNativeFullScreenIntro2 = 2L
        remoteInterIntro = 1L
        remoteInterHome = 2L
        remoteInterBack = 1L
        remoteNativeHome = 1L
        remoteNativeCollapsibleHome = 2L
        remoteInterSketchTracePreview = 1L
        remoteNativeCollapsibleDrawing = 2L
        remoteInterDone = 1L
        remoteNativeOther = 1L
        remoteNativeFullScreenAfterInter = 1L
        remoteNativeSetting = 1L
        remoteOnResume = 1L
        remoteTimeShowInter = 15000L
        remoteTimeLoadNative = 15000L
        remoteLanguageIntroFirstOpen = 0L
    }

    interface InitListener {
        fun onComplete()
        fun onFailure()
    }
}