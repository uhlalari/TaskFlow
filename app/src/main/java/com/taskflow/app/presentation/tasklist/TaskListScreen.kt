package com.taskflow.app.presentation.tasklist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskflow.app.R
import com.taskflow.app.presentation.components.DragDropListState
import com.taskflow.app.presentation.components.HomeOverviewCard
import com.taskflow.app.presentation.components.TaskCarouselItem
import com.taskflow.app.presentation.components.rememberDragDropListState
import com.taskflow.app.presentation.dashboard.DashboardWidgetType
import com.taskflow.app.presentation.designsystem.GlassButton
import com.taskflow.app.presentation.designsystem.GlassCard
import com.taskflow.app.presentation.designsystem.GlassPrimary
import com.taskflow.app.presentation.designsystem.GlassTertiary
import com.taskflow.app.presentation.designsystem.LiquidBackground
import org.koin.androidx.compose.koinViewModel

private const val STATE_TRANSITION_DURATION_MS = 300

private const val BANNER_ITEM_COUNT = 1

@Composable
fun TaskListScreen(
    onAddTaskClick: () -> Unit,
    onTaskClick: (Long) -> Unit,
    onOpenDashboardSettings: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    viewModel: TaskListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedMessageTemplate = stringResource(R.string.task_deleted_snackbar_message)
    val undoActionLabel = stringResource(R.string.task_deleted_undo_action)
    val completedMessageTemplate = stringResource(R.string.task_completed_snackbar_message)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            snackbarHostState.currentSnackbarData?.dismiss()
            when (event) {
                is TaskListEvent.TaskDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = deletedMessageTemplate.format(event.taskTitle),
                        actionLabel = undoActionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndoDelete()
                }

                is TaskListEvent.TaskCompleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = completedMessageTemplate.format(event.taskTitle, event.nextDueDateLabel),
                        actionLabel = undoActionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndoComplete()
                }
            }
        }
    }

    val onResumeRefreshWeather by rememberUpdatedState(viewModel::refreshWeather)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onResumeRefreshWeather()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LiquidBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = GlassPrimary,
                        contentColor = Color.Black,
                        actionColor = Color.Black
                    )
                }
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.task_list_title)) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(onClick = onOpenDashboardSettings) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = stringResource(R.string.dashboard_settings_content_description),
                                tint = GlassPrimary
                            )
                        }
                        IconButton(onClick = { onToggleTheme(!isDarkTheme) }) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = stringResource(
                                    if (isDarkTheme) {
                                        R.string.theme_toggle_light_content_description
                                    } else {
                                        R.string.theme_toggle_dark_content_description
                                    }
                                ),
                                tint = GlassPrimary
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddTaskClick, containerColor = GlassPrimary) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.task_list_add_content_description)
                    )
                }
            }
        ) { padding ->
            AnimatedContent(
                targetState = TaskListUiPhase.from(state),
                label = "task_list_phase",
                transitionSpec = {
                    fadeIn(tween(STATE_TRANSITION_DURATION_MS)) togetherWith
                        fadeOut(tween(STATE_TRANSITION_DURATION_MS))
                }
            ) { phase ->
                when (phase) {
                    TaskListUiPhase.LOADING -> LoadingContent(padding)
                    TaskListUiPhase.ERROR -> ErrorContent(padding, onRetry = viewModel::retry)
                    TaskListUiPhase.EMPTY -> EmptyContent(padding)
                    TaskListUiPhase.CONTENT -> HomeContent(
                        state = state,
                        padding = padding,
                        onComplete = viewModel::onCompleteTask,
                        onTaskClick = onTaskClick,
                        onDeleteTask = viewModel::onDeleteTask,
                        onReorderSections = viewModel::onReorderSections,
                        onRefreshWeather = viewModel::refreshWeather
                    )
                }
            }
        }
    }
}

private enum class TaskListUiPhase {
    LOADING, ERROR, EMPTY, CONTENT;

    companion object {
        fun from(state: TaskListState): TaskListUiPhase = when {
            state.isLoading -> LOADING
            state.hasError -> ERROR
            state.isEmpty -> EMPTY
            else -> CONTENT
        }
    }
}

@Composable
private fun LoadingContent(padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = GlassPrimary)
    }
}

