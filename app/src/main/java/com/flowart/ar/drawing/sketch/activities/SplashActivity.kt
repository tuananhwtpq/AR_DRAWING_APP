package com.flowart.ar.drawing.sketch.activities

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.OnBackPressedCallback
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivitySplashBinding
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.ads.RemoteConfig
import com.flowart.ar.drawing.sketch.utils.gone
import com.flowart.ar.drawing.sketch.utils.invisible
import com.flowart.ar.drawing.sketch.utils.visible
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.aoa.AppOnResumeAdsManager
import com.snake.squad.adslib.aoa.AppOpenAdsManager
import com.snake.squad.adslib.cmp.GoogleMobileAdsConsentManager
import com.snake.squad.adslib.utils.AdsHelper
import com.snake.squad.adslib.utils.GoogleENative
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    companion object {
        const val TAG = "SplashActivity"
    }
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var isInitAds = AtomicBoolean(false)

    override fun initData() {
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action == Intent.ACTION_MAIN
        ) {
            finish()
            return
        }

        resetCountInter()
    }

    override fun initView() {
        if (AdsHelper.isNetworkConnected(this)) {
            binding.tvLoadingAds.visible()
            initRemoteConfig()
        } else {
            binding.tvLoadingAds.invisible()
            Handler(Looper.getMainLooper()).postDelayed({
                replaceActivity()
            }, 3000)
        }
    }

    override fun initActionView() {
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    private fun setupCMP() {
        val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager(this)
        googleMobileAdsConsentManager.gatherConsent { error ->
            error?.let {
                initializeMobileAdsSdk()
            }

            if (googleMobileAdsConsentManager.canRequestAds) {
                initializeMobileAdsSdk()
            }
        }
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.get()) {
            //start action
            return
        }
        isMobileAdsInitializeCalled.set(true)
        initAds()
    }

    private fun initRemoteConfig() {
        RemoteConfig.initRemoteConfig(this, initListener = object : RemoteConfig.InitListener {
            override fun onComplete() {
                RemoteConfig.getAllRemoteValueToLocal()
                if (isInitAds.get()) {
                    return
                }
                isInitAds.set(true)
                setupCMP()
            }

            override fun onFailure() {
                RemoteConfig.getDefaultRemoteValue()
                setupCMP()
            }
        })
    }

    private fun initAds() {
        AdmobLib.setEnabledCheckTestDevice(false)
        AdmobLib.initialize(
            this,
            isDebug = AdsManager.isDebug,
            isShowAds = AdsManager.isShowAd,
            onInitializedAds = {
                if (it) {
                    Log.d(TAG, "Init ads")
                    checkTestDevice {
                        if (RemoteConfig.remoteNativeLanguage != 0L) {
                            Log.d(TAG, "Init remote native language")
                            AdmobLib.loadNative(
                                activity = this@SplashActivity,
                                admobNativeModel = AdsManager.NATIVE_LANGUAGE
                            )
                            AdmobLib.loadNative(
                                activity = this@SplashActivity,
                                admobNativeModel = AdsManager.NATIVE_LANGUAGE_2
                            )
                        }
                        loadAndShowOnResum()
                        loadAndShowNativeCollapsibleSplash {
                            Log.d(TAG, "Load and show native collapsible ")
                            loadAnsShowSplashAds()
                        }

                    }
                } else {
                    binding.tvLoadingAds.invisible()
                    replaceActivity()
                }
            })
    }

    private fun loadAndShowNativeCollapsibleSplash(navAction: () -> Unit) {
        when (RemoteConfig.remoteNativeCollapsibleSplash) {
            1L -> {
                //native small like banner
                binding.frNative.visible()
                AdmobLib.loadAndShowNative(
                    activity = this@SplashActivity,
                    viewGroup = binding.frNative,
                    admobNativeModel = AdsManager.NATIVE_COLLAPSIBLE_SPLASH,
                    size = GoogleENative.UNIFIED_SMALL_LIKE_BANNER,
                    layout = R.layout.native_ads_custom_small_like_banner,
                    onAdsLoaded = {
                        binding.whiteLine.visible()
                        navAction()
                    },
                    onAdsLoadFail = {
                        binding.whiteLine.gone()
                        navAction()
                    }
                )
            }

            2L -> {
                //native collapse
                binding.frNative.visible()
                binding.frNativeExpand.visible()
                AdmobLib.loadAndShowNativeCollapsibleSingle(
                    activity = this@SplashActivity,
                    admobNativeModel = AdsManager.NATIVE_COLLAPSIBLE_SPLASH,
                    viewGroupCollapsed = binding.frNative,
                    viewGroupExpanded = binding.frNativeExpand,
                    layoutCollapsed = R.layout.native_ads_custom_small_like_banner,
                    layoutExpanded = R.layout.native_ads_custom_medium_bottom,
                    onAdsLoaded = {
                        Log.d("TAGloadAndShowNativeCollapsibleSplash", "loaded: ")
                        binding.whiteLine.visible()
                        navAction()
                    },
                    onAdsLoadFail = {
                        Log.d("TAGloadAndShowNativeCollapsibleSplash", "failed: ")
                        binding.whiteLine.gone()
                        navAction()
                    }
                )
            }

            else -> {
                navAction()
            }
        }

    }

    private fun loadAnsShowSplashAds() {
        when (RemoteConfig.remoteSplashAds) {
            1L -> {
                //AOA splash
                AppOpenAdsManager(
                    activity = this@SplashActivity,
                    adsID = AdsManager.AOA_SPLASH,
                    onAdsCloseOrFailed = {
                        replaceActivity()
                    },
                    timeOut = 15000
                ).loadAndShowAoA()
                Log.d(TAG, "Load AOA ads")
            }

            2L -> {
                // inter splash
                AdmobLib.loadAndShowInterSplashWithNativeAfter(
                    mActivity = this@SplashActivity,
                    interModel = AdsManager.INTER_SPLASH,
                    nativeModel = AdsManager.NATIVE_FULL_SCREEN_AFTER_INTER,
                    isShowNativeAfter = AdsManager.isShowNativeFullScreen(),
                    nativeLayout = R.layout.native_ads_full_screen,
                    navAction = {
                        replaceActivity()
                    }
                )
                Log.d(TAG, " Load inter splash")
            }

            else -> {
                replaceActivity()
            }
        }
    }

    private fun loadAndShowOnResum() {
        if (RemoteConfig.remoteOnResume == 1L) {
            Log.d(TAG, "Load ad onResume")
            AppOnResumeAdsManager.initialize(application, AdsManager.ON_RESUME)
            AppOnResumeAdsManager.getInstance().disableForActivity(SplashActivity::class.java)
        }
    }

    private fun checkTestDevice(navAction: () -> Unit) {
        if (RemoteConfig.remoteNativeSetting != 0L) {
            Log.d(TAG, "Init check test device")
            AdmobLib.loadNative(
                activity = this,
                admobNativeModel = AdsManager.NATIVE_SETTING,
                size = GoogleENative.UNIFIED_SMALL_LIKE_BANNER,
                isCheckTestAds = true,
                onAdsLoaded = {
                    navAction()
                },
                onAdsLoadFail = {
                    navAction()
                }
            )
        } else {
            navAction()
        }
    }

    private fun replaceActivity() {
        SharedPrefManager.putBoolean("wantShowRate", false)
        val isShowedLanguage = SharedPrefManager.getBoolean("isShowedLanguage")
        val intent = when (RemoteConfig.remoteLanguageIntroFirstOpen) {
            1L -> {
                if (!isShowedLanguage) {
                    Intent(this@SplashActivity, LanguageActivity::class.java)
                } else {
                    Intent(this@SplashActivity, MainActivity::class.java)
                }
            }

            else -> {
                Intent(this@SplashActivity, LanguageActivity::class.java)
            }
        }

        intent.putExtra(Constants.LANGUAGE_EXTRA, false)
        startActivity(intent)
        finish()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            exitProcess(0)
        }
    }

    private fun resetCountInter() {
        AdsManager.countInterHome = 0
        AdsManager.countInterBackHome = 0
        AdsManager.countInterDone = 0
        AdsManager.countInterPreview = 0
    }

}