package com.taskflow.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.taskflow.app.data.local.database.entities.TaskExecutionEntity

@Dao
interface TaskExecutionDao {

    @Insert
    suspend fun insert(execution: TaskExecutionEntity)

    @Query("SELECT * FROM task_executions WHERE taskId = :taskId ORDER BY completedAtEpochSeconds DESC")
    suspend fun getHistoryForTask(taskId: Long): List<TaskExecutionEntity>

    @Query("DELETE FROM task_executions WHERE taskId = :taskId AND completedAtEpochSeconds = :completedAtEpochSeconds")
    suspend fun delete(taskId: Long, completedAtEpochSeconds: Long)
}
