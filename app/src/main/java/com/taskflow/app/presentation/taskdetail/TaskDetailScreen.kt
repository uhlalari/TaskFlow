package com.taskflow.app.presentation.taskdetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskflow.app.R
import com.taskflow.app.presentation.designsystem.GlassButton
import com.taskflow.app.presentation.designsystem.GlassCard
import com.taskflow.app.presentation.designsystem.GlassPrimary
import com.taskflow.app.presentation.designsystem.GlassTertiary
import com.taskflow.app.presentation.designsystem.LiquidBackground
import com.taskflow.app.presentation.util.DateFormats
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.format.DateTimeFormatter

private const val CONTENT_TRANSITION_DURATION_MS = 300

@Composable
fun TaskDetailScreen(
    taskId: Long,
    onBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: TaskDetailViewModel = koinViewModel(parameters = { parametersOf(taskId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val dateFormatter: DateTimeFormatter = DateFormats.TASK_DUE_DATE
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBack()
    }

    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteConfirmation = false
                viewModel.onDeleteClick()
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }

    LiquidBackground {
        Scaffold(containerColor = Color.Transparent) { padding ->
            AnimatedContent(
                targetState = state.isLoading,
                label = "task_detail_loading",
                transitionSpec = {
                    fadeIn(tween(CONTENT_TRANSITION_DURATION_MS)) togetherWith
                        fadeOut(tween(CONTENT_TRANSITION_DURATION_MS))
                }
            ) { isLoading ->
                when {
                    isLoading -> Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GlassPrimary)
                    }
                    state.isNotFound -> TaskNotFoundContent(padding = padding, onBack = onBack)
                    else -> TaskDetailContent(
                        state = state,
                        dateFormatter = dateFormatter,
                        padding = padding,
                        onDeleteClick = { showDeleteConfirmation = true },
                        onEditClick = { onEditClick(taskId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskNotFoundContent(padding: PaddingValues, onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.task_detail_not_found_message))
                GlassButton(text = stringResource(R.string.task_detail_not_found_back), onClick = onBack)
            }
        }
    }
}

@Composable
private fun TaskDetailContent(
    state: TaskDetailState,
    dateFormatter: DateTimeFormatter,
    padding: PaddingValues,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    // O conteúdo (descrição + histórico) rola numa única LazyColumn que ocupa o espaço
    // disponível, e os botões de ação ficam fixos no rodapé. Assim tarefas com descrições
    // longas (ex.: listas de compras) nunca empurram "Editar"/"Excluir" para fora da tela.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.task?.let { task ->
                item(key = "task_header") {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(task.title, style = MaterialTheme.typography.headlineSmall)
                            if (task.description.isNotBlank()) {
                                Text(task.description, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                stringResource(
                                    R.string.task_detail_next_execution,
                                    task.nextDueDate.format(dateFormatter)
                                ),
                                color = if (task.isOverdue) {
                                    GlassTertiary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            item(key = "history_title") {
                Text(
                    stringResource(R.string.task_detail_history_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.history.isEmpty()) {
                item(key = "history_empty") {
                    Text(
                        stringResource(R.string.task_detail_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.history, key = { it.id }) { execution ->
                    GlassCard(modifier = Modifier.fillMaxWidth().animateItem()) {
                        Text(execution.completedAt.format(dateFormatter))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassButton(
                text = stringResource(R.string.task_detail_edit_button),
                modifier = Modifier.fillMaxWidth(),
                onClick = onEditClick
            )

            GlassButton(
                text = stringResource(R.string.task_detail_delete_button),
                isLoading = state.isDeleting,
                modifier = Modifier.fillMaxWidth(),
                onClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.task_delete_confirmation_title)) },
        text = { Text(stringResource(R.string.task_delete_confirmation_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.task_delete_confirmation_confirm), color = GlassTertiary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.task_delete_confirmation_cancel))
            }
        }
    )
}
