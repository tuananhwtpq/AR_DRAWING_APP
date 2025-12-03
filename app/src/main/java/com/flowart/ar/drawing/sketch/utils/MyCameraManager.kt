package com.flowart.ar.drawing.sketch.utils

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.lifecycleScope
import com.flowart.ar.drawing.sketch.R
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService

class MyCameraManager(val mActivity: AppCompatActivity) {
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var camera: Camera
    private val TAG = "MyCameraManager"
    var listener: RecordListener? = null

    fun startCamera(viewFinder: PreviewView) {
        mActivity.lifecycleScope.launch {
            try {
                val cameraProvider = ProcessCameraProvider.getInstance(mActivity).await()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = viewFinder.surfaceProvider
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            Quality.HIGHEST,
                            FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                        )
                    )
                    .build()
                videoCapture = VideoCapture.withOutput(recorder)

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    mActivity,
                    cameraSelector,
                    preview,
                    imageCapture,
                    videoCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "startCamera Failed: ${e.message} ${e.printStackTrace()} ")
            }
        }
    }

    fun captureImage2() {
        val imageCapture = imageCapture ?: return
        val name = System.currentTimeMillis().formatDateTime()
        val contentValue = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/AR_DRAWING")
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            mActivity.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValue
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(mActivity),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.e(TAG, "Photo capture succeeded: ${outputFileResults.savedUri}")
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message} ", exception)
                }

            })
    }

    fun captureImage(onError: () -> Unit, navAction: (Uri) -> Unit) {
        val imageCapture = imageCapture ?: return
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val tempFile = File(mActivity.externalCacheDir, fileName)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(mActivity),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val uri = Uri.fromFile(tempFile)
                    Log.e(TAG, "Photo captured: $uri")
                    navAction(uri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        mActivity,
                        mActivity.getString(R.string.photo_capture_failed), Toast.LENGTH_SHORT
                    ).show()
                    onError()
                    Log.e(TAG, "Photo capture failed: ${exception.message} ", exception)
                }
            }
        )
    }


    fun recordVideo2() {
        videoCapture = videoCapture ?: return
//        binding.btnRecord.isEnabled = false // disable click record while recording
        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            recording = null
            return
        }
        val name = System.currentTimeMillis().formatDateTime()
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/AR_DRAWING")
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(mActivity.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording =
            videoCapture!!.output.prepareRecording(mActivity, mediaStoreOutputOptions).apply {
                if (PermissionChecker.checkSelfPermission(
                        mActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) ==
                    PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }

            }.start(ContextCompat.getMainExecutor(mActivity)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        listener?.onStartRecord()
                    }

                    is VideoRecordEvent.Pause -> {
                        listener?.onPauseRecord()
                    }

                    is VideoRecordEvent.Resume -> {
                        listener?.onResumeRecord()
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            listener?.onFinalizeWithSuccess()
                        } else {
                            listener?.onFinalizeWithError()
                            recording?.close()
                            recording = null
                            Log.e(
                                TAG, "Video capture ends with error: " +
                                        "${recordEvent.error}"
                            )
                        }
                    }
                }
            }
    }

    fun recordVideo(navAction: (Uri) -> Unit) {
        videoCapture = videoCapture ?: return
        val curRecording = recording

        if (curRecording != null) {
            curRecording.stop()
            recording = null
            return
        }

        val fileName = "VID_${System.currentTimeMillis()}.mp4"
        val tempFile = File(mActivity.externalCacheDir, fileName)

        val outputOptions = FileOutputOptions.Builder(tempFile).build()

        recording = videoCapture!!.output.prepareRecording(mActivity, outputOptions).apply {
            if (PermissionChecker.checkSelfPermission(
                    mActivity,
                    Manifest.permission.RECORD_AUDIO
                ) ==
                PermissionChecker.PERMISSION_GRANTED
            ) {
                withAudioEnabled()
            }
        }.start(ContextCompat.getMainExecutor(mActivity)) { recordEvent ->
            when (recordEvent) {
                is VideoRecordEvent.Start -> {
                    listener?.onStartRecord()
                }

                is VideoRecordEvent.Pause -> {
                    listener?.onPauseRecord()
                }

                is VideoRecordEvent.Resume -> {
                    listener?.onResumeRecord()
                }

                is VideoRecordEvent.Finalize -> {
                    if (!recordEvent.hasError()) {
                        listener?.onFinalizeWithSuccess()
                        val uri = Uri.fromFile(tempFile)
                        Log.e(TAG, "Video recorded: $uri")
                        navAction(uri)
                    } else {
                        listener?.onFinalizeWithError()
                        Toast.makeText(
                            mActivity,
                            mActivity.getString(R.string.video_capture_failed), Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "Video capture failed: ${recordEvent.error}")
                    }
                }
            }
        }
    }


    fun pauseRecord() {
        recording?.pause()
    }

    fun resumeRecord() {
        recording?.resume()
    }

    fun stopRecord() {
        recording?.stop()
        recording?.close()
        recording = null
    }

    fun enableFlash(isEnable: Boolean) {
        if (this::camera.isInitialized) {
            camera.cameraControl.enableTorch(isEnable)
        }
    }

    interface RecordListener {
        fun onStartRecord() {}
        fun onPauseRecord() {}
        fun onResumeRecord() {}
        fun onFinalizeWithSuccess() {}
        fun onFinalizeWithError() {}
    }

}