package com.taskflow.app.presentation.addtask

sealed interface AddTaskFormError {
    data object EmptyTitle : AddTaskFormError
    data object EmptyCategoryName : AddTaskFormError
    data object DuplicateCategoryName : AddTaskFormError
    data object Unknown : AddTaskFormError
}
