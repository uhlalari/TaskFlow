package com.taskflow.app.presentation.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.usecase.DeleteTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskId: Long,
    private val taskRepository: TaskRepository,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TaskDetailState())
    val state: StateFlow<TaskDetailState> = _state.asStateFlow()

    init {
        observeTask()
    }

    /**
     * Observa a tarefa em vez de carregá-la uma única vez: ao voltar da tela de edição
     * (que fica por cima desta na pilha), o detalhe reflete o título/descrição/recorrência
     * atualizados sem precisar recriar o ViewModel.
     */
    private fun observeTask() {
        taskRepository.observeTasks()
            .map { tasks -> tasks.find { it.id == taskId } }
            .distinctUntilChanged()
            .onEach { task ->
                val current = _state.value
                // Durante/após a exclusão a tarefa some do Flow; ignoramos esse null para
                // não piscar a mensagem de "tarefa não existe mais" antes de navegar de volta.
                if (task == null && (current.isDeleting || current.isDeleted)) return@onEach

                val history = if (task != null) taskRepository.getExecutionHistory(taskId) else emptyList()
                _state.update { it.copy(task = task, history = history, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onDeleteClick() {
        if (_state.value.isDeleting || _state.value.isDeleted) return

        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            deleteTaskUseCase(taskId)
            _state.update { it.copy(isDeleting = false, isDeleted = true) }
        }
    }
}
