package com.taskflow.app.presentation.util

import java.time.format.DateTimeFormatter

object DateFormats {
    val TASK_DUE_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
}
