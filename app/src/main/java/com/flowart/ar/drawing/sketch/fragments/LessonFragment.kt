package com.flowart.ar.drawing.sketch.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.activities.LessonDetailActivity
import com.flowart.ar.drawing.sketch.activities.MainActivity
import com.flowart.ar.drawing.sketch.bases.BaseFragment
import com.flowart.ar.drawing.sketch.databinding.FragmentLessonBinding
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick
import com.ssquad.ar.drawing.sketch.db.ImageRepositories

class LessonFragment : BaseFragment<FragmentLessonBinding>(FragmentLessonBinding::inflate) {

    private val handler = Handler(Looper.getMainLooper())
    private var isLoadingAds = false
    private var runnable = object : Runnable {
        override fun run() {
            if (!isLoadingAds && AdsManager.isReloadingNativeHome()) {
                isLoadingAds = true
                (requireActivity() as MainActivity).loadAndShowNativeHome(binding.frNative) {
                    AdsManager.updateNativeHome()
                    isLoadingAds = false
                }
            }
            handler.postDelayed(this, 5000L)
        }
    }

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

    @SuppressLint("ClickableViewAccessibility")
    override fun initActionView() {
        binding.lBeginner.setOnUnDoubleClick {
            (requireActivity() as MainActivity).loadAndShowInterHome {
                gotoDetail(0)
            }
        }

        binding.lIntermediate.setOnUnDoubleClick {
            (requireActivity() as MainActivity).loadAndShowInterHome {
                gotoDetail(1)
            }
        }

        binding.lProfessional.setOnUnDoubleClick {
            (requireActivity() as MainActivity).loadAndShowInterHome {
                gotoDetail(2)
            }
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
        AdsManager.lastNativeHomeShow = 0L
        handler.removeCallbacksAndMessages(null)
        handler.post(runnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    private fun gotoDetail(level: Int) {

        val intent = Intent(requireContext(), LessonDetailActivity::class.java)
        intent.putExtra("level", level)
        startActivity(intent)
    }

}