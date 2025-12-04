package com.flowart.ar.drawing.sketch.activities

import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityPermissionBinding
import com.flowart.ar.drawing.sketch.utils.PermissionUtils

class PermissionActivity :
    BaseActivity<ActivityPermissionBinding>(ActivityPermissionBinding::inflate) {
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            binding.layoutCamera.isSelected = PermissionUtils.checkCameraPermission(this)
            binding.layoutMicrophone.isSelected = PermissionUtils.checkRecordAudioPermission(this)
        }

    private val goToSetting =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            binding.layoutCamera.isSelected = PermissionUtils.checkCameraPermission(this)
            binding.layoutMicrophone.isSelected = PermissionUtils.checkRecordAudioPermission(this)
        }

    override fun initData() {

    }

    override fun initView() {
        binding.tvMessage.text = getString(
            R.string.for_the_best_experience_ar_drawing_needs_access_to_the_following_permissions,
            getString(R.string.ar_drawing)
        )
        binding.layoutCamera.isSelected = PermissionUtils.checkCameraPermission(this)
        binding.layoutMicrophone.isSelected = PermissionUtils.checkRecordAudioPermission(this)
    }

    override fun initActionView() {
        binding.layoutCamera.setOnClickListener {
            if (!PermissionUtils.checkCameraPermission(this)) {
                PermissionUtils.requestPermission(
                    this,
                    Manifest.permission.CAMERA,
                    permissionLauncher,
                    goToSetting
                )
            }
        }

        binding.layoutMicrophone.setOnClickListener {
            if (!PermissionUtils.checkRecordAudioPermission(this)) {
                PermissionUtils.requestPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO,
                    permissionLauncher,
                    goToSetting
                )
            }
        }

        binding.btnGrantLater.setOnClickListener {
            finish()
        }

        binding.btnContinue.setOnClickListener {
            if (PermissionUtils.checkRecordAudioPermission(this) && PermissionUtils.checkCameraPermission(
                    this
                )
            ) {
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.please_grant_permission_to_use_this_feature),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnContinue.isSelected = true
        binding.btnGrantLater.isSelected = true
        binding.tvCamera.isSelected = true
        binding.tvMicrophone.isSelected = true

    }

    override fun onResume() {
        super.onResume()
        showNativeAds()
    }

    private fun showNativeAds() {
//        if (RemoteConfig.remoteNativePermission == 0L) return
//        binding.frNative.visible()
//        AdmobLib.loadAndShowNative(
//            this,
//            AdsManager.nativeOtherModel,
//            binding.frNative,
//            size = GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON,
//            layout = R.layout.native_ads_lesson
//        )
    }
}