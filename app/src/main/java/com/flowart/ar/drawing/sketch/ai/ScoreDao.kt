package com.flowart.ar.drawing.sketch.ai

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * ScoreDao — Data Access Object cho score history.
 *
 * Cung cấp các query để:
 * - Lưu điểm mới
 * - Lấy lịch sử (mới nhất trước)
 * - Lấy điểm cao nhất
 * - Đếm tổng số lần vẽ
 */
@Dao
interface ScoreDao {

    /** Lưu 1 record điểm mới */
    @Insert
    suspend fun insert(record: ScoreRecord)

    /** Lấy tất cả lịch sử, mới nhất trước */
    @Query("SELECT * FROM score_history ORDER BY timestamp DESC")
    suspend fun getAll(): List<ScoreRecord>

    /** Lấy N bản ghi gần nhất */
    @Query("SELECT * FROM score_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ScoreRecord>

    /** Lấy điểm cao nhất từ trước tới nay */
    @Query("SELECT MAX(overallScore) FROM score_history")
    suspend fun getHighestScore(): Float?

    /** Lấy điểm trung bình */
    @Query("SELECT AVG(overallScore) FROM score_history")
    suspend fun getAverageScore(): Float?

    /** Đếm tổng số lần vẽ */
    @Query("SELECT COUNT(*) FROM score_history")
    suspend fun getTotalDrawings(): Int

    /** Xóa tất cả lịch sử */
    @Query("DELETE FROM score_history")
    suspend fun deleteAll()
}
