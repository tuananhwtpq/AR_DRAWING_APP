package com.flowart.ar.drawing.sketch.activities

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.adapters.MainViewPagerAdapter
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityMainBinding
import com.flowart.ar.drawing.sketch.fragments.DrawGuideDialog
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.ads.RemoteConfig
import com.flowart.ar.drawing.sketch.utils.gone
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick
import com.flowart.ar.drawing.sketch.utils.visible
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.rates.RatingDialog
import com.snake.squad.adslib.utils.GoogleENative
import kotlinx.coroutines.Runnable

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private lateinit var mAdapter: MainViewPagerAdapter

    override fun initData() {
//        AdmobLib.setDebugAds(true)
    }

    private var isLoading = false
    private val handler = Handler(Looper.getMainLooper())


    private var isCountingCollapsibleHome = false

    private val runnableCollapsibleHome: Runnable = object : Runnable {
        override fun run() {
            if (AdsManager.isReloadingCollapsibleHome() && !isLoading) {
                loadAndShowNativeCollapsibleHome { AdsManager.updateCollapsibleHome() }
            }
            handler.postDelayed(this, 1000L)

        }
    }

    override fun initView() {
        initViewPager()
        setTabPosition(0)
        //change cmt
    }

    override fun initActionView() {
        binding.navHome.setOnUnDoubleClick {
            loadAndShowNativeCollapsibleHome { AdsManager.updateCollapsibleHome() }
            setTabPosition(0)
        }
        binding.navLesson.setOnUnDoubleClick {
            loadAndShowNativeCollapsibleHome { AdsManager.updateCollapsibleHome() }

            setTabPosition(1)
        }
        binding.navGallery.setOnUnDoubleClick {
            loadAndShowNativeCollapsibleHome { AdsManager.updateCollapsibleHome() }

            setTabPosition(2)
        }
        binding.navSetting.setOnUnDoubleClick {
            loadAndShowNativeCollapsibleHome { AdsManager.updateCollapsibleHome() }
            setTabPosition(3)
        }

        binding.ivInfo.setOnClickListener {
            DrawGuideDialog().init(true).show(supportFragmentManager, "DrawGuideDialog")
        }
    }

    private fun initViewPager() {
        mAdapter = MainViewPagerAdapter(this)
        val onPageChangeCallBack = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                binding.ivInfo.isVisible = position == 0 || position == 1 || position == 2
                setTabPosition(position)
                when (position) {
                    0 -> binding.tvTitle.text = getString(R.string.flowart_ar_drawing_sketch_art)
                    1 -> binding.tvTitle.text = getString(R.string.what_will_you_explore_today)
                    2 -> binding.tvTitle.text = getString(R.string.your_drawing_journey)
                    3 -> binding.tvTitle.text = getString(R.string.let_s_tune_tour_app)
                }
            }
        }

        binding.vpMain.apply {
            adapter = mAdapter
            registerOnPageChangeCallback(onPageChangeCallBack)
            isUserInputEnabled = false
            offscreenPageLimit = 1
        }
    }

    private fun setTabPosition(position: Int) {
        binding.navHome.isSelected = position == 0
        binding.navLesson.isSelected = position == 1
        binding.navGallery.isSelected = position == 2
        binding.navSetting.isSelected = position == 3

        binding.vpMain.currentItem = position

        setSelectedText(binding.navHome)
        setSelectedText(binding.navLesson)
        setSelectedText(binding.navGallery)
        setSelectedText(binding.navSetting)

        binding.bottomNavHome.visibility = if (position == 0) View.VISIBLE else View.INVISIBLE
        binding.bottomNavLesson.visibility = if (position == 1) View.VISIBLE else View.INVISIBLE
        binding.bottomNavGallery.visibility = if (position == 2) View.VISIBLE else View.INVISIBLE
        binding.bottomNavSetting.visibility = if (position == 3) View.VISIBLE else View.INVISIBLE

        if (binding.navHome.isSelected) {
            binding.main.setBackgroundResource(R.drawable.main_bg_home)
        } else {
            binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.mainBackground))
        }

    }

    private fun setSelectedText(textView: TextView) {
        if (textView.isSelected) {
            textView.setTextColor(resources.getColor(R.color.mainTextColor))
        } else {
            textView.setTextColor(resources.getColor(R.color.unSelectedText))
        }
    }

    fun loadAndShowInterHome(blockView: Boolean = false, navAction: () -> Unit) {
        if (AdsManager.isShowInterHome()) {
            AdmobLib.loadAndShowInterWithNativeAfter(
                mActivity = this@MainActivity,
                interModel = AdsManager.INTER_HOME,
                nativeModel = AdsManager.NATIVE_FULL_SCREEN_AFTER_INTER,
                vShowInterAds = if (blockView) binding.vShowInterAds else null,
                isShowNativeAfter = AdsManager.isShowNativeFullScreen(),
                nativeLayout = R.layout.native_ads_full_screen,
                navAction = { navAction() },
                onInterCloseOrFailed = { if (it) AdsManager.updateTime() }
            )
        } else {
            navAction()
        }
    }

    override fun onResume() {
        super.onResume()
        val wantShowRate = SharedPrefManager.getBoolean("wantShowRate", true)
        if (!AdsManager.isShowedRate && wantShowRate) {
            RatingDialog.showRateAppDialogAuto(
                this@MainActivity,
                supportFragmentManager,
                time = 0,
                email = getString(R.string.rating_email)
            )
            AdsManager.isShowedRate = true
        }
    }

    override fun onStart() {
        super.onStart()
        handler.post(runnableCollapsibleHome)
        isCountingCollapsibleHome = true
    }

    override fun onStop() {
        super.onStop()
        binding.vShowInterAds.gone()

        if (isCountingCollapsibleHome) {
            handler.removeCallbacks(runnableCollapsibleHome)
            isCountingCollapsibleHome = false
        }
    }

    fun loadAndShowNativeCollapsibleHome(onShowOrFailed: () -> Unit) {
        if (isLoading) return
        when (RemoteConfig.remoteNativeCollapsibleHome) {
            1L -> {
                isLoading = true
                binding.frNativeSmall.visible()
                AdmobLib.loadAndShowNative(
                    activity = this,
                    admobNativeModel = AdsManager.NATIVE_COLLAPSIBLE_HOME,
                    viewGroup = binding.frNativeSmall,
                    size = GoogleENative.UNIFIED_SMALL_LIKE_BANNER,
                    layout = R.layout.native_ads_custom_small_like_banner,
                    onAdsLoaded = {
                        binding.whiteLine.visible()
                        onShowOrFailed()
                        isLoading = false
                    },
                    onAdsLoadFail = {
                        binding.whiteLine.gone()
                        onShowOrFailed()
                        isLoading = false
                    }
                )
            }

            2L -> {
                isLoading = true
                binding.frNativeSmall.visible()
                binding.frNativeExpand.visible()
                AdmobLib.loadAndShowNativeCollapsibleSingle(
                    activity = this@MainActivity,
                    admobNativeModel = AdsManager.NATIVE_COLLAPSIBLE_HOME,
                    viewGroupExpanded = binding.frNativeExpand,
                    viewGroupCollapsed = binding.frNativeSmall,
                    layoutExpanded = R.layout.native_ads_custom_medium_bottom,
                    layoutCollapsed = R.layout.native_ads_custom_small_like_banner,
                    onAdsLoaded = {
                        binding.whiteLine.visible()
                        onShowOrFailed()
                        isLoading = false
                    },
                    onAdsLoadFail = {
                        binding.whiteLine.gone()
                        onShowOrFailed()
                        isLoading = false
                    }
                )
            }
        }


    }

}