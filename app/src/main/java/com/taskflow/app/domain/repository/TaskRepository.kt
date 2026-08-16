package com.taskflow.app.domain.repository

import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.TaskExecution
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: Long): Task?
    suspend fun addTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: Long)
    suspend fun registerExecution(execution: TaskExecution)
    suspend fun getExecutionHistory(taskId: Long): List<TaskExecution>
    suspend fun deleteExecution(taskId: Long, completedAt: LocalDateTime)
}
