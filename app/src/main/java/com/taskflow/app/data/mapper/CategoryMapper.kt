package com.taskflow.app.data.mapper

import com.taskflow.app.data.local.database.entities.CategoryEntity
import com.taskflow.app.domain.model.Category

fun CategoryEntity.toDomain(): Category = Category(id, name, colorHex, icon, sortOrder)

fun Category.toEntity(): CategoryEntity = CategoryEntity(id, name, colorHex, icon, sortOrder)
