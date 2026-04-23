package com.flowart.ar.drawing.sketch.activities

import android.content.Intent
import androidx.activity.OnBackPressedCallback
import com.flowart.ar.drawing.sketch.adapters.IntroViewPagerAdapter
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityIntroBinding
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager

class IntroActivity : BaseActivity<ActivityIntroBinding>(ActivityIntroBinding::inflate) {

    private val mAdapter by lazy {
        IntroViewPagerAdapter(
            this,
            false,
            false
        )
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.vpIntro.currentItem in 1..mAdapter.itemCount) {
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
    }

    override fun initActionView() {
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    fun nextPage() {
        if (binding.vpIntro.currentItem < mAdapter.itemCount - 1) {
            binding.vpIntro.currentItem++
        } else {
                val intent = Intent(this@IntroActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
        }
    }
}