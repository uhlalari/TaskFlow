package com.taskflow.app.notification

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.scheduler.TaskNotificationScheduler
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class NotificationScheduler(
    private val workManager: WorkManager,
    private val alarmScheduler: AlarmScheduler
) : TaskNotificationScheduler {

    override fun schedule(task: Task) {
        if (alarmScheduler.canScheduleExactAlarms()) {
            alarmScheduler.schedule(task)
        } else {
            scheduleApproximate(task)
        }
    }

    override fun cancel(taskId: Long) {
        alarmScheduler.cancel(taskId)
        workManager.cancelUniqueWork(workTagFor(taskId))
    }

    private fun scheduleApproximate(task: Task) {
        val delay = Duration.between(LocalDateTime.now(), task.nextDueDate).toMinutes()
            .coerceAtLeast(0)

        val data = Data.Builder()
            .putLong(TaskNotificationWorker.KEY_TASK_ID, task.id)
            .putString(TaskNotificationWorker.KEY_TASK_TITLE, task.title)
            .build()

        val request = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .setInputData(data)
            .addTag(workTagFor(task.id))
            .build()

        workManager.enqueueUniqueWork(
            workTagFor(task.id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun workTagFor(taskId: Long) = "task_notification_$taskId"
}
