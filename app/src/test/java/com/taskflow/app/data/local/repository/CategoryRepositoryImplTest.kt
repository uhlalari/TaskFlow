package com.taskflow.app.data.local.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.taskflow.app.data.local.database.AppDatabase
import com.taskflow.app.domain.model.Category
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = android.app.Application::class)
class CategoryRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CategoryRepositoryImpl(database.categoryDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `addCategory persiste e observeCategories emite em ordem de insercao`() = runTest {
        // Ordem de inserção (sortOrder = próximo disponível), não alfabética — uma
        // categoria nova sempre entra ao final dos carrosséis da Home; a partir daí, o
        // usuário é quem decide a ordem, arrastando (ver `updateCategoriesOrder`).
        repository.observeCategories().test {
            assertEquals(emptyList<Category>(), awaitItem())

            repository.addCategory(Category(name = "Trabalho", colorHex = "#00D4FF", icon = "work"))
            assertEquals(listOf("Trabalho"), awaitItem().map { it.name })

            repository.addCategory(Category(name = "Academia", colorHex = "#00D4FF", icon = "label"))
            assertEquals(listOf("Trabalho", "Academia"), awaitItem().map { it.name })
        }
    }

    @Test
    fun `updateCategoriesOrder persiste a nova ordem escolhida pelo usuario`() = runTest {
        val trabalhoId = repository.addCategory(Category(name = "Trabalho", colorHex = "#00D4FF", icon = "work"))
        val academiaId = repository.addCategory(Category(name = "Academia", colorHex = "#00D4FF", icon = "label"))

        repository.observeCategories().test {
            // A coleta só começa aqui, depois das duas inserções acima — a primeira
            // emissão já reflete o estado atual ([Trabalho, Academia]), não "vazio".
            skipItems(1)

            repository.updateCategoriesOrder(listOf(academiaId, trabalhoId))

            assertEquals(listOf("Academia", "Trabalho"), awaitItem().map { it.name })
        }
    }
}
