package com.flowart.ar.drawing.sketch.fragments

import android.annotation.SuppressLint
import android.content.Intent
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.activities.LessonDetailActivity
import com.flowart.ar.drawing.sketch.bases.BaseFragment
import com.flowart.ar.drawing.sketch.databinding.FragmentLessonBinding
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick
import com.ssquad.ar.drawing.sketch.db.ImageRepositories

class LessonFragment : BaseFragment<FragmentLessonBinding>(FragmentLessonBinding::inflate) {

    override fun initData() {

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        ImageRepositories.INSTANCE.getDone(0).observe(this) {
            binding.tvProgressBeginner.text =
                getString(R.string.lesson_num, it.toString(), (10).toString())

            binding.sbBeginner.progress = it
        }
        ImageRepositories.INSTANCE.getDone(1).observe(this) {
            binding.tvProgressIntermediate.text =
                getString(R.string.lesson_num, it.toString(), (10).toString())
            binding.sbIntermediate.progress = it

        }
        ImageRepositories.INSTANCE.getDone(2).observe(this) {
            binding.tvProgressProfessional.text =
                getString(R.string.lesson_num, it.toString(), (10).toString())
            binding.sbProfessional.progress = it
        }
    }

    override fun initActionView() {
        binding.lBeginner.setOnUnDoubleClick {
            gotoDetail(0)
        }

        binding.lIntermediate.setOnUnDoubleClick {
            gotoDetail(1)
        }

        binding.lProfessional.setOnUnDoubleClick {
            gotoDetail(2)
        }
        binding.sbBeginner.setOnTouchListener { _, _ -> true }
        binding.sbIntermediate.setOnTouchListener { _, _ -> true }
        binding.sbProfessional.setOnTouchListener { _, _ -> true }

        binding.professionalDes.isSelected = true
        binding.beginnerDes.isSelected = true
        binding.intermediateDes.isSelected = true
    }

    override fun onResume() {
        super.onResume()
        showNativeAds()
    }

    private fun gotoDetail(level: Int) {
//        (activity as? MainActivity)?.showInterAds {
//            val intent = Intent(requireContext(), LessonDetailActivity::class.java)
//            intent.putExtra("level", level)
//            startActivity(intent)
//        }

        val intent = Intent(requireContext(), LessonDetailActivity::class.java)
        intent.putExtra("level", level)
        startActivity(intent)
    }

    private fun showNativeAds() {
//        if (RemoteConfig.remoteNativeLesson == 0L) return
//        binding.frNative.visible()
//        AdmobLib.loadAndShowNative(
//            requireActivity(),
//            AdsManager.nativeOtherModel,
//            binding.frNative,
//            size = GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON,
//            layout = R.layout.native_ads_lesson
//        )
    }
}