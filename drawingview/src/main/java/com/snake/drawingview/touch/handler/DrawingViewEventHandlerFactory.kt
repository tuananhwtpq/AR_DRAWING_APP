package com.snake.drawingview.touch.handler

import com.snake.drawingview.DrawingContext

internal class DrawingViewEventHandlerFactory {

    fun create(drawingContext: DrawingContext): MotionEventHandler {
        return DrawingViewEventHandler(
            TransformationEventHandler(
                drawingContext.transformation,
                drawingContext.rotationEnabled,
            ),
            TransformerEventHandler(
                drawingContext.transformation,
                BrushToolEventHandler(drawingContext),
            ),
        )
    }

}