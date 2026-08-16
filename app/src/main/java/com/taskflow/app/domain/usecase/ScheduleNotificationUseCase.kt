package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.scheduler.TaskNotificationScheduler

class ScheduleNotificationUseCase(private val notificationScheduler: TaskNotificationScheduler) {
    operator fun invoke(task: Task) {
        if (task.notificationEnabled) {
            notificationScheduler.schedule(task)
        }
    }
}