@Composable
private fun ErrorContent(padding: PaddingValues, onRetry: () -> Unit) {
    val message = stringResource(R.string.task_list_error_message)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(modifier = Modifier.semantics { contentDescription = message }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(message, textAlign = TextAlign.Center, color = GlassTertiary)
                GlassButton(
                    text = stringResource(R.string.task_list_retry_button),
                    onClick = onRetry
                )
            }
        }
    }
}

@Composable
private fun EmptyContent(padding: PaddingValues) {
    val message = stringResource(R.string.task_list_empty_message)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(modifier = Modifier.semantics { contentDescription = message }) {
            Text(message, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun HomeContent(
    state: TaskListState,
    padding: PaddingValues,
    onComplete: (Long) -> Unit,
    onTaskClick: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onReorderSections: (List<Long>) -> Unit,
    onRefreshWeather: () -> Unit
) {
    var orderedSections by remember(state.sections) { mutableStateOf(state.sections) }
    val listState = rememberLazyListState()

    val dragDropState = rememberDragDropListState(listState) { fromItemIndex, toItemIndex ->
        val fromSection = fromItemIndex - BANNER_ITEM_COUNT
        val toSection = toItemIndex - BANNER_ITEM_COUNT
        val isReorderable = fromSection in orderedSections.indices &&
            toSection in orderedSections.indices &&
            orderedSections[fromSection].categoryId != null &&
            orderedSections[toSection].categoryId != null

        if (isReorderable) {
            orderedSections = orderedSections.toMutableList().apply {
                add(toSection, removeAt(fromSection))
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshingWeather,
        onRefresh = onRefreshWeather,
        modifier = Modifier.fillMaxSize().padding(padding)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(state.enabledWidgets, key = { "widget_$it" }) { widgetType ->
                DashboardWidget(
                    widgetType = widgetType,
                    state = state,
                    onTaskClick = onTaskClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            itemsIndexed(orderedSections, key = { _, section -> section.categoryId ?: -1L }) { index, section ->
                val itemIndex = index + BANNER_ITEM_COUNT
                val isDragging = dragDropState.draggingItemIndex == itemIndex

                TaskSectionCarousel(
                    section = section,
                    modifier = Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer { translationY = if (isDragging) dragDropState.draggingItemOffset else 0f }
                        .let { if (isDragging) it else it.animateItem() },
                    onDragHandlePressed = { dragDropState.onDragStart(itemIndex) },
                    onDrag = dragDropState::onDrag,
                    onDragEnd = {
                        dragDropState.onDragEnd()
                        onReorderSections(orderedSections.mapNotNull { it.categoryId })
                    },
                    onComplete = onComplete,
                    onTaskClick = onTaskClick,
                    onDeleteTask = onDeleteTask
                )
            }
        }
    }
}

@Composable
private fun DashboardWidget(
    widgetType: DashboardWidgetType,
    state: TaskListState,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (widgetType) {
        DashboardWidgetType.OVERVIEW -> HomeOverviewCard(
            overdueCount = state.overdueCount,
            dueThisWeekCount = state.dueThisWeekCount,
            nextTask = state.nextTask,
            todayWeather = state.todayWeather,
            onTaskClick = onTaskClick,
            modifier = modifier
        )
    }
}

@Composable
private fun TaskSectionCarousel(
    section: TaskSection,
    onDragHandlePressed: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onComplete: (Long) -> Unit,
    onTaskClick: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = section.colorHex
        ?.runCatching { Color(toColorInt()) }
        ?.getOrNull()
        ?: GlassPrimary

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.categoryName ?: stringResource(R.string.task_list_uncategorized_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (section.categoryId != null) {
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = stringResource(R.string.task_section_reorder_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.pointerInput(section.categoryId) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragHandlePressed() },
                            onDrag = { change, dragAmount -> change.consume(); onDrag(dragAmount.y) },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() }
                        )
                    }
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(section.tasks, key = { it.id }) { task ->
                TaskCarouselItem(
                    task = task,
                    accentColor = accentColor,
                    onClick = { onTaskClick(task.id) },
                    onComplete = { onComplete(task.id) },
                    onDelete = { onDeleteTask(task.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}
