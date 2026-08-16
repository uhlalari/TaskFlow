package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.TaskCompletionResult
import com.taskflow.app.domain.model.TaskExecution
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.util.RecurrenceCalculator
import java.time.LocalDateTime

class CompleteTaskUseCase(
    private val taskRepository: TaskRepository,
    private val scheduleNotificationUseCase: ScheduleNotificationUseCase,
    private val recurrenceCalculator: RecurrenceCalculator
) {
    suspend operator fun invoke(taskId: Long): TaskCompletionResult? {
        val task = taskRepository.getTaskById(taskId) ?: return null
        val completedAt = LocalDateTime.now()

        taskRepository.registerExecution(TaskExecution(taskId = taskId, completedAt = completedAt))

        val updatedTask = task.copy(nextDueDate = recurrenceCalculator.calculateNextDueDate(task))
        taskRepository.updateTask(updatedTask)
        scheduleNotificationUseCase(updatedTask)

        return TaskCompletionResult(previousTask = task, updatedTask = updatedTask, completedAt = completedAt)
    }
}
