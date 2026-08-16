package com.taskflow.app.domain.model

import java.time.LocalDateTime

data class TaskExecution(
    val id: Long = 0,
    val taskId: Long,
    val completedAt: LocalDateTime
)
