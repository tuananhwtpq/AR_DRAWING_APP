package com.flowart.ar.drawing.sketch.activities

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityPermissionBinding
import com.flowart.ar.drawing.sketch.utils.PermissionUtils

class PermissionActivity :
    BaseActivity<ActivityPermissionBinding>(ActivityPermissionBinding::inflate) {

    private var currentRequestingPermission: String? = null
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            updateUiPermission()

            if (!isGranted && currentRequestingPermission != null) {
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    currentRequestingPermission!!
                )

                if (!shouldShowRationale) {
                    //showToast(getString(R.string.please_grant_permission_to_use_this_feature))

                    openAppSetting()
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        openAppSetting()
//                    }, 1500)
                    //showPermissionDialog()
                }
            }
            currentRequestingPermission = null
        }


    private val goToSetting =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            updateUiPermission()
        }

    override fun initData() {

    }

    override fun initView() {
        binding.tvMessage.text = getString(
            R.string.for_the_best_experience_ar_drawing_needs_access_to_the_following_permissions,
            getString(R.string.ar_drawing)
        )
//        binding.layoutCamera.isSelected = PermissionUtils.checkCameraPermission(this)
//        binding.layoutMicrophone.isSelected = PermissionUtils.checkRecordAudioPermission(this)
        updateUiPermission()
    }

    override fun initActionView() {
        binding.layoutCamera.setOnClickListener {
            if (PermissionUtils.checkCameraPermission(this)) {
                openAppSetting()
            } else {
                currentRequestingPermission = Manifest.permission.CAMERA
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.layoutMicrophone.setOnClickListener {
            if (PermissionUtils.checkRecordAudioPermission(this)) {
                openAppSetting()
            } else {
                currentRequestingPermission = Manifest.permission.RECORD_AUDIO
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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

    private fun updateUiPermission() {
        val isCameraGranted = PermissionUtils.checkCameraPermission(this)
        val isMicGranted = PermissionUtils.checkRecordAudioPermission(this)

        binding.layoutCamera.isSelected = isCameraGranted
        binding.switchCamera.isSelected = isCameraGranted

        binding.layoutMicrophone.isSelected = isMicGranted
        binding.switchMicrophone.isSelected = isMicGranted
    }

    private fun openAppSetting() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            goToSetting.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onResume() {
        super.onResume()
        updateUiPermission()
        showNativeAds()
        loadAndShowNativeOther_2(binding.frNative)
    }

//        private fun showPermissionDialog() {
//            androidx.appcompat.app.AlertDialog.Builder(this)
//                .setTitle("Permission required")
//                .setMessage(getString(R.string.please_grant_permission_to_use_this_feature))
//                .setPositiveButton("Go to Setting") { _, _ ->
//                    openAppSetting()
//                }
//                .setNegativeButton("Cancel", null)
//                .show()
//        }

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