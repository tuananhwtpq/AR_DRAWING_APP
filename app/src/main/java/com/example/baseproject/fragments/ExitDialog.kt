package com.example.baseproject.fragments

import com.example.baseproject.bases.BaseDialogFragment
import com.example.baseproject.databinding.FragmentExitDialogBinding
import com.example.baseproject.utils.setOnUnDoubleClick

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