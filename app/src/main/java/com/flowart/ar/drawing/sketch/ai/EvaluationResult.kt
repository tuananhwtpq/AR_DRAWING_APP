package com.flowart.ar.drawing.sketch.ai

/**
 * EvaluationResult — Kết quả đánh giá bản vẽ
 *
 * Chứa điểm số từ 3 metrics + điểm tổng + feedback text.
 * Được tạo bởi DrawingEvaluator và hiển thị trong ResultActivity.
 */
data class EvaluationResult(
    /** Điểm SSIM (0.0 → 1.0): độ tương đồng cấu trúc */
    val ssimScore: Float,

    /** Điểm Histogram (0.0 → 1.0): độ tương đồng phân bố sáng/tối */
    val histogramScore: Float,

    /** Điểm Contour (0.0 → 1.0): độ tương đồng hình dạng đường viền */
    val contourScore: Float,

    /** Điểm tổng (0.0 → 1.0): weighted average của 3 metrics */
    val overallScore: Float,

    /** Feedback text cho user */
    val feedback: String
) {
    /** Điểm tổng dưới dạng phần trăm (0 → 100) */
    val overallPercent: Int get() = (overallScore * 100).toInt()
    val ssimPercent: Int get() = (ssimScore * 100).toInt()
    val histogramPercent: Int get() = (histogramScore * 100).toInt()
    val contourPercent: Int get() = (contourScore * 100).toInt()
}
