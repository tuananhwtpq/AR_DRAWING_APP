package com.flowart.ar.drawing.sketch.utils.sticker

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.IntRange

open class DrawableSticker(override var drawable: Drawable) : Sticker() {
    private val realBounds: Rect

    init {
        realBounds = Rect(0, 0, width, height)
    }

    override fun setDrawable(drawable: Drawable): DrawableSticker? {
        this.drawable = drawable
        return this
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.concat(matrix)
        drawable.bounds = realBounds
        drawable.draw(canvas)
        canvas.restore()
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int): DrawableSticker {
        drawable.alpha = alpha
        return this
    }

    override val width: Int
        get() = drawable.intrinsicWidth

    override val height: Int
        get() = drawable.intrinsicHeight

}