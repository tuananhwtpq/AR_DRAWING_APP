package com.example.baseproject.activities

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityResultBinding
import com.example.baseproject.utils.BitmapUtils
import com.example.baseproject.utils.Constants
import com.example.baseproject.utils.SharedPrefManager
import com.example.baseproject.utils.formatDateTime
import com.example.baseproject.utils.setOnUnDoubleClick
import com.example.baseproject.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import kotlin.text.insert

class ResultActivity : BaseActivity<ActivityResultBinding>(ActivityResultBinding::inflate) {
    override fun initData() {
        if (bitmap == null) finish()
    }

    override fun initView() {
        Glide.with(this).load(bitmap).into(binding.ivPreview)
    }

    override fun initActionView() {
        binding.btnDownload.setOnClickListener {
            saveBitmap(bitmap)
        }

        binding.btnShare.setOnClickListener {
            shareBitmap(bitmap)
        }

        binding.backHome.setOnUnDoubleClick {
//            loadAndShowInterBack(binding.vShowInterAds) {
//                val intent = Intent(this, MainActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                startActivity(intent)
//            }
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

    }

    override fun onDestroy() {
        bitmap = null
        super.onDestroy()
    }

    companion object {
        var bitmap: Bitmap? = null
    }

    private fun saveBitmap(bitmap: Bitmap?) {
        if (bitmap == null) {
            showToast(getString(R.string.has_error_now))
            return
        }

        lifecycleScope.launch {
            val imageOutStream: OutputStream?
            val cv = ContentValues()
            val name = System.currentTimeMillis().formatDateTime()
            cv.put(MediaStore.Images.Media.DISPLAY_NAME, "ar_drawing_$name.png")
            cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            cv.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_DCIM + "/AR_DRAWING"
            )
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
            try {
                imageOutStream = contentResolver.openOutputStream(uri!!)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream!!)
                imageOutStream.close()
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.image_save_success))
                }

                SharedPrefManager.putInt(
                    Constants.KEY_DRAW_NUMBER,
                    SharedPrefManager.getInt(Constants.KEY_DRAW_NUMBER, 0) + 1
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.has_error_now))
                }
                e.printStackTrace()
            }
        }

    }

    private fun shareBitmap(bitmap: Bitmap?) {
        if (bitmap == null) {
            Toast.makeText(this, getString(R.string.has_error_now), Toast.LENGTH_SHORT).show()
            return
        }

        BitmapUtils.shareBitmap(this, bitmap)
    }
}