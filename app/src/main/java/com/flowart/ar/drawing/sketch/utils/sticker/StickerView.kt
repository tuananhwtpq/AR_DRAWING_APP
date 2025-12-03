package com.flowart.ar.drawing.sketch.utils.sticker


import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.utils.sticker.StickerUtils.notifySystemGallery
import com.flowart.ar.drawing.sketch.utils.sticker.StickerUtils.saveImageToGallery
import java.io.File
import java.util.Arrays
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class StickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr) {
    private var bringToFrontCurrentSticker = false

    @IntDef(
        value = [ActionMode.NONE, ActionMode.DRAG, ActionMode.ZOOM_WITH_TWO_FINGER, ActionMode.ICON, ActionMode.CLICK
        ]
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class ActionMode {
        companion object {
            const val NONE: Int = 0
            const val DRAG: Int = 1
            const val ZOOM_WITH_TWO_FINGER: Int = 2
            const val ICON: Int = 3
            const val CLICK: Int = 4
        }
    }

    @IntDef(flag = true, value = [FLIP_HORIZONTALLY, FLIP_VERTICALLY])
    @Retention(
        AnnotationRetention.SOURCE
    )
    annotation class Flip

    val stickers: MutableList<Sticker> = ArrayList()

    private val stickerRect = RectF()

    private val sizeMatrix = Matrix()
    private val downMatrix = Matrix()
    private val moveMatrix = Matrix()

    // region storing variables
    private val bitmapPoints = FloatArray(8)
    private val bounds = FloatArray(8)
    private val point = FloatArray(2)
    private val currentCenterPoint = PointF()
    private val tmp = FloatArray(2)
    private var midPoint = PointF()

    // endregion
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop


    //the first point down position
    private var downX = 0f
    private var downY = 0f

    private var oldDistance = 0f
    private var oldRotation = 0f

    @ActionMode
    private var currentMode = ActionMode.NONE

    var currentSticker: Sticker? = null

    var isLocked: Boolean = false
        private set
    var isConstrained: Boolean = false
        private set

    private var lastClickTime: Long = 0
    var minClickDelayTime: Int = DEFAULT_MIN_CLICK_DELAY_TIME
        private set

    init {
        var a: TypedArray? = null
        try {
            a = context.obtainStyledAttributes(attrs, R.styleable.StickerView)
            bringToFrontCurrentSticker =
                a.getBoolean(R.styleable.StickerView_bringToFrontCurrentSticker, false)

        } finally {
            a?.recycle()
        }
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            stickerRect.left = left.toFloat()
            stickerRect.top = top.toFloat()
            stickerRect.right = right.toFloat()
            stickerRect.bottom = bottom.toFloat()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        drawStickers(canvas)
    }

    private fun drawStickers(canvas: Canvas) {
        for (i in stickers.indices) {
            val sticker = stickers[i]
            sticker.draw(canvas)
        }

        if (currentSticker != null && !isLocked) {
            getStickerPoints(currentSticker, bitmapPoints)
        }
    }


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isLocked) return super.onInterceptTouchEvent(ev)

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y

                return findHandlingSticker() != null
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isLocked) {
            return super.onTouchEvent(event)
        }

        val action = MotionEventCompat.getActionMasked(event)

        when (action) {
            MotionEvent.ACTION_DOWN -> if (!onTouchDown(event)) {
                return false
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDistance = calculateDistance(event)
                oldRotation = calculateRotation(event)

                midPoint = calculateMidPoint(event)

//                if (currentSticker != null && isInStickerArea(
//                        currentSticker!!, event.getX(1),
//                        event.getY(1)
//                    )
//                ) {
//                    currentMode = ActionMode.ZOOM_WITH_TWO_FINGER
//                }
                if (currentSticker != null) {
                    currentMode = ActionMode.ZOOM_WITH_TWO_FINGER
                }
            }

            MotionEvent.ACTION_MOVE -> {
                handleCurrentMode(event)
                invalidate()
            }

            MotionEvent.ACTION_UP -> onTouchUp(event)
            MotionEvent.ACTION_POINTER_UP -> {
                currentMode = ActionMode.NONE
            }
        }
        return true
    }

    private fun onTouchDown(event: MotionEvent): Boolean {
        currentMode = ActionMode.DRAG

        downX = event.x
        downY = event.y

        midPoint = calculateMidPoint()
        oldDistance = calculateDistance(midPoint.x, midPoint.y, downX, downY)
        oldRotation = calculateRotation(midPoint.x, midPoint.y, downX, downY)

//        currentSticker = findHandlingSticker()
        if (stickers.isNotEmpty()) {
            currentSticker = stickers[stickerCount - 1]
        }


        if (currentSticker != null) {
            downMatrix.set(currentSticker!!.matrix)
            if (bringToFrontCurrentSticker) {
                stickers.remove(currentSticker)
                stickers.add(currentSticker!!)
            }
        }

        if (currentSticker == null) {
            return false
        }
        invalidate()
        return true
    }

    private fun onTouchUp(event: MotionEvent) {

        val currentTime = SystemClock.uptimeMillis()

        if (currentMode == ActionMode.DRAG && abs((event.x - downX).toDouble()) < touchSlop && abs(
                (event.y - downY).toDouble()
            ) < touchSlop && currentSticker != null
        ) {
            currentMode = ActionMode.CLICK

        }

        currentMode = ActionMode.NONE
        lastClickTime = currentTime
    }

    fun handleCurrentMode(event: MotionEvent) {
        when (currentMode) {
            ActionMode.NONE, ActionMode.CLICK -> {}
            ActionMode.DRAG -> if (currentSticker != null) {
                moveMatrix.set(downMatrix)
                moveMatrix.postTranslate(event.x - downX, event.y - downY)
                currentSticker!!.setMatrix(moveMatrix)
                if (isConstrained) {
                    constrainSticker(currentSticker!!)
                }
            }

            ActionMode.ZOOM_WITH_TWO_FINGER -> if (currentSticker != null) {
//                val newDistance = calculateDistance(event)
//                val newRotation = calculateRotation(event)
//
//                moveMatrix.set(downMatrix)
//                moveMatrix.postScale(
//                    newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
//                    midPoint.y
//                )
//                moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y)
//                currentSticker!!.setMatrix(moveMatrix)

                val newDistance = calculateDistance(event)
                val newRotation = calculateRotation(event)

                val scaleFactor = newDistance / oldDistance


                val currentScale = currentSticker!!.currentScale

                if (currentScale < 0.2f && scaleFactor < 1) {
                    moveMatrix.set(downMatrix)
                    moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y)
                    currentSticker!!.setMatrix(moveMatrix)
                } else {
                    moveMatrix.set(downMatrix)
                    moveMatrix.postScale(
                        scaleFactor, scaleFactor, midPoint.x,
                        midPoint.y
                    )
                    moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y)
                    currentSticker!!.setMatrix(moveMatrix)
                }
            }
        }
    }

    fun zoomAndRotateCurrentSticker(event: MotionEvent) {
        zoomAndRotateSticker(currentSticker, event)
    }

    fun zoomAndRotateSticker(sticker: Sticker?, event: MotionEvent) {
        if (sticker != null) {
            val newDistance = calculateDistance(midPoint.x, midPoint.y, event.x, event.y)
            val newRotation = calculateRotation(midPoint.x, midPoint.y, event.x, event.y)

            moveMatrix.set(downMatrix)
            moveMatrix.postScale(
                newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
                midPoint.y
            )
            moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y)
            currentSticker!!.setMatrix(moveMatrix)
        }
    }

    fun constrainSticker(sticker: Sticker) {
        var moveX = 0f
        var moveY = 0f
        val width = width
        val height = height
        sticker.getMappedCenterPoint(currentCenterPoint, point, tmp)
        if (currentCenterPoint.x < 0) {
            moveX = -currentCenterPoint.x
        }

        if (currentCenterPoint.x > width) {
            moveX = width - currentCenterPoint.x
        }

        if (currentCenterPoint.y < 0) {
            moveY = -currentCenterPoint.y
        }

        if (currentCenterPoint.y > height) {
            moveY = height - currentCenterPoint.y
        }

        sticker.matrix.postTranslate(moveX, moveY)
    }

    fun findHandlingSticker(): Sticker? {
        for (i in stickers.indices.reversed()) {
            if (isInStickerArea(stickers[i], downX, downY)) {
                return stickers[i]
            }
        }
        return null
    }

    fun isInStickerArea(sticker: Sticker, downX: Float, downY: Float): Boolean {
        tmp[0] = downX
        tmp[1] = downY
        return sticker.contains(tmp)
    }

    private fun calculateMidPoint(event: MotionEvent?): PointF {
        if (event == null || event.pointerCount < 2) {
            midPoint[0f] = 0f
            return midPoint
        }
        val x = (event.getX(0) + event.getX(1)) / 2
        val y = (event.getY(0) + event.getY(1)) / 2
        midPoint[x] = y
        return midPoint
    }

    private fun calculateMidPoint(): PointF {
        if (currentSticker == null) {
            midPoint[0f] = 0f
            return midPoint
        }
        currentSticker!!.getMappedCenterPoint(midPoint, point, tmp)
        return midPoint
    }

    /**
     * calculate rotation in line with two fingers and x-axis
     */
    private fun calculateRotation(event: MotionEvent?): Float {
        if (event == null || event.pointerCount < 2) {
            return 0f
        }
        return calculateRotation(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
    }

    private fun calculateRotation(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val x = (x1 - x2).toDouble()
        val y = (y1 - y2).toDouble()
        val radians = atan2(y, x)
        return Math.toDegrees(radians).toFloat()
    }

    /**
     * calculate Distance in two fingers
     */
    private fun calculateDistance(event: MotionEvent?): Float {
        if (event == null || event.pointerCount < 2) {
            return 0f
        }
        return calculateDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val x = (x1 - x2).toDouble()
        val y = (y1 - y2).toDouble()

        return sqrt(x * x + y * y).toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        for (i in stickers.indices) {
            val sticker = stickers[i]
            transformSticker(sticker)
        }
    }

    /**
     * Sticker's drawable will be too bigger or smaller
     * This method is to transform it to fit
     * step 1：let the center of the sticker image is coincident with the center of the View.
     * step 2：Calculate the zoom and zoom
     */
    fun transformSticker(sticker: Sticker?) {
        if (sticker == null) {
            Log.e(
                TAG,
                "transformSticker: the bitmapSticker is null or the bitmapSticker bitmap is null"
            )
            return
        }

        sizeMatrix.reset()

        val width = width.toFloat()
        val height = height.toFloat()
        val stickerWidth = sticker.width.toFloat()
        val stickerHeight = sticker.height.toFloat()
        //step 1
        val offsetX = (width - stickerWidth) / 2
        val offsetY = (height - stickerHeight) / 2

        sizeMatrix.postTranslate(offsetX, offsetY)

        //step 2
        val scaleFactor = if (width < height) {
            width / stickerWidth
        } else {
            height / stickerHeight
        }

        sizeMatrix.postScale(scaleFactor / 2f, scaleFactor / 2f, width / 2f, height / 2f)

        sticker.matrix.reset()
        sticker.setMatrix(sizeMatrix)

        invalidate()
    }

    fun flipCurrentSticker(direction: Int) {
        flip(currentSticker, direction)
    }

    fun flip(sticker: Sticker?, @Flip direction: Int) {
        if (sticker != null) {
            sticker.getCenterPoint(midPoint)
            if ((direction and FLIP_HORIZONTALLY) > 0) {
                sticker.matrix.preScale(-1f, 1f, midPoint.x, midPoint.y)
                sticker.setFlippedHorizontally(!sticker.isFlippedHorizontally)
            }
            if ((direction and FLIP_VERTICALLY) > 0) {
                sticker.matrix.preScale(1f, -1f, midPoint.x, midPoint.y)
                sticker.setFlippedVertically(!sticker.isFlippedVertically)
            }

            invalidate()
        }
    }

    fun remove(sticker: Sticker?): Boolean {
        if (stickers.contains(sticker)) {
            stickers.remove(sticker)
            if (currentSticker === sticker) {
                currentSticker = null
            }
            invalidate()

            return true
        } else {
            Log.d(TAG, "remove: the sticker is not in this StickerView")

            return false
        }
    }

    fun removeCurrentSticker(): Boolean {
        return remove(currentSticker)
    }

    fun removeAllStickers() {
        stickers.clear()
        if (currentSticker != null) {
            currentSticker!!.release()
            currentSticker = null
        }
        invalidate()
    }

    fun addSticker(sticker: Sticker): StickerView {
        return addSticker(sticker, Sticker.Position.CENTER)
    }

    fun addSticker(
        sticker: Sticker,
        @Sticker.Position position: Int
    ): StickerView {
        if (ViewCompat.isLaidOut(this)) {
            addStickerImmediately(sticker, position)
        } else {
            post { addStickerImmediately(sticker, position) }
        }
        return this
    }

    fun addStickerImmediately(sticker: Sticker, @Sticker.Position position: Int) {
        setStickerPosition(sticker, position)


        val scaleFactor: Float

        val widthScaleFactor = width.toFloat() / sticker.drawable.intrinsicWidth
        val heightScaleFactor = height.toFloat() / sticker.drawable.intrinsicHeight
        scaleFactor =
            if (widthScaleFactor > heightScaleFactor) heightScaleFactor else widthScaleFactor

        sticker.matrix
            .postScale(
                scaleFactor / 2,
                scaleFactor / 2,
                (width / 2).toFloat(),
                (height / 2).toFloat()
            )

        currentSticker = sticker
        stickers.add(sticker)
        invalidate()
    }

    fun setStickerPosition(sticker: Sticker, @Sticker.Position position: Int) {
        val width = width.toFloat()
        val height = height.toFloat()
        var offsetX = width - sticker.width
        var offsetY = height - sticker.height
        if ((position and Sticker.Position.TOP) > 0) {
            offsetY /= 4f
        } else if ((position and Sticker.Position.BOTTOM) > 0) {
            offsetY *= 3f / 4f
        } else {
            offsetY /= 2f
        }
        if ((position and Sticker.Position.LEFT) > 0) {
            offsetX /= 4f
        } else if ((position and Sticker.Position.RIGHT) > 0) {
            offsetX *= 3f / 4f
        } else {
            offsetX /= 2f
        }
        sticker.matrix.postTranslate(offsetX, offsetY)
    }

    fun getStickerPoints(sticker: Sticker?): FloatArray {
        val points = FloatArray(8)
        getStickerPoints(sticker, points)
        return points
    }

    fun getStickerPoints(sticker: Sticker?, dst: FloatArray) {
        if (sticker == null) {
            Arrays.fill(dst, 0f)
            return
        }
        sticker.getBoundPoints(bounds)
        sticker.getMappedPoints(dst, bounds)
    }

    fun save(file: File) {
        try {
            saveImageToGallery(file, createBitmap())
            notifySystemGallery(context, file)
        } catch (ignored: IllegalArgumentException) {
            //
        } catch (ignored: IllegalStateException) {
        }
    }

    @Throws(OutOfMemoryError::class)
    fun createBitmap(): Bitmap {
        currentSticker = null
        val bitmap = Bitmap.createBitmap(
            width,
            height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        return bitmap
    }

    val stickerCount: Int
        get() = stickers.size

    val isNoneSticker: Boolean
        get() = stickerCount == 0

    fun setLocked(locked: Boolean): StickerView {
        this.isLocked = locked
        invalidate()
        return this
    }

    fun setMinClickDelayTime(minClickDelayTime: Int): StickerView {
        this.minClickDelayTime = minClickDelayTime
        return this
    }

    fun setConstrained(constrained: Boolean): StickerView {
        this.isConstrained = constrained
        postInvalidate()
        return this
    }


    companion object {
        private const val TAG = "StickerView"

        private const val DEFAULT_MIN_CLICK_DELAY_TIME = 200

        const val FLIP_HORIZONTALLY: Int = 1
        const val FLIP_VERTICALLY: Int = 1 shl 1
    }
}