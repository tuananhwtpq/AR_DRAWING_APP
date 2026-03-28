package com.snake.drawingview.state

import com.snake.drawingview.DrawingContext
import com.snake.drawingview.actions.Action

class DrawingState(
    private val context: DrawingContext,
) {

    private val listeners = mutableSetOf<(DrawingState) -> Unit>()

    private var actionsStacks = ActionsStacks()

    fun setOnHistoryLimitReachedListener(listener: () -> Unit) {
        actionsStacks.onHistoryLimitReached = listener
    }

    fun addOnStateChangedListener(listener: (DrawingState) -> Unit) {
        listeners.add(listener)
    }

    fun removeOnStateChangedListener(listener: (DrawingState) -> Unit) {
        listeners.remove(listener)
    }

    internal fun update(action: Action) {
        val oppositeAction = action.getOppositeAction(context)
        actionsStacks.clearRedoStack()
        actionsStacks.pushUndo(oppositeAction)
        action.perform(context)
        notifyListeners()
    }

    fun setActionStacks(actionsStacks: ActionsStacks) {
        this.actionsStacks = actionsStacks
        notifyListeners()
    }

    internal fun reset() {
        actionsStacks.clear()
        notifyListeners()
    }

    fun undo() {
        if (!canCallUndo()) {
            throw IllegalStateException("Can't undo. Call rasmState.canCallUndo() before calling this method.")
        }
        val action = actionsStacks.popUndo()
        val oppositeAction = action.getOppositeAction(context)
        actionsStacks.pushRedo(oppositeAction)
        action.perform(context)
        notifyListeners()
    }

    fun redo() {
        if (!canCallRedo()) {
            throw IllegalStateException("Can't redo. Call rasmState.canCallRedo() before calling this method.")
        }
        val action = actionsStacks.popRedo()
        val oppositeAction = action.getOppositeAction(context)
        actionsStacks.pushUndo(oppositeAction)
        action.perform(context)
        notifyListeners()
    }

    fun canCallUndo() = actionsStacks.hasUndo()

    fun canCallRedo() = actionsStacks.hasRedo()

    private fun notifyListeners() {
        for (listener in listeners.toList()) {
            listener.invoke(this)
        }
    }

}
