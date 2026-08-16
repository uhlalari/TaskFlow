package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.TaskValidationException
import com.taskflow.app.domain.repository.TaskRepository

class UpdateTaskUseCase(
    private val taskRepository: TaskRepository,
    private val scheduleNotificationUseCase: ScheduleNotificationUseCase
) {
    suspend operator fun invoke(task: Task) {
        if (task.title.isBlank()) throw TaskValidationException.EmptyTitle

        taskRepository.updateTask(task)
        scheduleNotificationUseCase(task)
    }
}
