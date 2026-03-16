package com.example.baseproject.fragments

import com.bumptech.glide.Glide
import com.example.baseproject.R
import com.example.baseproject.bases.BaseDialogFragment
import com.example.baseproject.databinding.FragmentDrawGuideDialogBinding


class DrawGuideDialog : BaseDialogFragment<FragmentDrawGuideDialogBinding>(
    FragmentDrawGuideDialogBinding::inflate
) {

    private var isInit = false
    private var isSketch = false
    override fun initView() {
        if (!isInit) dismiss()
        Glide.with(requireContext()).load(if (isSketch) R.raw.guide_sketch else R.raw.guide_trace)
            .into(binding.gifGuide)
    }

    override fun initActionView() {
        binding.btnOk.setOnClickListener {
            dismiss()
        }
    }

    fun init(isSketch: Boolean = false): DrawGuideDialog {
        isInit = true
        this.isSketch = isSketch
        return this
    }
}