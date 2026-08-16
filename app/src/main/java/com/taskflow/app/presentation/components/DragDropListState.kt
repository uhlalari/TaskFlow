package com.taskflow.app.presentation.components

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class DragDropListState(private val lazyListState: LazyListState, private val onMove: (Int, Int) -> Unit) {

    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    private var draggingItemInitialOffset by mutableFloatStateOf(0f)
    private var draggedDistance by mutableFloatStateOf(0f)

    private val currentItemInfo: LazyListItemInfo?
        get() = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == draggingItemIndex }

    val draggingItemOffset: Float
        get() = currentItemInfo?.let { draggingItemInitialOffset + draggedDistance - it.offset } ?: 0f

    fun onDragStart(index: Int) {
        draggingItemIndex = index
        draggedDistance = 0f
        draggingItemInitialOffset = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index }
            ?.offset
            ?.toFloat()
            ?: 0f
    }

    fun onDrag(deltaY: Float) {
        draggedDistance += deltaY
        val current = currentItemInfo ?: return

        val draggedTop = current.offset + draggingItemOffset
        val draggedMiddle = draggedTop + current.size / 2f

        val target = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            item.index != draggingItemIndex && draggedMiddle >= item.offset && draggedMiddle <= item.offset + item.size
        }

        if (target != null) {
            onMove(current.index, target.index)
            draggingItemIndex = target.index
        }
    }

    fun onDragEnd() {
        draggingItemIndex = null
        draggedDistance = 0f
        draggingItemInitialOffset = 0f
    }
}

@Composable
fun rememberDragDropListState(lazyListState: LazyListState, onMove: (Int, Int) -> Unit): DragDropListState =
    remember(lazyListState) { DragDropListState(lazyListState, onMove) }
