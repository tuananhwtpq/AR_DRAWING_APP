package com.flowart.ar.drawing.sketch.ai

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * BackgroundRemover — Xóa nền ảnh bằng AI (ML Kit Subject Segmentation)
 *
 * Cách hoạt động:
 * 1. Đưa ảnh Bitmap vào ML Kit → chạy AI inference (chỉ 1 lần)
 * 2. ML Kit trả về "mask" — mỗi pixel có giá trị 0.0 (nền) → 1.0 (chủ thể)
 * 3. Lưu mask lại → user điều chỉnh threshold bằng slider
 * 4. Áp mask với threshold mới → kết quả thay đổi ngay (không cần chạy lại AI)
 *
 * Kiến thức AI liên quan (dùng khi bảo vệ):
 * - Semantic Segmentation: phân loại từng pixel thuộc class nào
 * - U-Net architecture: model encoder-decoder chuyên dùng cho segmentation
 * - Confidence Mask: mỗi pixel có giá trị confidence thể hiện "chắc chắn" bao nhiêu
 * - On-device inference: model chạy trực tiếp trên điện thoại, không cần server
 */
class BackgroundRemover {

    private val segmenter = SubjectSegmentation.getClient(
        SubjectSegmenterOptions.Builder()
            .enableForegroundConfidenceMask()
            .build()
    )

    // Lưu lại mask và ảnh gốc sau lần chạy AI đầu tiên
    // để user thay đổi threshold mà không cần chạy lại model
    private var cachedMask: FloatArray? = null
    private var cachedOriginalBitmap: Bitmap? = null

    /**
     * Chạy AI để phân tích ảnh và lấy mask.
     * Chỉ cần gọi 1 lần duy nhất cho mỗi ảnh.
     *
     * @return true nếu thành công, false nếu thất bại
     */
    suspend fun analyze(bitmap: Bitmap): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val result = segmenter.process(inputImage).await()
                val maskBuffer = result.foregroundConfidenceMask ?: return@withContext false

                // Lưu mask vào FloatArray (copy từ FloatBuffer)
                val maskArray = FloatArray(maskBuffer.remaining())
                maskBuffer.get(maskArray)

                cachedMask = maskArray
                cachedOriginalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Áp mask đã lưu với threshold mới.
     * Gọi được nhiều lần mà KHÔNG cần chạy lại AI — rất nhanh (~10-50ms).
     *
     * @param threshold Ngưỡng 0.0 → 1.0
     *   - threshold THẤP (0.1-0.3): giữ nhiều pixel hơn → ít bị mất chủ thể
     *   - threshold CAO (0.7-0.9): xóa aggressive hơn → nền sạch hơn nhưng có thể mất chi tiết
     *   - threshold MẶC ĐỊNH (0.5): cân bằng
     * @return Bitmap đã xóa nền, hoặc null nếu chưa gọi analyze()
     */
    fun applyThreshold(threshold: Float): Bitmap? {
        val mask = cachedMask ?: return null
        val original = cachedOriginalBitmap ?: return null

        val width = original.width
        val height = original.height

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        original.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            if (i < mask.size && mask[i] < threshold) {
                pixels[i] = Color.TRANSPARENT
            }
        }

        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return resultBitmap
    }

    /**
     * Shortcut: analyze + applyThreshold trong 1 lần gọi.
     */
    suspend fun removeBackground(bitmap: Bitmap, threshold: Float = 0.5f): Bitmap? {
        val success = analyze(bitmap)
        if (!success) return null
        return applyThreshold(threshold)
    }

    /**
     * Kiểm tra đã chạy analyze chưa (đã có mask chưa)
     */
    fun hasAnalyzed(): Boolean = cachedMask != null

    fun close() {
        segmenter.close()
        cachedMask = null
        cachedOriginalBitmap = null
    }
}
