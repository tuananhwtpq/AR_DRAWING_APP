package com.snake.drawingview.renderer

import android.graphics.Bitmap
import android.graphics.Rect
import com.snake.drawingview.DrawingContext

internal class DrawingRendererFactory {

    fun createOnscreenRenderer(drawingContext: DrawingContext, bitmapTemplate: Bitmap?): Renderer? {
        if (!drawingContext.hasDrawing) {
            return null
        }
        return if (drawingContext.brushToolStatus.active) {
            createBrushToolResultRenderer(drawingContext, bitmapTemplate)
        } else {
            createLayerRenderer(drawingContext, bitmapTemplate)
        }
    }

//    fun createOffscreenRenderer(drawingContext: DrawingContext): Renderer {
//        return ListRenderer(
//            RectRenderer(
//                Rect(0, 0, drawingContext.drawingWidth, drawingContext.drawingHeight),
//                drawingContext.backgroundColor,
//            ),
//            BitmapRenderer(
//                drawingContext.brushToolBitmaps.layerBitmap
//            )
//        )
//    }

    private fun createBrushToolResultRenderer(
        drawingContext: DrawingContext,
        bitmapTemplate: Bitmap?
    ): Renderer {
        return TransformedRenderer(
            drawingContext.transformation,
            ListRenderer(
                RectRenderer(
                    Rect(0, 0, drawingContext.drawingWidth, drawingContext.drawingHeight),
                    drawingContext.backgroundColor,
                ),
                BitmapRenderer(
                    drawingContext.brushToolBitmaps.resultBitmap,
                    bitmapTemplate
                )
            ),
        )
    }

    private fun createLayerRenderer(
        drawingContext: DrawingContext,
        bitmapTemplate: Bitmap?
    ): Renderer {
        return TransformedRenderer(
            drawingContext.transformation,
            ListRenderer(
                RectRenderer(
                    Rect(0, 0, drawingContext.drawingWidth, drawingContext.drawingHeight),
                    drawingContext.backgroundColor,
                ),
                BitmapRenderer(
                    drawingContext.brushToolBitmaps.layerBitmap,
                    bitmapTemplate
                )
            ),
        )
    }

}
