package com.taskflow.app.domain.model

import java.time.LocalDateTime

data class TaskCompletionResult(
    val previousTask: Task,
    val updatedTask: Task,
    val completedAt: LocalDateTime
)
