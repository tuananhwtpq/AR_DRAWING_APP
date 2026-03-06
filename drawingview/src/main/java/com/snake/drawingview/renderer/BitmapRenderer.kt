package com.snake.drawingview.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

internal class BitmapRenderer(
    private val bitmap: Bitmap,
    private val bitmapTemplate: Bitmap?
): Renderer {

    override fun render(canvas: Canvas) {
        val paint = Paint()
        bitmapTemplate?.let {
            val x = (bitmap.width - it.width) / 2
            val y = (bitmap.height - it.height) / 2
            canvas.drawBitmap(it, x.toFloat(), y.toFloat(), paint)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, Paint())
    }

}