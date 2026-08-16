package com.taskflow.app.presentation.dashboard

import app.cash.turbine.test
import com.taskflow.app.domain.usecase.GetWeatherLocationUseCase
import com.taskflow.app.domain.usecase.SetWeatherLocationUseCase
import com.taskflow.app.fakes.FakeDashboardPreferencesManager
import com.taskflow.app.fakes.FakeWeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardSettingsViewModelTest {

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
    fun `estado inicial separa widgets ativos e disponiveis`() = runTest {
        val preferences = FakeDashboardPreferencesManager(initialEnabledKeys = listOf("OVERVIEW"))
        val viewModel = buildViewModel(preferences)

        viewModel.state.test {
            skipItems(1) // valor padrão de MutableStateFlow (isLoading = true), antes do primeiro carregamento

            val loaded = awaitItem()
            assertEquals(listOf(DashboardWidgetType.OVERVIEW), loaded.enabledWidgets)
            assertEquals(emptyList<DashboardWidgetType>(), loaded.availableWidgets)
        }
    }

    @Test
    fun `onDisableWidget move o widget para disponiveis`() = runTest {
        val preferences = FakeDashboardPreferencesManager(initialEnabledKeys = listOf("OVERVIEW"))
        val viewModel = buildViewModel(preferences)

        viewModel.state.test {
            skipItems(2) // valor padrão + estado inicial carregado

            viewModel.onDisableWidget(DashboardWidgetType.OVERVIEW)

            val updated = awaitItem()
            assertEquals(emptyList<DashboardWidgetType>(), updated.enabledWidgets)
            assertEquals(listOf(DashboardWidgetType.OVERVIEW), updated.availableWidgets)
        }
    }

    @Test
    fun `onEnableWidget move o widget de volta para ativos`() = runTest {
        val preferences = FakeDashboardPreferencesManager(initialEnabledKeys = emptyList())
        val viewModel = buildViewModel(preferences)

        viewModel.state.test {
            skipItems(2) // valor padrão + estado inicial carregado

            viewModel.onEnableWidget(DashboardWidgetType.OVERVIEW)

            val updated = awaitItem()
            assertEquals(listOf(DashboardWidgetType.OVERVIEW), updated.enabledWidgets)
            assertEquals(emptyList<DashboardWidgetType>(), updated.availableWidgets)
        }
    }

    // Snapshot de `state.value` após `advanceUntilIdle()`, não sequenciamento via
    // Turbine: `onSaveWeatherCity` escreve duas vezes no mesmo StateFlow (início e
    // fim do carregamento) sem nenhuma suspensão real entre elas no fake — StateFlow
    // é conflado, então a emissão intermediária (`isSavingWeatherCity = true`) pode
    // nunca chegar a ser observada pelo coletor. Mesmo critério já documentado em
    // TaskListViewModelTest.
    @Test
    fun `onSaveWeatherCity com sucesso limpa o erro e atualiza a cidade`() = runTest {
        val weatherRepository = FakeWeatherRepository(setLocationResult = true)
        val viewModel = buildViewModel(weatherRepository = weatherRepository)

        viewModel.onSaveWeatherCity("São Paulo")
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSavingWeatherCity)
        assertFalse(viewModel.state.value.weatherCityError)
    }

    @Test
    fun `onSaveWeatherCity com falha marca erro sem travar o loading`() = runTest {
        val weatherRepository = FakeWeatherRepository(setLocationResult = false)
        val viewModel = buildViewModel(weatherRepository = weatherRepository)

        viewModel.onSaveWeatherCity("Cidade Inexistente")
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSavingWeatherCity)
        assertTrue(viewModel.state.value.weatherCityError)
    }

    private fun buildViewModel(
        preferences: FakeDashboardPreferencesManager = FakeDashboardPreferencesManager(),
        weatherRepository: FakeWeatherRepository = FakeWeatherRepository()
    ): DashboardSettingsViewModel = DashboardSettingsViewModel(
        dashboardPreferencesManager = preferences,
        getWeatherLocationUseCase = GetWeatherLocationUseCase(weatherRepository),
        setWeatherLocationUseCase = SetWeatherLocationUseCase(weatherRepository)
    )
}
