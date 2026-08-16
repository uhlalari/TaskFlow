package com.taskflow.app.data.local.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.taskflow.app.data.local.database.AppDatabase
import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.TaskExecution
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime

/**
 * Testa o repositório contra um Room real em memória (não um mock do DAO), para
 * garantir que as queries SQL e o mapeamento Entity <-> Domain funcionam de ponta a
 * ponta. Usamos Robolectric para ter um `Context` Android sem precisar de emulador.
 *
 * `application = android.app.Application::class` evita que o Robolectric instancie
 * `TaskFlowApplication` (que chama `startKoin`) a cada teste — o Koin não foi feito
 * para ser reiniciado repetidamente na mesma JVM, e este teste não depende de DI.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = android.app.Application::class)
class TaskRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TaskRepositoryImpl(database.taskDao(), database.taskExecutionDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `addTask persiste e observeTasks emite a tarefa criada`() = runTest {
        val task = sampleTask()

        repository.observeTasks().test {
            assertEquals(emptyList<Task>(), awaitItem())

            val id = repository.addTask(task)

            val afterInsert = awaitItem()
            assertEquals(1, afterInsert.size)
            assertEquals(id, afterInsert.first().id)
            assertEquals(task.title, afterInsert.first().title)
        }
    }

    @Test
    fun `deleteTask remove a tarefa do banco`() = runTest {
        val id = repository.addTask(sampleTask())

        repository.deleteTask(id)

        assertNull(repository.getTaskById(id))
    }

    @Test
    fun `registerExecution grava historico recuperavel por getExecutionHistory`() = runTest {
        val id = repository.addTask(sampleTask())
        val completedAt = LocalDateTime.now()

        repository.registerExecution(TaskExecution(taskId = id, completedAt = completedAt))

        val history = repository.getExecutionHistory(id)
        assertEquals(1, history.size)
        assertEquals(id, history.first().taskId)
    }

    private fun sampleTask() = Task(
        title = "Limpar aquário",
        categoryId = null,
        recurrenceType = RecurrenceType.WEEKLY,
        nextDueDate = LocalDateTime.now().plusDays(7)
    )
}
