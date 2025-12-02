package com.example.baseproject.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivitySketchBinding
import com.example.baseproject.fragments.DrawGuideDialog
import com.example.baseproject.models.LessonModel
import com.example.baseproject.models.SketchImage
import com.example.baseproject.utils.BitmapUtils
import com.example.baseproject.utils.Constants
import com.example.baseproject.utils.MyCameraManager
import com.example.baseproject.utils.SharedPrefManager
import com.example.baseproject.utils.enumz.SketchEffect
import com.example.baseproject.utils.formatTime
import com.example.baseproject.utils.gone
import com.example.baseproject.utils.invisible
import com.example.baseproject.utils.setOnUnDoubleClick
import com.example.baseproject.utils.showToast
import com.example.baseproject.utils.sticker.DrawableSticker
import com.example.baseproject.utils.sticker.Sticker
import com.example.baseproject.utils.visible
import com.ssquad.ar.drawing.sketch.db.ImageRepositories
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.OpenCVLoader

class SketchActivity : BaseActivity<ActivitySketchBinding>(ActivitySketchBinding::inflate) {

    private var isPhoto = true
        set(value) {
            field = value
            binding.tvPhoto.alpha = if (value) 1f else 0.5f
            binding.tvVideo.alpha = if (value) 0.5f else 1f
            binding.vIndicatorPhoto.isVisible = value
            binding.vIndicatorVideo.isVisible = !value
            binding.btnCaptureImage.isVisible = value
            binding.btnRecord.isVisible = !value
        }
    private var totalStep = 1
        set(value) {
            field = value
            binding.tvStep.text = getString(R.string.step_num, currentStep, totalStep)
        }

    private var isOriginal = false
        set(value) {
            field = value
            //binding.stickerView.isVisible = !value
            binding.btnStroke.isSelected = !value
            binding.btnOriginal.isSelected = value
        }

    private var lesson: LessonModel? = null

    private var listImage = listOf<String>()

    private val lessonId by lazy {
        intent.getIntExtra(Constants.KEY_LESSON_ID, 0)
    }

    private var currentStep = 1
        set(value) {
            field = value
            binding.btnPrevStep.isEnabled = value > 1
            binding.btnPrevStep.alpha = if (value > 1) 1f else 0.6f
            binding.btnNextStep.isEnabled = value < totalStep
            binding.btnNextStep.alpha = if (value < totalStep) 1f else 0.6f
            binding.tvStep.text = getString(R.string.step_num, currentStep, totalStep)
            updateView()
        }

