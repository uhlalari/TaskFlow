package com.taskflow.app.data.local.repository

import com.taskflow.app.data.local.database.dao.TaskDao
import com.taskflow.app.data.local.database.dao.TaskExecutionDao
import com.taskflow.app.data.mapper.toDomain
import com.taskflow.app.data.mapper.toEntity
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.TaskExecution
import com.taskflow.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneOffset

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val taskExecutionDao: TaskExecutionDao
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> =
        taskDao.observeActiveTasks().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getTaskById(id: Long): Task? =
        taskDao.getById(id)?.toDomain()

    override suspend fun addTask(task: Task): Long =
        taskDao.insert(task.toEntity())

    override suspend fun updateTask(task: Task) =
        taskDao.update(task.toEntity())

    override suspend fun deleteTask(id: Long) =
        taskDao.deleteById(id)

    override suspend fun registerExecution(execution: TaskExecution) =
        taskExecutionDao.insert(execution.toEntity())

    override suspend fun getExecutionHistory(taskId: Long): List<TaskExecution> =
        taskExecutionDao.getHistoryForTask(taskId).map { it.toDomain() }

    override suspend fun deleteExecution(taskId: Long, completedAt: LocalDateTime) =
        taskExecutionDao.delete(taskId, completedAt.toEpochSecond(ZoneOffset.UTC))
}
