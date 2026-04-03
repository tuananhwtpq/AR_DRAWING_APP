package com.flowart.ar.drawing.sketch.ai

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

/**
 * ScoreChartView — Custom View vẽ biểu đồ tiến bộ (Line Chart).
 *
 * Hiển thị điểm số qua từng lần vẽ dưới dạng line chart.
 * Vẽ bằng Canvas, KHÔNG dùng thư viện ngoài.
 *
 * Kiến thức cho bảo vệ:
 * - Custom View: kế thừa View, override onDraw()
 * - Canvas API: drawLine, drawCircle, drawPath, drawText
 * - LinearGradient: tạo hiệu ứng gradient dưới line
 */
class ScoreChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val scores = mutableListOf<Float>()

    // Paints
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        strokeWidth = 4f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val dotBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33FFFFFF")
        strokeWidth = 1f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(8f, 8f), 0f)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#99FFFFFF")
        textSize = 28f
        textAlign = Paint.Align.RIGHT
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    /**
     * Set dữ liệu điểm số (0.0 → 1.0).
     * Gọi hàm này rồi view tự vẽ lại.
     */
    fun setScores(data: List<Float>) {
        scores.clear()
        scores.addAll(data)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (scores.isEmpty()) return

        val paddingLeft = 80f
        val paddingRight = 40f
        val paddingTop = 40f
        val paddingBottom = 60f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Vẽ grid lines (0%, 25%, 50%, 75%, 100%)
        for (i in 0..4) {
            val y = paddingTop + chartHeight * (1 - i / 4f)
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint)
            canvas.drawText("${i * 25}%", paddingLeft - 10f, y + 10f, labelPaint)
        }

        if (scores.size == 1) {
            // Chỉ có 1 điểm — vẽ dot đơn lẻ
            val x = paddingLeft + chartWidth / 2
            val y = paddingTop + chartHeight * (1 - scores[0])
            canvas.drawCircle(x, y, 12f, dotBorderPaint)
            canvas.drawCircle(x, y, 8f, dotPaint)
            canvas.drawText("${(scores[0] * 100).toInt()}%", x, y - 20f, valuePaint)
            return
        }

        // Tính toạ độ các điểm
        val stepX = chartWidth / (scores.size - 1)
        val points = scores.mapIndexed { index, score ->
            val x = paddingLeft + stepX * index
            val y = paddingTop + chartHeight * (1 - score)
            Pair(x, y)
        }

        // Vẽ gradient fill dưới line
        val fillPath = Path()
        fillPath.moveTo(points.first().first, paddingTop + chartHeight)
        points.forEach { fillPath.lineTo(it.first, it.second) }
        fillPath.lineTo(points.last().first, paddingTop + chartHeight)
        fillPath.close()

        fillPaint.shader = LinearGradient(
            0f, paddingTop, 0f, paddingTop + chartHeight,
            Color.parseColor("#664CAF50"),
            Color.parseColor("#004CAF50"),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(fillPath, fillPaint)

        // Vẽ line
        val linePath = Path()
        linePath.moveTo(points[0].first, points[0].second)
        for (i in 1 until points.size) {
            linePath.lineTo(points[i].first, points[i].second)
        }
        canvas.drawPath(linePath, linePaint)

        // Vẽ dots + labels
        points.forEachIndexed { index, (x, y) ->
            canvas.drawCircle(x, y, 10f, dotBorderPaint)
            canvas.drawCircle(x, y, 6f, dotPaint)

            // Hiển thị % trên mỗi dot
            val percent = (scores[index] * 100).toInt()
            canvas.drawText("${percent}%", x, y - 16f, valuePaint)
        }

        // Vẽ label "Lần 1, 2, 3..." ở trục X
        val xLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#99FFFFFF")
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        points.forEachIndexed { index, (x, _) ->
            canvas.drawText(
                "#${index + 1}",
                x,
                paddingTop + chartHeight + 40f,
                xLabelPaint
            )
        }
    }
}
