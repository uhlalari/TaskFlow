package com.taskflow.app.presentation.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightBackground = Color(0xFFF6EEE3)
private val LightSurface = Color(0xFFEFE1CF)
private val LightPrimaryRose = Color(0xFFD98CA0)
private val LightSecondaryCaramel = Color(0xFFA9714A)
private val LightTertiaryRaspberry = Color(0xFFC1476B)
private val LightOnSurfaceCoffee = Color(0xFF4A342A)
private val LightOnSurfaceVariantMocha = Color(0xFF8A6E5D)

private val DarkBackground = Color(0xFF0A0A0C)
private val DarkSurface = Color(0xFF121214)
private val DarkPrimaryNeonGreen = Color(0xFF39FF88)
private val DarkSecondaryNeonPink = Color(0xFFFF3DAE)
private val DarkTertiaryAlertPink = Color(0xFFFF1F5A)
private val DarkOnSurfaceOffWhite = Color(0xFFEDEAE6)
private val DarkOnSurfaceVariantGray = Color(0xFFA8A6A3)

private val LightColors = lightColorScheme(
    primary = LightPrimaryRose,
    onPrimary = LightOnSurfaceCoffee,
    secondary = LightSecondaryCaramel,
    onSecondary = Color.White,
    tertiary = LightTertiaryRaspberry,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurfaceCoffee,
    surface = LightSurface,
    onSurface = LightOnSurfaceCoffee,
    onSurfaceVariant = LightOnSurfaceVariantMocha
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimaryNeonGreen,
    onPrimary = DarkBackground,
    secondary = DarkSecondaryNeonPink,
    onSecondary = DarkBackground,
    tertiary = DarkTertiaryAlertPink,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnSurfaceOffWhite,
    surface = DarkSurface,
    onSurface = DarkOnSurfaceOffWhite,
    onSurfaceVariant = DarkOnSurfaceVariantGray
)

val GlassPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.primary

val GlassSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.secondary

val GlassTertiary: Color
    @Composable get() = MaterialTheme.colorScheme.tertiary

@Composable
fun TaskFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colorScheme, content = content)
}
