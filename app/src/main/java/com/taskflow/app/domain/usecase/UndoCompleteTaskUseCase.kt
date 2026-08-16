package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.TaskCompletionResult
import com.taskflow.app.domain.repository.TaskRepository

class UndoCompleteTaskUseCase(
    private val taskRepository: TaskRepository,
    private val scheduleNotificationUseCase: ScheduleNotificationUseCase
) {
    suspend operator fun invoke(result: TaskCompletionResult) {
        taskRepository.deleteExecution(result.previousTask.id, result.completedAt)
        taskRepository.updateTask(result.previousTask)
        scheduleNotificationUseCase(result.previousTask)
    }
}
