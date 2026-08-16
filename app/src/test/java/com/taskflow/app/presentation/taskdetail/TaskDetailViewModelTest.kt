package com.taskflow.app.presentation.taskdetail

import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.usecase.DeleteTaskUseCase
import com.taskflow.app.fakes.FakeTaskNotificationScheduler
import com.taskflow.app.fakes.FakeTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModelTest {

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
    fun `carrega a tarefa e seu historico ao iniciar`() = runTest {
        val task = Task(
            id = 1,
            title = "Trocar filtro de barro",
            categoryId = null,
            recurrenceType = RecurrenceType.CUSTOM_DAYS,
            customIntervalDays = 180,
            nextDueDate = LocalDateTime.now().plusDays(10)
        )
        val repository = FakeTaskRepository(listOf(task))
        val viewModel = TaskDetailViewModel(
            taskId = 1,
            taskRepository = repository,
            deleteTaskUseCase = DeleteTaskUseCase(repository, FakeTaskNotificationScheduler())
        )

        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(task.title, state.task?.title)
        assertTrue(state.isLoading.not())
        assertTrue(state.isNotFound.not())
    }

    @Test
    fun `tarefa inexistente marca isNotFound`() = runTest {
        val repository = FakeTaskRepository(emptyList())
        val viewModel = TaskDetailViewModel(
            taskId = 99,
            taskRepository = repository,
            deleteTaskUseCase = DeleteTaskUseCase(repository, FakeTaskNotificationScheduler())
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.isNotFound)
    }

    @Test
    fun `onDeleteClick remove a tarefa e marca isDeleted`() = runTest {
        val task = Task(
            id = 1,
            title = "Vacina do cachorro",
            categoryId = null,
            recurrenceType = RecurrenceType.YEARLY,
            nextDueDate = LocalDateTime.now().plusDays(30)
        )
        val repository = FakeTaskRepository(listOf(task))
        val viewModel = TaskDetailViewModel(
            taskId = 1,
            taskRepository = repository,
            deleteTaskUseCase = DeleteTaskUseCase(repository, FakeTaskNotificationScheduler())
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onDeleteClick()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isDeleted)
        assertTrue(repository.observeTasks().value.isEmpty())
        // `isNotFound` não deve ficar true aqui: a tela deve navegar de volta via
        // `isDeleted`, não mostrar a mensagem de "tarefa não existe mais".
        assertTrue(state.isNotFound.not())
    }
}
