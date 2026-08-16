package com.taskflow.app.data.local.repository

import com.taskflow.app.data.local.database.dao.CategoryDao
import com.taskflow.app.data.mapper.toDomain
import com.taskflow.app.data.mapper.toEntity
import com.taskflow.app.domain.model.Category
import com.taskflow.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(private val categoryDao: CategoryDao) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addCategory(category: Category): Long {
        val nextSortOrder = categoryDao.getMaxSortOrder() + 1
        return categoryDao.insert(category.toEntity().copy(sortOrder = nextSortOrder))
    }

    override suspend fun updateCategoriesOrder(orderedCategoryIds: List<Long>) {
        val entitiesById = categoryDao.observeAll().first().associateBy { it.id }
        val reordered = orderedCategoryIds.mapIndexedNotNull { index, categoryId ->
            entitiesById[categoryId]?.copy(sortOrder = index)
        }
        categoryDao.updateAll(reordered)
    }
}
