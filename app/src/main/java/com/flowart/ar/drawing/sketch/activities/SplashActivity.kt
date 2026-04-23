package com.flowart.ar.drawing.sketch.activities

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivitySplashBinding
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.flowart.ar.drawing.sketch.utils.invisible
import kotlin.system.exitProcess

class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    companion object {
        const val TAG = "SplashActivity"
    }

    override fun initData() {
    }

    override fun initView() {
        binding.tvLoadingAds.invisible()
        Handler(Looper.getMainLooper()).postDelayed({
            replaceActivity()
        }, 6000L)
    }

    override fun initActionView() {
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }
    private fun replaceActivity() {
        SharedPrefManager.putBoolean("wantShowRate", false)
        val isShowedLanguage = SharedPrefManager.getBoolean("isShowedLanguage")
        val intent = if (!isShowedLanguage) {
            Intent(this@SplashActivity, LanguageActivity::class.java)
        } else {
            Intent(this@SplashActivity, MainActivity::class.java)
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

}