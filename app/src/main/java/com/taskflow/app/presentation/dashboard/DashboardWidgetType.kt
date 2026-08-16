package com.taskflow.app.presentation.dashboard

enum class DashboardWidgetType {
    OVERVIEW
}

fun List<String>.toDashboardWidgetTypes(): List<DashboardWidgetType> =
    mapNotNull { key -> runCatching { DashboardWidgetType.valueOf(key) }.getOrNull() }
