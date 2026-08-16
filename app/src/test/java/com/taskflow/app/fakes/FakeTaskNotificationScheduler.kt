package com.taskflow.app.fakes

import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.scheduler.TaskNotificationScheduler

class FakeTaskNotificationScheduler : TaskNotificationScheduler {
    val scheduledTasks = mutableListOf<Task>()
    val cancelledTaskIds = mutableListOf<Long>()

    override fun schedule(task: Task) {
        scheduledTasks += task
    }

    override fun cancel(taskId: Long) {
        cancelledTaskIds += taskId
    }
}
