package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.scheduler.TaskNotificationScheduler

class DeleteTaskUseCase(
    private val taskRepository: TaskRepository,
    private val notificationScheduler: TaskNotificationScheduler
) {
    suspend operator fun invoke(taskId: Long) {
        notificationScheduler.cancel(taskId)
        taskRepository.deleteTask(taskId)
    }
}
