package com.taskflow.app.domain.model

import java.time.LocalDateTime

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryId: Long?,
    val recurrenceType: RecurrenceType,
    val customIntervalDays: Int? = null,
    val nextDueDate: LocalDateTime,
    val notificationEnabled: Boolean = true,
    val isActive: Boolean = true
) {
    val isOverdue: Boolean
        get() = isActive && nextDueDate.isBefore(LocalDateTime.now())
}
