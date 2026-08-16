package com.taskflow.app.presentation.tasklist

import app.cash.turbine.test
import com.taskflow.app.domain.util.RecurrenceCalculator
import com.taskflow.app.domain.model.Category
import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.CurrentWeather
import com.taskflow.app.domain.model.WeatherCondition
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
import com.taskflow.app.presentation.dashboard.DashboardWidgetType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial expoe tarefas ordenadas com atrasadas primeiro dentro da secao`() = runTest {
        val overdue = task(id = 1, dueInDays = -2)
        val upcoming = task(id = 2, dueInDays = 3)
        val repository = FakeTaskRepository(listOf(upcoming, overdue))
        val viewModel = buildViewModel(taskRepository = repository)

        viewModel.state.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val loaded = awaitItem()
            assertEquals(false, loaded.isLoading)
            // Sem categoria atribuída, ambas caem na seção "sem categoria" única.
            assertEquals(listOf(1L, 2L), loaded.sections.single().tasks.map { it.id })
        }
    }

    @Test
    fun `agrupa tarefas por categoria respeitando a ordem vinda do repositorio e mantem sem categoria por ultimo`() = runTest {
        // A ordem das categorias já chega pronta do repositório (sortOrder — ver
        // CategoryDao/CategoryRepositoryImpl); o ViewModel não reordena de novo, só
        // agrupa e preserva a ordem recebida.
        val homeCategory = Category(id = 2, name = "Casa", colorHex = "#FF3DAE", icon = "home", sortOrder = 0)
        val petCategory = Category(id = 1, name = "Pets", colorHex = "#39FF88", icon = "pets", sortOrder = 1)
        val petTask = task(id = 1, dueInDays = 1, categoryId = 1)
        val homeTask = task(id = 2, dueInDays = 1, categoryId = 2)
        val looseTask = task(id = 3, dueInDays = 1, categoryId = null)

        val viewModel = buildViewModel(
            taskRepository = FakeTaskRepository(listOf(petTask, homeTask, looseTask)),
            categoryRepository = FakeCategoryRepository(listOf(homeCategory, petCategory))
        )

        viewModel.state.test {
            skipItems(1) // loading

            val loaded = awaitItem()
            assertEquals(listOf("Casa", "Pets", null), loaded.sections.map { it.categoryName })
            assertEquals(listOf(2L), loaded.sections[0].tasks.map { it.id })
            assertEquals(listOf(1L), loaded.sections[1].tasks.map { it.id })
            assertEquals(listOf(3L), loaded.sections[2].tasks.map { it.id })
        }
    }

    @Test
    fun `calcula contagem de atrasadas e vencendo essa semana`() = runTest {
        val overdue = task(id = 1, dueInDays = -1)
        val dueThisWeek = task(id = 2, dueInDays = 3)
        val dueNextMonth = task(id = 3, dueInDays = 30)
        val repository = FakeTaskRepository(listOf(overdue, dueThisWeek, dueNextMonth))
        val viewModel = buildViewModel(taskRepository = repository)

        viewModel.state.test {
            skipItems(1) // loading

            val loaded = awaitItem()
            assertEquals(1, loaded.overdueCount)
            assertEquals(1, loaded.dueThisWeekCount)
        }
    }

    @Test
    fun `nextTask e a tarefa mais urgente, atrasada tem prioridade`() = runTest {
        val overdue = task(id = 1, dueInDays = -1)
        val upcoming = task(id = 2, dueInDays = 1)
        val repository = FakeTaskRepository(listOf(upcoming, overdue))
        val viewModel = buildViewModel(taskRepository = repository)

        viewModel.state.test {
            skipItems(1) // loading

            val loaded = awaitItem()
            assertEquals(1L, loaded.nextTask?.id)
            assertTrue(loaded.nextTask?.isOverdue == true)
        }
    }

    @Test
    fun `todayWeather atualiza quando a cidade e configurada depois, sem recriar o ViewModel`() = runTest {
        val currentWeather = CurrentWeather(temperatureCelsius = 20.0, condition = WeatherCondition.CLEAR)
        val weatherRepository = FakeWeatherRepository(currentWeather = currentWeather, initialCityName = null)
        val viewModel = buildViewModel(taskRepository = FakeTaskRepository(), weatherRepository = weatherRepository)

        viewModel.state.test {
            skipItems(1) // loading

            assertEquals(null, awaitItem().todayWeather)

            weatherRepository.setLocation("São Paulo")

            val updated = awaitItem()
            assertEquals("20°", updated.todayWeather?.temperatureLabel)
        }
    }

    @Test
    fun `refreshWeather busca o clima de novo e nao trava isRefreshingWeather em true`() = runTest {
        val currentWeather = CurrentWeather(temperatureCelsius = 22.0, condition = WeatherCondition.RAIN)
        val weatherRepository = FakeWeatherRepository(currentWeather = currentWeather, initialCityName = "São Paulo")
        val viewModel = buildViewModel(taskRepository = FakeTaskRepository(), weatherRepository = weatherRepository)

        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("22°", viewModel.state.value.todayWeather?.temperatureLabel)

        viewModel.refreshWeather()
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isRefreshingWeather)
        assertEquals("22°", viewModel.state.value.todayWeather?.temperatureLabel)
    }

    @Test
    fun `enabledWidgets reflete as chaves persistidas em DashboardPreferencesManager`() = runTest {
        val preferences = FakeDashboardPreferencesManager(initialEnabledKeys = emptyList())
        val viewModel = buildViewModel(taskRepository = FakeTaskRepository(), dashboardPreferencesManager = preferences)

        viewModel.state.test {
            skipItems(1) // loading

            val loaded = awaitItem()
            assertEquals(emptyList<DashboardWidgetType>(), loaded.enabledWidgets)
        }
    }

    @Test
    fun `onCompleteTask delega para o usecase e atualiza a proxima execucao`() = runTest {
        val task = task(id = 1, dueInDays = -1)
        val repository = FakeTaskRepository(listOf(task))
        val viewModel = buildViewModel(taskRepository = repository)

        viewModel.state.test {
            skipItems(2) // loading + primeira emissão com a tarefa atrasada

            viewModel.onCompleteTask(taskId = 1)

            val afterComplete = awaitItem()
            assertTrue(afterComplete.sections.single().tasks.single().isOverdue.not())
        }
    }

    @Test
    fun `onDeleteTask remove a tarefa e emite evento de exclusao`() = runTest {
        val task = task(id = 1, dueInDays = 1)
        val repository = FakeTaskRepository(listOf(task))
        val viewModel = buildViewModel(taskRepository = repository)

        dispatcher.scheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.onDeleteTask(taskId = 1)

            val event = awaitItem()
            assertEquals(TaskListEvent.TaskDeleted(task.title), event)
        }
    }

    @Test
    fun `onUndoDelete recria a tarefa excluida`() = runTest {
        val task = task(id = 1, dueInDays = 1)
        val repository = FakeTaskRepository(listOf(task))
        val viewModel = buildViewModel(taskRepository = repository)

        viewModel.state.test {
            skipItems(2) // loading + estado inicial com a tarefa

            viewModel.onDeleteTask(taskId = 1)
            val afterDelete = awaitItem()
            assertTrue(afterDelete.isEmpty)

            viewModel.onUndoDelete()
            val afterUndo = awaitItem()
            assertEquals(task.title, afterUndo.sections.single().tasks.single().title)
        }
    }

    private fun buildViewModel(
        taskRepository: FakeTaskRepository,
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(),
        dashboardPreferencesManager: FakeDashboardPreferencesManager = FakeDashboardPreferencesManager(),
        weatherRepository: FakeWeatherRepository = FakeWeatherRepository()
    ): TaskListViewModel {
        val notificationScheduler = FakeTaskNotificationScheduler()
        val scheduleNotificationUseCase = ScheduleNotificationUseCase(notificationScheduler)
        return TaskListViewModel(
            getTasksUseCase = GetTasksUseCase(taskRepository),
            getCategoriesUseCase = GetCategoriesUseCase(categoryRepository),
            completeTaskUseCase = CompleteTaskUseCase(
                taskRepository = taskRepository,
                scheduleNotificationUseCase = scheduleNotificationUseCase,
                recurrenceCalculator = RecurrenceCalculator()
            ),
            undoCompleteTaskUseCase = UndoCompleteTaskUseCase(taskRepository, scheduleNotificationUseCase),
            deleteTaskUseCase = DeleteTaskUseCase(taskRepository, notificationScheduler),
            addTaskUseCase = AddTaskUseCase(taskRepository, scheduleNotificationUseCase, RecurrenceCalculator()),
            reorderCategoriesUseCase = ReorderCategoriesUseCase(categoryRepository),
            dashboardPreferencesManager = dashboardPreferencesManager,
            getCurrentWeatherUseCase = GetCurrentWeatherUseCase(weatherRepository),
            getWeatherLocationUseCase = GetWeatherLocationUseCase(weatherRepository)
        )
    }

    private fun task(id: Long, dueInDays: Long, categoryId: Long? = null) = Task(
        id = id,
        title = "Tarefa $id",
        categoryId = categoryId,
        recurrenceType = RecurrenceType.DAILY,
        nextDueDate = LocalDateTime.now().plusDays(dueInDays)
    )
}
