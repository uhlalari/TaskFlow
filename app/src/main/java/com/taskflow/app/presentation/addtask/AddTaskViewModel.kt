package com.taskflow.app.presentation.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.domain.util.RecurrenceCalculator
import com.taskflow.app.domain.model.CategoryValidationException
import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.TaskValidationException
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.usecase.AddCategoryUseCase
import com.taskflow.app.domain.usecase.AddTaskUseCase
import com.taskflow.app.domain.usecase.GetCategoriesUseCase
import com.taskflow.app.domain.usecase.UpdateTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Suppress("LongParameterList")
class AddTaskViewModel(
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val taskRepository: TaskRepository,
    private val recurrenceCalculator: RecurrenceCalculator,
    private val editingTaskId: Long? = null
) : ViewModel() {

    private val _state = MutableStateFlow(AddTaskState(isEditMode = editingTaskId != null))
    val state: StateFlow<AddTaskState> = _state.asStateFlow()

    private var originalTask: Task? = null

    init {
        getCategoriesUseCase()
            .onEach { categories -> _state.value = _state.value.copy(categories = categories) }
            .launchIn(viewModelScope)

        if (editingTaskId != null) {
            loadTaskToEdit(editingTaskId)
        }
    }

    private fun loadTaskToEdit(taskId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingTaskToEdit = true)
            val task = taskRepository.getTaskById(taskId)
            originalTask = task
            _state.value = if (task != null) {
                _state.value.copy(
                    title = task.title,
                    description = task.description,
                    recurrenceType = task.recurrenceType,
                    customIntervalDaysText = task.customIntervalDays?.toString().orEmpty(),
                    selectedCategoryId = task.categoryId,
                    isLoadingTaskToEdit = false
                )
            } else {
                _state.value.copy(isLoadingTaskToEdit = false)
            }
        }
    }

    fun onTitleChange(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    fun onDescriptionChange(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun onRecurrenceTypeChange(type: RecurrenceType) {
        _state.value = _state.value.copy(recurrenceType = type)
    }

    fun onCustomIntervalDaysChange(text: String) {
        if (text.isEmpty() || text.all(Char::isDigit)) {
            _state.value = _state.value.copy(customIntervalDaysText = text)
        }
    }

    fun onCategorySelected(categoryId: Long) {
        _state.value = _state.value.copy(selectedCategoryId = categoryId)
    }

    fun onAddCategoryClick() {
        _state.value = _state.value.copy(isAddCategoryDialogVisible = true, newCategoryError = null)
    }

    fun onDismissAddCategoryDialog() {
        _state.value = _state.value.copy(isAddCategoryDialogVisible = false, newCategoryError = null)
    }

    fun onConfirmAddCategory(name: String) {
        viewModelScope.launch {
            runCatching { addCategoryUseCase(name) }
                .onSuccess { newCategoryId ->
                    _state.value = _state.value.copy(
                        isAddCategoryDialogVisible = false,
                        newCategoryError = null,
                        selectedCategoryId = newCategoryId
                    )
                }
                .onFailure { throwable ->
                    val error = when (throwable) {
                        is CategoryValidationException.EmptyName -> AddTaskFormError.EmptyCategoryName
                        is CategoryValidationException.DuplicateName -> AddTaskFormError.DuplicateCategoryName
                        else -> AddTaskFormError.Unknown
                    }
                    _state.value = _state.value.copy(newCategoryError = error)
                }
        }
    }

    fun onSaveClick() {
        val current = _state.value

        viewModelScope.launch {
            _state.value = current.copy(isSaving = true, error = null)
            runCatching {
                if (current.isEditMode) {
                    updateTaskUseCase(buildUpdatedTask(current))
                } else {
                    addTaskUseCase(buildNewTask(current))
                }
            }.onSuccess {
                _state.value = _state.value.copy(isSaving = false, isSaved = true)
            }.onFailure { throwable ->
                val error = when (throwable) {
                    is TaskValidationException.EmptyTitle -> AddTaskFormError.EmptyTitle
                    else -> AddTaskFormError.Unknown
                }
                _state.value = _state.value.copy(isSaving = false, error = error)
            }
        }
    }

    private fun buildNewTask(current: AddTaskState): Task = Task(
        title = current.title,
        description = current.description,
        categoryId = current.selectedCategoryId,
        recurrenceType = current.recurrenceType,
        customIntervalDays = current.customIntervalDaysText.toIntOrNull(),
        nextDueDate = LocalDateTime.now()
    )

    private fun buildUpdatedTask(current: AddTaskState): Task {
        val original = requireNotNull(originalTask) { "onSaveClick chamado em modo de edição sem tarefa carregada" }
        val customIntervalDays = current.customIntervalDaysText.toIntOrNull()
        val recurrenceChanged = original.recurrenceType != current.recurrenceType ||
            (current.recurrenceType == RecurrenceType.CUSTOM_DAYS && original.customIntervalDays != customIntervalDays)

        val updated = original.copy(
            title = current.title,
            description = current.description,
            categoryId = current.selectedCategoryId,
            recurrenceType = current.recurrenceType,
            customIntervalDays = customIntervalDays
        )

        return if (recurrenceChanged) {
            updated.copy(nextDueDate = recurrenceCalculator.calculateNextDueDate(updated, from = LocalDateTime.now()))
        } else {
            updated
        }
    }
}
