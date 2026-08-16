package com.taskflow.app.domain.repository

import com.taskflow.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun addCategory(category: Category): Long
    suspend fun updateCategoriesOrder(orderedCategoryIds: List<Long>)
}
