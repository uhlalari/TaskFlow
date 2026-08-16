package com.taskflow.app.presentation.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.usecase.DeleteTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskId: Long,
    private val taskRepository: TaskRepository,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TaskDetailState())
    val state: StateFlow<TaskDetailState> = _state.asStateFlow()

    init {
        loadTask()
    }

    private fun loadTask() {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            val history = taskRepository.getExecutionHistory(taskId)
            _state.value = TaskDetailState(task = task, history = history, isLoading = false)
        }
    }

    fun onDeleteClick() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true)
            deleteTaskUseCase(taskId)
            _state.value = _state.value.copy(isDeleting = false, isDeleted = true)
        }
    }
}