    private var isRecording: Boolean = false
        set(value) {
            field = value
            if (value) {
                binding.lCamera.isVisible = false
            }
            if (value) startElapsedTimeUpdate() else stopElapsedTimeUpdate()
        }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
//            loadAndShowInterBack(binding.vShowInterAds) {
//                finish()
//            }
            finish()
        }
    }

    private var startTime = System.currentTimeMillis()

    private var recordTime = 0L
        set(value) {
            field = value
            binding.tvRecordTime.text = value.formatTime()
        }

    private val image by lazy {
        intent.getStringExtra(Constants.KEY_IMAGE_PATH)
    }

    private val imageUri by lazy {
        intent.getStringExtra(Constants.KEY_IMAGE_URI)
    }

    private val isFromLesson by lazy {
        intent.getBooleanExtra(Constants.IS_FROM_LESSON, false)
    }

    private val cameraManager by lazy {
        MyCameraManager(this)
    }

    private var startTimeMillis: Long = 0
    private var elapsedTimeHandler: Handler? = null
    private var elapsedTimeRunnable: Runnable? = null

    private var bitmapOrigin: Bitmap? = null
    private var sketchImage: SketchImage? = null
    private var currentEffect: SketchEffect? = null
    private var currentThickness: Int = 50

    override fun initData() {

        if (OpenCVLoader.initDebug()) {
            Log.d("SketchActivity", "OpenCV loaded successfully")
        } else {
            Log.e("SketchActivity", "OpenCV initialization failed!")
        }

        cameraManager.startCamera(binding.viewFinder)
        setListener()

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        if (SharedPrefManager.getBoolean("first_sketch", true)) {
            SharedPrefManager.putBoolean("first_sketch", false)
            DrawGuideDialog().init(true).show(supportFragmentManager, "DrawGuideDialog")
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        binding.lStep.isVisible = isFromLesson
        isOriginal = false
        setTemplateView()
        binding.lShowOriginal.isVisible = !isFromLesson
        initBlurOpacity(this)
        initBlurCamera(this)
        initBlurRecording(this)
        initBlurAllOpacity(this)
        updateFilterSelection(binding.btnOriginal)
    }

    override fun initActionView() {

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnOpacity.setOnClickListener {
            it.isSelected = !it.isSelected
            binding.lOpacity.isVisible = it.isSelected
            binding.lCamera.isVisible = false
            binding.btnCamera.isSelected = false
            setupBottomColor()
        }

        binding.btnCamera.setOnClickListener {
            it.isSelected = !it.isSelected
            binding.btnOpacity.isSelected = false
            if (!isRecording) {
                binding.lCamera.isVisible = it.isSelected
            } else {
                binding.lRecording.isVisible = it.isSelected
            }
            binding.lOpacity.isVisible = false
            setupBottomColor()
        }

        binding.btnLock.setOnClickListener {
            it.isSelected = !it.isSelected
            setupBottomColor()
        }

        binding.btnFlash.setOnClickListener {
            it.isSelected = !it.isSelected
            setupBottomColor()
            cameraManager.enableFlash(it.isSelected)

        }


        binding.btnCaptureImage.setOnUnDoubleClick {
            binding.btnCaptureImage.isEnabled = false
            cameraManager.captureImage(onError = { binding.btnCaptureImage.isEnabled = true }) {
//                showInterDone {
//                    recordTime = 0
//                    binding.tvRecordTime.gone()
//                    val intent = Intent(this, SketchResultActivity::class.java).apply {
//                        putExtra("media_uri", it.toString())
//                        putExtra("isImage", true)
//                    }
//                    startActivity(intent)
//                }

                recordTime = 0
                binding.tvRecordTime.gone()
                val intent = Intent(this, SketchResultActivity::class.java).apply {
                    putExtra("media_uri", it.toString())
                    putExtra("isImage", true)
                }
                startActivity(intent)

            }
        }

        binding.tvPhoto.setOnClickListener {
            isPhoto = true
        }

        binding.tvVideo.setOnClickListener {
            isPhoto = false
        }

        binding.btnRecord.setOnUnDoubleClick {
            cameraManager.recordVideo {
//                showInterDone {
//                    binding.lRecording.gone()
//                    binding.ivPause.visible()
//                    binding.ivResumeRecord.gone()
//                    isRecording = false
//                    val intent = Intent(this, SketchResultActivity::class.java).apply {
//                        putExtra("media_uri", it.toString())
//                        putExtra("isImage", false)
//                    }
//                    startActivity(intent)
//                }

                binding.lRecording.gone()
                binding.ivPause.visible()
                binding.ivResumeRecord.gone()
                isRecording = false
                val intent = Intent(this, SketchResultActivity::class.java).apply {
                    putExtra("media_uri", it.toString())
                    putExtra("isImage", false)
                }
                startActivity(intent)
            }
        }

        binding.ivPause.setOnClickListener {
            cameraManager.pauseRecord()
        }

        binding.ivResumeRecord.setOnClickListener {
            cameraManager.resumeRecord()
        }

        binding.ivStopAndSave.setOnClickListener {
            recordTime = 0
            isRecording = false
            cameraManager.stopRecord()
            binding.tvRecordTime.gone()
            binding.lCamera.isVisible = binding.btnCamera.isSelected
        }

        binding.btnSave.setOnUnDoubleClick {
            lesson?.let {
                if (!it.isDone) {
                    lifecycleScope.launch {
                        ImageRepositories.INSTANCE.markDone(it.id)
                    }
                    it.isDone = true
                }
            }
//            loadAndShowInterBack(binding.vShowInterAds) {
//                val intent = Intent(this, MainActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//            }
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }

        binding.seekBarOpacity.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                currentThickness = seekBar?.progress ?: 50
                if (currentEffect == null) {
                    binding.stickerView.alpha = currentThickness / 100f
                    binding.stickerView.invalidate()
                } else {
                    applyEffect(currentEffect, currentThickness)
                }
            }
        })


        binding.btnStroke.setOnClickListener {
            isOriginal = false
            currentEffect = SketchEffect.STROKE_ONLY
            updateFilterSelection(binding.btnStroke)
            applyEffect(currentEffect, currentThickness)


        }

        binding.btnOriginal.setOnClickListener {
            isOriginal = true
            currentEffect = null
            updateFilterSelection(binding.btnOriginal)
            applyEffect(null, 100)
        }

        binding.btnOriginToSket.setOnClickListener {
            isOriginal = false
            currentEffect = SketchEffect.ORIGINAL_TO_SKETCH
            updateFilterSelection(binding.btnOriginToSket)
            applyEffect(currentEffect, currentThickness)
        }

        binding.btnGrayToSket.setOnClickListener {
            isOriginal = false
            currentEffect = SketchEffect.GRAY_TO_SKETCH
            updateFilterSelection(binding.btnGrayToSket)
            applyEffect(currentEffect, currentThickness)
        }

        binding.btnGrayToSoftSket.setOnClickListener {
            isOriginal = false
            currentEffect = SketchEffect.GRAY_TO_SOFT_SKETCH
            updateFilterSelection(binding.btnGrayToSoftSket)
            applyEffect(currentEffect, currentThickness)
        }

        binding.btnNextStep.setOnClickListener {
            currentStep += 1
        }

        binding.btnPrevStep.setOnClickListener {
            currentStep -= 1
        }

        binding.btnLock.setOnClickListener {
            it.isSelected = true
            binding.vLock.visible()
            binding.lLockBottom.visible()
        }

        binding.btnUnLock.setOnClickListener {
            binding.btnLock.isSelected = false
            binding.vLock.gone()
            binding.lLockBottom.gone()
        }

