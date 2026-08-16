package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.Category
import com.taskflow.app.domain.model.CategoryValidationException
import com.taskflow.app.fakes.FakeCategoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddCategoryUseCaseTest {

    @Test
    fun `rejeita nome vazio`() = runTest {
        val useCase = AddCategoryUseCase(FakeCategoryRepository())

        val exception = runCatching { useCase("   ") }.exceptionOrNull()

        assertTrue(exception is CategoryValidationException.EmptyName)
    }

    @Test
    fun `rejeita nome duplicado ignorando maiusculas e espacos`() = runTest {
        val repository = FakeCategoryRepository(
            initialCategories = listOf(Category(id = 1, name = "Pets", colorHex = "#000000", icon = "pets"))
        )
        val useCase = AddCategoryUseCase(repository)

        val exception = runCatching { useCase("  pets  ") }.exceptionOrNull()

        assertTrue(exception is CategoryValidationException.DuplicateName)
    }

    @Test
    fun `aceita nome novo e nao duplicado`() = runTest {
        val repository = FakeCategoryRepository(
            initialCategories = listOf(Category(id = 1, name = "Pets", colorHex = "#000000", icon = "pets"))
        )
        val useCase = AddCategoryUseCase(repository)

        useCase("Casa")

        assertEquals(listOf("Pets", "Casa"), repository.observeCategories().value.map { it.name })
    }
}
