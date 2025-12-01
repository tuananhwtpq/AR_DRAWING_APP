package com.example.baseproject.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivitySketchBinding
import com.example.baseproject.models.LessonModel
import com.example.baseproject.utils.BitmapUtils
import com.example.baseproject.utils.Constants
import com.example.baseproject.utils.MyCameraManager
import com.example.baseproject.utils.SharedPrefManager
import com.example.baseproject.utils.formatTime
import com.example.baseproject.utils.gone
import com.example.baseproject.utils.onProgressChange
import com.example.baseproject.utils.setOnUnDoubleClick
import com.example.baseproject.utils.sticker.DrawableSticker
import com.example.baseproject.utils.sticker.Sticker
import com.example.baseproject.utils.visible
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.GoogleENative
import com.ssquad.ar.drawing.sketch.db.ImageRepositories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SketchActivity : BaseActivity<ActivitySketchBinding>(ActivitySketchBinding::inflate) {

    private var isPhoto = true
        set(value) {
            field = value
            binding.tvPhoto.setTextColor(Color.parseColor(if (value) "#29313D" else "#B2B2BD"))
            binding.tvVideo.setTextColor(Color.parseColor(if (!value) "#29313D" else "#B2B2BD"))
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
            binding.stickerView.isVisible = !value
            binding.btnStroke.isSelected = !value
            binding.btnStroke.setTextColor(Color.parseColor(if (value) "#B2B2BD" else "#FFFFFF"))
            binding.btnOriginal.isSelected = value
            binding.btnOriginal.setTextColor(Color.parseColor(if (value) "#FFFFFF" else "#B2B2BD"))
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

    override fun initData() {
        cameraManager.startCamera(binding.viewFinder)
        setListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        if (SharedPrefManager.getBoolean("first_sketch", true)) {
            SharedPrefManager.putBoolean("first_sketch", false)
            //DrawGuideDialog().init(true).show(supportFragmentManager, "DrawGuideDialog")
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        binding.lStep.isVisible = isFromLesson
        isOriginal = false
        setTemplateView()
        binding.lShowOriginal.isVisible = !isFromLesson
    }

    override fun initActionView() {

        Log.d("SketchActivity", "image path: $image")
        Log.d("SketchActivity", "image uri: $imageUri")


        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnOpacity.setOnClickListener {
            it.isSelected = !it.isSelected
            binding.lOpacity.isVisible = it.isSelected
            binding.lCamera.isVisible = false
            binding.btnCamera.isSelected = false
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
        }

        binding.btnLock.setOnClickListener {
            it.isSelected = !it.isSelected
        }

        binding.btnFlash.setOnClickListener {
            it.isSelected = !it.isSelected
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

        binding.btnStroke.setOnClickListener {
            isOriginal = false
        }

        binding.btnOriginal.setOnClickListener {
            isOriginal = true
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

        binding.seekBarOpacity.onProgressChange { progress ->
            binding.stickerView.alpha = progress / 100f
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
//        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = BitmapUtils.getBitmapFromUri(this@SketchActivity, uri)
                val drawable = BitmapDrawable(resources, bitmap)
                withContext(Dispatchers.Main) {
                    binding.stickerView.addSticker(
                        DrawableSticker(drawable),
                        Sticker.Position.CENTER
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SketchActivity,
                        getString(R.string.has_error_now),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addImageAsset(path: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = BitmapUtils.getBitmapFromAsset(this@SketchActivity, path)
                val drawable = BitmapDrawable(resources, bitmap)
                withContext(Dispatchers.Main) {
                    binding.stickerView.addSticker(
                        DrawableSticker(drawable),
                        Sticker.Position.CENTER
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SketchActivity,
                        getString(R.string.has_error_now),
                        Toast.LENGTH_SHORT
                    ).show()
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