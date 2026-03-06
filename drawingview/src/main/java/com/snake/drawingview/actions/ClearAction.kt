package com.snake.drawingview.actions

import android.graphics.Bitmap
import android.graphics.Rect
import com.snake.drawingview.DrawingContext

internal class ClearAction: Action {

    override fun perform(context: DrawingContext) {
        context.brushToolBitmaps.layerBitmap.eraseColor(0)
    }

    override fun getOppositeAction(context: DrawingContext): Action {
        val drawingCopy = context.brushToolBitmaps.layerBitmap.copy(Bitmap.Config.ARGB_8888, false)
        val rect = Rect(0, 0, drawingCopy.width, drawingCopy.height)
        return DrawBitmapAction(drawingCopy, rect, rect)
    }

}