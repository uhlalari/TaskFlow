package com.taskflow.app.domain.util

import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.domain.model.Task
import java.time.LocalDateTime

class RecurrenceCalculator {

    fun calculateNextDueDate(task: Task, from: LocalDateTime = LocalDateTime.now()): LocalDateTime =
        when (task.recurrenceType) {
            RecurrenceType.DAILY -> from.plusDays(1)
            RecurrenceType.WEEKLY -> from.plusWeeks(1)
            RecurrenceType.MONTHLY -> from.plusMonths(1)
            RecurrenceType.YEARLY -> from.plusYears(1)
            RecurrenceType.CUSTOM_DAYS -> from.plusDays(
                (task.customIntervalDays ?: 1).toLong()
            )
        }
}
