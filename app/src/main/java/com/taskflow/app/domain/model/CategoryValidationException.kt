package com.taskflow.app.domain.model

sealed class CategoryValidationException(message: String) : Exception(message) {
    data object EmptyName : CategoryValidationException("O nome da categoria não pode ser vazio")
    data object DuplicateName : CategoryValidationException("Já existe uma categoria com esse nome")
}
