package com.taskflow.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taskflow.app.R
import com.taskflow.app.presentation.designsystem.GlassCard
import com.taskflow.app.presentation.designsystem.GlassTertiary
import com.taskflow.app.presentation.model.TaskUiModel

private val CAROUSEL_ITEM_WIDTH = 168.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCarouselItem(
    task: TaskUiModel,
    accentColor: Color,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val dueDateColor by animateColorAsState(
        targetValue = if (task.isOverdue) GlassTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "task_carousel_due_date_color"
    )

    Box(modifier = modifier) {
        GlassCard(
            tint = accentColor,
            modifier = Modifier
                .width(CAROUSEL_ITEM_WIDTH)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                    onLongClick = { isMenuExpanded = true }
                )
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(accentColor, RoundedCornerShape(50))
                )
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.dueDateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = dueDateColor,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = stringResource(R.string.task_complete_content_description),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onComplete,
                                    onLongClick = { isMenuExpanded = true }
                                )
                        )
                    }
                }
            }
        }

        DropdownMenu(expanded = isMenuExpanded, onDismissRequest = { isMenuExpanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.task_carousel_complete_action)) },
                onClick = {
                    isMenuExpanded = false
                    onComplete()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.task_carousel_delete_action)) },
                onClick = {
                    isMenuExpanded = false
                    onDelete()
                }
            )
        }
    }
}
