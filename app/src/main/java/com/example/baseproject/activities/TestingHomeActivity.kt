//package com.example.baseproject.activities
//
//import android.Manifest
//import android.content.Intent
//import android.net.Uri
//import android.os.Environment
//import android.util.Log
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.core.content.FileProvider
//import androidx.lifecycle.lifecycleScope
//import com.example.baseproject.bases.BaseActivity
//import com.example.baseproject.databinding.ActivityHomeBinding
//import com.example.baseproject.utils.showToast
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File
//import java.text.SimpleDateFormat
//
//class TestingHomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {
//
//    private var imageUri: Uri? = null
//
//    val takePickerLauncher =
//        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
//            if (success && imageUri != null) {
//                imageUri?.let {
//                    progressImage(it)
//                }
//            } else {
//                showToast("Camera Permission Denied")
//            }
//        }
//
//    val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        if (uri != null) {
//            progressImage(uri)
//        } else {
//            showToast("No image selected")
//        }
//    }
//
//    val requestPermissionsLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//            if (permissions[Manifest.permission.CAMERA] == true) {
//                launchCamera()
//            } else {
//                showToast("Permissions Denied")
//            }
//        }
//
//    companion object {
//        private const val TAG = "TestingHomeActivity"
//    }
//
//    override fun initData() {
//
//    }
//
//    override fun initView() {
//    }
//
//    override fun initActionView() {
//        binding.btnUpload.setOnClickListener {
//            pickImageLauncher.launch("image/*")
//        }
//        binding.btnCamera.setOnClickListener {
//            checkCameraPermission()
//        }
//    }
//
//    private fun checkCameraPermission() {
//        val permissions =
//            arrayOf(Manifest.permission.CAMERA)
//        requestPermissionsLauncher.launch(permissions)
//    }
//
//    private fun progressImage(imageUri: Uri) {
//        val intent = Intent(this, TestingActivity::class.java)
//        intent.putExtra("imageUri", imageUri.toString())
//        startActivity(intent)
//    }
//
//    private fun launchCamera() {
//        lifecycleScope.launch {
//            val files = withContext(Dispatchers.IO){
//                try {
//                    createImageFile()
//                } catch (e: Exception){
//                    Log.e(TAG, "Error creating image file: ${e.message} - ${e.printStackTrace()}")
//                    null
//                }
//            }
//
//            if (files != null){
//                imageUri = FileProvider.getUriForFile(
//                    this@TestingHomeActivity,
//                    "${applicationContext.packageName}.fileprovider",
//                    files
//                )
//                imageUri?.let {
//                    takePickerLauncher.launch(it)
//                }
//            } else{
//                showToast("Unable to create image file")
//            }
//        }
//
//    }
//
//    private fun createImageFile(): File {
//        val currentTime = System.currentTimeMillis()
//        val simpleFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
//        val formatedTime = simpleFormat.format(currentTime)
//        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        return File.createTempFile("JPEG_${formatedTime}_", ".jpg", storageDir)
//    }
//
//
//}