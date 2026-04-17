package com.flowart.ar.drawing.sketch.activities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.ai.DrawingEvaluator
import com.flowart.ar.drawing.sketch.ai.EvaluationResult
import com.flowart.ar.drawing.sketch.ai.HeatmapGenerator
import com.flowart.ar.drawing.sketch.ai.ScoreDao
import com.flowart.ar.drawing.sketch.ai.ScoreDatabase
import com.flowart.ar.drawing.sketch.ai.ScoreRecord
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityResultBinding
import com.flowart.ar.drawing.sketch.utils.BitmapUtils
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.ads.RemoteConfig
import com.flowart.ar.drawing.sketch.utils.formatDateTime
import com.flowart.ar.drawing.sketch.utils.gone
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick
import com.flowart.ar.drawing.sketch.utils.showToast
import com.flowart.ar.drawing.sketch.utils.visible
import com.snake.squad.adslib.AdmobLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.OpenCVLoader
import java.io.OutputStream

class ResultActivity : BaseActivity<ActivityResultBinding>(ActivityResultBinding::inflate) {

    private val evaluator = DrawingEvaluator()
    private val heatmapGenerator = HeatmapGenerator()
    private var heatmapBitmap: Bitmap? = null
    private var isShowingHeatmap = false
    private lateinit var scoreDao: ScoreDao

    private var isOpenCvReady = false

