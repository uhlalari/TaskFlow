package com.taskflow.app.presentation.tasklist

import com.taskflow.app.presentation.dashboard.DashboardWidgetType
import com.taskflow.app.presentation.model.TaskUiModel
import com.taskflow.app.presentation.model.WeatherUiModel

data class TaskListState(
    val sections: List<TaskSection> = emptyList(),
    val overdueCount: Int = 0,
    val dueThisWeekCount: Int = 0,
    val nextTask: TaskUiModel? = null,
    val todayWeather: WeatherUiModel? = null,
    val isRefreshingWeather: Boolean = false,
    val enabledWidgets: List<DashboardWidgetType> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false
) {
    val isEmpty: Boolean get() = sections.isEmpty()
}

data class TaskSection(
    val categoryId: Long?,
    val categoryName: String?,
    val colorHex: String?,
    val tasks: List<TaskUiModel>
)
