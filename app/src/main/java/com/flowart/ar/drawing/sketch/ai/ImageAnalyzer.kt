package com.flowart.ar.drawing.sketch.ai

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * ImageAnalyzer — Nhận diện nội dung ảnh bằng AI (ML Kit Image Labeling)
 *
 * Cách hoạt động:
 * 1. Đưa ảnh Bitmap vào ML Kit Image Labeling
 * 2. Model MobileNet (chạy on-device) phân tích ảnh
 * 3. Trả về danh sách labels: VD ["Dog" 95%, "Animal" 89%, "Pet" 82%]
 *
 * Kiến thức AI liên quan (dùng khi bảo vệ):
 * - Image Classification: phân loại ảnh thuộc category nào
 * - MobileNet: lightweight CNN thiết kế cho mobile (ít tham số, chạy nhanh)
 * - Confidence Score: mỗi label có điểm 0.0 → 1.0 thể hiện độ chắc chắn
 * - Top-K Results: chỉ lấy K labels có confidence cao nhất
 */
class ImageAnalyzer {

    // Tạo ML Kit Image Labeler
    // confidenceThreshold = 0.5 → chỉ trả về labels có confidence >= 50%
    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
    )

    /**
     * Kết quả nhận diện 1 label.
     * @param label Tên vật thể (VD: "Dog", "Flower", "Car")
     * @param confidence Độ tự tin 0.0 → 1.0 (VD: 0.95 = 95%)
     */
    data class DetectedLabel(
        val label: String,
        val confidence: Float
    ) {
        /** Hiển thị đẹp: "🐱 Cat — 95%" */
        fun toDisplayString(): String {
            val percent = (confidence * 100).toInt()
            return "$label — $percent%"
        }
    }

    /**
     * Phân tích ảnh và trả về danh sách labels.
     *
     * @param bitmap Ảnh cần phân tích
     * @return Danh sách labels sắp xếp theo confidence giảm dần, hoặc empty nếu thất bại
     */
    suspend fun analyze(bitmap: Bitmap): List<DetectedLabel> {
        return withContext(Dispatchers.Default) {
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val labels = labeler.process(inputImage).await()

                labels.map { label ->
                    DetectedLabel(
                        label = label.text,
                        confidence = label.confidence
                    )
                }.sortedByDescending { it.confidence }  // Sắp xếp cao → thấp

            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    fun close() {
        labeler.close()
    }
}
