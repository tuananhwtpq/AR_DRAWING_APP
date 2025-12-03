package com.flowart.ar.drawing.sketch.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.adapters.LanguageAdapter
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityLanguageBinding
import com.flowart.ar.drawing.sketch.utils.Common
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.gone
import com.flowart.ar.drawing.sketch.utils.invisible
import com.flowart.ar.drawing.sketch.utils.visible
import kotlinx.coroutines.launch

class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {

    private var adapter: LanguageAdapter? = null
    private var isFromHome = true

    override fun initData() {
        isFromHome = intent.getBooleanExtra(Constants.LANGUAGE_EXTRA, true)
        if (!isFromHome) {
            lifecycleScope.launch {
                requestNotiPer()
            }
        }
    }

    override fun initView() {
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
//            showInterLanguage {
//                applyLanguage()
//            }
            applyLanguage()
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
        }
        binding.rcvLanguage.apply {
            layoutManager = LinearLayoutManager(this@LanguageActivity)
        }
        binding.rcvLanguage.adapter = adapter
        if (isFromHome) {
            adapter?.setSelectedPositionLanguage(Common.getSelectedLanguage())
        }
    }

}