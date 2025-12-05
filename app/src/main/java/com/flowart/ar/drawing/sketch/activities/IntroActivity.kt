package com.flowart.ar.drawing.sketch.activities

import android.content.Intent
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.adapters.IntroViewPagerAdapter
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityIntroBinding
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.ads.RemoteConfig
import com.flowart.ar.drawing.sketch.utils.gone
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.AdsHelper
import com.snake.squad.adslib.utils.GoogleENative
import com.snake.squad.adslib.utils.SharedPrefManager

class IntroActivity : BaseActivity<ActivityIntroBinding>(ActivityIntroBinding::inflate) {

    private val mAdapter by lazy {
        IntroViewPagerAdapter(
            this,
            isShowNativeFull1,
            isShowNativeFull2
        )
    }

    private val isShowNativeFull1 by lazy {
        RemoteConfig.remoteNativeFullScreenIntro != 0L &&
                AdmobLib.getShowAds() && !AdmobLib.getCheckTestDevice() &&
                AdsHelper.isNetworkConnected(this)
    }

    private val isShowNativeFull2 by lazy {
        RemoteConfig.remoteNativeFullScreenIntro2 != 0L &&
                AdmobLib.getShowAds() && !AdmobLib.getCheckTestDevice() &&
                AdsHelper.isNetworkConnected(this)
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.vpIntro.currentItem in 1..2) {
                binding.vpIntro.currentItem -= 1
            } else {
                finish()
            }
        }
    }

    override fun initData() {
        SharedPrefManager.putBoolean("wantShowRate", false)
    }

    override fun initView() {
        binding.vpIntro.adapter = mAdapter

        val onPageChangeCallback: ViewPager2.OnPageChangeCallback =
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                }
            }
        binding.vpIntro.registerOnPageChangeCallback(onPageChangeCallback)

    }

    override fun initActionView() {
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    fun nextPage() {
        if (binding.vpIntro.currentItem < mAdapter.itemCount - 1) {
            binding.vpIntro.currentItem++
        } else {
            showInterIntro {
                val intent = Intent(this@IntroActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun showInterIntro(navAction: () -> Unit) {
        if (RemoteConfig.remoteInterIntro == 1L) {
            AdmobLib.loadAndShowInterWithNativeAfter(
                this,
                AdsManager.INTER_INTRO,
                AdsManager.NATIVE_FULL_SCREEN_AFTER_INTER,
                binding.vShowInterAds,
                isShowNativeAfter = AdsManager.isShowNativeFullScreen(),
                nativeLayout = R.layout.native_ads_full_screen,
                navAction = { navAction() }
            )
        } else {
            navAction()
        }
    }

    override fun onStop() {
        super.onStop()
        binding.vShowInterAds.gone()
    }

    fun showNativeFullScreen(frNative: ViewGroup) {
        if (!AdsHelper.isNetworkConnected(this) || !AdmobLib.getShowAds() || AdmobLib.getCheckTestDevice()) {
            binding.vpIntro.currentItem++
            return
        }
        if (isShowNativeFull1 && binding.vpIntro.currentItem == 1) {
            AdmobLib.showNative(
                this,
                AdsManager.NATIVE_FULL_SCREEN_INTRO,
                frNative,
                layout = R.layout.native_ads_full_screen,
                size = GoogleENative.UNIFIED_FULL_SCREEN,
                onAdsShowFail = {
                    binding.vpIntro.currentItem++
                    binding.vpIntro.isUserInputEnabled = true
                    Unit
                })
            return
        }
        if (isShowNativeFull2 && (binding.vpIntro.currentItem == 2 || binding.vpIntro.currentItem == 3)) {
            AdmobLib.showNative(
                this,
                AdsManager.NATIVE_FULL_SCREEN_INTRO_2,
                frNative,
                layout = R.layout.native_ads_full_screen,
                size = GoogleENative.UNIFIED_FULL_SCREEN,
                onAdsShowFail = {
                    binding.vpIntro.currentItem++
                    binding.vpIntro.isUserInputEnabled = true
                    Unit
                })
        }
    }

}