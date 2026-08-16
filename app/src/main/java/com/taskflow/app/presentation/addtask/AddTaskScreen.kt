package com.taskflow.app.presentation.addtask

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskflow.app.R
import com.taskflow.app.domain.model.RecurrenceType
import com.taskflow.app.presentation.designsystem.GlassButton
import com.taskflow.app.presentation.designsystem.GlassCard
import com.taskflow.app.presentation.designsystem.GlassChip
import com.taskflow.app.presentation.designsystem.GlassPrimary
import com.taskflow.app.presentation.designsystem.GlassTertiary
import com.taskflow.app.presentation.designsystem.GlassTextField
import com.taskflow.app.presentation.designsystem.LiquidBackground
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AddTaskScreen(
    onTaskSaved: () -> Unit,
    taskId: Long? = null,
    viewModel: AddTaskViewModel = koinViewModel(parameters = { parametersOf(taskId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onTaskSaved()
    }

    if (state.isAddCategoryDialogVisible) {
        AddCategoryDialog(
            error = state.newCategoryError,
            onConfirm = viewModel::onConfirmAddCategory,
            onDismiss = viewModel::onDismissAddCategoryDialog
        )
    }

    LiquidBackground {
        Scaffold(containerColor = Color.Transparent) { padding ->
            if (state.isLoadingTaskToEdit) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GlassPrimary)
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(
                        if (state.isEditMode) R.string.edit_task_title else R.string.add_task_title
                    ),
                    style = MaterialTheme.typography.headlineSmall
                )

                GlassTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    label = stringResource(R.string.add_task_field_title),
                    modifier = Modifier.fillMaxWidth()
                )

                GlassTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = stringResource(R.string.add_task_field_description),
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.add_task_recurrence_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(RecurrenceType.entries) { type ->
                        GlassChip(
                            label = recurrenceLabel(type),
                            selected = state.recurrenceType == type,
                            onClick = { viewModel.onRecurrenceTypeChange(type) }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.recurrenceType == RecurrenceType.CUSTOM_DAYS,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    GlassTextField(
                        value = state.customIntervalDaysText,
                        onValueChange = viewModel::onCustomIntervalDaysChange,
                        label = stringResource(R.string.add_task_custom_interval_label),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(stringResource(R.string.add_task_category_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.categories) { category ->
                        GlassChip(
                            label = category.name,
                            selected = state.selectedCategoryId == category.id,
                            onClick = { viewModel.onCategorySelected(category.id) }
                        )
                    }
                    item {
                        GlassChip(
                            label = stringResource(R.string.add_task_new_category_chip),
                            selected = false,
                            onClick = viewModel::onAddCategoryClick
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    state.error?.let { error ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(errorMessage(error), color = GlassTertiary)
                        }
                    }
                }

                GlassButton(
                    text = stringResource(R.string.add_task_save_button),
                    isLoading = state.isSaving,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    onClick = viewModel::onSaveClick
                )
            }
        }
    }
}

@Composable
private fun recurrenceLabel(type: RecurrenceType): String = when (type) {
    RecurrenceType.DAILY -> stringResource(R.string.recurrence_daily)
    RecurrenceType.WEEKLY -> stringResource(R.string.recurrence_weekly)
    RecurrenceType.MONTHLY -> stringResource(R.string.recurrence_monthly)
    RecurrenceType.YEARLY -> stringResource(R.string.recurrence_yearly)
    RecurrenceType.CUSTOM_DAYS -> stringResource(R.string.recurrence_custom)
}

@Composable
private fun errorMessage(error: AddTaskFormError): String = when (error) {
    AddTaskFormError.EmptyTitle -> stringResource(R.string.add_task_error_empty_title)
    AddTaskFormError.EmptyCategoryName -> stringResource(R.string.add_category_error_empty_name)
    AddTaskFormError.DuplicateCategoryName -> stringResource(R.string.add_category_error_duplicate_name)
    AddTaskFormError.Unknown -> stringResource(R.string.add_task_error_unknown)
}

@Composable
private fun AddCategoryDialog(
    error: AddTaskFormError?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_category_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.add_category_field_name),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(errorMessage(it), color = GlassTertiary) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text(stringResource(R.string.add_category_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.add_category_cancel))
            }
        }
    )
}
