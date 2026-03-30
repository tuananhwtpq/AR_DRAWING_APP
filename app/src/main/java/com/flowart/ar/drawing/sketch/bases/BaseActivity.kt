package com.flowart.ar.drawing.sketch.bases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.utils.Common
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.ads.RemoteConfig
import com.flowart.ar.drawing.sketch.utils.visible
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.GoogleENative
import java.util.Locale

abstract class BaseActivity<viewBinding : ViewBinding>(val inflater: (LayoutInflater) -> viewBinding) :
    AppCompatActivity() {

    val binding: viewBinding by lazy { inflater(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            AdmobLib.onRestoreInstanceState(it)
        }
        localeConfiguration()
        setContentView(binding.root)
        initData()
        initView()
        initActionView()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        AdmobLib.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        AdmobLib.onRestoreInstanceState(savedInstanceState)
        super.onRestoreInstanceState(savedInstanceState)
    }

    abstract fun initData()

    abstract fun initView()

    abstract fun initActionView()

    private fun localeConfiguration() {
        val language = Common.getSelectedLanguage()
        val locale = Locale(language.key)
        Locale.setDefault(locale)
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun loadAndShowNativeHome(frNative: ViewGroup, onShowOrFailed: () -> Unit) {
        if (RemoteConfig.remoteNativeHome != 0L) {
            frNative.visible()
            AdmobLib.loadAndShowNative(
                activity = this,
                admobNativeModel = AdsManager.NATIVE_HOME,
                viewGroup = frNative,
                size = GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON,
                layout = R.layout.native_ads_custom_medium_like_button,
                onAdsLoaded = {
                    onShowOrFailed()
                },
                onAdsLoadFail = {
                    onShowOrFailed()
                }
            )
        }
    }

    fun loadAndShowNativeSetting(frNative: ViewGroup) {
        if (RemoteConfig.remoteNativeSetting != 0L) {
            frNative.visible()
            AdmobLib.loadAndShowNative(
                activity = this,
                admobNativeModel = AdsManager.NATIVE_SETTING,
                viewGroup = frNative,
                size = GoogleENative.UNIFIED_SMALL_LIKE_BANNER,
                layout = R.layout.native_ads_custom_small_like_banner
            )
        }
    }

    fun loadAndShowNativeOther(frNative: ViewGroup) {
        if (RemoteConfig.remoteNativeOther != 0L) {
            frNative.visible()
            AdmobLib.loadAndShowNative(
                activity = this,
                admobNativeModel = AdsManager.NATIVE_OTHER,
                viewGroup = frNative,
                size = GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON,
                layout = R.layout.native_ads_custom_medium_like_button
            )
        }
    }

    fun loadAndShowNativeOther_2(frNative: ViewGroup) {
        if (RemoteConfig.remoteNativeOther != 0L) {
            frNative.visible()
            AdmobLib.loadAndShowNative(
                activity = this,
                admobNativeModel = AdsManager.NATIVE_OTHER,
                viewGroup = frNative,
                size = GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON,
                layout = R.layout.native_ads_custom_medium_like_button
            )
        }
    }

    fun loadAndShowInterBackHome(viewBlock: View? = null, navAction: () -> Unit) {
        if (AdsManager.isShowInterBackHome()) {
            AdmobLib.loadAndShowInterWithNativeAfter(
                mActivity = this,
                interModel = AdsManager.INTER_BACK,
                nativeModel = AdsManager.NATIVE_FULL_SCREEN_AFTER_INTER,
                vShowInterAds = viewBlock,
                isShowNativeAfter = AdsManager.isShowNativeFullScreen(),
                nativeLayout = R.layout.native_ads_full_screen,
                isShowOnTestDevice = true,
                navAction = { navAction() },
                onInterCloseOrFailed = { isDone ->
                    if (isDone) {
                        AdsManager.updateTime()
                    }
                }
            )
        } else {
            navAction()
        }
    }

    fun loadAndShowInterDone(viewBlock: View? = null, navAction: () -> Unit) {

        if (AdsManager.isShowInterDone()) {
            AdmobLib.loadAndShowInterWithNativeAfter(
                mActivity = this,
                interModel = AdsManager.INTER_DONE,
                nativeModel = AdsManager.NATIVE_FULL_SCREEN_AFTER_INTER,
                vShowInterAds = viewBlock,
                isShowNativeAfter = AdsManager.isShowNativeFullScreen(),
                nativeLayout = R.layout.native_ads_full_screen,
                isShowOnTestDevice = true,
                navAction = { navAction() },
                onInterCloseOrFailed = { isDone ->
                    if (isDone) {
                        AdsManager.updateTime()
                    }
                }
            )
        } else {
            navAction()
        }
    }

}