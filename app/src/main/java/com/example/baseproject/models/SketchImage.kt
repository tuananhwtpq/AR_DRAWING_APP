package com.example.baseproject.models

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.example.baseproject.utils.enumz.SketchEffect
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.nio.IntBuffer
import kotlin.math.min
import kotlin.math.roundToInt

class SketchImage(private val originalBitmap: Bitmap) {

    fun getImageAs(effectType: SketchEffect, thickness: Int): Bitmap {
        Log.d("SketchImage", "Effect type: $effectType")

        val downscaledBitmap = downscaleImage(originalBitmap, 800)

        val result = when (effectType) {
            SketchEffect.ORIGINAL_TO_GRAY -> toGrayScale(downscaledBitmap)
            SketchEffect.ORIGINAL_TO_SKETCH -> {
                val sketchBitmap = toSketch(downscaledBitmap, thickness)
                toColoredSketch(sketchBitmap, thickness)
            }
            SketchEffect.ORIGINAL_TO_COLORED_SKETCH -> toColoredSketch(downscaledBitmap, thickness)
            SketchEffect.ORIGINAL_TO_SOFT_SKETCH -> toSoftSketch(downscaledBitmap, thickness)
            SketchEffect.ORIGINAL_TO_SOFT_COLOR_SKETCH -> toSoftColorSketch(downscaledBitmap, thickness)

            SketchEffect.GRAY_TO_SKETCH -> toSketch(toGrayScale(downscaledBitmap), thickness)
            SketchEffect.GRAY_TO_COLORED_SKETCH -> toColoredSketch(toGrayScale(downscaledBitmap), thickness)
            SketchEffect.GRAY_TO_SOFT_SKETCH -> toSoftSketch(toGrayScale(downscaledBitmap), thickness)
            SketchEffect.GRAY_TO_SOFT_COLOR_SKETCH -> toSoftColorSketch(toGrayScale(downscaledBitmap), thickness)

            SketchEffect.SKETCH_TO_COLORED_SKETCH -> {
                val sketchBitmap2 = toSketch(downscaledBitmap, thickness)
                toColoredSketch(sketchBitmap2, thickness)
            }
            SketchEffect.STROKE_ONLY -> toStrokeOnly(downscaledBitmap, thickness)
        }

        Log.d("SketchImage", "Result dimensions: ${result.width}x${result.height}")
        return result
    }

