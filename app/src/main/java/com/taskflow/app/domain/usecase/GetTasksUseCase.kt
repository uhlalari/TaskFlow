package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTasksUseCase(private val taskRepository: TaskRepository) {

    operator fun invoke(): Flow<List<Task>> =
        taskRepository.observeTasks().map { tasks ->
            tasks.sortedWith(compareBy({ !it.isOverdue }, { it.nextDueDate }))
        }
}
