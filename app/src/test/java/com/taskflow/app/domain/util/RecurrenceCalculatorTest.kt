package com.taskflow.app.domain.util

import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class RecurrenceCalculatorTest {

    private val calculator = RecurrenceCalculator()

    @Test
    fun `calcula proxima data semanal`() {
        val now = LocalDateTime.of(2026, 1, 1, 10, 0)
        val task = Task(
            title = "Limpar aquário",
            categoryId = null,
            recurrenceType = RecurrenceType.WEEKLY,
            nextDueDate = now
        )

        val next = calculator.calculateNextDueDate(task, from = now)

        assertEquals(now.plusWeeks(1), next)
    }
}
