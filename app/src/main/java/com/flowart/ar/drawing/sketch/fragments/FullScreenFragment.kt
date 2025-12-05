package com.flowart.ar.drawing.sketch.fragments

import com.flowart.ar.drawing.sketch.activities.IntroActivity
import com.flowart.ar.drawing.sketch.bases.BaseFragment
import com.flowart.ar.drawing.sketch.databinding.FragmentFullScreenBinding

class FullScreenFragment :
    BaseFragment<FragmentFullScreenBinding>(FragmentFullScreenBinding::inflate) {
    override fun initData() {

    }

    override fun initView() {
    }

    override fun initActionView() {
        binding.ivClose.setOnClickListener {
            (activity as? IntroActivity)?.nextPage()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? IntroActivity)?.showNativeFullScreen(binding.frNative)
    }


}