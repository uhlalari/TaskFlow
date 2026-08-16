package com.taskflow.app.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class TaskUiModel(
    val id: Long,
    val title: String,
    val dueDateLabel: String,
    val isOverdue: Boolean,
    val dueInMinutes: Long
)
