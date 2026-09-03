package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.util.RecurrenceCalculator
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.TaskValidationException
import com.taskflow.app.domain.repository.TaskRepository
import java.time.LocalDateTime

class AddTaskUseCase(
    private val taskRepository: TaskRepository,
    private val scheduleNotificationUseCase: ScheduleNotificationUseCase,
    private val recurrenceCalculator: RecurrenceCalculator
) {
    suspend operator fun invoke(task: Task, recalculateNextDueDate: Boolean = true): Long {
        if (task.title.isBlank()) throw TaskValidationException.EmptyTitle

        val taskToPersist = if (recalculateNextDueDate) {
            task.copy(nextDueDate = rollForwardToFuture(task))
        } else {
            task
        }

        val id = taskRepository.addTask(taskToPersist)
        scheduleNotificationUseCase(taskToPersist.copy(id = id))
        return id
    }

    /**
     * `task.nextDueDate` é a data de início escolhida pelo usuário (a "âncora" da
     * recorrência, ex.: uma terça-feira para uma recorrência semanal). Se essa data
     * já estiver no futuro, ela é respeitada como está. Caso contrário, avançamos por
     * múltiplos do intervalo de recorrência a partir dela até chegar ao futuro,
     * preservando o dia/âncora escolhido (em vez de recalcular a partir de "agora",
     * o que faria a recorrência "derivar" para outro dia da semana/mês).
     */
    private fun rollForwardToFuture(task: Task): LocalDateTime {
        var dueDate = task.nextDueDate
        while (!dueDate.isAfter(LocalDateTime.now())) {
            dueDate = recurrenceCalculator.calculateNextDueDate(task, from = dueDate)
        }
        return dueDate
    }
}
