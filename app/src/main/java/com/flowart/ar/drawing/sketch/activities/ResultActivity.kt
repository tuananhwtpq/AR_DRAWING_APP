package com.flowart.ar.drawing.sketch.activities

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityResultBinding
import com.flowart.ar.drawing.sketch.utils.BitmapUtils
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.ads.RemoteConfig
import com.flowart.ar.drawing.sketch.utils.formatDateTime
import com.flowart.ar.drawing.sketch.utils.gone
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick
import com.flowart.ar.drawing.sketch.utils.showToast
import com.flowart.ar.drawing.sketch.utils.visible
import com.snake.squad.adslib.AdmobLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

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
            loadAndShowInterBackHome(binding.vShowInterAds) {
                SharedPrefManager.putBoolean("wantShowRate", true)
                gotoMain()
            }
        }

    }

    private fun gotoMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onDestroy() {
        bitmap = null
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        loadAndShowNativeOther()
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

    fun loadAndShowNativeOther() {
        when (RemoteConfig.remoteNativeOther) {
            1L -> {
                binding.frNativeSmall.visible()
                binding.frNativeExpand.visible()
                AdmobLib.loadAndShowNativeCollapsibleSingle(
                    activity = this,
                    admobNativeModel = AdsManager.NATIVE_OTHER,
                    viewGroupExpanded = binding.frNativeExpand,
                    viewGroupCollapsed = binding.frNativeSmall,
                    layoutExpanded = R.layout.native_ads_custom_medium_bottom,
                    layoutCollapsed = R.layout.native_ads_custom_small_like_banner,
                    onAdsLoaded = {
                        binding.whiteLine.visible()
                    },
                    onAdsLoadFail = {
                        binding.whiteLine.gone()
                    }
                )
            }
        }
    }
}