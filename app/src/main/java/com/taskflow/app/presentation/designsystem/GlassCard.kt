package com.taskflow.app.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 24,
    tint: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            .background(color = tint.copy(alpha = 0.14f))
            .drawWithCache {
                val highlightBrush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0f)),
                    center = Offset(size.width * 0.15f, size.height * 0.1f),
                    radius = size.minDimension.coerceAtLeast(1f) * 0.7f
                )
                onDrawBehind { drawRect(brush = highlightBrush) }
            }
            .border(width = 1.dp, color = tint.copy(alpha = 0.35f), shape = shape)
            .padding(16.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            content()
        }
    }
}
