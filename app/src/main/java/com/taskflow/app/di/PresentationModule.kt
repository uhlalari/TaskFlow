package com.taskflow.app.di

import com.taskflow.app.presentation.addtask.AddTaskViewModel
import com.taskflow.app.presentation.dashboard.DashboardSettingsViewModel
import com.taskflow.app.presentation.taskdetail.TaskDetailViewModel
import com.taskflow.app.presentation.tasklist.TaskListViewModel
import com.taskflow.app.presentation.theme.ThemeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { TaskListViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { DashboardSettingsViewModel(get(), get(), get()) }
    viewModel { params -> TaskDetailViewModel(params.get(), get(), get()) }
    viewModel { params ->
        AddTaskViewModel(get(), get(), get(), get(), get(), get(), editingTaskId = params.getOrNull())
    }
    viewModel { ThemeViewModel(get()) }
}
