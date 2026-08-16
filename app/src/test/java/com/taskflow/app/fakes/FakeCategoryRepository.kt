package com.taskflow.app.fakes

import com.taskflow.app.domain.model.Category
import com.taskflow.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeCategoryRepository(initialCategories: List<Category> = emptyList()) : CategoryRepository {

    private val categoriesFlow = MutableStateFlow(initialCategories)
    private var nextId = (initialCategories.maxOfOrNull { it.id } ?: 0L) + 1

    override fun observeCategories(): StateFlow<List<Category>> = categoriesFlow

    override suspend fun addCategory(category: Category): Long {
        val id = if (category.id != 0L) category.id else nextId++
        val nextSortOrder = (categoriesFlow.value.maxOfOrNull { it.sortOrder } ?: -1) + 1
        categoriesFlow.value = categoriesFlow.value + category.copy(id = id, sortOrder = nextSortOrder)
        return id
    }

    override suspend fun updateCategoriesOrder(orderedCategoryIds: List<Long>) {
        val byId = categoriesFlow.value.associateBy { it.id }
        categoriesFlow.value = orderedCategoryIds.mapIndexedNotNull { index, id ->
            byId[id]?.copy(sortOrder = index)
        }
    }
}
