package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.repository.CategoryRepository

class ReorderCategoriesUseCase(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(orderedCategoryIds: List<Long>) {
        categoryRepository.updateCategoriesOrder(orderedCategoryIds)
    }
}