    private fun downscaleImage(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width > maxSize || height > maxSize) {
            val aspectRatio = width.toFloat() / height.toFloat()

            val (newWidth, newHeight) = if (width > height) {
                maxSize to (maxSize / aspectRatio).roundToInt()
            } else {
                (maxSize * aspectRatio).roundToInt() to maxSize
            }
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }
        return bitmap
    }

    private fun toStrokeOnly(bitmap: Bitmap, thickness: Int): Bitmap {
        val matGray = Mat()
        Utils.bitmapToMat(bitmap, matGray)

        Imgproc.cvtColor(matGray, matGray, Imgproc.COLOR_RGB2GRAY)
        Imgproc.medianBlur(matGray, matGray, 5)

        val matEdges = Mat()

        var blockSize = (thickness / 3.5).toInt()
        if (blockSize % 2 == 0) blockSize++
        if (blockSize < 3) blockSize = 3

        Imgproc.adaptiveThreshold(
            matGray,
            matEdges,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            blockSize,
            5.0
        )

        val resultBitmap = Bitmap.createBitmap(matEdges.cols(), matEdges.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matEdges, resultBitmap)

        matGray.release()
        matEdges.release()

        return resultBitmap
    }

    private fun toGrayScale(bitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)

        val grayBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, grayBitmap)

        mat.release()
        return grayBitmap
    }

    private fun toSketch(bitmap: Bitmap, thickness: Int): Bitmap {
        val grayBitmap = toGrayScale(bitmap)

        val matGray = Mat()
        Utils.bitmapToMat(grayBitmap, matGray)

        val matInverted = Mat()
        Core.bitwise_not(matGray, matInverted)

        val matBlurred = Mat()
        val kSize = (thickness * 2 + 1).toDouble()
        Imgproc.GaussianBlur(matInverted, matBlurred, Size(kSize, kSize), 0.0)

        val blurredBitmap = Bitmap.createBitmap(matBlurred.cols(), matBlurred.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matBlurred, blurredBitmap)

        matGray.release()
        matInverted.release()
        matBlurred.release()

        return colorDodgeBlend(grayBitmap, blurredBitmap)
    }

    private fun colorDodgeBlend(source: Bitmap, layer: Bitmap): Bitmap {
        val base = source.copy(Bitmap.Config.ARGB_8888, true)
        val blend = layer.copy(Bitmap.Config.ARGB_8888, false)

        val buffBase = IntBuffer.allocate(base.width * base.height)
        base.copyPixelsToBuffer(buffBase)
        buffBase.rewind()

        val buffBlend = IntBuffer.allocate(blend.width * blend.height)
        blend.copyPixelsToBuffer(buffBlend)
        buffBlend.rewind()

        val buffOut = IntBuffer.allocate(base.width * base.height)
        buffOut.rewind()

        while (buffOut.position() < buffOut.limit()) {
            val filterInt = buffBlend.get()
            val srcInt = buffBase.get()

            val redValueFinal = colorDodge(Color.red(filterInt), Color.red(srcInt))
            val greenValueFinal = colorDodge(Color.green(filterInt), Color.green(srcInt))
            val blueValueFinal = colorDodge(Color.blue(filterInt), Color.blue(srcInt))

            buffOut.put(Color.argb(255, redValueFinal, greenValueFinal, blueValueFinal))
        }

        buffOut.rewind()
        base.copyPixelsFromBuffer(buffOut)
        blend.recycle()

        return base
    }

    private fun colorDodge(in1: Int, in2: Int): Int {
        val image = in2.toFloat()
        val mask = in1.toFloat()

        if (image == 255f) return 255

        val value = (mask.toLong() shl 8) / (255 - image)
        return min(255f, value).toInt()
    }

    private fun toColoredSketch(bitmap: Bitmap, thickness: Int): Bitmap {
        val sketchBitmap = toSketch(bitmap, thickness)

        val matOriginal = Mat()
        val matSketch = Mat()
        val matColoredSketch = Mat()

        Utils.bitmapToMat(bitmap, matOriginal)
        Utils.bitmapToMat(sketchBitmap, matSketch)

        Core.addWeighted(matOriginal, 0.5, matSketch, 0.5, 0.0, matColoredSketch)

        val coloredSketchBitmap = Bitmap.createBitmap(
            matColoredSketch.cols(),
            matColoredSketch.rows(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(matColoredSketch, coloredSketchBitmap)

        matOriginal.release()
        matSketch.release()
        matColoredSketch.release()

        return coloredSketchBitmap
    }

    private fun toSoftSketch(bitmap: Bitmap, thickness: Int): Bitmap {
        val matGray = Mat()
        Utils.bitmapToMat(toGrayScale(bitmap), matGray)

        val matBlurred = Mat()
        val kSize = (thickness * 2 + 1).toDouble()
        Imgproc.GaussianBlur(matGray, matBlurred, Size(kSize, kSize), 0.0)

        val matResult = Mat()
        Core.divide(matGray, matBlurred, matResult, 256.0)

        val softSketchBitmap = Bitmap.createBitmap(matResult.cols(), matResult.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matResult, softSketchBitmap)

        matGray.release()
        matBlurred.release()
        matResult.release()

        return softSketchBitmap
    }

    private fun toSoftColorSketch(bitmap: Bitmap, thickness: Int): Bitmap {
        val softSketch = toSoftSketch(bitmap, thickness)
        val coloredSketch = toColoredSketch(bitmap, thickness)

        val matSoftSketch = Mat()
        val matColoredSketch = Mat()
        val matResult = Mat()

        Utils.bitmapToMat(softSketch, matSoftSketch)
        Utils.bitmapToMat(coloredSketch, matColoredSketch)

        Core.addWeighted(matSoftSketch, 0.5, matColoredSketch, 0.5, 0.0, matResult)

        val softColorSketchBitmap = Bitmap.createBitmap(matResult.cols(), matResult.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matResult, softColorSketchBitmap)

        matSoftSketch.release()
        matColoredSketch.release()
        matResult.release()

        return softColorSketchBitmap
    }
}