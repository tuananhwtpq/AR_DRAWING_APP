package com.snake.drawingview.actions

import com.snake.drawingview.DrawingContext

internal class ChangeBackgroundAction(
    private val newColor: Int,
): Action {

    override fun perform(context: DrawingContext) {
        context.backgroundColor = newColor
    }

    override fun getOppositeAction(context: DrawingContext): Action {
        return ChangeBackgroundAction(context.backgroundColor)
    }

}
