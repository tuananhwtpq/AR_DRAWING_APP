package com.example.baseproject.utils

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.setOnUnDoubleClick(interval: Long = 500L, onViewClick: (View?) -> Unit) {
    setOnClickListener(UnDoubleClick(defaultInterval = interval, onViewClick = onViewClick))
}

fun Activity.showToast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_SHORT ).show()
}

fun Fragment.showToast(message: String){
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT ).show()
}