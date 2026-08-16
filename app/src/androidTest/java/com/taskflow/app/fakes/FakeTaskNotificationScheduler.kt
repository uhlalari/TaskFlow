package com.taskflow.app.fakes

import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.scheduler.TaskNotificationScheduler

class FakeTaskNotificationScheduler : TaskNotificationScheduler {
    override fun schedule(task: Task) = Unit
    override fun cancel(taskId: Long) = Unit
}
