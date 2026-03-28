package com.snake.drawingview.state

import com.snake.drawingview.actions.Action

/**
 * This class is responsible for managing two stacks that are used to store undo & redo actions.
 * There is a limit to the max size of the stored actions. When this limit is reached, old actions
 * from both stacks are dropped, until the size becomes bellow the max limit.
 */

private val MemoryLimit = Runtime.getRuntime().maxMemory() / 5 //TODO: allow client to choose this value
private const val MAX_HISTORY_SIZE = 50
class ActionsStacks {

    var onHistoryLimitReached: (() -> Unit)? = null

    private var hasNotifiedLimit = false

    private val undoStack = mutableListOf<Action>()
    private val redoStack = mutableListOf<Action>()

    fun pushRedo(action: Action) {
        redoStack.push(action)
    }

    fun pushUndo(action: Action) {
        undoStack.push(action)
    }

    fun clearRedoStack() {
        redoStack.clear()
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
        hasNotifiedLimit = false
    }

    fun popUndo(): Action {
        return undoStack.pop()
    }

    fun popRedo(): Action {
        return redoStack.pop()
    }

    fun hasRedo(): Boolean {
        return redoStack.size != 0
    }

    fun hasUndo(): Boolean {
        return undoStack.size != 0
    }

    private fun MutableList<Action>.pop(): Action {
        return removeAt(size - 1)
    }

//    private fun MutableList<Action>.push(action: Action) {
//        while (findCurrentSize() + action.size > MemoryLimit) {
//            if (!dropAction()) //the size of this record is so large, it is larger than the MaxSize.
//                return
//        }
//        add(action)
//    }

    private fun MutableList<Action>.push(action: Action) {
        while (this.size >= MAX_HISTORY_SIZE) {
            this.removeAt(0)

            if (!hasNotifiedLimit) {
                hasNotifiedLimit = true
                onHistoryLimitReached?.invoke()
            }

        }
        add(action)
    }

    private fun findCurrentSize(): Long {
        var size = 0L
        for (entry in undoStack) {
            size += entry.size
        }
        for (entry in redoStack) {
            size += entry.size
        }
        return size
    }

    private fun dropAction(): Boolean {
        if (undoStack.size == 0 && redoStack.size == 0) return false
        if (undoStack.size >= redoStack.size) undoStack.removeAt(0) else redoStack.removeAt(0)
        return true
    }

}