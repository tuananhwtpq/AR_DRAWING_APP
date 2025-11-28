package com.snake.drawingview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.snake.drawingview.brushtool.data.Brush
import com.snake.drawingview.brushtool.data.BrushesRepository
import com.snake.drawingview.renderer.DrawingRendererFactory
import com.snake.drawingview.renderer.Renderer
import com.snake.drawingview.state.ActionsStacks
import com.snake.drawingview.state.DrawingState
import com.snake.drawingview.touch.handler.DrawingViewEventHandlerFactory
import com.snake.drawingview.touch.handler.MotionEventHandler


class DrawingView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
): View(context, attrs, defStyleAttr) {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet? = null): this(context, attrs, 0)

    private val drawingContext = DrawingContext()

    init {
        drawingContext.state.addOnStateChangedListener(::onDrawingStateChanged)
        drawingContext.brushToolStatus.addOnChangeListener {
            updateRenderer()
        }
    }

    private val eventHandlerFactory = DrawingViewEventHandlerFactory()
    private var touchHandler: MotionEventHandler? = null
    private var rendererFactory = DrawingRendererFactory()
    private var render: Renderer? = null
    private var bitmapTemplate: Bitmap? = null
    private var onTouchViewHandler: OnTouchViewHandler? = null

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (drawingContext.hasDrawing || width == 0 || height == 0) {
            return
        }
        drawingContext.setDrawing(width, height)
        updateRenderer()
        resetTransformation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        render?.render(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchHandler = eventHandlerFactory.create(drawingContext)
                touchHandler!!.handleFirstTouch(event)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    touchHandler!!.handleLastTouch(event)
                } else {
                    touchHandler!!.cancel()
                }
                touchHandler = null
            }
            else -> {
                touchHandler!!.handleTouch(event)
            }
        }
        onTouchViewHandler?.onTouch()
        invalidate()
        return true
    }

    private fun resetTransformation() {
        drawingContext.resetTransformation(width, height)
        invalidate()
    }

    private fun updateRenderer() {
        render = rendererFactory.createOnscreenRenderer(drawingContext, bitmapTemplate)
        invalidate()
    }

    private fun onDrawingStateChanged(drawingState: DrawingState) {
        render = rendererFactory.createOnscreenRenderer(drawingContext, bitmapTemplate)
        invalidate()
    }

    fun getDrawingWidth(): Int {
        return width
    }

    fun getDrawingHeight(): Int {
        return height
    }

    fun setBrushOrEraser(brush: Brush) {
        val brushesRepository = BrushesRepository(resources)
        drawingContext.brushConfig = brushesRepository.get(brush)
    }

    fun clear() {
        drawingContext.clear()
        onTouchViewHandler?.onTouch()
    }

    fun undo() {
        drawingContext.state.undo()
        onTouchViewHandler?.onTouch()
    }

    fun redo() {
        drawingContext.state.redo()
        onTouchViewHandler?.onTouch()
    }

    fun setBrushColor(color: Int) {
        drawingContext.brushColor = color
    }

    fun getState(): DrawingState {
        return drawingContext.state
    }

    fun setBrushOrEraserSize(size: Float) {
        drawingContext.brushConfig.size = size
    }

    fun setDrawingFrame(bitmapTemplate: Bitmap? = null, bitmapDrawing: Bitmap, actionsStacks: ActionsStacks) {
        bitmapTemplate?.let { this.bitmapTemplate = it }
        drawingContext.setDrawing(bitmapDrawing, false)
        drawingContext.setActionStacks(actionsStacks)
    }

    fun setOnTouchView(onTouchViewHandler: OnTouchViewHandler) {
        this.onTouchViewHandler = onTouchViewHandler
    }

    fun changeBackgroundColor(color: Int) {
        drawingContext.setBackgroundColor(color)
    }

}
