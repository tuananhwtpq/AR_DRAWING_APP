//package com.example.baseproject.activities
//
//import android.content.ContentValues
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.net.Uri
//import android.os.Environment
//import android.provider.MediaStore
//import android.util.Log
//import android.widget.SeekBar
//import android.widget.Toast
//import androidx.lifecycle.lifecycleScope
//import com.example.baseproject.bases.BaseActivity
//import com.example.baseproject.databinding.ActivityMainBinding
//import com.example.baseproject.utils.enumz.SketchEffect
//import com.example.baseproject.utils.invisible
//import com.example.baseproject.utils.showToast
//import com.example.baseproject.utils.visible
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.opencv.android.OpenCVLoader
//import java.io.OutputStream
//
//class TestingActivity : BaseActivity<TestingActivity>(ActivityMainBinding::inflate) {
//
//    private var imageUriString: String? = null
//
//    private var bitmapOrigin: Bitmap? = null
//
//    private var currentEffect = SketchEffect.ORIGINAL_TO_GRAY
//    private var sketchImage: SketchImage? = null
//
//    override fun initData() {
//        testOpenCV()
//        imageUriString = intent.getStringExtra("imageUri")
//        if (imageUriString != null) {
//            val imageUri = Uri.parse(imageUriString)
//            loadImage(imageUri)
//        }
//
//    }
//
//    override fun initView() {
//        setupControls()
//
//    }
//
//    override fun initActionView() {
//    }
//
//    private fun setupControls() {
//        binding.percentText.text = "100 %"
//        binding.simpleSeekBar.progress = 100
//        binding.simpleSeekBar.max = 100
//
//        binding.simpleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
//
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                binding.percentText.text = "$progress %"
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                val progress = seekBar?.progress ?: 100
//                applyEffect(currentEffect, progress)
//            }
//        })
//        setupButtonClick()
//    }
//
//    private fun setupButtonClick(){
//        //binding.tvOriginToGray.setOnClickListener { setupSketchType(0) }
//        binding.tvOriginToSket.setOnClickListener { setupSketchType(1) } // giu
//        //binding.tvOriginToColoredSketch.setOnClickListener { setupSketchType(2) }
//        //binding.tvOriginToSoftSket.setOnClickListener { setupSketchType(3) }
//        //binding.tvOriginToSoftColoredSket.setOnClickListener { setupSketchType(4) }
//        binding.tvGrayToSket.setOnClickListener { setupSketchType(5) } // giu
//        //binding.tvGrayToColorSket.setOnClickListener { setupSketchType(6) }
//        binding.tvGrayToSoftSket.setOnClickListener { setupSketchType(7) } //
//        //binding.tvGrayToSoftColorSket.setOnClickListener { setupSketchType(8) }
//        //binding.tvSketToColorSket.setOnClickListener { setupSketchType(9) }
//        binding.tvStrokeOnly.setOnClickListener { setupSketchType(10) } // giu
//
//        binding.btnDownload.setOnClickListener {
//            sketchImage?.let { sketchImage ->
//
//                lifecycleScope.launch(Dispatchers.Default) {
//                    val finalImage = sketchImage.getImageAs(currentEffect, binding.progressBar.progress)
//                    saveImageToGallery(finalImage)
//                    withContext(Dispatchers.Main){
//                        //handle progressbar
//                    }
//                }
//            } ?: showToast("No image saved")
//        }
//    }
//
//    private suspend fun saveImageToGallery(bitmap: Bitmap) = withContext(Dispatchers.IO){
//        val filename = "sketch_${System.currentTimeMillis()}.jpg"
//        var outputStream: OutputStream ?= null
//        var success = false
//
//        try {
//
//            val contentValue = ContentValues().apply {
//                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
//                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
//                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
//            }
//
//            val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValue)
//            if (imageUri != null){
//                outputStream = contentResolver.openOutputStream(imageUri)
//            }
//
//            outputStream?.use { stream ->
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//                success = true
//            }
//
//
//        } catch (e: Exception){
//            Log.d(TAG, "Loi khong luu duoc hinh: ${e.message} - ${e.printStackTrace()}")
//        }
//
//        withContext(Dispatchers.Main){
//            if (success){
//                showToast("Image saved to gallery")
//            } else{
//                showToast("Failed to save image")
//            }
//        }
//    }
//
//
//    private fun setupSketchType(indexType: Int){
//        currentEffect = when(indexType){
//            0 -> SketchEffect.ORIGINAL_TO_GRAY
//            1 -> SketchEffect.ORIGINAL_TO_SKETCH
//            2 -> SketchEffect.ORIGINAL_TO_COLORED_SKETCH
//            3 -> SketchEffect.ORIGINAL_TO_SOFT_SKETCH
//            4 -> SketchEffect.ORIGINAL_TO_SOFT_COLOR_SKETCH
//            5 -> SketchEffect.GRAY_TO_SKETCH
//            6 -> SketchEffect.GRAY_TO_COLORED_SKETCH
//            7 -> SketchEffect.GRAY_TO_SOFT_SKETCH
//            8 -> SketchEffect.GRAY_TO_SOFT_COLOR_SKETCH
//            9 -> SketchEffect.SKETCH_TO_COLORED_SKETCH
//            10 -> SketchEffect.STROKE_ONLY
//            else -> SketchEffect.ORIGINAL_TO_GRAY
//        }
//
//        binding.simpleSeekBar.progress = 100
//        applyEffect(currentEffect, 100)
//    }
//
//    fun testOpenCV() {
//        if (OpenCVLoader.initDebug()) {
//            Toast.makeText(applicationContext, "Open CV load success", Toast.LENGTH_LONG).show()
//        } else {
//            Toast.makeText(applicationContext, "Open CV load fail", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private fun loadImage(imageUri: Uri) {
//        try {
//            contentResolver.openInputStream(imageUri).use { inputStream ->
//                bitmapOrigin = BitmapFactory.decodeStream(inputStream)
//            }
//
//            //user bitmap
//            contentResolver.openInputStream(imageUri).use { inputStream ->
//                val userBitmap = BitmapFactory.decodeStream(inputStream)
//                binding.ivUser.setImageBitmap(userBitmap)
//            }
//
//            //target bitmap
//            bitmapOrigin?.let { bitmap ->
//                binding.ivTarget.setImageBitmap(bitmap)
//                sketchImage = SketchImage(bitmap)
//                applyEffect(currentEffect, 100)
//            }
//        } catch (e: Exception) {
//            Log.d("MainAc", "Loi load target image: ${e.message} - ${e.printStackTrace()}")
//        }
//    }
//
//    private fun applyEffect(effect: SketchEffect, thickness: Int){
//        if (effect == null) return
//
//        binding.progressBar.visible()
//
//        lifecycleScope.launch(Dispatchers.Default) {
//            val resultImage = sketchImage?.getImageAs(effect, thickness)
//
//            withContext(Dispatchers.Main){
//                binding.ivTarget.setImageBitmap(resultImage)
//                binding.progressBar.invisible()
//            }
//        }
//    }
//
//    companion object{
//        private const val TAG = "TestingActivity"
//    }
//
//
//
//}