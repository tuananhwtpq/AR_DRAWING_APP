package com.flowart.ar.drawing.sketch.ai

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * HeatmapGenerator — Tạo bản đồ nhiệt so sánh bản vẽ vs template
 *
 * Cách hoạt động:
 * 1. absdiff(template, drawing) → tạo ảnh "khác biệt"
 * 2. applyColorMap(diff, COLORMAP_JET) → tô màu:
 *    - Xanh dương = giống (pixel khác biệt ít)
 *    - Vàng = gần giống
 *    - Đỏ = khác nhiều (pixel khác biệt lớn)
 * 3. Overlay lên bản vẽ user với độ trong suốt ~50%
 */
class HeatmapGenerator {

    /**
     * Tạo heatmap overlay.
     *
     * @param templateBitmap Ảnh mẫu
     * @param drawingBitmap Bản vẽ user
     * @return Bitmap heatmap overlay, hoặc null nếu lỗi
     */
    suspend fun generate(templateBitmap: Bitmap, drawingBitmap: Bitmap): Bitmap? {
        return withContext(Dispatchers.Default) {
            try {
                // Chuyển sang grayscale Mat
                val templateMat = bitmapToGrayMat(templateBitmap)
                val drawingMat = bitmapToGrayMat(drawingBitmap)

                // Resize cho cùng kích thước
                val resizedDrawing = Mat()
                Imgproc.resize(drawingMat, resizedDrawing, templateMat.size())

                // Bước 1: Tính absolute difference
                val diffMat = Mat()
                Core.absdiff(templateMat, resizedDrawing, diffMat)

                // Bước 2: Áp color map (JET: xanh → vàng → đỏ)
                val heatmapMat = Mat()
                Imgproc.applyColorMap(diffMat, heatmapMat, Imgproc.COLORMAP_JET)

                // Bước 3: Chuyển drawing sang màu để overlay
                val drawingColor = Mat()
                Imgproc.cvtColor(resizedDrawing, drawingColor, Imgproc.COLOR_GRAY2BGR)

                // Bước 4: Blend heatmap + drawing (50/50)
                val blended = Mat()
                Core.addWeighted(drawingColor, 0.5, heatmapMat, 0.5, 0.0, blended)

                // Chuyển Mat → Bitmap
                val resultBitmap = Bitmap.createBitmap(
                    blended.cols(), blended.rows(), Bitmap.Config.ARGB_8888
                )
                Imgproc.cvtColor(blended, blended, Imgproc.COLOR_BGR2RGBA)
                Utils.matToBitmap(blended, resultBitmap)

                // Cleanup
                templateMat.release()
                drawingMat.release()
                resizedDrawing.release()
                diffMat.release()
                heatmapMat.release()
                drawingColor.release()
                blended.release()

                resultBitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun bitmapToGrayMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        val bmp = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        Utils.bitmapToMat(bmp, mat)
        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGBA2GRAY)
        mat.release()
        return grayMat
    }
}
