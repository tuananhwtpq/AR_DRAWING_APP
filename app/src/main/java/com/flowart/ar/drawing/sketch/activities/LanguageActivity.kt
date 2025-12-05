package com.flowart.ar.drawing.sketch.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.adapters.LanguageAdapter
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityLanguageBinding
import com.flowart.ar.drawing.sketch.utils.Common
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.ads.RemoteConfig
import com.flowart.ar.drawing.sketch.utils.gone
import com.flowart.ar.drawing.sketch.utils.invisible
import com.flowart.ar.drawing.sketch.utils.visible
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.models.AdmobNativeModel
import com.snake.squad.adslib.utils.GoogleENative
import kotlinx.coroutines.launch

class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {

    companion object {
        const val TAG = "LanguageActivity"
    }
    private var adapter: LanguageAdapter? = null
    private var isFromHome = true

    override fun initData() {
        //ko di tu man home -> show qc va request
        isFromHome = intent.getBooleanExtra(Constants.LANGUAGE_EXTRA, true)
        if (!isFromHome) {
            lifecycleScope.launch {
                requestNotiPer()
                loadNativeIntro()
            }
        }
    }

    override fun initView() {
        SharedPrefManager.putBoolean("isShowedLanguage", true)
        initLanguage()
    }

    override fun initActionView() {

        binding.ivDone.invisible()

        if (!isFromHome) {
            binding.ivBack.gone()
        } else {
            binding.ivBack.visible()
            binding.ivDone.visible()
            binding.ivBack.setOnClickListener {
                finish()
            }
        }

        binding.ivDone.setOnClickListener {
            val currLanguage = adapter?.getSelectedPositionLanguage()
            if (currLanguage == null) {
                Toast.makeText(
                    this,
                    getString(R.string.please_select_language_first), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            adapter?.let { Common.setSelectedLanguage(currLanguage) }

            loadAndShowInterLanguage {
                applyLanguage()
            }
        }
    }

    private fun requestNotiPer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1000)
        }
    }

    private fun applyLanguage() {
        val curr = adapter?.getSelectedPositionLanguage()
        if (curr == null) {
            Toast.makeText(
                this,
                getString(R.string.please_select_language_first), Toast.LENGTH_SHORT
            ).show()
            return
        }
        Common.setSelectedLanguage(curr)

        if (!isFromHome) {
            val intent = Intent(this@LanguageActivity, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            val intent = Intent(this@LanguageActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun initLanguage() {
        val languageList = Common.getLanguageList()
        adapter = LanguageAdapter(
            this@LanguageActivity,
            languageList
        ) {
            binding.ivDone.visible()
            if (!isFromHome) {
                loadAndShowNativeLanguage(AdsManager.NATIVE_LANGUAGE_2)
            }
        }
        binding.rcvLanguage.apply {
            layoutManager = LinearLayoutManager(this@LanguageActivity)
        }
        binding.rcvLanguage.adapter = adapter
        if (isFromHome) {
            adapter?.setSelectedPositionLanguage(Common.getSelectedLanguage())
        }
    }

    private fun loadNativeIntro() {
        if (RemoteConfig.remoteNativeIntro != 0L) {
            AdmobLib.loadNative(this@LanguageActivity, AdsManager.NATIVE_INTRO)
            Log.d(TAG, "Init remote native intro")
        }
        if (RemoteConfig.remoteNativeFullScreenIntro != 0L) {
            AdmobLib.loadNative(this@LanguageActivity, AdsManager.NATIVE_FULL_SCREEN_INTRO)
            Log.d(TAG, "Init remote native full screen intro")

        }

        if (RemoteConfig.remoteNativeFullScreenIntro2 != 0L) {
            AdmobLib.loadNative(this@LanguageActivity, AdsManager.NATIVE_FULL_SCREEN_INTRO_2)
            Log.d(TAG, "Init remote native full screen intro 2")

        }
    }


    /**
     * Native language goi o onResume de chieu qc
     * Native langaue 2 hien thi khi nguoi dung chon ngon ngu
     */

    private fun loadAndShowNativeLanguage(model: AdmobNativeModel) {
        if (RemoteConfig.remoteNativeLanguage != 0L) {
            binding.frNativeExpand.visible()
            Log.d(TAG, "loadAndShowNativeLanguage")
            if (isFromHome) {
                AdmobLib.loadAndShowNative(
                    activity = this,
                    admobNativeModel = model,
                    viewGroup = binding.frNativeExpand,
                    size = GoogleENative.UNIFIED_MEDIUM,
                    layout = R.layout.native_ads_custom_medium_bottom
                )
            } else {
                AdmobLib.showNative(
                    activity = this,
                    admobNativeModel = model,
                    viewGroup = binding.frNativeExpand,
                    size = GoogleENative.UNIFIED_MEDIUM,
                    layout = R.layout.native_ads_custom_medium_bottom
                )
            }

        }
    }

    /**
     * Hien thi QC sau khi da chon ngon ngu
     */
    private fun loadAndShowInterLanguage(navAction: () -> Unit) {
        if (RemoteConfig.remoteInterLanguage != 0L) {
            AdmobLib.loadAndShowInterWithNativeAfter(
                mActivity = this,
                interModel = AdsManager.INTER_LANGUAGE,
                nativeModel = AdsManager.NATIVE_FULL_SCREEN_AFTER_INTER,
                vShowInterAds = binding.viewBlock,
                isShowNativeAfter = AdsManager.isShowNativeFullScreen(),
                nativeLayout = R.layout.native_ads_full_screen,
                navAction = { navAction() }
            )
            Log.d(TAG, "loadAndShowInterLanguage")
        } else {
            navAction()
            Log.d(TAG, "Goto Nav Action")
        }
    }

    override fun onResume() {
        super.onResume()
        loadAndShowNativeLanguage(AdsManager.NATIVE_LANGUAGE)
    }

}