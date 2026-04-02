package com.flowart.ar.drawing.sketch.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.ai.BackgroundRemover
import com.flowart.ar.drawing.sketch.ai.CategoryMapper
import com.flowart.ar.drawing.sketch.ai.ImageAnalyzer
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityPreviewImageBinding
import com.flowart.ar.drawing.sketch.fragments.DrawGuideDialog
import com.flowart.ar.drawing.sketch.utils.BitmapUtils
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.PermissionUtils
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.gone
import com.flowart.ar.drawing.sketch.utils.visible
import com.google.android.material.chip.Chip
import com.snake.squad.adslib.AdmobLib
import com.ssquad.ar.drawing.sketch.db.ImageRepositories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreviewImageActivity :
    BaseActivity<ActivityPreviewImageBinding>(ActivityPreviewImageBinding::inflate) {
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isFromHome || isFromGallery) {
                loadAndShowInterBackHome(binding.vShowInterAds) {
                    finish()
                }
            } else {
                SharedPrefManager.putBoolean("wantShowRate", true)
                finish()
            }
        }
    }

    private val isFromHome by lazy {
        intent.getBooleanExtra("isFromHome", false)
    }

    private val isFromGallery by lazy {
        intent.getBooleanExtra("isFromGallery", false)
    }
    private val image by lazy {
        intent.getStringExtra(Constants.KEY_IMAGE_PATH)
    }

    private val isFromLesson by lazy {
        intent.getBooleanExtra(Constants.IS_FROM_LESSON, false)
    }

    private val lessonId by lazy {
        intent.getIntExtra(Constants.KEY_LESSON_ID, 0)
    }

    private val imageUri by lazy {
        intent.getStringExtra(Constants.KEY_IMAGE_URI)
    }

    private val isFromCategoryDetail by lazy {
        intent.getBooleanExtra("isFromCategoryDetail", false)
    }


    private val imageId by lazy {
        intent.getIntExtra("imageId", -1)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                gotoSketchActivity()
            }
        }

    // === AI Features ===
    private val backgroundRemover = BackgroundRemover()
    private val imageAnalyzer = ImageAnalyzer()
    private var isShowingRemovedBg = false
    private var removedBgBitmapUri: String? = null
    private var suggestedCategoryId: Int = -1
    private var suggestedCategoryName: Int = -1  // String resource ID

    override fun initData() {

    }

    override fun initView() {
        if (imageUri != null) {
            Glide.with(this).load(imageUri).into(binding.ivPreview)
        } else {
            Glide.with(this).load("file:///android_asset/$image").into(binding.ivPreview)
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        // Chỉ hiện nút AI Remove BG khi user chọn ảnh từ gallery (có imageUri)
        binding.btnRemoveBg.isVisible = imageUri != null

        // Tự động chạy AI nhận diện khi có ảnh từ gallery
        if (imageUri != null) {
            analyzeImage()
        }
    }

    override fun initActionView() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnTrace.setOnClickListener {
            loadAndShowInterPreview {
                goToTraceActivity()
            }
        }

        binding.btnSketch.setOnClickListener {

            loadAndShowInterPreview {
                if (PermissionUtils.checkCameraPermission(this) &&
                    PermissionUtils.checkRecordAudioPermission(this)
                ) {
                    gotoSketchActivity()
                } else {
                    val intent = Intent(this, PermissionActivity::class.java)
                    launcher.launch(intent)
                }
            }
        }

        binding.ivInfo.setOnClickListener {
            DrawGuideDialog().init(true).show(supportFragmentManager, "DrawGuideDialog")
        }

        // === AI Remove Background Button ===
        binding.btnRemoveBg.setOnClickListener {
            if (isShowingRemovedBg) {
                // Đang xem ảnh đã xóa nền → bấm lại để xem ảnh gốc
                showOriginalImage()
            } else {
                // Chạy AI xóa nền
                removeBackground()
            }
        }

        // === Slider điều chỉnh threshold ===
        binding.seekBarThreshold.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser && backgroundRemover.hasAnalyzed()) {
                    // Chuyển progress (0-100) thành threshold (0.0-1.0)
                    val threshold = progress / 100f
                    // Áp mask với threshold mới — rất nhanh, không chạy lại AI
                    val result = backgroundRemover.applyThreshold(threshold)
                    result?.let { binding.ivPreview.setImageBitmap(it) }
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                // Khi user thả tay → lưu cache ảnh mới để truyền sang activity khác
                if (backgroundRemover.hasAnalyzed()) {
                    val threshold = (seekBar?.progress ?: 50) / 100f
                    val result = backgroundRemover.applyThreshold(threshold)
                    result?.let { saveToCacheAndUpdateUri(it) }
                }
            }
        })
    }

    /**
     * Chạy AI xóa nền ảnh.
     * Flow: lấy bitmap → resize nếu quá lớn → gọi BackgroundRemover.analyze() → applyThreshold() → hiển thị
     */
    private fun removeBackground() {
        val uri = imageUri ?: return

        // Hiện loading, ẩn text nút
        binding.progressAi.visible()
        binding.btnRemoveBg.text = ""
        binding.btnRemoveBg.isEnabled = false

        lifecycleScope.launch {
            try {
                // Lấy bitmap từ URI (chạy trên IO thread)
                val originalBitmap = withContext(Dispatchers.IO) {
                    BitmapUtils.getBitmapFromUri(this@PreviewImageActivity, Uri.parse(uri))
                }

                if (originalBitmap == null) {
                    showError("Không thể đọc ảnh")
                    resetRemoveBgButton()
                    return@launch
                }

                // Resize nếu ảnh quá lớn để tránh OOM
                val resizedBitmap = BitmapUtils.resizeBitmapForAI(originalBitmap)
                Log.d(
                    TAG,
                    "Remove BG: original=${originalBitmap.width}x${originalBitmap.height}, resized=${resizedBitmap.width}x${resizedBitmap.height}"
                )

                // Bước 1: Gọi AI analyze
                val success = backgroundRemover.analyze(resizedBitmap)

                if (success) {
                    // Bước 2: Áp mask với threshold mặc định
                    val threshold = binding.seekBarThreshold.progress / 100f
                    val resultBitmap = backgroundRemover.applyThreshold(threshold)

                    if (resultBitmap != null) {
                        binding.ivPreview.setImageBitmap(resultBitmap)
                        isShowingRemovedBg = true
                        saveToCacheAndUpdateUri(resultBitmap)
                        binding.layoutThreshold.visible()
                        binding.btnRemoveBg.text = "🔄 Show Original"
                        binding.btnRemoveBg.isEnabled = true
                        binding.progressAi.gone()
                    } else {
                        showError("AI không thể xóa nền ảnh này")
                        resetRemoveBgButton()
                    }
                } else {
                    showError("AI không thể phân tích ảnh này. Kiểm tra kết nối mạng (lần đầu cần tải model).")
                    resetRemoveBgButton()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in removeBackground: ${e.message}", e)
                showError("Lỗi khi xóa nền: ${e.localizedMessage ?: "Lỗi không xác định"}")
                resetRemoveBgButton()
            }
        }
    }

    /**
     * Lưu bitmap vào cache file và cập nhật URI.
     */
    private fun saveToCacheAndUpdateUri(bitmap: Bitmap) {
        lifecycleScope.launch {
            val cachedUri = withContext(Dispatchers.IO) {
                BitmapUtils.createCacheFile(this@PreviewImageActivity, bitmap)
            }
            removedBgBitmapUri = cachedUri.toString()
        }
    }

    /**
     * Hiển thị lại ảnh gốc (khi user muốn so sánh)
     */
    private fun showOriginalImage() {
        Glide.with(this).load(imageUri).into(binding.ivPreview)
        isShowingRemovedBg = false
        binding.btnRemoveBg.text = "✨ AI Remove Background"
        binding.layoutThreshold.gone()  // Ẩn slider khi xem ảnh gốc
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun resetRemoveBgButton() {
        binding.progressAi.gone()
        binding.btnRemoveBg.text = "✨ AI Remove Background"
        binding.btnRemoveBg.isEnabled = true
    }

    override fun onResume() {
        super.onResume()
        loadAndShowNativeOtherMedium(binding.frNative)
    }

    override fun onStop() {
        super.onStop()
        binding.vShowInterAds.gone()
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundRemover.close()
        imageAnalyzer.close()
    }

    // ===========================================
    // === AI Object Detection & Smart Suggest ===
    // ===========================================

    /**
     * Tự động phân tích ảnh khi user chọn từ gallery.
     * Chạy ngầm (không hiện loading) — kết quả hiện ra nhẹ nhàng bên dưới ảnh.
     */
    private fun analyzeImage() {
        val uri = imageUri ?: return

        lifecycleScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapUtils.getBitmapFromUri(this@PreviewImageActivity, Uri.parse(uri))
                } ?: return@launch

                // Resize nếu ảnh quá lớn
                val resizedBitmap = BitmapUtils.resizeBitmapForAI(bitmap, 1024)

                // Gọi AI nhận diện
                val labels = imageAnalyzer.analyze(resizedBitmap)

                if (labels.isNotEmpty()) {
                    showDetectedLabels(labels)

                    val suggestion = CategoryMapper.findBestCategory(labels)
                    if (suggestion != null) {
                        showCategorySuggestion(suggestion)
                    }
                }
            } catch (e: Exception) {
                // Không hiện lỗi cho user vì đây là tính năng chạy ngầm
                Log.e(TAG, "Error in analyzeImage: ${e.message}", e)
            }
        }
    }

    /**
     * Hiển thị các labels AI nhận diện dưới dạng chips với animation.
     * Mỗi chip xuất hiện lần lượt (staggered fade-in).
     */
    private fun showDetectedLabels(labels: List<ImageAnalyzer.DetectedLabel>) {
        binding.chipGroupLabels.removeAllViews()
        binding.chipGroupLabels.visible()

        // Chỉ hiển tối đa 5 labels
        labels.take(5).forEachIndexed { index, detectedLabel ->
            val emoji = getEmojiForLabel(detectedLabel.label)
            val chip = Chip(this).apply {
                text = "$emoji ${detectedLabel.toDisplayString()}"
                isClickable = false
                setTextColor(getColor(R.color.mainTextColor))
                alpha = 0f // Bắt đầu ẩn
            }
            binding.chipGroupLabels.addView(chip)

            // Staggered fade-in: mỗi chip delay 100ms
            chip.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay((index * 100).toLong())
                .start()
        }
    }

    /**
     * Trả về emoji phù hợp với label nhận diện.
     */
    private fun getEmojiForLabel(label: String): String {
        val lower = label.lowercase()
        return when {
            lower.contains("cat") || lower.contains("kitten") -> "🐱"
            lower.contains("dog") || lower.contains("puppy") -> "🐶"
            lower.contains("bird") -> "🐦"
            lower.contains("fish") -> "🐟"
            lower.contains("animal") || lower.contains("pet") -> "🐾"
            lower.contains("flower") || lower.contains("plant") || lower.contains("rose") -> "🌸"
            lower.contains("tree") || lower.contains("forest") -> "🌳"
            lower.contains("car") || lower.contains("vehicle") -> "🚗"
            lower.contains("food") || lower.contains("fruit") -> "🍎"
            lower.contains("person") || lower.contains("people") || lower.contains("face") -> "👤"
            lower.contains("building") || lower.contains("house") -> "🏠"
            lower.contains("sky") || lower.contains("cloud") -> "☁️"
            lower.contains("water") || lower.contains("sea") || lower.contains("ocean") -> "🌊"
            lower.contains("mountain") -> "⛰️"
            lower.contains("art") || lower.contains("drawing") || lower.contains("paint") -> "🎨"
            else -> "🏷️"
        }
    }

    /**
     * Hiển gợi ý category + xử lý navigate.
     * VD: "🐾 Xem thêm templates Animal? [Xem →]"
     */
    private fun showCategorySuggestion(suggestion: CategoryMapper.SuggestedCategory) {
        binding.layoutSuggestion.visible()
        binding.tvSuggestion.text =
            "${suggestion.emoji} Xem thêm templates ${suggestion.displayName}?"

        // Lưu category info để navigate
        suggestedCategoryId = suggestion.categoryId
        suggestedCategoryName = when (suggestion.categoryId) {
            1 -> R.string.anime
            2 -> R.string.cartoon
            3 -> R.string.animal
            4 -> R.string.chibi
            5 -> R.string.flower
            else -> R.string.animal
        }

        binding.btnViewCategory.setOnClickListener {
            val intent = Intent(this, CategoryDetailActivity::class.java)
            intent.putExtra("type", suggestedCategoryId)
            intent.putExtra("categoryName", suggestedCategoryName)
            startActivity(intent)
        }
    }

    private fun goToTraceActivity() {
        val intent = Intent(this, TraceActivity::class.java)
        intent.putExtra(Constants.KEY_IMAGE_PATH, image)
        // Nếu đang dùng ảnh đã xóa nền → truyền URI đã xóa nền
        intent.putExtra(
            Constants.KEY_IMAGE_URI,
            if (isShowingRemovedBg) removedBgBitmapUri else imageUri
        )
        intent.putExtra(Constants.IS_FROM_LESSON, isFromLesson)
        intent.putExtra(Constants.KEY_LESSON_ID, lessonId)
        startActivity(intent)
        finish()
        if (imageId != -1) {
            ImageRepositories.INSTANCE.addToRecent(imageId)
        }
    }

    private fun gotoSketchActivity() {
        val intent = Intent(this, SketchActivity::class.java)
        intent.putExtra(Constants.KEY_IMAGE_PATH, image)
        intent.putExtra(Constants.IS_FROM_LESSON, isFromLesson)
        intent.putExtra(Constants.KEY_LESSON_ID, lessonId)
        // Nếu đang dùng ảnh đã xóa nền → truyền URI đã xóa nền
        intent.putExtra(
            Constants.KEY_IMAGE_URI,
            if (isShowingRemovedBg) removedBgBitmapUri else imageUri
        )
        if (imageId != -1) {
            ImageRepositories.INSTANCE.addToRecent(imageId)
        }
        startActivity(intent)
        finish()
    }

    fun loadAndShowInterPreview(navAction: () -> Unit) {
        if (AdsManager.isShowInterSketchTracePreview()) {
            AdmobLib.loadAndShowInterWithNativeAfter(
                mActivity = this,
                interModel = AdsManager.INTER_SKETCH_TRACE_PREVIEW,
                nativeModel = AdsManager.NATIVE_FULL_SCREEN_AFTER_INTER,
                vShowInterAds = binding.vShowInterAds,
                isShowNativeAfter = AdsManager.isShowNativeFullScreen(),
                nativeLayout = R.layout.native_ads_full_screen,
                navAction = { navAction() },
                onInterCloseOrFailed = { if (it) AdsManager.updateTime() }
            )
        } else {
            navAction()
        }
    }

    companion object {
        private const val TAG = "PreviewImageActivity"
    }
}