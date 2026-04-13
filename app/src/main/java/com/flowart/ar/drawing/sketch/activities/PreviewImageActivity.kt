package com.flowart.ar.drawing.sketch.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.ai.BackgroundRemover
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
    private var isShowingRemovedBg = false          // Đang hiển thị ảnh đã xóa nền?
    private var removedBgBitmapUri: String? =
        null   // URI của ảnh đã xóa nền (để truyền sang activity khác)

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
        // Ảnh từ assets (templates có sẵn) thường đã sạch nền rồi
        binding.btnRemoveBg.isVisible = imageUri != null
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
     * Flow: lấy bitmap → gọi BackgroundRemover.analyze() (1 lần) → applyThreshold() → hiển thị
     */
    private fun removeBackground() {
        val uri = imageUri ?: return

        // Hiện loading, ẩn text nút
        binding.progressAi.visible()
        binding.btnRemoveBg.text = ""
        binding.btnRemoveBg.isEnabled = false

        lifecycleScope.launch {
            // Lấy bitmap từ URI (chạy trên IO thread)
            val originalBitmap = withContext(Dispatchers.IO) {
                BitmapUtils.getBitmapFromUri(this@PreviewImageActivity, Uri.parse(uri))
            }

            if (originalBitmap == null) {
                showError("Không thể đọc ảnh")
                resetRemoveBgButton()
                return@launch
            }

            // Bước 1: Gọi AI analyze (chỉ chạy 1 lần — tốn thời gian nhất)
            val success = backgroundRemover.analyze(originalBitmap)

            if (success) {
                // Bước 2: Áp mask với threshold mặc định 0.5
                val threshold = binding.seekBarThreshold.progress / 100f
                val resultBitmap = backgroundRemover.applyThreshold(threshold)

                if (resultBitmap != null) {
                    // Hiển thị ảnh đã xóa nền
                    binding.ivPreview.setImageBitmap(resultBitmap)
                    isShowingRemovedBg = true

                    // Lưu cache
                    saveToCacheAndUpdateUri(resultBitmap)

                    // Hiện slider để user điều chỉnh
                    binding.layoutThreshold.visible()

                    // Đổi text nút
                    binding.btnRemoveBg.text = "🔄 Show Original"
                    binding.btnRemoveBg.isEnabled = true
                    binding.progressAi.gone()
                } else {
                    showError("AI không thể xóa nền ảnh này")
                    resetRemoveBgButton()
                }
            } else {
                showError("AI không thể phân tích ảnh này")
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
        backgroundRemover.close()  // Giải phóng tài nguyên ML Kit
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
}