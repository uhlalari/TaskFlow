package com.taskflow.app.presentation.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.max

private data class Blob(
    val centerFraction: Offset,
    val radiusFraction: Float,
    val alpha: Float
)

private val BLOB_LAYOUT = listOf(
    Blob(centerFraction = Offset(0.12f, 0.08f), radiusFraction = 0.55f, alpha = 0.14f),
    Blob(centerFraction = Offset(0.92f, 0.22f), radiusFraction = 0.5f, alpha = 0.12f),
    Blob(centerFraction = Offset(0.45f, 0.95f), radiusFraction = 0.65f, alpha = 0.10f)
)

fun Modifier.colorfulBlobs(colors: List<Color>): Modifier = this.then(
    Modifier.drawWithCache {
        val blobBrushes = BLOB_LAYOUT.mapIndexed { index, blob ->
            val color = colors[index % colors.size]
            val center = Offset(size.width * blob.centerFraction.x, size.height * blob.centerFraction.y)
            val radius = max(size.width, size.height) * blob.radiusFraction
            Triple(
                Brush.radialGradient(
                    colors = listOf(color.copy(alpha = blob.alpha), color.copy(alpha = 0f)),
                    center = center,
                    radius = radius
                ),
                center,
                radius
            )
        }
        onDrawBehind {
            blobBrushes.forEach { (brush, center, radius) ->
                drawCircle(brush = brush, radius = radius, center = center)
            }
        }
    }
)
