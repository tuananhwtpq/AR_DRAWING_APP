package com.flowart.ar.drawing.sketch.fragments

import android.content.Intent
import android.net.Uri
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.activities.LanguageActivity
import com.flowart.ar.drawing.sketch.bases.BaseFragment
import com.flowart.ar.drawing.sketch.databinding.FragmentSettingBinding
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick
import com.flowart.ar.drawing.sketch.utils.showToast
import com.snake.squad.adslib.rates.RatingDialog

class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {
    override fun initData() {

    }

    override fun initView() {

    }

    override fun initActionView() {
        binding.layoutRateUs.setOnClickListener {
            RatingDialog.showRateAppDialogAuto(
                requireActivity(), requireActivity().supportFragmentManager, -1, getString(
                    R.string.rating_email
                )
            )
        }

        binding.layoutLanguage.setOnUnDoubleClick {
//            (activity as? MainActivity)?.showInterAds {
//                val intent = Intent(requireContext(), LanguageActivity::class.java)
//                startActivity(intent)
//            }
            startActivity(Intent(requireContext(), LanguageActivity::class.java))
        }

        binding.layoutPrivacy.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PRIVACY_POLICY))
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.layoutShare.setOnClickListener {
            shareApp()
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
            val shareMessage =
                "https://play.google.com/store/apps/details?id=${requireContext().packageName}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(getString(R.string.failed_to_share))
        }
    }

    override fun onResume() {
        super.onResume()
        showNativeAds()
    }

    private fun showNativeAds() {
//        if (RemoteConfig.remoteNativeSetting == 0L) return
//        binding.frNative.visible()
//        if (AdsManager.nativeSettingModel.nativeAd.value == null) {
//            AdmobLib.loadAndShowNative(
//                requireActivity(),
//                AdsManager.nativeSettingModel,
//                binding.frNative,
//                size = GoogleENative.UNIFIED_SMALL_LIKE_BANNER,
//                layout = R.layout.native_ads_custom_small_like_banner
//            )
//        } else {
//            AdmobLib.showNative(
//                requireActivity(),
//                AdsManager.nativeSettingModel,
//                binding.frNative,
//                size = GoogleENative.UNIFIED_SMALL_LIKE_BANNER,
//                layout = R.layout.native_ads_custom_small_like_banner
//            )
//        }
    }
}