package com.flowart.ar.drawing.sketch.ai

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

/**
 * DrawingEvaluator — Đánh giá bản vẽ user so với template bằng OpenCV
 *
 * Sử dụng 3 metrics:
 * 1. SSIM (Structural Similarity Index): so sánh cấu trúc ảnh
 * 2. Histogram Comparison: so sánh phân bố sáng/tối
 * 3. Contour Matching (Hu Moments): so sánh hình dạng đường viền
 *
 * Tất cả metrics đều dùng hàm sẵn có của OpenCV.
 *
 * Kiến thức AI liên quan (dùng khi bảo vệ):
 * - SSIM so sánh luminance, contrast, structure — giống cách mắt người đánh giá
 * - Histogram cho biết tổng thể tone ảnh có tương đồng không
 * - Hu Moments bất biến với scale, rotation, translation
 */
class DrawingEvaluator {

    // Trọng số cho mỗi metric (tổng = 1.0)
    // Contour quan trọng nhất vì đánh giá hình dạng — yếu tố chính khi vẽ
    companion object {
        private const val WEIGHT_SSIM = 0.35f
        private const val WEIGHT_HISTOGRAM = 0.25f
        private const val WEIGHT_CONTOUR = 0.40f
    }

    /**
     * Đánh giá bản vẽ so với template.
     *
     * @param templateBitmap Ảnh mẫu (template user vẽ theo)
     * @param drawingBitmap Bản vẽ của user
     * @return EvaluationResult chứa tất cả scores và feedback
     */
    suspend fun evaluate(templateBitmap: Bitmap, drawingBitmap: Bitmap): EvaluationResult {
        return withContext(Dispatchers.Default) {
            // Chuyển Bitmap → Mat (format OpenCV)
            val templateMat = bitmapToGrayMat(templateBitmap)
            val drawingMat = bitmapToGrayMat(drawingBitmap)

            // Resize drawing cho cùng kích thước với template
            val resizedDrawing = Mat()
            Imgproc.resize(drawingMat, resizedDrawing, templateMat.size())

            // Tính 3 metrics
            val ssim = calculateSSIM(templateMat, resizedDrawing)
            val histogram = compareHistogram(templateMat, resizedDrawing)
            val contour = matchContours(templateMat, resizedDrawing)

            // Tính điểm tổng (weighted average)
            val overall =
                (ssim * WEIGHT_SSIM + histogram * WEIGHT_HISTOGRAM + contour * WEIGHT_CONTOUR)
                    .coerceIn(0f, 1f)

            // Tạo feedback text
            val feedback = generateFeedback(overall)

            // Giải phóng memory
            templateMat.release()
            drawingMat.release()
            resizedDrawing.release()

            EvaluationResult(
                ssimScore = ssim,
                histogramScore = histogram,
                contourScore = contour,
                overallScore = overall,
                feedback = feedback
            )
        }
    }

