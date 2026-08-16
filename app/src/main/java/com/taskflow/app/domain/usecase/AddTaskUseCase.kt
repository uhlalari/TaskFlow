package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.util.RecurrenceCalculator
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.TaskValidationException
import com.taskflow.app.domain.repository.TaskRepository
import java.time.LocalDateTime

class AddTaskUseCase(
    private val taskRepository: TaskRepository,
    private val scheduleNotificationUseCase: ScheduleNotificationUseCase,
    private val recurrenceCalculator: RecurrenceCalculator
) {
    suspend operator fun invoke(task: Task, recalculateNextDueDate: Boolean = true): Long {
        if (task.title.isBlank()) throw TaskValidationException.EmptyTitle

        val taskToPersist = if (recalculateNextDueDate) {
            task.copy(nextDueDate = recurrenceCalculator.calculateNextDueDate(task, from = LocalDateTime.now()))
        } else {
            task
        }

        val id = taskRepository.addTask(taskToPersist)
        scheduleNotificationUseCase(taskToPersist.copy(id = id))
        return id
    }
}
