package com.taskflow.app.presentation.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.data.local.preferences.DashboardPreferencesManager
import com.taskflow.app.domain.model.Category
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.TaskCompletionResult
import com.taskflow.app.domain.usecase.AddTaskUseCase
import com.taskflow.app.domain.usecase.CompleteTaskUseCase
import com.taskflow.app.domain.usecase.DeleteTaskUseCase
import com.taskflow.app.domain.usecase.GetCategoriesUseCase
import com.taskflow.app.domain.usecase.GetCurrentWeatherUseCase
import com.taskflow.app.domain.usecase.GetTasksUseCase
import com.taskflow.app.domain.usecase.GetWeatherLocationUseCase
import com.taskflow.app.domain.usecase.ReorderCategoriesUseCase
import com.taskflow.app.domain.usecase.UndoCompleteTaskUseCase
import com.taskflow.app.presentation.dashboard.toDashboardWidgetTypes
import com.taskflow.app.presentation.model.TaskUiModel
import com.taskflow.app.presentation.model.WeatherUiModel
import com.taskflow.app.presentation.util.DateFormats
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.roundToInt

private const val DUE_THIS_WEEK_DAYS = 7L

@Suppress("LongParameterList")
class TaskListViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val undoCompleteTaskUseCase: UndoCompleteTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val reorderCategoriesUseCase: ReorderCategoriesUseCase,
    private val dashboardPreferencesManager: DashboardPreferencesManager,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val getWeatherLocationUseCase: GetWeatherLocationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TaskListState())
    val state: StateFlow<TaskListState> = _state.asStateFlow()

    private val _events = Channel<TaskListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var lastDeletedTask: Task? = null
    private var lastCompletion: TaskCompletionResult? = null
    private var latestTasks: List<Task> = emptyList()

    init {
        observeTasks()
        observeWeather()
    }

    private fun observeWeather() {
        getWeatherLocationUseCase()
            .onEach { fetchAndApplyWeather() }
            .launchIn(viewModelScope)
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshingWeather = true) }
            fetchAndApplyWeather()
            _state.update { it.copy(isRefreshingWeather = false) }
        }
    }

    private suspend fun fetchAndApplyWeather() {
        val weather = getCurrentWeatherUseCase()
        _state.update { state ->
            state.copy(
                todayWeather = weather?.let {
                    WeatherUiModel(
                        condition = it.condition,
                        temperatureLabel = "${it.temperatureCelsius.roundToInt()}°"
                    )
                }
            )
        }
    }

    private fun observeTasks() {
        _state.update { it.copy(isLoading = true, hasError = false) }
        combine(
            getTasksUseCase(),
            getCategoriesUseCase(),
            dashboardPreferencesManager.enabledWidgetKeys
        ) { tasks, categories, widgetKeys -> Triple(tasks, categories, widgetKeys) }
            .onEach { (tasks, categories, widgetKeys) ->
                latestTasks = tasks
                _state.update {
                    it.copy(
                        sections = buildSections(tasks, categories),
                        overdueCount = tasks.count { task -> task.isOverdue },
                        dueThisWeekCount = tasks.count { task -> task.isDueThisWeek() },
                        nextTask = tasks.firstOrNull()?.toUiModel(),
                        enabledWidgets = widgetKeys.toDashboardWidgetTypes(),
                        isLoading = false,
                        hasError = false
                    )
                }
            }
            .catch { _state.update { it.copy(isLoading = false, hasError = true) } }
            .launchIn(viewModelScope)
    }

    private fun buildSections(tasks: List<Task>, categories: List<Category>): List<TaskSection> {
        val tasksByCategory = tasks.groupBy { it.categoryId }

        val categorySections = categories.mapNotNull { category ->
            val categoryTasks = tasksByCategory[category.id].orEmpty()
            if (categoryTasks.isEmpty()) return@mapNotNull null
            TaskSection(
                categoryId = category.id,
                categoryName = category.name,
                colorHex = category.colorHex,
                tasks = categoryTasks.map { it.toUiModel() }
            )
        }

        val uncategorizedTasks = tasksByCategory[null].orEmpty()
        val uncategorizedSection = uncategorizedTasks.takeIf { it.isNotEmpty() }?.let {
            TaskSection(categoryId = null, categoryName = null, colorHex = null, tasks = it.map { task -> task.toUiModel() })
        }

        return categorySections + listOfNotNull(uncategorizedSection)
    }

    private fun Task.isDueThisWeek(): Boolean =
        !isOverdue && nextDueDate.isBefore(LocalDateTime.now().plusDays(DUE_THIS_WEEK_DAYS))

    private fun Task.toUiModel(): TaskUiModel = TaskUiModel(
        id = id,
        title = title,
        dueDateLabel = nextDueDate.format(DateFormats.TASK_DUE_DATE),
        isOverdue = isOverdue,
        dueInMinutes = Duration.between(LocalDateTime.now(), nextDueDate).toMinutes()
    )

    fun retry() {
        observeTasks()
    }

    fun onCompleteTask(taskId: Long) {
        viewModelScope.launch {
            val result = completeTaskUseCase(taskId) ?: return@launch
            lastCompletion = result
            _events.send(
                TaskListEvent.TaskCompleted(
                    taskTitle = result.previousTask.title,
                    nextDueDateLabel = result.updatedTask.nextDueDate.format(DateFormats.TASK_DUE_DATE)
                )
            )
        }
    }

    fun onUndoComplete() {
        val completion = lastCompletion ?: return
        lastCompletion = null
        viewModelScope.launch {
            undoCompleteTaskUseCase(completion)
        }
    }

    fun onDeleteTask(taskId: Long) {
        val task = latestTasks.find { it.id == taskId } ?: return
        viewModelScope.launch {
            lastDeletedTask = task
            deleteTaskUseCase(taskId)
            _events.send(TaskListEvent.TaskDeleted(task.title))
        }
    }

    fun onUndoDelete() {
        val task = lastDeletedTask ?: return
        lastDeletedTask = null
        viewModelScope.launch {
            addTaskUseCase(task.copy(id = 0), recalculateNextDueDate = false)
        }
    }

    fun onReorderSections(orderedCategoryIds: List<Long>) {
        viewModelScope.launch {
            reorderCategoriesUseCase(orderedCategoryIds)
        }
    }

    private fun MutableStateFlow<TaskListState>.update(transform: (TaskListState) -> TaskListState) {
        value = transform(value)
    }
}
