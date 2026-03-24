package com.flowart.ar.drawing.sketch.utils

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Locale

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

fun Activity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun SeekBar.onProgressChange(onProgressChange: (Int) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                onProgressChange(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }


    })
}

fun Long.formatTime(check: Boolean = true): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours == 0L && check) String.format("%02d:%02d", minutes % 60, seconds % 60)
    else
        String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
}

fun Long.formatDateTime(): String {
    val sdf = SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US)
    return sdf.format(this)
}


fun Fragment.getColor(colorId: Int) = ContextCompat.getColor(requireContext(), colorId)


fun Context.convertToPx(value: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
}