package com.taskflow.app.data.mapper

import com.taskflow.app.data.local.database.entities.TaskEntity
import com.taskflow.app.data.local.database.entities.TaskExecutionEntity
import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.TaskExecution
import java.time.LocalDateTime
import java.time.ZoneOffset

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    categoryId = categoryId,
    recurrenceType = RecurrenceType.valueOf(recurrenceType),
    customIntervalDays = customIntervalDays,
    nextDueDate = LocalDateTime.ofEpochSecond(nextDueDateEpochSeconds, 0, ZoneOffset.UTC),
    notificationEnabled = notificationEnabled,
    isActive = isActive
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    categoryId = categoryId,
    recurrenceType = recurrenceType.name,
    customIntervalDays = customIntervalDays,
    nextDueDateEpochSeconds = nextDueDate.toEpochSecond(ZoneOffset.UTC),
    notificationEnabled = notificationEnabled,
    isActive = isActive
)

fun TaskExecutionEntity.toDomain(): TaskExecution = TaskExecution(
    id = id,
    taskId = taskId,
    completedAt = LocalDateTime.ofEpochSecond(completedAtEpochSeconds, 0, ZoneOffset.UTC)
)

fun TaskExecution.toEntity(): TaskExecutionEntity = TaskExecutionEntity(
    id = id,
    taskId = taskId,
    completedAtEpochSeconds = completedAt.toEpochSecond(ZoneOffset.UTC)
)
