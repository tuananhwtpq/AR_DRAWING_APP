package com.flowart.ar.drawing.sketch.fragments

import com.bumptech.glide.Glide
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.bases.BaseDialogFragment
import com.flowart.ar.drawing.sketch.databinding.FragmentDrawGuideDialogBinding


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