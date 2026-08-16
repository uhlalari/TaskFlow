package com.taskflow.app.presentation.designsystem

import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private const val BUBBLE_COUNT = 22
private const val EDGE_FADE_FRACTION = 0.1f

private data class Bubble(
    /** X base (0f..1f) — o "wiggle" lateral é aplicado em cima disso, não substitui. */
    val baseX: Float,
    val radiusFraction: Float,
    /** Multiplicador de velocidade: cada bolha cai num ritmo levemente diferente,
     * para não parecerem todas "grudadas" caindo em fileira. */
    val speed: Float,
    /** Deslocamento de fase (0f..1f) no laço de animação — espalha as bolhas ao
     * longo do tempo em vez de todas nascerem juntas. */
    val phase: Float,
    val wigglePhase: Float,
    /** Índice na lista de cores recebida por [floatingBubbles] — cada bolha usa uma
     * cor de acento diferente, ciclando entre elas. */
    val colorIndex: Int,
    /** Posição do brilho especular (reflexo de luz) relativa ao raio da bolha,
     * variada por bolha para não parecerem "clonadas". */
    val highlightOffsetFraction: Offset
)

private val BUBBLES = List(BUBBLE_COUNT) { index ->
    val random = Random(seed = index * 37 + 11)
    Bubble(
        baseX = random.nextFloat(),
        radiusFraction = 0.018f + random.nextFloat() * 0.042f,
        speed = 0.5f + random.nextFloat() * 0.7f,
        phase = random.nextFloat(),
        wigglePhase = random.nextFloat() * 6.28f,
        colorIndex = index,
        highlightOffsetFraction = Offset(
            -0.3f - random.nextFloat() * 0.15f,
            -0.3f - random.nextFloat() * 0.15f
        )
    )
}

fun Modifier.floatingBubbles(progress: State<Float>, colors: List<Color>): Modifier = this.then(
    Modifier.drawWithCache {
        onDrawBehind {
            val time = progress.value
            val minDimension = min(size.width, size.height)

            BUBBLES.forEach { bubble ->
                val loopProgress = (time * bubble.speed + bubble.phase) % 1f
                val y = (-0.15f + loopProgress * 1.3f) * size.height
                val wiggle = sin(loopProgress * 2f * Math.PI.toFloat() + bubble.wigglePhase) * 0.025f
                val x = (bubble.baseX + wiggle) * size.width
                val radius = bubble.radiusFraction * minDimension
                val center = Offset(x, y)
                val color = colors[bubble.colorIndex % colors.size]

                val edgeFade = when {
                    loopProgress < EDGE_FADE_FRACTION -> loopProgress / EDGE_FADE_FRACTION
                    loopProgress > 1f - EDGE_FADE_FRACTION -> (1f - loopProgress) / EDGE_FADE_FRACTION
                    else -> 1f
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.04f * edgeFade), color.copy(alpha = 0.30f * edgeFade)),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = color.copy(alpha = 0.45f * edgeFade),
                    radius = radius,
                    center = center,
                    style = Stroke(width = radius * 0.1f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f * edgeFade),
                    radius = radius * 0.28f,
                    center = Offset(
                        center.x + bubble.highlightOffsetFraction.x * radius,
                        center.y + bubble.highlightOffsetFraction.y * radius
                    )
                )
            }
        }
    }
)
