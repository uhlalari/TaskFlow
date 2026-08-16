package com.taskflow.app.presentation.addtask

import com.taskflow.app.domain.model.Category
import com.taskflow.app.domain.model.RecurrenceType

data class AddTaskState(
    val title: String = "",
    val description: String = "",
    val recurrenceType: RecurrenceType = RecurrenceType.DAILY,
    val customIntervalDaysText: String = "",
    val selectedCategoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: AddTaskFormError? = null,
    val isAddCategoryDialogVisible: Boolean = false,
    val newCategoryError: AddTaskFormError? = null,
    val isEditMode: Boolean = false,
    val isLoadingTaskToEdit: Boolean = false
)