    override fun initData() {
        if (bitmap == null) finish()

        // Khởi tạo OpenCV
        try {
            isOpenCvReady = OpenCVLoader.initLocal()
            if (!isOpenCvReady) {
                Log.w(TAG, "OpenCV initialization failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "OpenCV init error: ${e.message}", e)
            isOpenCvReady = false
        }

        // Khởi tạo Score DB
        scoreDao = ScoreDatabase.getInstance(this).scoreDao()
    }

    override fun initView() {
        Glide.with(this).load(bitmap).into(binding.ivPreview)

        // Tự động chạy AI Evaluation nếu có cả template, drawing, và OpenCV
        if (bitmap != null && templateBitmap != null && isOpenCvReady) {
            runEvaluation()
        }
    }

    override fun initActionView() {
        binding.btnDownload.setOnClickListener {
            saveBitmap(bitmap)
        }

        binding.btnShare.setOnClickListener {
            shareBitmap(bitmap)
        }

        binding.backHome.setOnUnDoubleClick {
            loadAndShowInterBackHome(binding.vShowInterAds) {
                SharedPrefManager.putBoolean("wantShowRate", true)
                gotoMain()
            }
        }

        // Heatmap toggle button
        binding.btnToggleHeatmap.setOnClickListener {
            toggleHeatmap()
        }

        // Nút Vẽ lại — quay lại để user thử lại
        binding.btnRedraw.setOnClickListener {
            finish() // Quay về TraceActivity
        }
    }

    /**
     * Chạy AI Evaluation so sánh bản vẽ với template.
     */
    private fun runEvaluation() {
        val drawing = bitmap ?: return
        val template = templateBitmap ?: return

        binding.progressEvaluation.visible()

        lifecycleScope.launch {
            try {
                // Chạy evaluation (SSIM + Histogram + Contour)
                val result = evaluator.evaluate(template, drawing)

                // Tạo heatmap
                try {
                    heatmapBitmap = heatmapGenerator.generate(template, drawing)
                } catch (e: Exception) {
                    Log.e(TAG, "Heatmap generation failed: ${e.message}", e)
                    // Heatmap fail không ảnh hưởng điểm số
                }

                // Cập nhật UI với animation
                binding.progressEvaluation.gone()

                // Fade-in animation cho evaluation section
                val fadeIn = AlphaAnimation(0f, 1f).apply {
                    duration = 500
                    fillAfter = true
                }
                binding.layoutEvaluation.startAnimation(fadeIn)
                binding.layoutEvaluation.visible()

                // Animate điểm tổng (0% → final%)
                animateScore(result.overallPercent)

                binding.tvFeedback.text = result.feedback

                // Animate từng progress bar với màu sắc
                animateProgressBar(binding.progressSsim, result.ssimPercent)
                animateProgressBar(binding.progressHistogram, result.histogramPercent)
                animateProgressBar(binding.progressContour, result.contourPercent)

                binding.btnToggleHeatmap.isVisible = heatmapBitmap != null
                binding.btnRedraw.visible()

                // Lưu điểm vào Room DB
                saveScore(result)

                // Hiện thống kê
                showStats()

            } catch (e: Exception) {
                Log.e(TAG, "Evaluation failed: ${e.message}", e)
                binding.progressEvaluation.gone()
                // Không hiện lỗi cho user — chỉ ẩn evaluation section
            }
        }
    }

    /**
     * Toggle hiển thị heatmap / ảnh bản vẽ gốc
     */
    private fun toggleHeatmap() {
        if (isShowingHeatmap) {
            // Quay về ảnh gốc
            Glide.with(this).load(bitmap).into(binding.ivPreview)
            binding.btnToggleHeatmap.text = "🔥 Xem Heatmap"
            binding.ivHeatmap.gone()
            isShowingHeatmap = false
        } else {
            // Hiện heatmap
            heatmapBitmap?.let {
                binding.ivHeatmap.setImageBitmap(it)
                binding.ivHeatmap.visible()
                binding.btnToggleHeatmap.text = "🖼️ Xem bản vẽ"
                isShowingHeatmap = true
            }
        }
    }

    /**
     * Animate điểm tổng: đếm từ 0% lên giá trị cuối.
     * Tạo hiệu ứng "wow" — user thấy con số chạy lên dần.
     */
    private fun animateScore(targetPercent: Int) {
        val animator = ValueAnimator.ofInt(0, targetPercent)
        animator.duration = 1200 // 1.2 giây
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            binding.tvOverallScore.text = "✨ $value%"
        }
        animator.start()
    }

    /**
     * Animate progress bar: fill từ 0 → target với màu sắc theo mức:
     * 🟢 ≥ 75% = Xanh lá (tốt)
     * 🟡 ≥ 50% = Vàng cam (trung bình)
     * 🔴 < 50% = Đỏ (cần cải thiện)
     */
    private fun animateProgressBar(progressBar: ProgressBar, targetPercent: Int) {
        // Tô màu theo mức
        val color = when {
            targetPercent >= 75 -> android.graphics.Color.parseColor("#4CAF50") // Xanh lá
            targetPercent >= 50 -> android.graphics.Color.parseColor("#FF9800") // Cam
            else -> android.graphics.Color.parseColor("#F44336")                // Đỏ
        }
        progressBar.progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)

        // Animate fill
        val animator = ObjectAnimator.ofInt(progressBar, "progress", 0, targetPercent)
        animator.duration = 1000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    /**
     * Lưu điểm vào Room DB.
     */
    private fun saveScore(result: EvaluationResult) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val record = ScoreRecord(
                    templateName = templateName ?: "Unknown",
                    ssimScore = result.ssimScore,
                    histogramScore = result.histogramScore,
                    contourScore = result.contourScore,
                    overallScore = result.overallScore
                )
                scoreDao.insert(record)
                Log.d(TAG, "Score saved: ${result.overallPercent}%")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save score: ${e.message}", e)
            }
        }
    }

    /**
     * Hiện thống kê từ lịch sử: điểm cao nhất, trung bình, tổng số lần vẽ.
     */
    private fun showStats() {
        lifecycleScope.launch {
            try {
                val best = withContext(Dispatchers.IO) { scoreDao.getHighestScore() }
                val avg = withContext(Dispatchers.IO) { scoreDao.getAverageScore() }
                val total = withContext(Dispatchers.IO) { scoreDao.getTotalDrawings() }

                if (total > 0) {
                    binding.layoutStats.visible()
                    binding.tvStatsBest.text = "🏆 Điểm cao nhất: ${((best ?: 0f) * 100).toInt()}%"
                    binding.tvStatsAvg.text = "📈 Điểm trung bình: ${((avg ?: 0f) * 100).toInt()}%"
                    binding.tvStatsTotal.text = "🎨 Tổng số lần vẽ: $total"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load stats: ${e.message}", e)
            }
        }
    }

    private fun gotoMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onDestroy() {
        bitmap = null
        templateBitmap = null
        templateName = null
        heatmapBitmap = null
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        loadAndShowNativeOther()
    }

    companion object {
        private const val TAG = "ResultActivity"
        var bitmap: Bitmap? = null
        var templateBitmap: Bitmap? = null  // Template cho AI Evaluation
        var templateName: String? = null    // Tên template cho score history
    }

    private fun saveBitmap(bitmap: Bitmap?) {
        if (bitmap == null) {
            showToast(getString(R.string.has_error_now))
            return
        }

        lifecycleScope.launch {
            val imageOutStream: OutputStream?
            val cv = ContentValues()
            val name = System.currentTimeMillis().formatDateTime()
            cv.put(MediaStore.Images.Media.DISPLAY_NAME, "ar_drawing_$name.png")
            cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            cv.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_DCIM + "/AR_DRAWING"
            )
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
            try {
                imageOutStream = contentResolver.openOutputStream(uri!!)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream!!)
                imageOutStream.close()
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.image_save_success))
                }

                SharedPrefManager.putInt(
                    Constants.KEY_DRAW_NUMBER,
                    SharedPrefManager.getInt(Constants.KEY_DRAW_NUMBER, 0) + 1
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.has_error_now))
                }
                e.printStackTrace()
            }
        }

    }

    private fun shareBitmap(bitmap: Bitmap?) {
        if (bitmap == null) {
            Toast.makeText(this, getString(R.string.has_error_now), Toast.LENGTH_SHORT).show()
            return
        }

        BitmapUtils.shareBitmap(this, bitmap)
    }

    fun loadAndShowNativeOther() {
        when (RemoteConfig.remoteNativeOther) {
            1L -> {
                binding.frNativeSmall.visible()
                binding.frNativeExpand.visible()
                AdmobLib.loadAndShowNativeCollapsibleSingle(
                    activity = this,
                    admobNativeModel = AdsManager.NATIVE_OTHER,
                    viewGroupExpanded = binding.frNativeExpand,
                    viewGroupCollapsed = binding.frNativeSmall,
                    layoutExpanded = R.layout.native_ads_custom_medium_bottom,
                    layoutCollapsed = R.layout.native_ads_custom_small_like_banner,
                    onAdsLoaded = {
                        binding.whiteLine.visible()
                    },
                    onAdsLoadFail = {
                        binding.whiteLine.gone()
                    }
                )
            }
        }
    }
}