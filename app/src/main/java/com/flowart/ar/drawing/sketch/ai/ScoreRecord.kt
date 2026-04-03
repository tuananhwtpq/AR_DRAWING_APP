package com.flowart.ar.drawing.sketch.ai

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ScoreRecord — Lưu lịch sử điểm đánh giá bản vẽ.
 *
 * Mỗi record = 1 lần user vẽ xong + được AI chấm điểm.
 * Dùng Room DB để persist dữ liệu qua các phiên.
 */
@Entity(tableName = "score_history")
data class ScoreRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Tên template (hoặc đường dẫn ảnh) */
    val templateName: String,

    /** Điểm SSIM (0.0 → 1.0) */
    val ssimScore: Float,

    /** Điểm Histogram (0.0 → 1.0) */
    val histogramScore: Float,

    /** Điểm Contour (0.0 → 1.0) */
    val contourScore: Float,

    /** Điểm tổng (0.0 → 1.0) */
    val overallScore: Float,

    /** Timestamp (milliseconds) */
    val timestamp: Long = System.currentTimeMillis()
) {
    val overallPercent: Int get() = (overallScore * 100).toInt()
}
