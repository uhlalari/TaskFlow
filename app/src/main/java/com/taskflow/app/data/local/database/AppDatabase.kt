package com.taskflow.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.taskflow.app.data.local.database.dao.CategoryDao
import com.taskflow.app.data.local.database.dao.TaskDao
import com.taskflow.app.data.local.database.dao.TaskExecutionDao
import com.taskflow.app.data.local.database.entities.CategoryEntity
import com.taskflow.app.data.local.database.entities.TaskEntity
import com.taskflow.app.data.local.database.entities.TaskExecutionEntity

@Database(
    entities = [TaskEntity::class, TaskExecutionEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskExecutionDao(): TaskExecutionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "taskflow.db"
    }
}
