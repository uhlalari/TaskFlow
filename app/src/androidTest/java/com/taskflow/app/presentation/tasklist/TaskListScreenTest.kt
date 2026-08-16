package com.taskflow.app.presentation.tasklist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.taskflow.app.domain.util.RecurrenceCalculator
import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.usecase.AddTaskUseCase
import com.taskflow.app.domain.usecase.CompleteTaskUseCase
import com.taskflow.app.domain.usecase.DeleteTaskUseCase
import com.taskflow.app.domain.usecase.GetCategoriesUseCase
import com.taskflow.app.domain.usecase.GetCurrentWeatherUseCase
import com.taskflow.app.domain.usecase.GetTasksUseCase
import com.taskflow.app.domain.usecase.GetWeatherLocationUseCase
import com.taskflow.app.domain.usecase.ReorderCategoriesUseCase
import com.taskflow.app.domain.usecase.ScheduleNotificationUseCase
import com.taskflow.app.domain.usecase.UndoCompleteTaskUseCase
import com.taskflow.app.fakes.FakeCategoryRepository
import com.taskflow.app.fakes.FakeDashboardPreferencesManager
import com.taskflow.app.fakes.FakeTaskNotificationScheduler
import com.taskflow.app.fakes.FakeTaskRepository
import com.taskflow.app.fakes.FakeWeatherRepository
import com.taskflow.app.presentation.designsystem.TaskFlowTheme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class TaskListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_isDisplayed_whenThereAreNoTasks() {
        val viewModel = buildViewModel(FakeTaskRepository())

        composeTestRule.setContent {
            TaskFlowTheme(darkTheme = true) {
                TaskListScreen(
                    onAddTaskClick = {},
                    onTaskClick = {},
                    onOpenDashboardSettings = {},
                    isDarkTheme = true,
                    onToggleTheme = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule
            .onNodeWithText("Nenhuma tarefa cadastrada ainda. Toque em + para começar.")
            .assertIsDisplayed()
    }

    @Test
    fun taskTitle_isDisplayed_whenThereIsATask() {
        val task = Task(
            id = 1,
            title = "Limpar aquário",
            categoryId = null,
            recurrenceType = RecurrenceType.WEEKLY,
            nextDueDate = LocalDateTime.now().plusDays(1)
        )
        val viewModel = buildViewModel(FakeTaskRepository(listOf(task)))

        composeTestRule.setContent {
            TaskFlowTheme(darkTheme = true) {
                TaskListScreen(
                    onAddTaskClick = {},
                    onTaskClick = {},
                    onOpenDashboardSettings = {},
                    isDarkTheme = true,
                    onToggleTheme = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Limpar aquário").assertIsDisplayed()
    }

    private fun buildViewModel(repository: FakeTaskRepository): TaskListViewModel {
        val scheduler = FakeTaskNotificationScheduler()
        val scheduleNotificationUseCase = ScheduleNotificationUseCase(scheduler)
        val categoryRepository = FakeCategoryRepository()
        return TaskListViewModel(
            getTasksUseCase = GetTasksUseCase(repository),
            getCategoriesUseCase = GetCategoriesUseCase(categoryRepository),
            completeTaskUseCase = CompleteTaskUseCase(
                taskRepository = repository,
                scheduleNotificationUseCase = scheduleNotificationUseCase,
                recurrenceCalculator = RecurrenceCalculator()
            ),
            undoCompleteTaskUseCase = UndoCompleteTaskUseCase(repository, scheduleNotificationUseCase),
            deleteTaskUseCase = DeleteTaskUseCase(repository, scheduler),
            addTaskUseCase = AddTaskUseCase(repository, scheduleNotificationUseCase, RecurrenceCalculator()),
            reorderCategoriesUseCase = ReorderCategoriesUseCase(categoryRepository),
            dashboardPreferencesManager = FakeDashboardPreferencesManager(),
            getCurrentWeatherUseCase = GetCurrentWeatherUseCase(FakeWeatherRepository()),
            getWeatherLocationUseCase = GetWeatherLocationUseCase(FakeWeatherRepository())
        )
    }
}
