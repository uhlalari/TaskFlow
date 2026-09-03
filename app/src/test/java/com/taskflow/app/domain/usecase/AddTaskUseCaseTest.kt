package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.util.RecurrenceCalculator
import com.taskflow.app.fakes.FakeTaskNotificationScheduler
import com.taskflow.app.fakes.FakeTaskRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class AddTaskUseCaseTest {

    private val taskRepository = FakeTaskRepository()
    private val notificationScheduler = FakeTaskNotificationScheduler()
    private val useCase = AddTaskUseCase(
        taskRepository,
        ScheduleNotificationUseCase(notificationScheduler),
        RecurrenceCalculator()
    )

    @Test
    fun `data de inicio futura escolhida pelo usuario e respeitada sem alteracao`() = runTest {
        val nextTuesday = LocalDateTime.now()
            .plusWeeks(1)
            .with(DayOfWeek.TUESDAY)
            .withHour(9).withMinute(0)
        val task = task(recurrenceType = RecurrenceType.WEEKLY, nextDueDate = nextTuesday)

        useCase(task)

        val saved = taskRepository.observeTasks().value.single()
        assertEquals(nextTuesday, saved.nextDueDate)
    }

    @Test
    fun `data de inicio semanal ja passada avanca preservando o dia da semana escolhido`() = runTest {
        val pastTuesday = LocalDateTime.now().minusWeeks(3).with(DayOfWeek.TUESDAY).withHour(9).withMinute(0)
        val task = task(recurrenceType = RecurrenceType.WEEKLY, nextDueDate = pastTuesday)

        useCase(task)

        val saved = taskRepository.observeTasks().value.single()
        assertTrue(saved.nextDueDate.isAfter(LocalDateTime.now()))
        assertEquals(DayOfWeek.TUESDAY, saved.nextDueDate.dayOfWeek)
    }

    private fun task(recurrenceType: RecurrenceType, nextDueDate: LocalDateTime) = Task(
        title = "Regar plantas",
        categoryId = null,
        recurrenceType = recurrenceType,
        nextDueDate = nextDueDate
    )
}
