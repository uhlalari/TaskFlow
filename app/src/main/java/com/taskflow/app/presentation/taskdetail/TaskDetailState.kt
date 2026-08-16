package com.taskflow.app.presentation.taskdetail

import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.TaskExecution

data class TaskDetailState(
    val task: Task? = null,
    val history: List<TaskExecution> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false
) {
    val isNotFound: Boolean
        get() = !isLoading && task == null && !isDeleted
}
