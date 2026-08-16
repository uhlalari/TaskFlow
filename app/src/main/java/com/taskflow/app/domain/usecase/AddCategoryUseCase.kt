package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.Category
import com.taskflow.app.domain.model.CategoryValidationException
import com.taskflow.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first

private const val DEFAULT_USER_CATEGORY_COLOR = "#6C63FF"
private const val DEFAULT_USER_CATEGORY_ICON = "label"

class AddCategoryUseCase(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(name: String): Long {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) throw CategoryValidationException.EmptyName
        val nameAlreadyExists = categoryRepository.observeCategories().first()
            .any { it.name.trim().equals(trimmedName, ignoreCase = true) }
        if (nameAlreadyExists) throw CategoryValidationException.DuplicateName

        return categoryRepository.addCategory(
            Category(
                name = trimmedName,
                colorHex = DEFAULT_USER_CATEGORY_COLOR,
                icon = DEFAULT_USER_CATEGORY_ICON
            )
        )
    }
}
