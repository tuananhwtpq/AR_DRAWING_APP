package com.flowart.ar.drawing.sketch.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.flowart.ar.drawing.sketch.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


object BitmapUtils {
    fun getBitmapFromDrawableResource(context: Context, resID: Int, alpha: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, resID) ?: return null

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth / 2 else 100
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight / 2 else 100

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, canvas.width, canvas.height)

        val paint = Paint()
        paint.alpha = alpha.coerceIn(0, 255)

        canvas.saveLayerAlpha(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint.alpha)
        drawable.draw(canvas)
        canvas.restore()

        return bitmap
    }

    fun setBackgroundForBitmap(bitmap: Bitmap, color: Int): Bitmap {
        if (bitmap.config == null) return bitmap

        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config!!)
        val canvas = Canvas(newBitmap)
        canvas.drawColor(color)
        canvas.drawBitmap(bitmap, 0f, 0f, Paint())

        return newBitmap
    }

    fun getBitmapFromAsset(context: Context, path: String): Bitmap? {
        val assetManager = context.assets
        val inputStream: InputStream
        var bitmap: Bitmap? = null
        try {
            inputStream = assetManager.open(path)
            bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
        } catch (e: Exception) {

        }
        return bitmap
    }

    fun resizeBitmap(originalBitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height

        val aspectRatio = width.toFloat() / height.toFloat()

        var newWidth = maxWidth
        var newHeight = (maxWidth / aspectRatio).toInt()

        if (newHeight > maxHeight) {
            newHeight = maxHeight
            newWidth = (maxHeight * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
    }

    fun scaleBitmap(bitmap: Bitmap, scaleFactor: Float): Bitmap {
        if (scaleFactor <= 0) return bitmap

        val newWidth = (bitmap.width * scaleFactor).toInt()
        val newHeight = (bitmap.height * scaleFactor).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }


    fun getBitmapFromUri2(context: Context, imageUri: Uri): Bitmap? {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        inputStream?.use {
            try {
                val tmpBitmap = BitmapFactory.decodeStream(it)
                val ei = ExifInterface(it)
                val orientation: Int = ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
                )
                return when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(tmpBitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(tmpBitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(tmpBitmap, 270f)
                    ExifInterface.ORIENTATION_NORMAL -> tmpBitmap
                    else -> tmpBitmap
                }
            } catch (e: Exception) {
                return null
            }

        } ?: return null
//        return context.contentResolver.openInputStream(imageUri)?.use {
//            BitmapFactory.decodeStream(it)
//        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix: Matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    fun getBitmapFromUri(context: Context, imageUri: Uri): Bitmap? {
        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null

        val exif = ExifInterface(inputStream)

        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        inputStream.close()

        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    fun applyAlphaToBitmap(bitmap: Bitmap, alpha: Int = 128): Bitmap {
        val mutableBitmap =
            Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            this.alpha = alpha.coerceIn(0, 255)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return mutableBitmap
    }

    fun shareBitmap(context: Context, bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "shared_image.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Image"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                context.getString(R.string.failed_to_share_image), Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun createCacheFile(context: Context, bitmap: Bitmap): Uri {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "bitmap_cache_file.png")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return fileUri
    }

}