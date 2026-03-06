package com.snake.drawingview.actions

import com.snake.drawingview.DrawingContext


interface Action {

    val size get() = 0

    /**
     * Move the context from state A to B.
     */
    fun perform(context: DrawingContext)

    /**
     * Should be called in state A (before calling perform(context))
     * @return an action that move the context from state B to A.
     */
    fun getOppositeAction(context: DrawingContext): Action

}
