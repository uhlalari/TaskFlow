package com.taskflow.app.presentation.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

private const val BUBBLE_LOOP_DURATION_MS = 24_000

// Exposto pelo LiquidBackground para que qualquer GlassCard na árvore de composição
// abaixo dele consiga aplicar um blur real (backdrop blur, via Haze) sobre os blobs/
// bolhas/grain do fundo, em vez de só simular translucidez com alpha.
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

@Composable
fun LiquidBackground(content: @Composable () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val hazeState = rememberHazeState()

    val bubbleProgress = rememberInfiniteTransition(label = "liquid_bubbles").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(BUBBLE_LOOP_DURATION_MS, easing = LinearEasing)),
        label = "bubble_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .colorfulBlobs(colors = listOf(colorScheme.primary, colorScheme.secondary, colorScheme.tertiary))
            .floatingBubbles(
                progress = bubbleProgress,
                colors = listOf(colorScheme.primary, colorScheme.secondary, colorScheme.tertiary)
            )
            .noiseOverlay()
            .hazeSource(state = hazeState)
    ) {
        CompositionLocalProvider(
            LocalContentColor provides colorScheme.onBackground,
            LocalHazeState provides hazeState
        ) {
            content()
        }
    }
}
