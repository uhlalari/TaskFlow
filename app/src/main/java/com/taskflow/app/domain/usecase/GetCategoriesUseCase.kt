package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.Category
import com.taskflow.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesUseCase(private val categoryRepository: CategoryRepository) {
    operator fun invoke(): Flow<List<Category>> = categoryRepository.observeCategories()
}
