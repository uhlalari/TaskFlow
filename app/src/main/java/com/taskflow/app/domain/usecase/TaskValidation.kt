package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.TaskValidationException

/**
 * Regras de validação compartilhadas entre criação e edição de tarefas.
 *
 * O intervalo personalizado precisa ser >= 1: com 0 (ou vazio) o cálculo de
 * recorrência não avança no tempo e `AddTaskUseCase.rollForwardToFuture` entraria
 * em loop infinito, travando o app.
 */
internal fun Task.validate() {
    if (title.isBlank()) throw TaskValidationException.EmptyTitle
    if (recurrenceType == RecurrenceType.CUSTOM_DAYS && (customIntervalDays ?: 0) < 1) {
        throw TaskValidationException.InvalidCustomInterval
    }
}
