package com.taskflow.app.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 24,
    tint: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    val hazeState = LocalHazeState.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDark = isSystemInDarkTheme()

    // Vidro tingido, mas bem sutil: o contorno não pode competir com o blur.
    val tintAlpha = if (isDark) 0.08f else 0.13f
    val fallbackAlpha = if (isDark) 0.14f else 0.20f
    val rimAlpha = if (isDark) 0.10f else 0.18f
    val specularAlpha = if (isDark) 0.10f else 0.18f

    val backdropModifier = if (hazeState != null) {
        Modifier.hazeEffect(
            state = hazeState,
            style = HazeStyle(
                tint = HazeTint(tint.copy(alpha = tintAlpha)),
                backgroundColor = Color.Transparent,
                blurRadius = 26.dp,
                noiseFactor = 0f,
                fallbackTint = HazeTint(tint.copy(alpha = fallbackAlpha))
            )
        )
    } else {
        Modifier
            .background(color = surfaceColor.copy(alpha = 0.30f))
            .background(color = tint.copy(alpha = 0.12f))
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(backdropModifier)
            .drawBehind {
                val width = size.width
                val height = size.height
                val minDim = size.minDimension.coerceAtLeast(1f)

                // Brilho suave no topo-esquerdo, misturando a cor do tema.
                val specular = Brush.radialGradient(
                    colors = listOf(
                        tint.copy(alpha = specularAlpha),
                        Color.White.copy(alpha = specularAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.20f, height * 0.15f),
                    radius = minDim * 0.65f
                )

                // Volume na base com a própria cor do tema, bem fraquinho.
                val volume = Brush.linearGradient(
                    colors = listOf(Color.Transparent, tint.copy(alpha = 0.06f)),
                    start = Offset(0f, height * 0.45f),
                    end = Offset(0f, height)
                )

                drawRect(brush = specular)
                drawRect(brush = volume)
            }
            .border(
                width = 1.dp,
                color = tint.copy(alpha = rimAlpha),
                shape = shape
            )
            .padding(16.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            content()
        }
    }
}
