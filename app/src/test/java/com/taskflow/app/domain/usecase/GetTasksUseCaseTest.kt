package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class GetTasksUseCaseTest {

    private val repository: TaskRepository = mock()
    private val useCase = GetTasksUseCase(repository)

    @Test
    fun `retorna tarefas ordenadas por atraso`() = runTest {
        val overdue = task(id = 1, dueInDays = -1)
        val upcoming = task(id = 2, dueInDays = 1)
        whenever(repository.observeTasks()).thenReturn(flowOf(listOf(upcoming, overdue)))

        val result = useCase().first()

        assertEquals(overdue.id, result.first().id)
    }

    private fun task(id: Long, dueInDays: Long) = Task(
        id = id,
        title = "Tarefa $id",
        categoryId = null,
        recurrenceType = RecurrenceType.DAILY,
        nextDueDate = LocalDateTime.now().plusDays(dueInDays)
    )
}