//        binding.seekBarOpacity.onProgressChange { progress ->
//            binding.stickerView.alpha = progress / 100f
//        }
    }

    private fun setupBottomColor() {

//        if (imageView.isSelected){
//            frame.visible()
//            textView.setTextColor(resources.getColor(R.color.mainTextColor))
//        } else{
//            frame.invisible()
//            textView.setTextColor(resources.getColor(R.color.unSelectedText))
//        }
        //frame.visibility = if (imageView.isSelected) View.VISIBLE else View.INVISIBLE

        binding.bottomNavOpacity.visibility =
            if (binding.btnOpacity.isSelected) View.VISIBLE else View.INVISIBLE
        binding.bottomNavCamera.visibility =
            if (binding.btnCamera.isSelected) View.VISIBLE else View.INVISIBLE
        binding.bottomNavLock.visibility =
            if (binding.btnLock.isSelected) View.VISIBLE else View.INVISIBLE
        binding.bottomNavFlash.visibility =
            if (binding.btnFlash.isSelected) View.VISIBLE else View.INVISIBLE

        if (binding.btnOpacity.isSelected) {
            binding.navOpacity.setTextColor(resources.getColor(R.color.mainTextColor))
        } else {
            binding.navOpacity.setTextColor(resources.getColor(R.color.unSelectedText))
        }

        if (binding.btnCamera.isSelected) {
            binding.navCamera.setTextColor(resources.getColor(R.color.mainTextColor))
        } else {
            binding.navCamera.setTextColor(resources.getColor(R.color.unSelectedText))
        }

        if (binding.btnLock.isSelected) {
            binding.navLock.setTextColor(resources.getColor(R.color.mainTextColor))
        } else {
            binding.navLock.setTextColor(resources.getColor(R.color.unSelectedText))
        }

        if (binding.btnFlash.isSelected) {
            binding.navFlash.setTextColor(resources.getColor(R.color.mainTextColor))
        } else {
            binding.navFlash.setTextColor(resources.getColor(R.color.unSelectedText))
        }
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        showNativeColl()
    }

    override fun onPause() {
        cameraManager.enableFlash(false)
        val timeSpent = SharedPrefManager.getLong(Constants.KEY_SPENT_TIME, 0L)
        SharedPrefManager.putLong(
            Constants.KEY_SPENT_TIME,
            timeSpent + System.currentTimeMillis() - startTime
        )
        binding.btnFlash.isSelected = false
        binding.lRecording.gone()
        binding.tvRecordTime.gone()
        binding.lCamera.isVisible = binding.btnCamera.isSelected
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.vShowInterAds.gone()
        binding.btnCaptureImage.isEnabled = true
        recordTime = 0
        isRecording = false
    }

    private fun setTemplateView() {
        if (isFromLesson) {
            lifecycleScope.launch {
                lesson = ImageRepositories.INSTANCE.getLessonById(lessonId)
                if (lesson != null) {
                    listImage = lesson!!.listStep
                    totalStep = lesson!!.listStep.size
                    addImageAsset(lesson!!.listStep[currentStep - 1])
                }
                withContext(Dispatchers.Main) {
                    binding.lLoading.gone()
                    binding.blurOpacityAll.invisible()
                    currentStep = 1
                }
            }
        } else {
            if (imageUri != null) {
                addImage(Uri.parse(imageUri))
            } else {
                if (image != null) {
                    addImageAsset(image!!)
                }
            }
            binding.lLoading.gone()
        }
    }

    private fun setListener() {
        cameraManager.listener = object : MyCameraManager.RecordListener {
            override fun onStartRecord() {
                binding.tvRecordTime.visible()
                isRecording = true
                binding.lRecording.visible()
                startTimeMillis = System.currentTimeMillis()
            }

            override fun onResumeRecord() {
                binding.ivResumeRecord.gone()
                binding.ivPause.visible()
                isRecording = true
            }

            override fun onPauseRecord() {
                binding.ivPause.gone()
                binding.ivResumeRecord.visible()
                isRecording = false
            }

            override fun onFinalizeWithError() {
                binding.lRecording.gone()
                binding.tvRecordTime.gone()
                isRecording = false
                recordTime = 0
            }

            override fun onFinalizeWithSuccess() {
                binding.lRecording.gone()
                binding.tvRecordTime.gone()
                isRecording = false
                recordTime = 0
            }
        }
    }

    private fun startElapsedTimeUpdate() {
        elapsedTimeHandler = Handler(Looper.getMainLooper())
        elapsedTimeRunnable = object : Runnable {
            override fun run() {
                if (isRecording) {
                    recordTime += 1000
                    elapsedTimeHandler?.postDelayed(this, 1000)
                }
            }
        }
        elapsedTimeHandler?.post(elapsedTimeRunnable!!)
    }

    private fun stopElapsedTimeUpdate() {
        elapsedTimeHandler?.removeCallbacks(elapsedTimeRunnable!!)
    }

    private fun addImage(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = BitmapUtils.getBitmapFromUri(this@SketchActivity, uri)
                bitmapOrigin = bitmap

                if (bitmap != null) {
                    sketchImage = SketchImage(bitmap)
                }

                val drawable = BitmapDrawable(resources, bitmap)
                withContext(Dispatchers.Main) {
                    binding.stickerView.removeAllStickers()
                    binding.stickerView.addSticker(
                        DrawableSticker(drawable),
                        Sticker.Position.CENTER
                    )
                    isOriginal = true
                    updateFilterSelection(binding.btnOriginal)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.has_error_now))
                }
            }
        }
    }

    private fun addImageAsset(path: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = BitmapUtils.getBitmapFromAsset(this@SketchActivity, path)
                bitmapOrigin = bitmap

                if (bitmap != null) {
                    sketchImage = SketchImage(bitmap)
                }

                val drawable = BitmapDrawable(resources, bitmap)
                withContext(Dispatchers.Main) {
                    binding.stickerView.removeAllStickers()
                    binding.stickerView.addSticker(
                        DrawableSticker(drawable),
                        Sticker.Position.CENTER
                    )
                    isOriginal = true
                    updateFilterSelection(binding.btnOriginal)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.has_error_now))
                }
            }
        }
    }

    private fun updateView() {
        val bitmap =
            BitmapUtils.getBitmapFromAsset(this@SketchActivity, listImage[currentStep - 1])
        bitmap?.let {
            val drawable = BitmapDrawable(resources, it)
            binding.stickerView.currentSticker =
                binding.stickerView.currentSticker?.setDrawable(drawable)
            binding.stickerView.invalidate()
        }
    }

    private fun applyEffect(effect: SketchEffect?, thickness: Int) {
        if (sketchImage == null || bitmapOrigin == null) return

        binding.lLoading.visible()

        lifecycleScope.launch(Dispatchers.Default) {

            val rawBitmap = if (effect == null) {
                bitmapOrigin
            } else {
                sketchImage?.getImageAs(effect, thickness)
            }

            val resultBitmap =
                if (effect != null && rawBitmap != null && effect == SketchEffect.STROKE_ONLY) {
                    removeWhiteBackground(rawBitmap)
                } else {
                    rawBitmap
                }

//            val resultBitmap = if (effect == null) {
//                bitmapOrigin
//            } else {
//                sketchImage?.getImageAs(effect, thickness)
//            }

            withContext(Dispatchers.Main) {

                if (effect != null) {
                    binding.stickerView.alpha = 0.5f
                } else {
                    binding.stickerView.alpha = binding.seekBarOpacity.progress / 100f
                }

                resultBitmap?.let { bmp ->
                    val drawable = BitmapDrawable(resources, bmp)

                    val currentSticker = binding.stickerView.currentSticker
                    if (currentSticker != null && currentSticker is DrawableSticker) {
                        currentSticker.setDrawable(drawable)
                        //currentSticker.realDrawable = drawable
                        binding.stickerView.invalidate()
                    } else {
                        binding.stickerView.addSticker(
                            DrawableSticker(drawable),
                            Sticker.Position.CENTER
                        )
                    }
                }
                binding.lLoading.gone()
            }
        }
    }

    private fun initBlurOpacity(context: Context) {
        val blurView = binding.blurOpacity
        val radius = 25f

        blurView.setupWith(binding.root)
            .setBlurAlgorithm(RenderScriptBlur(context))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)
            .setOverlayColor(Color.parseColor("#CCFFFFFF"))

        blurView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val cornerRadius = 8 * view.resources.displayMetrics.density
                outline.setRoundRect(
                    0,
                    0,
                    view.width,
                    (view.height + cornerRadius).toInt(),
                    cornerRadius
                )
            }
        }
        blurView.clipToOutline = true
    }

    private fun initBlurCamera(context: Context) {
        val blurView = binding.blurCamera
        val radius = 25f

        blurView.setupWith(binding.root)
            .setBlurAlgorithm(RenderScriptBlur(context))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)
            .setOverlayColor(Color.parseColor("#CCFFFFFF"))

        blurView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val cornerRadius = 8 * view.resources.displayMetrics.density
                outline.setRoundRect(
                    0,
                    0,
                    view.width,
                    (view.height + cornerRadius).toInt(),
                    cornerRadius
                )
            }
        }
        blurView.clipToOutline = true
    }

    private fun initBlurAllOpacity(context: Context) {
        val blurView = binding.blurOpacityAll
        val radius = 25f

        blurView.setupWith(binding.root)
            .setBlurAlgorithm(RenderScriptBlur(context))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)
            .setOverlayColor(Color.parseColor("#CCFFFFFF"))

        blurView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val cornerRadius = 8 * view.resources.displayMetrics.density
                outline.setRoundRect(
                    0,
                    0,
                    view.width,
                    view.height,
                    cornerRadius
                )
            }
        }
        blurView.clipToOutline = true
    }

    private fun updateFilterSelection(selectedView: TextView) {
        val filters = listOf(
            binding.btnOriginal,
            binding.btnStroke,
            binding.btnOriginToSket,
            binding.btnGrayToSket,
            binding.btnGrayToSoftSket
        )

        filters.forEach { view ->
            if (view == selectedView) {
                view.setBackgroundResource(R.drawable.stroke_bg)
                view.alpha = 1.0f
            } else {
                view.background = null
                view.alpha = 0.5f
            }
        }
    }


    private fun initBlurRecording(context: Context) {
//        val blurView = binding.blurRecording
//        val radius = 25f
//
//        blurView.setupWith(binding.root)
//            .setBlurAlgorithm(RenderScriptBlur(context))
//            .setBlurRadius(radius)
//            .setBlurAutoUpdate(true)
//            .setOverlayColor(Color.parseColor("#CCFFFFFF"))
//
//        blurView.outlineProvider = object : ViewOutlineProvider() {
//            override fun getOutline(view: View, outline: Outline) {
//                val cornerRadius = 8 * view.resources.displayMetrics.density
//                outline.setRoundRect(
//                    0,
//                    0,
//                    view.width,
//                    (view.height + cornerRadius).toInt(),
//                    cornerRadius)
//            }
//        }
//        blurView.clipToOutline = true
    }

    private fun removeWhiteBackground(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]

            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            val brightness = (r + g + b) / 3
            if (brightness > 230) {
                pixels[i] = Color.TRANSPARENT
            } else {
                pixels[i] = pixel
            }
        }
        newBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return newBitmap
    }


    private fun showInterDone(navAction: () -> Unit) {
//        if (AdsManager.isShowInterDone()) {
//            loadAndShowInterWithNativeAfter(AdsManager.interOtherModel, binding.vShowInterAds) {
//                navAction()
//            }
//        } else {
//            navAction()
//        }
    }

    private fun showNativeColl() {
//        when (RemoteConfig.remoteNativeCollapsibleSketch) {
//            0L -> return
//            1L -> {
//                binding.viewLine.invisible()
//                binding.frNative.visible()
//                AdmobLib.loadAndShowNative(
//                    this,
//                    AdsManager.nativeOtherModel,
//                    binding.frBanner,
//                    size = GoogleENative.UNIFIED_SMALL_LIKE_BANNER,
//                    layout = R.layout.native_ads_custom_small_like_banner,
//                    onAdsLoadFail = {
//                        binding.viewLine.gone()
//                    }
//                )
//            }
//
//            2L -> {
//                binding.frNative.visible()
//                binding.frBanner.visible()
//                binding.viewLine.invisible()
//                AdmobLib.loadAndShowNativeCollapsibleSingle(
//                    this,
//                    AdsManager.nativeOtherModel,
//                    viewGroupExpanded = binding.frNative,
//                    viewGroupCollapsed = binding.frBanner,
//                    layoutExpanded = R.layout.native_ads_custom_medium_bottom,
//                    layoutCollapsed = R.layout.native_ads_custom_small_like_banner,
//                    onAdsLoadFail = {
//                        binding.viewLine.gone()
//                    }
//                )
//            }
//        }
    }
}