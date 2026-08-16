package com.taskflow.app.presentation.addtask

import com.taskflow.app.domain.util.RecurrenceCalculator
import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.usecase.AddCategoryUseCase
import com.taskflow.app.domain.usecase.AddTaskUseCase
import com.taskflow.app.domain.usecase.GetCategoriesUseCase
import com.taskflow.app.domain.usecase.ScheduleNotificationUseCase
import com.taskflow.app.domain.usecase.UpdateTaskUseCase
import com.taskflow.app.fakes.FakeCategoryRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Testes deste ViewModel usam snapshots de `state.value` após `advanceUntilIdle()`
 * em vez de sequenciar emissões com Turbine: como os fakes de repositório são
 * síncronos, múltiplas atualizações de estado podem acontecer antes de qualquer
 * coletor conseguir observar o valor intermediário (conflação de StateFlow),
 * tornando a contagem exata de emissões um detalhe de implementação frágil demais
 * para testar diretamente.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddTaskViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var notificationScheduler: FakeTaskNotificationScheduler
    private lateinit var recurrenceCalculator: RecurrenceCalculator

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        taskRepository = FakeTaskRepository()
        categoryRepository = FakeCategoryRepository()
        notificationScheduler = FakeTaskNotificationScheduler()
        recurrenceCalculator = RecurrenceCalculator()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(editingTaskId: Long? = null): AddTaskViewModel {
        val scheduleNotificationUseCase = ScheduleNotificationUseCase(notificationScheduler)
        return AddTaskViewModel(
            addTaskUseCase = AddTaskUseCase(taskRepository, scheduleNotificationUseCase, recurrenceCalculator),
            updateTaskUseCase = UpdateTaskUseCase(taskRepository, scheduleNotificationUseCase),
            getCategoriesUseCase = GetCategoriesUseCase(categoryRepository),
            addCategoryUseCase = AddCategoryUseCase(categoryRepository),
            taskRepository = taskRepository,
            recurrenceCalculator = recurrenceCalculator,
            editingTaskId = editingTaskId
        )
    }

    @Test
    fun `onSaveClick com titulo vazio retorna erro de validacao e nao salva`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onSaveClick()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(AddTaskFormError.EmptyTitle, state.error)
        assertTrue(state.isSaved.not())
        assertTrue(taskRepository.observeTasks().value.isEmpty())
    }

    @Test
    fun `onSaveClick com recorrencia mensal calcula proximo vencimento em um mes`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onTitleChange("Pagar aluguel")
        viewModel.onRecurrenceTypeChange(RecurrenceType.MONTHLY)
        viewModel.onSaveClick()
        dispatcher.scheduler.advanceUntilIdle()

        val savedTask = taskRepository.observeTasks().value.single()
        val daysUntilDue = java.time.Duration.between(LocalDateTime.now(), savedTask.nextDueDate).toDays()

        // Não comparamos uma data exata (o teste rodaria em instantes ligeiramente
        // diferentes), só garantimos que o vencimento é claramente "cerca de um mês
        // à frente" e não "agora" — que era o bug relatado (recorrência ignorada).
        assertTrue("esperado ~30 dias à frente, mas foi $daysUntilDue", daysUntilDue in 27..31)
    }

    @Test
    fun `onSaveClick com titulo valido salva a tarefa`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onTitleChange("Trocar filtro do aquário")
        viewModel.onSaveClick()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isSaved)
        assertNull(state.error)
        assertEquals(1, taskRepository.observeTasks().value.size)
    }

    @Test
    fun `onConfirmAddCategory com nome vazio mantem dialogo aberto com erro`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onAddCategoryClick()
        viewModel.onConfirmAddCategory("   ")
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(AddTaskFormError.EmptyCategoryName, state.newCategoryError)
        assertTrue(state.isAddCategoryDialogVisible)
    }

    @Test
    fun `onConfirmAddCategory com nome valido fecha dialogo e seleciona a categoria`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onAddCategoryClick()
        viewModel.onConfirmAddCategory("Academia")
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isAddCategoryDialogVisible.not())
        assertNull(state.newCategoryError)
        assertEquals(1, state.categories.size)
        assertEquals("Academia", state.categories.first().name)
        assertEquals(state.categories.first().id, state.selectedCategoryId)
    }

    @Test
    fun `modo de edicao carrega os campos da tarefa existente`() = runTest {
        val dueDate = LocalDateTime.now().plusDays(5)
        taskRepository.addTask(
            Task(
                id = 1,
                title = "Vacina do cachorro",
                description = "Levar carteirinha",
                categoryId = null,
                recurrenceType = RecurrenceType.YEARLY,
                nextDueDate = dueDate
            )
        )

        val viewModel = buildViewModel(editingTaskId = 1)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isEditMode)
        assertEquals("Vacina do cachorro", state.title)
        assertEquals("Levar carteirinha", state.description)
        assertEquals(RecurrenceType.YEARLY, state.recurrenceType)
    }

    @Test
    fun `editar sem trocar recorrencia preserva o vencimento original`() = runTest {
        val originalDueDate = LocalDateTime.now().plusDays(5)
        taskRepository.addTask(
            Task(
                id = 1,
                title = "Vacina do cachorro",
                categoryId = null,
                recurrenceType = RecurrenceType.YEARLY,
                nextDueDate = originalDueDate
            )
        )

        val viewModel = buildViewModel(editingTaskId = 1)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onDescriptionChange("Levar carteirinha de vacinação")
        viewModel.onSaveClick()
        dispatcher.scheduler.advanceUntilIdle()

        val updatedTask = taskRepository.observeTasks().value.single()
        assertEquals(originalDueDate, updatedTask.nextDueDate)
        assertEquals("Levar carteirinha de vacinação", updatedTask.description)
    }

    @Test
    fun `editar trocando a recorrencia recalcula o vencimento`() = runTest {
        taskRepository.addTask(
            Task(
                id = 1,
                title = "Trocar filtro",
                categoryId = null,
                recurrenceType = RecurrenceType.MONTHLY,
                nextDueDate = LocalDateTime.now().plusDays(30)
            )
        )

        val viewModel = buildViewModel(editingTaskId = 1)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onRecurrenceTypeChange(RecurrenceType.WEEKLY)
        viewModel.onSaveClick()
        dispatcher.scheduler.advanceUntilIdle()

        val updatedTask = taskRepository.observeTasks().value.single()
        val daysUntilDue = java.time.Duration.between(LocalDateTime.now(), updatedTask.nextDueDate).toDays()
        assertTrue("esperado ~7 dias à frente, mas foi $daysUntilDue", daysUntilDue in 5..8)
    }
}
