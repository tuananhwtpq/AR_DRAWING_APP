package com.flowart.ar.drawing.sketch.fragments

import com.flowart.ar.drawing.sketch.bases.BaseDialogFragment
import com.flowart.ar.drawing.sketch.databinding.FragmentExitDialogBinding
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick

class ExitDialog :
    BaseDialogFragment<FragmentExitDialogBinding>(FragmentExitDialogBinding::inflate) {

    private var isInit = false
    private var onExit: () -> Unit = {}
    private var onDismiss: () -> Unit = {}
    override fun initView() {
        if (!isInit) dismiss()
    }

    override fun initActionView() {
        binding.btnExit.setOnUnDoubleClick {
            onExit()
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    fun init(onExit: () -> Unit, onDismiss: () -> Unit): ExitDialog {
        isInit = true
        this.onExit = onExit
        this.onDismiss = onDismiss
        return this
    }

}