    /**
     * SSIM — Structural Similarity Index
     *
     * So sánh 2 ảnh dựa trên 3 yếu tố: luminance, contrast, structure.
     * Giá trị 0.0 (hoàn toàn khác) → 1.0 (giống hệt).
     *
     * Cách tính (simplified):
     * 1. Tính mean (μ) của mỗi ảnh
     * 2. Tính variance (σ²) của mỗi ảnh
     * 3. Tính covariance (σ₁₂) giữa 2 ảnh
     * 4. SSIM = (2μ₁μ₂ + C1)(2σ₁₂ + C2) / (μ₁² + μ₂² + C1)(σ₁² + σ₂² + C2)
     */
    private fun calculateSSIM(img1: Mat, img2: Mat): Float {
        val c1 = 6.5025   // (0.01 * 255)²
        val c2 = 58.5225  // (0.03 * 255)²

        val img1Float = Mat()
        val img2Float = Mat()
        img1.convertTo(img1Float, CvType.CV_32F)
        img2.convertTo(img2Float, CvType.CV_32F)

        // μ₁, μ₂ — mean (trung bình) qua Gaussian blur
        val mu1 = Mat()
        val mu2 = Mat()
        Imgproc.GaussianBlur(img1Float, mu1, org.opencv.core.Size(11.0, 11.0), 1.5)
        Imgproc.GaussianBlur(img2Float, mu2, org.opencv.core.Size(11.0, 11.0), 1.5)

        // μ₁², μ₂², μ₁μ₂
        val mu1Sq = Mat()
        val mu2Sq = Mat()
        val mu1Mu2 = Mat()
        Core.multiply(mu1, mu1, mu1Sq)
        Core.multiply(mu2, mu2, mu2Sq)
        Core.multiply(mu1, mu2, mu1Mu2)

        // σ₁², σ₂², σ₁₂
        val sigma1Sq = Mat()
        val sigma2Sq = Mat()
        val sigma12 = Mat()

        val img1Sq = Mat()
        val img2Sq = Mat()
        val img1Img2 = Mat()
        Core.multiply(img1Float, img1Float, img1Sq)
        Core.multiply(img2Float, img2Float, img2Sq)
        Core.multiply(img1Float, img2Float, img1Img2)

        Imgproc.GaussianBlur(img1Sq, sigma1Sq, org.opencv.core.Size(11.0, 11.0), 1.5)
        Imgproc.GaussianBlur(img2Sq, sigma2Sq, org.opencv.core.Size(11.0, 11.0), 1.5)
        Imgproc.GaussianBlur(img1Img2, sigma12, org.opencv.core.Size(11.0, 11.0), 1.5)

        Core.subtract(sigma1Sq, mu1Sq, sigma1Sq)
        Core.subtract(sigma2Sq, mu2Sq, sigma2Sq)
        Core.subtract(sigma12, mu1Mu2, sigma12)

        // SSIM formula
        // numerator = (2*mu1*mu2 + C1) * (2*sigma12 + C2)
        // denominator = (mu1^2 + mu2^2 + C1) * (sigma1^2 + sigma2^2 + C2)
        val t1 = Mat()
        val t2 = Mat()
        val t3 = Mat()

        // 2*mu1*mu2 + C1
        Core.multiply(mu1Mu2, Scalar(2.0), t1)
        Core.add(t1, Scalar(c1), t1)

        // 2*sigma12 + C2
        Core.multiply(sigma12, Scalar(2.0), t2)
        Core.add(t2, Scalar(c2), t2)

        // t3 = numerator
        Core.multiply(t1, t2, t3)

        // mu1^2 + mu2^2 + C1
        Core.add(mu1Sq, mu2Sq, t1)
        Core.add(t1, Scalar(c1), t1)

        // sigma1^2 + sigma2^2 + C2
        Core.add(sigma1Sq, sigma2Sq, t2)
        Core.add(t2, Scalar(c2), t2)

        // denominator
        val denominator = Mat()
        Core.multiply(t1, t2, denominator)

        // SSIM map
        val ssimMap = Mat()
        Core.divide(t3, denominator, ssimMap)

        val mean = Core.mean(ssimMap)
        val ssimValue = mean.`val`[0].toFloat().coerceIn(0f, 1f)

        // Cleanup
        listOf(
            img1Float, img2Float, mu1, mu2, mu1Sq, mu2Sq, mu1Mu2,
            sigma1Sq, sigma2Sq, sigma12, img1Sq, img2Sq, img1Img2,
            t1, t2, t3, denominator, ssimMap
        ).forEach { it.release() }

        return ssimValue
    }

