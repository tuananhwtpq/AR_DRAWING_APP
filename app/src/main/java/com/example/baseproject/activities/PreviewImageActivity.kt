package com.example.baseproject.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityPreviewImageBinding
import com.example.baseproject.utils.Constants
import com.example.baseproject.utils.PermissionUtils
import com.example.baseproject.utils.gone
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.GoogleENative
import com.ssquad.ar.drawing.sketch.db.ImageRepositories

class PreviewImageActivity :
    BaseActivity<ActivityPreviewImageBinding>(ActivityPreviewImageBinding::inflate) {
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            //loadAndShowInterBack(binding.vShowInterAds) { finish() }
            finish()
        }
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
            showInterStart {
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
        }

        binding.btnSketch.setOnClickListener {
            showInterStart {
                if (PermissionUtils.checkCameraPermission(this) && PermissionUtils.checkRecordAudioPermission(
                        this
                    )
                ) {
                    gotoSketchActivity()
                } else {
                    val intent = Intent(this, PermissionActivity::class.java)
                    launcher.launch(intent)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showNativeAds()
    }

    override fun onStop() {
        super.onStop()
        binding.vShowInterAds.gone()
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

    private fun showNativeAds() {
//        if (RemoteConfig.remoteNativePreview == 0L) return
//        binding.frNative.visible()
//        AdmobLib.loadAndShowNative(
//            this,
//            AdsManager.nativeOtherModel,
//            binding.frNative,
//            size = GoogleENative.UNIFIED_MEDIUM,
//            layout = R.layout.native_ads_custom_medium_bottom
//        )
    }

    private fun showInterStart(navAction: () -> Unit) {
//        if (AdsManager.isShowInterStart()) {
//            loadAndShowInterWithNativeAfter(AdsManager.interOtherModel, binding.vShowInterAds) {
//                navAction()
//            }
//        } else {
//            navAction()
//        }
//    }
    }
}