package com.taskflow.app.presentation.tasklist

sealed interface TaskListEvent {
    data class TaskDeleted(val taskTitle: String) : TaskListEvent
    data class TaskCompleted(val taskTitle: String, val nextDueDateLabel: String) : TaskListEvent
}
