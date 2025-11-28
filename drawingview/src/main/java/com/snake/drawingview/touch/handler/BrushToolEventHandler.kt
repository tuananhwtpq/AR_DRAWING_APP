package com.snake.drawingview.touch.handler

import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import com.snake.drawingview.brushtool.BrushToolFactory
import com.snake.drawingview.brushtool.model.TouchEvent
import com.snake.drawingview.DrawingContext
import com.snake.drawingview.actions.DrawBitmapAction
import kotlin.math.max
import kotlin.math.min

internal class BrushToolEventHandler(
    private val drawingContext: DrawingContext,
): MotionEventHandler {

    private val brushTool = BrushToolFactory(drawingContext.brushToolBitmaps)
        .create(drawingContext.brushColor, drawingContext.brushConfig)
    private val touchEvent = TouchEvent()
    private var pointerId = 0
    private var ignoreEvents = false
    private var firstEventTime = 0L

    override fun handleFirstTouch(event: MotionEvent) {
        firstEventTime = System.currentTimeMillis()
        if (event.pointerCount > 1) {
            ignoreEvents = true
            return
        }
        val pointerIdx = 0
        pointerId = event.getPointerId(pointerIdx)

        touchEvent.set(event, pointerIdx)
        startDrawing(touchEvent)
    }

    override fun handleTouch(event: MotionEvent) {
        if (ignoreEvents) {
            return
        }
        val pointerIdx = event.findPointerIndex(pointerId)
        touchEvent.set(event, pointerIdx)
        if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            ignoreEvents = true
            cancelDrawing()
        } else {
            brushTool.continueDrawing(touchEvent)
        }
    }

    override fun handleLastTouch(event: MotionEvent) {
        if (ignoreEvents) {
            return
        }
        val pointerIdx = event.findPointerIndex(pointerId)
        touchEvent.set(event, pointerIdx)
        endDrawing(touchEvent)
    }

    override fun cancel() {
        if (!ignoreEvents) {
            cancelDrawing()
        }
    }

    private fun startDrawing(event: TouchEvent) {
        drawingContext.brushToolStatus.active = true
        drawingContext.brushToolBitmaps.resultBitmap.eraseColor(0)
        drawingContext.brushToolBitmaps.strokeBitmap.eraseColor(0)
        if (drawingContext.brushConfig.isEraser) {
            Canvas(drawingContext.brushToolBitmaps.strokeBitmap)
                .drawBitmap(
                    drawingContext.brushToolBitmaps.layerBitmap,
                    0f, 0f, null,
                )
        }
        brushTool.startDrawing(event)
    }

    private fun endDrawing(event: TouchEvent) {
        brushTool.endDrawing(event)
        drawingContext.brushToolStatus.active = false
        updateRasmState()
    }

    private fun cancelDrawing() {
        brushTool.cancel()
        drawingContext.brushToolStatus.active = false
        if (System.currentTimeMillis() - firstEventTime > 500) {
            updateRasmState()
        }
    }

    private fun updateRasmState() {
        val strokeBoundary = Rect(brushTool.strokeBoundary)
        val resultBitmap = drawingContext.brushToolBitmaps.resultBitmap
        strokeBoundary.left = max(strokeBoundary.left, 0)
        strokeBoundary.top = max(strokeBoundary.top, 0)
        strokeBoundary.right = min(strokeBoundary.right, resultBitmap.width)
        strokeBoundary.bottom = min(strokeBoundary.bottom, resultBitmap.height)
        if (strokeBoundary.width() > 0 && strokeBoundary.height() > 0) {
            drawingContext.state.update(
                DrawBitmapAction(
                    resultBitmap,
                    strokeBoundary,
                    strokeBoundary,
                ),
            )
        }
    }

}

private fun TouchEvent.set(event: MotionEvent, pointerIdx: Int) {
    x = event.getX(pointerIdx)
    y = event.getY(pointerIdx)
}
