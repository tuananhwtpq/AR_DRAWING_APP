package com.flowart.ar.drawing.sketch.activities

import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityPreviewImageBinding
import com.flowart.ar.drawing.sketch.fragments.DrawGuideDialog
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.PermissionUtils
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.gone
import com.snake.squad.adslib.AdmobLib
import com.ssquad.ar.drawing.sketch.db.ImageRepositories

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

    override fun initData() {

    }

    override fun initView() {
        if (imageUri != null) {
            Glide.with(this).load(imageUri).into(binding.ivPreview)
        } else {
            Glide.with(this).load("file:///android_asset/$image").into(binding.ivPreview)
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
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
    }

    override fun onResume() {
        super.onResume()
        loadAndShowNativeOtherMedium(binding.frNative)
    }

    override fun onStop() {
        super.onStop()
        binding.vShowInterAds.gone()
    }

    private fun goToTraceActivity() {
        val intent = Intent(this, TraceActivity::class.java)
        intent.putExtra(Constants.KEY_IMAGE_PATH, image)
        intent.putExtra(Constants.KEY_IMAGE_URI, imageUri)
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
        intent.putExtra(Constants.KEY_IMAGE_URI, imageUri)
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