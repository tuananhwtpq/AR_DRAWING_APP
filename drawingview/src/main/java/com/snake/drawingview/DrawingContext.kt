package com.snake.drawingview

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import com.snake.drawingview.actions.ChangeBackgroundAction
import com.snake.drawingview.actions.ClearAction
import com.snake.drawingview.brushtool.BrushToolBitmaps
import com.snake.drawingview.brushtool.BrushToolStatus
import com.snake.drawingview.brushtool.model.BrushConfig
import com.snake.drawingview.state.ActionsStacks
import com.snake.drawingview.state.DrawingState

class DrawingContext internal constructor() {

    private var nullableBrushToolBitmaps: BrushToolBitmaps? = null
        set(value) {
            require(value != null)
            field = value
        }
    internal val brushToolBitmaps get() = nullableBrushToolBitmaps!!
    var brushToolStatus = BrushToolStatus()
    val hasDrawing get() = nullableBrushToolBitmaps != null
    val drawingWidth get() = brushToolBitmaps.layerBitmap.width
    val drawingHeight get() = brushToolBitmaps.layerBitmap.height
    val state = DrawingState(this)
    val transformation = Matrix()
    var brushConfig = BrushConfig()
    var brushColor = Color.BLACK
    var rotationEnabled = false
    internal var backgroundColor = Color.TRANSPARENT

    fun setDrawing(
        drawingWidth: Int,
        drawingHeight: Int,
    ) = setDrawing(Bitmap.createBitmap(drawingWidth, drawingHeight, ARGB_8888), true)

    fun setDrawing(bitmap: Bitmap, isReset: Boolean) {
        nullableBrushToolBitmaps = BrushToolBitmaps.createFromDrawing(bitmap)
        if (isReset) state.reset()
    }
    fun clear() {
        state.update(
            ClearAction(),
        )
    }

    fun setBackgroundColor(color: Int) {
        state.update(
            ChangeBackgroundAction(color),
        )
    }

    internal fun resetTransformation(containerWidth: Int, containerHeight: Int) {
        transformation.reset()

        val dx = (containerWidth - drawingWidth) / 2f
        val dy = (containerHeight - containerWidth) / 2f

        transformation.postTranslate(dx, dy)

        Log.e("DrawingView", "resetTransformation c: $containerWidth - $containerHeight")
        Log.e("DrawingView", "resetTransformation: $drawingWidth - $drawingHeight")
    }


    internal fun setActionStacks(actionsStacks: ActionsStacks) {
        state.setActionStacks(actionsStacks)
    }

}
