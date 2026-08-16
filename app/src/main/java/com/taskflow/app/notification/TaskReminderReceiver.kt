package com.taskflow.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: return
        if (taskId == -1L) return

        val data = Data.Builder()
            .putLong(TaskNotificationWorker.KEY_TASK_ID, taskId)
            .putString(TaskNotificationWorker.KEY_TASK_TITLE, taskTitle)
            .build()

        val request = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
    }
}
