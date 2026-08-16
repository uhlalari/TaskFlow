package com.taskflow.app.domain.model

sealed class TaskValidationException(message: String) : Exception(message) {
    data object EmptyTitle : TaskValidationException("O título da tarefa não pode ser vazio")
}
