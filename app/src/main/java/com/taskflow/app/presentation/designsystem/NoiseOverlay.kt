package com.taskflow.app.presentation.designsystem

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import kotlin.random.Random

private const val NOISE_TILE_SIZE_PX = 96
private const val DEFAULT_NOISE_ALPHA = 0.035f
private const val NOISE_SEED = 42L

fun Modifier.noiseOverlay(alpha: Float = DEFAULT_NOISE_ALPHA): Modifier = this.then(
    Modifier.drawWithCache {
        val noiseBrush = ShaderBrush(
            BitmapShader(
                generateNoiseBitmap(NOISE_TILE_SIZE_PX),
                Shader.TileMode.REPEAT,
                Shader.TileMode.REPEAT
            )
        )
        onDrawBehind {
            drawRect(brush = noiseBrush, alpha = alpha)
        }
    }
)

private fun generateNoiseBitmap(size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val random = Random(NOISE_SEED)
    for (x in 0 until size) {
        for (y in 0 until size) {
            val gray = random.nextInt(256)
            bitmap.setPixel(x, y, android.graphics.Color.argb(255, gray, gray, gray))
        }
    }
    return bitmap
}
