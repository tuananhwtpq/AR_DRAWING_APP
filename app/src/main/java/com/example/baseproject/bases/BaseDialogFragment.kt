package com.example.baseproject.bases

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class BaseDialogFragment<B : ViewBinding>(
    private val inflater: (LayoutInflater) -> B
) : DialogFragment() {
    protected val binding: B by lazy { inflater(layoutInflater) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { mActivity ->

            val builder = AlertDialog.Builder(mActivity)
            builder.setView(binding.root)
            initView()
            initActionView()

            builder.create().apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        } ?: throw IllegalStateException("Activity can't null")
    }

    abstract fun initView()

    abstract fun initActionView()
}