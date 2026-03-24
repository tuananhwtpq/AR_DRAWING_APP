package com.flowart.ar.drawing.sketch.activities

import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivitySketchResultBinding
import com.flowart.ar.drawing.sketch.utils.formatDateTime
import com.flowart.ar.drawing.sketch.utils.gone
import com.flowart.ar.drawing.sketch.utils.onProgressChange
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick
import com.flowart.ar.drawing.sketch.utils.visible
import java.io.File

class SketchResultActivity : BaseActivity<ActivitySketchResultBinding>(
    ActivitySketchResultBinding::inflate
) {
    private val isImage by lazy {
        intent.getBooleanExtra("isImage", false)
    }

    private val mediaUri by lazy {
        Uri.parse(intent.getStringExtra("media_uri"))
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            File(mediaUri.path!!).delete()
            finish()
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if (binding.videoView.isPlaying) {
                binding.sbTime.progress = binding.videoView.currentPosition
                handler.postDelayed(this, 50)
            }
        }
    }

    private val hideControlsRunnable = Runnable {
        binding.vPlay.isVisible = false
    }

    override fun initData() {
        binding.lLoading.visible()
        binding.lVideo.isVisible = !isImage
        binding.ivPreview.isVisible = isImage
    }

    override fun initView() {
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        if (isImage) {
            Glide.with(this).load(mediaUri).into(binding.ivPreview)
            binding.lLoading.gone()
        } else {
            setVideoView()
        }
    }

    override fun initActionView() {
        binding.ivBack.setOnUnDoubleClick {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivDelete.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivDownload.setOnUnDoubleClick {
            saveMediaToGallery(mediaUri, isImage)
        }

//        binding.vPlay.setOnClickListener {
//            if (binding.videoView.isPlaying) {
//                binding.videoView.pause()
//            } else {
//                binding.videoView.resume()
//            }
//            binding.vPlay.setImageResource(if (binding.videoView.isPlaying) R.drawable.ic_play_2 else R.drawable.ic_pause_2)
//        }
//
//        binding.sbTime.onProgressChange { binding.videoView.seekTo(it) }
        binding.videoContainer.setOnClickListener {
            toggleVideoState()
        }

        binding.vPlay.setOnClickListener {
            toggleVideoState()
        }

        binding.sbTime.onProgressChange {
            binding.videoView.seekTo(it)
            setupController()
        }
    }

    private fun toggleVideoState() {
        if (binding.videoView.isPlaying) {
            binding.videoView.pause()

            handler.removeCallbacks(hideControlsRunnable)
            binding.vPlay.isVisible = true

            binding.vPlay.setImageResource(R.drawable.ic_pause_2)
        } else {
            binding.videoView.start()

            binding.vPlay.setImageResource(R.drawable.ic_play_2)
            setupController()
            handler.post(runnable)
        }
    }

    override fun onResume() {
        super.onResume()
        showBannerAds()
    }

    private fun setVideoView() {
        if (mediaUri == null) return
        binding.videoView.setVideoURI(mediaUri)
        binding.videoView.setOnPreparedListener { mp ->
            binding.sbTime.max = mp.duration
            binding.lLoading.gone()

            binding.videoView.start()
            binding.vPlay.setImageResource(R.drawable.ic_play_2)

            handler.postDelayed(runnable, 50)

            setupController()
        }

        binding.videoView.setOnCompletionListener {
            handler.removeCallbacks(runnable)
            handler.removeCallbacks(hideControlsRunnable)
            binding.vPlay.isVisible = true
            binding.sbTime.progress = 0
            binding.vPlay.setImageResource(R.drawable.ic_pause_2)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        handler.removeCallbacks(hideControlsRunnable)
    }


    private fun saveMediaToGallery(uri: Uri, isImage: Boolean) {
        try {
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    System.currentTimeMillis().formatDateTime()
                )
                put(MediaStore.MediaColumns.MIME_TYPE, if (isImage) "image/jpeg" else "video/mp4")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + "/AR_DRAWING"
                )
            }

            val contentUri = if (isImage) MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            else MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            val outputStream =
                contentResolver.openOutputStream(
                    contentResolver.insert(
                        contentUri,
                        contentValues
                    )!!
                )
            val inputStream = contentResolver.openInputStream(uri)

            inputStream?.copyTo(outputStream!!)
            inputStream?.close()
            outputStream?.close()

            Toast.makeText(this, getString(R.string.saved_to_gallery), Toast.LENGTH_LONG).show()
            File(uri.path!!).delete()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.has_error_now), Toast.LENGTH_LONG).show()
        }
    }

    private fun setupController() {
        binding.vPlay.isVisible = true
        handler.removeCallbacks(hideControlsRunnable)
        if (binding.videoView.isPlaying) {
            handler.postDelayed(hideControlsRunnable, 2000)
        }
    }

    private fun showBannerAds() {
//        if (RemoteConfig.remoteBannerPreviewVideo == 0L) return
//        binding.frBanner.visible()
//        binding.viewLine.visible()
//        if (RemoteConfig.remoteBannerPreviewVideo == 1L) {
//            AdmobLib.loadAndShowBanner(
//                this,
//                AdsManager.BANNER_OTHER,
//                binding.frBanner,
//                binding.viewLine
//            )
//            return
//        }
//
//        if (RemoteConfig.remoteBannerPreviewVideo == 2L) {
//            AdmobLib.loadAndShowBannerCollapsible(
//                this,
//                AdsManager.bannerCollapsePreviewVideoModel,
//                binding.frBanner,
//                binding.viewLine
//            )
//            return
//        }
    }
}