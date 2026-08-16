package com.taskflow.app.data.local.database.seed

import com.taskflow.app.data.local.database.dao.CategoryDao
import com.taskflow.app.data.local.database.entities.CategoryEntity

class CategorySeeder(private val categoryDao: CategoryDao) {

    suspend fun seedIfNeeded() {
        DEFAULT_CATEGORIES.forEachIndexed { index, category ->
            categoryDao.insert(category.copy(sortOrder = index))
        }
    }

    private companion object {
        val DEFAULT_CATEGORIES = listOf(
            CategoryEntity(name = "Pets", colorHex = "#2EC4B6", icon = "pets"),
            CategoryEntity(name = "Casa", colorHex = "#E07A5F", icon = "home"),
            CategoryEntity(name = "Saúde", colorHex = "#E63946", icon = "favorite"),
            CategoryEntity(name = "Manutenção", colorHex = "#F4A261", icon = "build")
        )
    }
}
