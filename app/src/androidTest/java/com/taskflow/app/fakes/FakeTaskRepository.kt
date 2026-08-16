package com.taskflow.app.fakes

import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.TaskExecution
import com.taskflow.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

/**
 * Cópia enxuta do fake usado nos testes unitários (`src/test`). Duplicamos aqui porque
 * `test` e `androidTest` são source sets independentes no Gradle; para projetos maiores
 * vale a pena extrair um source set compartilhado (`sharedTest`) ou um módulo
 * `:testing` — decidimos não fazer isso ainda dado o tamanho atual do app.
 */
class FakeTaskRepository(initialTasks: List<Task> = emptyList()) : TaskRepository {

    private val tasksFlow = MutableStateFlow(initialTasks)
    private val executions = mutableListOf<TaskExecution>()
    private var nextId = (initialTasks.maxOfOrNull { it.id } ?: 0L) + 1

    override fun observeTasks(): StateFlow<List<Task>> = tasksFlow

    override suspend fun getTaskById(id: Long): Task? = tasksFlow.value.find { it.id == id }

    override suspend fun addTask(task: Task): Long {
        val id = if (task.id != 0L) task.id else nextId++
        tasksFlow.value = tasksFlow.value + task.copy(id = id)
        return id
    }

    override suspend fun updateTask(task: Task) {
        tasksFlow.value = tasksFlow.value.map { if (it.id == task.id) task else it }
    }

    override suspend fun deleteTask(id: Long) {
        tasksFlow.value = tasksFlow.value.filterNot { it.id == id }
    }

    override suspend fun registerExecution(execution: TaskExecution) {
        executions += execution
    }

    override suspend fun getExecutionHistory(taskId: Long): List<TaskExecution> =
        executions.filter { it.taskId == taskId }

    override suspend fun deleteExecution(taskId: Long, completedAt: LocalDateTime) {
        executions.removeAll { it.taskId == taskId && it.completedAt == completedAt }
    }
}
