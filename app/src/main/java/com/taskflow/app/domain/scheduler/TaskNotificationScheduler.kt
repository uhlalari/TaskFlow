package com.taskflow.app.domain.scheduler

import com.taskflow.app.domain.model.Task

interface TaskNotificationScheduler {
    fun schedule(task: Task)
    fun cancel(taskId: Long)
}
