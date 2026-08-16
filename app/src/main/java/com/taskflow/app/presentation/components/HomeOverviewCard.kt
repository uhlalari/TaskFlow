package com.taskflow.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taskflow.app.R
import com.taskflow.app.presentation.designsystem.GlassCard
import com.taskflow.app.presentation.designsystem.GlassPrimary
import com.taskflow.app.presentation.designsystem.GlassTertiary
import com.taskflow.app.presentation.designsystem.toIcon
import com.taskflow.app.presentation.model.TaskUiModel
import com.taskflow.app.presentation.model.WeatherUiModel
import kotlin.math.abs

private const val MINUTES_PER_HOUR = 60
private const val HOURS_PER_DAY = 24
private const val MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY

@Composable
fun HomeOverviewCard(
    overdueCount: Int,
    dueThisWeekCount: Int,
    nextTask: TaskUiModel?,
    todayWeather: WeatherUiModel?,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_summary_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(
                            if (overdueCount > 0) {
                                R.string.home_summary_subtitle_attention
                            } else {
                                R.string.home_summary_subtitle_ok
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                todayWeather?.let { weather ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = weather.condition.toIcon(),
                            contentDescription = null,
                            tint = GlassPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = weather.temperatureLabel,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryStat(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.WarningAmber,
                    count = overdueCount,
                    label = stringResource(R.string.home_summary_overdue),
                    color = GlassTertiary
                )
                SummaryStat(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CalendarMonth,
                    count = dueThisWeekCount,
                    label = stringResource(R.string.home_summary_due_this_week),
                    color = GlassPrimary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )

            Text(stringResource(R.string.next_task_widget_title), style = MaterialTheme.typography.titleSmall)
            NextTaskRow(task = nextTask, onClick = onTaskClick)
        }
    }
}

@Composable
private fun SummaryStat(
    icon: ImageVector,
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(text = count.toString(), style = MaterialTheme.typography.headlineSmall, color = color)
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
@Composable
private fun NextTaskRow(task: TaskUiModel?, onClick: (Long) -> Unit) {
    if (task == null) {
        Text(
            text = stringResource(R.string.next_task_widget_empty),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clickable { onClick(task.id) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Bolt,
            contentDescription = null,
            tint = if (task.isOverdue) GlassTertiary else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Column(
            modifier = Modifier.weight(1f).padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = relativeDueLabel(task.dueInMinutes),
                style = MaterialTheme.typography.bodySmall,
                color = if (task.isOverdue) GlassTertiary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun relativeDueLabel(dueInMinutes: Long): String {
    val isOverdue = dueInMinutes < 0
    val absMinutes = abs(dueInMinutes)

    if (absMinutes < 1) return stringResource(R.string.next_task_due_now)

    val quantityLabel = if (absMinutes < MINUTES_PER_DAY) {
        val hours = (absMinutes / MINUTES_PER_HOUR).toInt().coerceAtLeast(1)
        pluralStringResource(R.plurals.relative_time_hours, hours, hours)
    } else {
        val days = (absMinutes / MINUTES_PER_DAY).toInt()
        pluralStringResource(R.plurals.relative_time_days, days, days)
    }

    return stringResource(
        if (isOverdue) R.string.next_task_overdue else R.string.next_task_due_in,
        quantityLabel
    )
}
