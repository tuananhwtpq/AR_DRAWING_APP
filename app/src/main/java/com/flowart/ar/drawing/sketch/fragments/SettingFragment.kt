package com.flowart.ar.drawing.sketch.fragments

import android.content.Intent
import com.flowart.ar.drawing.sketch.activities.LanguageActivity
import com.flowart.ar.drawing.sketch.bases.BaseFragment
import com.flowart.ar.drawing.sketch.databinding.FragmentSettingBinding
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick

class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {
    override fun initData() {

    }

    override fun initView() {

    }

    override fun initActionView() {

        binding.layoutLanguage.setOnUnDoubleClick {
                startActivity(Intent(requireContext(), LanguageActivity::class.java))
        }

//        binding.layoutShare.setOnClickListener {
//            shareApp()
//        }
    }

//    private fun shareApp() {
//        try {
//            val shareIntent = Intent(Intent.ACTION_SEND)
//            shareIntent.setType("text/plain")
//            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
//            val shareMessage =
//                "https://play.google.com/store/apps/details?id=${requireContext().packageName}"
//            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
//            startActivity(Intent.createChooser(shareIntent, "choose one"))
//        } catch (e: Exception) {
//            e.printStackTrace()
//            showToast(getString(R.string.failed_to_share))
//        }
//    }

    override fun onResume() {
        super.onResume()
    }
}