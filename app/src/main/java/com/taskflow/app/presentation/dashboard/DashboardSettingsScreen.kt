package com.taskflow.app.presentation.dashboard

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskflow.app.R
import com.taskflow.app.presentation.components.rememberDragDropListState
import com.taskflow.app.presentation.designsystem.GlassButton
import com.taskflow.app.presentation.designsystem.GlassCard
import com.taskflow.app.presentation.designsystem.GlassPrimary
import com.taskflow.app.presentation.designsystem.GlassTertiary
import com.taskflow.app.presentation.designsystem.GlassTextField
import com.taskflow.app.presentation.designsystem.LiquidBackground
import org.koin.androidx.compose.koinViewModel

private const val HEADER_ITEM_COUNT = 1

@Composable
fun DashboardSettingsScreen(viewModel: DashboardSettingsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LiquidBackground {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.dashboard_settings_title), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = stringResource(R.string.dashboard_settings_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                DashboardWidgetList(
                    state = state,
                    onReorderEnabledWidgets = viewModel::onReorderEnabledWidgets,
                    onEnableWidget = viewModel::onEnableWidget,
                    onDisableWidget = viewModel::onDisableWidget,
                    modifier = Modifier.weight(1f)
                )

                WeatherLocationSection(
                    state = state,
                    onSave = viewModel::onSaveWeatherCity,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun WeatherLocationSection(
    state: DashboardSettingsState,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember(state.weatherCityName) { mutableStateOf(state.weatherCityName == null) }
    var cityInput by remember(state.weatherCityName) { mutableStateOf(state.weatherCityName.orEmpty()) }

    GlassCard(tint = GlassPrimary, modifier = modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.WbSunny, contentDescription = null, tint = GlassPrimary)
                Text(
                    text = stringResource(R.string.weather_settings_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (isEditing) {
                Text(
                    text = stringResource(R.string.weather_settings_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                GlassTextField(
                    value = cityInput,
                    onValueChange = { cityInput = it },
                    label = stringResource(R.string.weather_settings_city_label),
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.weatherCityError) {
                    Text(
                        text = stringResource(R.string.weather_settings_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassTertiary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(modifier = Modifier.padding(top = 12.dp)) {
                    if (state.weatherCityName != null) {
                        GlassButton(
                            text = stringResource(R.string.weather_settings_cancel),
                            onClick = {
                                cityInput = state.weatherCityName.orEmpty()
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                    }
                    GlassButton(
                        text = stringResource(R.string.weather_settings_save),
                        isLoading = state.isSavingWeatherCity,
                        onClick = { onSave(cityInput) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.weather_settings_city_configured, state.weatherCityName.orEmpty()),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { isEditing = true }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.weather_settings_edit_content_description),
                            tint = GlassPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardWidgetList(
    state: DashboardSettingsState,
    onReorderEnabledWidgets: (List<DashboardWidgetType>) -> Unit,
    onEnableWidget: (DashboardWidgetType) -> Unit,
    onDisableWidget: (DashboardWidgetType) -> Unit,
    modifier: Modifier = Modifier
) {
    var orderedEnabled by remember(state.enabledWidgets) { mutableStateOf(state.enabledWidgets) }
    val listState = rememberLazyListState()

    val dragDropState = rememberDragDropListState(listState) { fromItemIndex, toItemIndex ->
        val fromIndex = fromItemIndex - HEADER_ITEM_COUNT
        val toIndex = toItemIndex - HEADER_ITEM_COUNT
        if (fromIndex in orderedEnabled.indices && toIndex in orderedEnabled.indices) {
            orderedEnabled = orderedEnabled.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.dashboard_settings_active_section), style = MaterialTheme.typography.titleSmall)
        }

        itemsIndexed(orderedEnabled, key = { _, type -> "enabled_$type" }) { index, type ->
            val itemIndex = index + HEADER_ITEM_COUNT
            val isDragging = dragDropState.draggingItemIndex == itemIndex

            WidgetRow(
                type = type,
                isEnabled = true,
                onToggle = { onDisableWidget(type) },
                modifier = Modifier
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer { translationY = if (isDragging) dragDropState.draggingItemOffset else 0f }
                    .let { if (isDragging) it else it.animateItem() },
                dragHandle = {
                    Icon(
                        imageVector = Icons.Filled.DragIndicator,
                        contentDescription = stringResource(R.string.dashboard_settings_reorder_content_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.pointerInput(type) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { dragDropState.onDragStart(itemIndex) },
                                onDrag = { change, dragAmount -> change.consume(); dragDropState.onDrag(dragAmount.y) },
                                onDragEnd = {
                                    dragDropState.onDragEnd()
                                    onReorderEnabledWidgets(orderedEnabled)
                                },
                                onDragCancel = { dragDropState.onDragEnd() }
                            )
                        }
                    )
                }
            )
        }

        if (state.availableWidgets.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.dashboard_settings_available_section),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            items(state.availableWidgets, key = { "available_$it" }) { type ->
                WidgetRow(type = type, isEnabled = false, onToggle = { onEnableWidget(type) })
            }
        }
    }
}

@Composable
private fun WidgetRow(
    type: DashboardWidgetType,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandle: @Composable (() -> Unit)? = null
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(type.nameRes), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(type.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = isEnabled, onCheckedChange = { onToggle() })
            dragHandle?.invoke()
        }
    }
}

@get:StringRes
private val DashboardWidgetType.nameRes: Int
    get() = when (this) {
        DashboardWidgetType.OVERVIEW -> R.string.dashboard_widget_overview_name
    }

@get:StringRes
private val DashboardWidgetType.descriptionRes: Int
    get() = when (this) {
        DashboardWidgetType.OVERVIEW -> R.string.dashboard_widget_overview_description
    }

