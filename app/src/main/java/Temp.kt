//package com.example.baseproject.activities
//
//package com.converter.image2sketch
//
//import android.Manifest
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.os.Environment
//import android.provider.MediaStore
//import android.util.Log
//import android.widget.Button
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.FileProvider
//import androidx.lifecycle.lifecycleScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File
//import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.Date
//
//class TestingHomeActivity : AppCompatActivity() {
//
//    private var photoUri: Uri? = null
//
//    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
//        if (success && photoUri != null) {
//            Log.d(TAG, "Camera image captured: $photoUri")
//            processImage(photoUri!!)
//        } else {
//            Toast.makeText(this, "Camera operation failed or canceled", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // 2. Thay thế startActivityForResult cho Gallery
//    private val pickGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        if (uri != null) {
//            Log.d(TAG, "Gallery image selected: $uri")
//            processImage(uri)
//        } else {
//            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // 3. Xử lý quyền hạn hiện đại
//    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//        if (permissions[Manifest.permission.CAMERA] == true &&
//            permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
//        ) {
//            launchCamera()
//        } else {
//            Toast.makeText(this, "Permissions are required to use the camera", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_home)
//
//        findViewById<Button>(R.id.btn_upload).setOnClickListener {
//            pickGalleryLauncher.launch("image/*")
//        }
//
//        findViewById<Button>(R.id.btn_camera).setOnClickListener {
//            checkPermissionsAndOpenCam()
//        }
//    }
//
//    private fun checkPermissionsAndOpenCam() {
//        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        requestPermissionLauncher.launch(permissions)
//    }
//
//    private fun launchCamera() {
//        // Sử dụng Coroutine thay cho AsyncTask
//        lifecycleScope.launch {
//            val file = withContext(Dispatchers.IO) {
//                try {
//                    createImageFile()
//                } catch (e: IOException) {
//                    Log.e(TAG, "Error creating file", e)
//                    null
//                }
//            }
//
//            if (file != null) {
//                photoUri = FileProvider.getUriForFile(
//                    this@TestingHomeActivity,
//                    "${applicationContext.packageName}.fileprovider",
//                    file
//                )
//                takePictureLauncher.launch(photoUri)
//            } else {
//                Toast.makeText(this@TestingHomeActivity, "Unable to create image file", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun createImageFile(): File {
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
//    }
//
//    private fun processImage(imageUri: Uri) {
//        // Kotlin 'apply' pattern cho Intent
//        Intent(this, TestingActivity::class.java).apply {
//            putExtra("imageUri", imageUri.toString())
//            startActivity(this)
//        }
//    }
//
//    companion object {
//        private const val TAG = "TestingHomeActivity"
//    }
//}
//
//
//
//package com.converter.image2sketch
//
//import android.content.ContentValues
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.Environment
//import android.provider.MediaStore
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.ProgressBar
//import android.widget.SeekBar
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import com.google.android.material.tabs.TabLayout
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.opencv.android.OpenCVLoader
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//
//class TestingActivity : AppCompatActivity() {
//
//    // Sử dụng lateinit để tránh null check liên tục
//    private lateinit var targetImageView: ImageView
//    private lateinit var userImageView: ImageView
//    private lateinit var progressBar: ProgressBar
//    private lateinit var downloadButton: Button
//    private lateinit var seekBar: SeekBar
//    private lateinit var percentTextView: TextView
//    private lateinit var tabLayout: TabLayout
//
//    private var bmOriginal: Bitmap? = null
//    private var sketchImage: SketchImage? = null // Giả định class SketchImage đã được refactor
//
//    // Sử dụng Enum thay vì Int constant
//    private var currentEffect = SketchEffect.ORIGINAL_TO_GRAY
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        initViews()
//        checkOpenCV()
//
//        val imageUriString = intent.getStringExtra("imageUri")
//        if (imageUriString != null) {
//            val uri = Uri.parse(imageUriString)
//            loadImages(uri)
//        } else {
//            Log.e(TAG, "No image URI provided.")
//            Toast.makeText(this, "Error: No image found", Toast.LENGTH_SHORT).show()
//            finish()
//        }
//
//        setupControls()
//    }
//
//    private fun initViews() {
//        targetImageView = findViewById(R.id.iv_target)
//        userImageView = findViewById(R.id.iv_user)
//        progressBar = findViewById(R.id.pb)
//        downloadButton = findViewById(R.id.btn_download)
//        seekBar = findViewById(R.id.simpleSeekBar)
//        percentTextView = findViewById(R.id.tv_pb)
//        tabLayout = findViewById(R.id.tabLayout)
//    }
//
//    private fun checkOpenCV() {
//        if (OpenCVLoader.initDebug()) {
//            Log.d("OpenCV", "OpenCV initialized successfully")
//        } else {
//            Log.e("OpenCV", "OpenCV initialization failed")
//            Toast.makeText(this, "OpenCV Error", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private fun loadImages(imageUri: Uri) {
//        try {
//            // Sử dụng 'use' để tự động đóng stream
//            contentResolver.openInputStream(imageUri)?.use { inputStream ->
//                bmOriginal = BitmapFactory.decodeStream(inputStream)
//            }
//
//            // Load user image (thumbnail?) - logic tương tự
//            contentResolver.openInputStream(imageUri)?.use { inputStream ->
//                val userBitmap = BitmapFactory.decodeStream(inputStream)
//                userImageView.setImageBitmap(userBitmap)
//            }
//
//            bmOriginal?.let { bitmap ->
//                targetImageView.setImageBitmap(bitmap)
//                sketchImage = SketchImage(bitmap) // Refactored constructor
//                applyEffect(currentEffect, 100)
//            } ?: Log.e(TAG, "Failed to decode bitmap.")
//
//        } catch (e: IOException) {
//            Log.e(TAG, "Error loading image: ${e.message}")
//        }
//    }
//
//    private fun setupControls() {
//        percentTextView.text = "100 %"
//        seekBar.max = 100
//        seekBar.progress = 100
//
//        // Setup Tabs
//        SketchEffect.values().forEach { effect ->
//            // Format tên Enum cho đẹp: ORIGINAL_TO_GRAY -> Original To Gray
//            val name = effect.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
//            tabLayout.addTab(tabLayout.newTab().setText(name))
//        }
//
//        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab) {
//                // Lấy enum dựa trên index
//                currentEffect = SketchEffect.values().getOrElse(tab.position) { SketchEffect.ORIGINAL_TO_GRAY }
//                seekBar.progress = 100
//                applyEffect(currentEffect, 100)
//            }
//            override fun onTabUnselected(tab: TabLayout.Tab?) {}
//            override fun onTabReselected(tab: TabLayout.Tab?) {}
//        })
//
//        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                percentTextView.text = "$progress %"
//                // Lưu ý: Xử lý realtime ở đây có thể gây lag nếu ảnh lớn
//                // Tốt nhất chỉ nên update khi onStopTrackingTouch hoặc dùng Coroutine debounce
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                progressBar.visibility = View.VISIBLE
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                applyEffect(currentEffect, seekBar.progress)
//                progressBar.visibility = View.INVISIBLE
//            }
//        })
//
//        downloadButton.setOnClickListener {
//            // Xử lý lại ảnh ở chất lượng hiện tại để lưu
//            sketchImage?.let { sketch ->
//                progressBar.visibility = View.VISIBLE
//                lifecycleScope.launch(Dispatchers.Default) {
//                    val finalBitmap = sketch.getImageAs(currentEffect, seekBar.progress)
//                    saveImageToGallery(finalBitmap)
//                    withContext(Dispatchers.Main) {
//                        progressBar.visibility = View.INVISIBLE
//                    }
//                }
//            } ?: Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // Hàm áp dụng hiệu ứng chạy trên Background Thread để tránh đơ UI
//    private fun applyEffect(effect: SketchEffect, thickness: Int) {
//        if (sketchImage == null) return
//
//        // Hiện loading nếu cần
//        // progressBar.visibility = View.VISIBLE
//
//        lifecycleScope.launch(Dispatchers.Default) {
//            val resultBitmap = sketchImage!!.getImageAs(effect, thickness)
//
//            withContext(Dispatchers.Main) {
//                targetImageView.setImageBitmap(resultBitmap)
//                // progressBar.visibility = View.INVISIBLE
//            }
//        }
//    }
//
//    private suspend fun saveImageToGallery(bitmap: Bitmap) = withContext(Dispatchers.IO) {
//        val filename = "sketch_${System.currentTimeMillis()}.jpg"
//        var outputStream: java.io.OutputStream? = null
//        var success = false
//
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                val contentValues = ContentValues().apply {
//                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
//                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
//                }
//
//                val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//                if (imageUri != null) {
//                    outputStream = contentResolver.openOutputStream(imageUri)
//                }
//            } else {
//                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                val imageFile = File(imagesDir, filename)
//                outputStream = FileOutputStream(imageFile)
//            }
//
//            outputStream?.use { stream ->
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//                success = true
//            }
//
//        } catch (e: IOException) {
//            Log.e(TAG, "Save error: ${e.message}")
//        }
//
//        withContext(Dispatchers.Main) {
//            val msg = if (success) "Image saved to Gallery" else "Failed to save image"
//            Toast.makeText(this@TestingActivity, msg, Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    companion object {
//        private val TAG = TestingActivity::class.java.simpleName
//    }
//}