    /**
     * Histogram Comparison — So sánh phân bố sáng/tối
     *
     * Đếm số pixel cho mỗi mức sáng (0-255) → tạo histogram → so sánh 2 histogram.
     * Dùng phương pháp CORREL (correlation): 1.0 = giống hệt, 0.0 = khác hoàn toàn.
     */
    private fun compareHistogram(img1: Mat, img2: Mat): Float {
        val histSize = MatOfInt(256)
        val ranges = MatOfFloat(0f, 256f)

        val hist1 = Mat()
        val hist2 = Mat()

        // Tính histogram cho mỗi ảnh
        Imgproc.calcHist(listOf(img1), MatOfInt(0), Mat(), hist1, histSize, ranges)
        Imgproc.calcHist(listOf(img2), MatOfInt(0), Mat(), hist2, histSize, ranges)

        // Normalize histogram
        Core.normalize(hist1, hist1, 0.0, 1.0, Core.NORM_MINMAX)
        Core.normalize(hist2, hist2, 0.0, 1.0, Core.NORM_MINMAX)

        // So sánh bằng correlation
        val result = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CORREL)

        hist1.release()
        hist2.release()

        return result.toFloat().coerceIn(0f, 1f)
    }

    /**
     * Contour Matching — So sánh hình dạng đường viền (Hu Moments)
     *
     * 1. Tìm contours (đường viền) trong cả 2 ảnh
     * 2. Tính Hu Moments — 7 giá trị mô tả hình dạng, bất biến với scale/rotation
     * 3. So sánh bằng matchShapes()
     *
     * matchShapes trả về giá trị nhỏ = giống, lớn = khác.
     * Ta chuyển về 0-1 bằng: score = 1 / (1 + distance)
     */
    private fun matchContours(img1: Mat, img2: Mat): Float {
        // Threshold để tạo ảnh nhị phân (trắng/đen)
        val thresh1 = Mat()
        val thresh2 = Mat()
        Imgproc.threshold(img1, thresh1, 127.0, 255.0, Imgproc.THRESH_BINARY_INV)
        Imgproc.threshold(img2, thresh2, 127.0, 255.0, Imgproc.THRESH_BINARY_INV)

        // Tìm contours
        val contours1 = mutableListOf<MatOfPoint>()
        val contours2 = mutableListOf<MatOfPoint>()
        Imgproc.findContours(
            thresh1,
            contours1,
            Mat(),
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )
        Imgproc.findContours(
            thresh2,
            contours2,
            Mat(),
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        if (contours1.isEmpty() || contours2.isEmpty()) {
            thresh1.release()
            thresh2.release()
            return 0f
        }

        // Lấy contour lớn nhất (chủ thể chính)
        val largest1 = contours1.maxByOrNull { Imgproc.contourArea(it) } ?: return 0f
        val largest2 = contours2.maxByOrNull { Imgproc.contourArea(it) } ?: return 0f

        // So sánh bằng Hu Moments
        val distance = Imgproc.matchShapes(largest1, largest2, Imgproc.CONTOURS_MATCH_I2, 0.0)

        // Chuyển distance → score (0-1)
        // distance nhỏ = giống → score cao
        val score = (1.0 / (1.0 + distance)).toFloat().coerceIn(0f, 1f)

        thresh1.release()
        thresh2.release()

        return score
    }

    /**
     * Chuyển Bitmap → Mat grayscale (ảnh xám)
     * Vì tất cả metrics so sánh trên ảnh xám, không cần màu.
     */
    private fun bitmapToGrayMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        val bmp = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        Utils.bitmapToMat(bmp, mat)
        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGBA2GRAY)
        mat.release()
        return grayMat
    }

    /**
     * Tạo feedback text dựa trên điểm tổng.
     */
    private fun generateFeedback(score: Float): String {
        val percent = (score * 100).toInt()
        return when {
            percent >= 90 -> "🌟 Xuất sắc! Bản vẽ gần như hoàn hảo!"
            percent >= 75 -> "👏 Rất tốt! Bạn đã nắm bắt được hầu hết chi tiết."
            percent >= 60 -> "👍 Khá tốt! Tiếp tục luyện tập để cải thiện."
            percent >= 40 -> "💪 Tạm được! Hãy chú ý hơn vào đường nét và tỷ lệ."
            else -> "📝 Cần cải thiện. Hãy thử vẽ chậm hơn và theo sát template."
        }
    }
}
